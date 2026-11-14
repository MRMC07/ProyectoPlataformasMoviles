package com.example.proyectoplataformasmoviles.presentation.profile

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

fun prepareProfilePhoto(
    contentResolver: ContentResolver,
    uri: Uri
): String {
    val photo = if (Build.VERSION.SDK_INT >= 28) {
        ImageDecoder.decodeBitmap(
            ImageDecoder.createSource(contentResolver, uri)
        ) { decoder, imageInfo, _ ->
            val scale = minOf(
                1.0,
                128.0 / max(
                    imageInfo.size.width,
                    imageInfo.size.height
                )
            )

            decoder.setTargetSize(
                max(1, (imageInfo.size.width * scale).roundToInt()),
                max(1, (imageInfo.size.height * scale).roundToInt())
            )

            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        contentResolver.openInputStream(uri).use { inputStream ->
            BitmapFactory.decodeStream(inputStream, null, options)
        }

        require(options.outWidth > 0 && options.outHeight > 0) {
            "Imagen no válida."
        }

        options.inJustDecodeBounds = false

        while (
            max(options.outWidth, options.outHeight) /
            options.inSampleSize > 256
        ) {
            options.inSampleSize *= 2
        }

        val originalPhoto = requireNotNull(
            contentResolver.openInputStream(uri).use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }
        ) {
            "No se pudo abrir la imagen."
        }

        val scale = minOf(
            1.0,
            128.0 / max(originalPhoto.width, originalPhoto.height)
        )

        val resizedPhoto = Bitmap.createScaledBitmap(
            originalPhoto,
            max(1, (originalPhoto.width * scale).roundToInt()),
            max(1, (originalPhoto.height * scale).roundToInt()),
            true
        )

        if (resizedPhoto !== originalPhoto) {
            originalPhoto.recycle()
        }

        resizedPhoto
    }

    return try {
        val imageBytes = ByteArrayOutputStream().use { outputStream ->
            check(
                photo.compress(
                    Bitmap.CompressFormat.JPEG,
                    75,
                    outputStream
                )
            )

            outputStream.toByteArray()
        }

        Base64.encodeToString(imageBytes, Base64.NO_WRAP).also {
            require(it.length <= 32768) {
                "La foto supera el tamaño permitido."
            }
        }
    } finally {
        photo.recycle()
    }
}