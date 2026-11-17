package com.example.nexora.presentation.publish

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import com.example.nexora.domain.model.MAX_LISTING_IMAGE_LENGTH
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

fun prepareListingImage(
    contentResolver: ContentResolver,
    uri: Uri
): String {
    var image = if (Build.VERSION.SDK_INT >= 28) {
        ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(contentResolver, uri)
        ) { decoder, imageInfo, _ ->
            val scale = minOf(
                1.0,
                640.0 / max(
                    imageInfo.size.width,
                    imageInfo.size.height
                )
            )

            decoder.setTargetSize(
                max(
                    1,
                    (imageInfo.size.width * scale).roundToInt()
                ),
                max(
                    1,
                    (imageInfo.size.height * scale).roundToInt()
                )
            )

            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            inSampleSize = 1
        }

        contentResolver.openInputStream(uri).use { inputStream ->
            BitmapFactory.decodeStream(
                inputStream,
                null,
                options
            )
        }

        require(
            options.outWidth > 0 &&
                    options.outHeight > 0
        ) {
            "La imagen seleccionada no es válida."
        }

        options.inJustDecodeBounds = false

        while (
            max(options.outWidth, options.outHeight) /
            options.inSampleSize > 640
        ) {
            options.inSampleSize *= 2
        }

        requireNotNull(
            contentResolver.openInputStream(uri).use { inputStream ->
                BitmapFactory.decodeStream(
                    inputStream,
                    null,
                    options
                )
            }
        ) {
            "No se pudo abrir la imagen."
        }
    }

    var quality = 75

    try {
        while (true) {
            val imageBytes = ByteArrayOutputStream().use { outputStream ->
                check(
                    image.compress(
                        Bitmap.CompressFormat.JPEG,
                        quality,
                        outputStream
                    )
                ) {
                    "No se pudo preparar la imagen."
                }

                outputStream.toByteArray()
            }

            val encodedImage = Base64.encodeToString(
                imageBytes,
                Base64.NO_WRAP
            )

            if (encodedImage.length <= MAX_LISTING_IMAGE_LENGTH) {
                return encodedImage
            }

            if (quality > 45) {
                quality -= 15
            } else {
                require(max(image.width, image.height) > 160) {
                    "No pudimos reducir esa imagen. Seleccioná otra."
                }

                val resizedImage = Bitmap.createScaledBitmap(
                    image,
                    max(1, (image.width * 0.75).roundToInt()),
                    max(1, (image.height * 0.75).roundToInt()),
                    true
                )

                if (resizedImage !== image) {
                    image.recycle()
                }

                image = resizedImage
                quality = 75
            }
        }
    } finally {
        image.recycle()
    }
}

fun decodeListingImage(encodedImage: String?): Bitmap? {
    if (
        encodedImage.isNullOrBlank() ||
        encodedImage.length > MAX_LISTING_IMAGE_LENGTH
    ) {
        return null
    }

    return try {
        val imageBytes = Base64.decode(
            encodedImage,
            Base64.NO_WRAP
        )

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            inSampleSize = 1
        }

        BitmapFactory.decodeByteArray(
            imageBytes,
            0,
            imageBytes.size,
            options
        )

        if (options.outWidth <= 0 || options.outHeight <= 0) {
            null
        } else {
            options.inJustDecodeBounds = false

            while (
                max(options.outWidth, options.outHeight) /
                options.inSampleSize > 640
            ) {
                options.inSampleSize *= 2
            }

            BitmapFactory.decodeByteArray(
                imageBytes,
                0,
                imageBytes.size,
                options
            )
        }
    } catch (_: IllegalArgumentException) {
        null
    }
}