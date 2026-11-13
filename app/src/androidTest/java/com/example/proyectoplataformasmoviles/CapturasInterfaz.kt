package com.example.proyectoplataformasmoviles

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import java.io.ByteArrayOutputStream

fun capturarInterfaz(nombre: String) {
    val imagen = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        ?: error("No se pudo capturar la pantalla")
    val salida = ByteArrayOutputStream()
    imagen.compress(Bitmap.CompressFormat.PNG, 100, salida)
    Base64.encodeToString(salida.toByteArray(), Base64.NO_WRAP).chunked(3000).forEachIndexed { indice, parte ->
        Log.i("CapturaNexora", "NEXORA_IMAGEN|$nombre|$indice|$parte")
    }
    imagen.recycle()
}
