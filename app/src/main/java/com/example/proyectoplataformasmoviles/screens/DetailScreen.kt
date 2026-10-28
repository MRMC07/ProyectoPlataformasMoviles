package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.proyectoplataformasmoviles.presentation.detail.DetailUiState
import java.util.Locale

@Composable
fun DetailScreen(
    state: DetailUiState,
    onBackClick: () -> Unit,
    onContactClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listing = state.listing

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (
                listing != null &&
                !state.isLoading &&
                state.errorMessage == null
            ) {
                Button(
                    onClick = onContactClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("Contactar emprendedor")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextButton(onClick = onBackClick) {
                Text("Volver")
            }

            Text(
                text = "Detalle",
                style = MaterialTheme.typography.headlineMedium
            )

            when {
                state.isLoading -> {
                    CircularProgressIndicator()
                    Text("Cargando publicación…")
                }

                state.errorMessage != null -> {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )

                    Button(onClick = onRetryClick) {
                        Text("Reintentar")
                    }
                }

                listing == null -> {
                    Text("La publicación no está disponible.")
                    Text(
                        "Puede haber sido eliminada o no estar " +
                                "guardada en este dispositivo."
                    )

                    Button(onClick = onRetryClick) {
                        Text("Reintentar")
                    }
                }

                else -> {
                    if (listing.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = listing.imageUrl,
                            contentDescription = listing.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Text(
                        text = listing.title,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = String.format(
                            Locale.US,
                            "Q%.2f",
                            listing.price
                        ),
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = listing.category,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = "Descripción",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = listing.description,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Información del emprendedor",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = listing.sellerName.ifBlank {
                                    "Nombre no disponible"
                                }
                            )
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Compra con seguridad",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                "Acuerda los detalles del producto o servicio " +
                                        "antes de realizar cualquier pago."
                            )
                        }
                    }
                }
            }
        }
    }
}