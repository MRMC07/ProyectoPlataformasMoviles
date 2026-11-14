package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EstadoPublicacion(
    cargando: Boolean = false,
    mensaje: String = "La publicación no está disponible.",
    alVolver: () -> Unit,
    alReintentar: (() -> Unit)? = null
) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (cargando) CircularProgressIndicator()
        Text(if (cargando) "Cargando publicación…" else mensaje, style = MaterialTheme.typography.bodyLarge)
        if (alReintentar != null) TextButton(onClick = alReintentar) { Text("Reintentar") }
        TextButton(onClick = alVolver) { Text("Regresar") }
    }
}
