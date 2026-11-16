package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.domain.repository.ListingRepository
import com.example.proyectoplataformasmoviles.presentation.publish.decodeListingImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

private data class ListingImageUiState(
    val encodedImage: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@Composable
fun ListingImageView(
    listing: Listing,
    repository: ListingRepository,
    modifier: Modifier = Modifier
) {
    if (!listing.hasImage) {
        Box(
            modifier = modifier.background(
                MaterialTheme.colorScheme.surfaceVariant
            ),
            contentAlignment = Alignment.Center
        ) {
            if (listing.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = listing.imageUrl,
                    contentDescription = listing.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("Sin imagen")
            }
        }

        return
    }

    var retryCount by remember(listing.id) {
        mutableIntStateOf(0)
    }

    val imageFlow = remember(
        listing.id,
        repository,
        retryCount
    ) {
        repository.observeListingImage(listing.id)
            .map { encodedImage ->
                ListingImageUiState(
                    encodedImage = encodedImage,
                    isLoading = false
                )
            }
            .onStart {
                emit(ListingImageUiState())
            }
            .catch { error ->
                if (error is CancellationException) {
                    throw error
                }

                emit(
                    ListingImageUiState(
                        isLoading = false,
                        errorMessage = "No se pudo cargar la imagen."
                    )
                )
            }
    }

    val imageState by imageFlow.collectAsStateWithLifecycle(
        initialValue = ListingImageUiState()
    )

    val image = remember(imageState.encodedImage) {
        decodeListingImage(
            imageState.encodedImage
        )?.asImageBitmap()
    }

    Box(
        modifier = modifier.background(
            MaterialTheme.colorScheme.surfaceVariant
        ),
        contentAlignment = Alignment.Center
    ) {
        when {
            imageState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            }

            imageState.errorMessage != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = imageState.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall
                    )

                    TextButton(
                        onClick = {
                            retryCount += 1
                        }
                    ) {
                        Text("Reintentar")
                    }
                }
            }

            image != null -> {
                Image(
                    bitmap = image,
                    contentDescription = listing.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            else -> {
                Text("Imagen no disponible")
            }
        }
    }
}
