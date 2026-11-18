package com.example.nexora.screens

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
fun ListingUnavailableScreen(
    isLoading: Boolean = false,
    message: String = "La publicación no está disponible.",
    onBack: () -> Unit,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        }

        Text(
            text = if (isLoading) "Cargando publicación…" else message,
            style = MaterialTheme.typography.bodyLarge
        )

        if (onRetry != null) {
            TextButton(onClick = onRetry) {
                Text("Reintentar")
            }
        }

        TextButton(onClick = onBack) {
            Text("Regresar")
        }
    }
}