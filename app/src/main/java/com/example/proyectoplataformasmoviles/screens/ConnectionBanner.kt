
package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ConnectionBanner(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isConnected) {
        Text(
            text = "Sin conexión a internet. " +
                    "Tus borradores siguen disponibles.",
            modifier = modifier
                .fillMaxWidth()
                .background(Color(0xFFFFE6B3))
                .padding(12.dp),
            color = Color(0xFF664400),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
