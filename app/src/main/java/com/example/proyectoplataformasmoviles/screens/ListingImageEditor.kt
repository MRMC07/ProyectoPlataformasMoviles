package com.example.proyectoplataformasmoviles.screens

import android.content.ContentResolver
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.domain.repository.ListingRepository
import com.example.proyectoplataformasmoviles.presentation.publish.PublishUiState
import com.example.proyectoplataformasmoviles.presentation.publish.decodeListingImage

@Composable
fun ListingImageEditor(
    state: PublishUiState,
    repository: ListingRepository,
    onImageSelected: (ContentResolver, Uri) -> Unit,
    onRemoveImageClick: () -> Unit,
    initialListing: Listing? = null
) {
    val context = LocalContext.current

    val imagePicker = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onImageSelected(
                context.contentResolver,
                uri
            )
        }
    }

    val selectedImage = remember(state.selectedImageBase64) {
        decodeListingImage(
            state.selectedImageBase64
        )?.asImageBitmap()
    }

    val hasExistingImage = !state.removeImage &&
            initialListing?.let {
                it.hasImage || it.imageUrl.isNotBlank()
            } == true

    val hasImage = state.selectedImageBase64 != null ||
            hasExistingImage

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Imagen de la publicación",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                selectedImage != null -> {
                    Image(
                        bitmap = selectedImage,
                        contentDescription = "Vista previa de la publicación",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                hasExistingImage && initialListing != null -> {
                    ListingImageView(
                        listing = initialListing,
                        repository = repository,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    Text("Seleccioná una imagen")
                }
            }
        }

        OutlinedButton(
            onClick = {
                imagePicker.launch(
                    PickVisualMediaRequest(
                        PickVisualMedia.ImageOnly
                    )
                )
            },
            enabled = state.canEditForm
        ) {
            Text(
                text = if (hasImage) {
                    "Cambiar imagen"
                } else {
                    "Seleccionar imagen"
                }
            )
        }

        if (hasImage) {
            TextButton(
                onClick = onRemoveImageClick,
                enabled = state.canEditForm
            ) {
                Text("Quitar imagen")
            }
        }

        if (state.isProcessingImage) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )

            Text("Preparando imagen...")
        }
    }
}
