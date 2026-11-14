package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyectoplataformasmoviles.domain.repository.ListingRepository
import com.example.proyectoplataformasmoviles.presentation.detail.DetailViewModel
import com.example.proyectoplataformasmoviles.presentation.publish.PublishViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun EditListingRoute(
    listingId: String,
    repository: ListingRepository,
    onBackClick: () -> Unit
) {
    val detailViewModel: DetailViewModel = viewModel(
        key = "edit-source:$listingId",
        factory = viewModelFactory {
            initializer {
                DetailViewModel(
                    listingId = listingId,
                    repository = repository
                )
            }
        }
    )

    val detailState by detailViewModel.uiState
        .collectAsStateWithLifecycle()

    val listing = detailState.listing
    val user = FirebaseAuth.getInstance().currentUser

    val canEdit = user != null &&
            !user.isAnonymous &&
            listing != null &&
            listing.propietario == user.uid

    val statusMessage = when {
        detailState.isLoading ->
            "Cargando publicación..."

        detailState.errorMessage != null ->
            detailState.errorMessage

        listing == null ->
            "La publicación ya no está disponible."

        !canEdit ->
            "Solo podés editar tus propias publicaciones."

        else -> null
    }

    if (statusMessage != null) {
        Scaffold { contentPadding ->
            Column(
                modifier = Modifier
                    .padding(contentPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (detailState.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text(text = statusMessage)

                if (detailState.errorMessage != null) {
                    TextButton(
                        onClick = detailViewModel::onRetryClick
                    ) {
                        Text("Reintentar")
                    }
                }

                TextButton(onClick = onBackClick) {
                    Text("Regresar")
                }
            }
        }

        return
    }

    val editableListing = requireNotNull(listing)
    val currentUser = requireNotNull(user)

    val editViewModel: PublishViewModel = viewModel(
        key = "edit:${currentUser.uid}:$listingId",
        factory = viewModelFactory {
            initializer {
                PublishViewModel(
                    repository = repository,
                    initialListing = editableListing
                )
            }
        }
    )

    val editState by editViewModel.uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(editState.isPublished) {
        if (editState.isPublished) {
            onBackClick()
        }
    }

    PublishScreen(
        state = editState,
        onTitleChange = editViewModel::onTitleChange,
        onDescriptionChange = editViewModel::onDescriptionChange,
        onPriceChange = editViewModel::onPriceChange,
        onCategoryChange = editViewModel::onCategoryChange,
        onPublishClick = editViewModel::onPublishClick,
        onBackClick = onBackClick
    )
}