
package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.presentation.home.HomeUiState
import java.util.Locale

@Composable
fun HomeScreen(
    state: HomeUiState,
    onListingClick: (String) -> Unit,
    onPublishClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            Column {
                HorizontalDivider()

                Button(
                    onClick = onPublishClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Publicar producto o servicio")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Explorar",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Descubre productos y servicios " +
                    "de emprendedores.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            when {
                state.isLoading && state.listings.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally,
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Cargando publicaciones...")
                        }
                    }
                }

                state.errorMessage != null &&
                    state.listings.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally,
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "No pudimos cargar el catálogo",
                                style =
                                    MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = state.errorMessage,
                                color = MaterialTheme.colorScheme.error
                            )

                            Button(onClick = onRetryClick) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                !state.isLoading && state.listings.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment =
                                Alignment.CenterHorizontally,
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Todavía no hay publicaciones",
                                style =
                                    MaterialTheme.typography.titleLarge
                            )

                            Text(
                                "Sé el primero en publicar " +
                                    "un producto o servicio."
                            )

                            Button(onClick = onPublishClick) {
                                Text("Crear publicación")
                            }
                        }
                    }
                }

                else -> {
                    Text(
                        text = "${state.listings.size} publicaciones",
                        style = MaterialTheme.typography.labelLarge
                    )

                    state.errorMessage?.let { message ->
                        Text(
                            text = "Sin conexión: $message",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding =
                            PaddingValues(bottom = 16.dp),
                        horizontalArrangement =
                            Arrangement.spacedBy(12.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.listings,
                            key = { listing -> listing.id }
                        ) { listing ->
                            ListingCard(
                                listing = listing,
                                onClick = {
                                    onListingClick(listing.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ListingCard(
    listing: Listing,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (listing.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = listing.imageUrl,
                    contentDescription = listing.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sin imagen",
                        color =
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier.padding(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = listing.category,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = listing.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = String.format(
                        Locale.US,
                        "Q%.2f",
                        listing.price
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = listing.sellerName.ifBlank {
                        "Emprendedor"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
