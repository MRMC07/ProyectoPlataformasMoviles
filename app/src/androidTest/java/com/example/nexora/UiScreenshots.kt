package com.example.nexora

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import java.io.ByteArrayOutputStream

fun captureUiScreenshot(screenshotName: String) {
    val screenshot = InstrumentationRegistry
        .getInstrumentation()
        .uiAutomation
        .takeScreenshot()
        ?: error("Unable to capture screenshot.")

    try {
        val encodedImage = ByteArrayOutputStream().use { outputStream ->
            check(
                screenshot.compress(
                    Bitmap.CompressFormat.PNG,
                    100,
                    outputStream
                )
            ) {
                "Unable to encode screenshot."
            }

            Base64.encodeToString(
                outputStream.toByteArray(),
                Base64.NO_WRAP
            )
        }

        encodedImage.chunked(3000).forEachIndexed { chunkIndex, chunk ->
            Log.i(
                "NexoraScreenshot",
                "NEXORA_IMAGE|$screenshotName|$chunkIndex|$chunk"
            )
        }
    } finally {
        screenshot.recycle()
    }
}