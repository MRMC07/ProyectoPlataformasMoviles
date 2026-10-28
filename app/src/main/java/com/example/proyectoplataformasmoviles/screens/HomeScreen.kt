package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            Button(
                onClick = onPublishClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Publicar")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Inicio",
                style = MaterialTheme.typography.headlineMedium
            )

            if (state.isLoading) {
                CircularProgressIndicator()
                Text("Cargando publicaciones…")
            }

            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )

                Button(onClick = onRetryClick) {
                    Text("Reintentar")
                }
            }

            if (
                !state.isLoading &&
                state.errorMessage == null &&
                state.listings.isEmpty()
            ) {
                Text("Todavía no hay publicaciones.")
                Text("Publica un producto o servicio para comenzar.")
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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

@Composable
private fun ListingCard(
    listing: Listing,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = listing.title,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = String.format(
                    Locale.US,
                    "Q%.2f",
                    listing.price
                ),
                style = MaterialTheme.typography.titleSmall
            )

            Text(
                text = listing.category,
                style = MaterialTheme.typography.bodyMedium
            )

            if (listing.sellerName.isNotBlank()) {
                Text(
                    text = "Publicado por ${listing.sellerName}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}