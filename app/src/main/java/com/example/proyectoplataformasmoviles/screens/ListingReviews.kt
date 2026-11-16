package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyectoplataformasmoviles.data.repository.ReviewRepository
import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.domain.model.summarizeReviews
import com.example.proyectoplataformasmoviles.presentation.review.ReviewsViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ListingReviews(
    listing: Listing,
    repository: ReviewRepository
) {
    val authentication = remember {
        FirebaseAuth.getInstance()
    }

    var currentUser by remember {
        mutableStateOf(authentication.currentUser)
    }

    DisposableEffect(authentication) {
        val listener = FirebaseAuth.AuthStateListener {
            currentUser = it.currentUser
        }

        authentication.addAuthStateListener(listener)

        onDispose {
            authentication.removeAuthStateListener(listener)
        }
    }

    val userId = currentUser
        ?.takeUnless { it.isAnonymous }
        ?.uid

    val reviewsViewModel: ReviewsViewModel = viewModel(
        key = "reviews:${listing.id}:${currentUser?.uid}:$userId",
        factory = viewModelFactory {
            initializer {
                ReviewsViewModel(
                    listingId = listing.id,
                    repository = repository,
                    userId = userId
                )
            }
        }
    )

    val state by reviewsViewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    val summary = summarizeReviews(state.reviews)

    val ownReview = state.reviews.firstOrNull {
        it.userId == userId
    }

    val dateFormat = remember {
        DateFormat.getDateInstance(
            DateFormat.MEDIUM,
            Locale("es", "GT")
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HorizontalDivider()

        Text(
            text = "Reseñas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
            }

            state.errorMessage != null -> {
                Text(
                    text = state.errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )

                TextButton(
                    onClick = reviewsViewModel::onRetryClick
                ) {
                    Text("Reintentar")
                }
            }

            else -> {
                Text(
                    text = if (summary.reviewCount == 0) {
                        "Esta publicación todavía no tiene reseñas."
                    } else {
                        String.format(
                            Locale.US,
                            "★ %.1f de 5 · %d reseñas",
                            summary.rating,
                            summary.reviewCount
                        )
                    }
                )

                when {
                    userId == null -> {
                        Text(
                            "Iniciá sesión con tu cuenta para escribir una reseña."
                        )
                    }

                    listing.propietario == userId -> {
                        Text(
                            "No podés reseñar tu propia publicación."
                        )
                    }

                    !listing.isActive -> {
                        Text(
                            "Esta publicación está retirada y no recibe reseñas."
                        )
                    }

                    state.completedRequestId == null -> {
                        Text(
                            "Podrás escribir una reseña cuando quien ofrece el servicio marque tu solicitud como completada."
                        )
                    }

                    else -> {
                        Text(
                            text = if (ownReview == null) {
                                "Escribí tu reseña"
                            } else {
                                "Editar mi reseña"
                            },
                            fontWeight = FontWeight.SemiBold
                        )

                        Row {
                            for (rating in 1..5) {
                                TextButton(
                                    onClick = {
                                        reviewsViewModel.onRatingChange(
                                            rating
                                        )
                                    },
                                    enabled = !state.isSaving,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = if (
                                            rating <= state.rating
                                        ) {
                                            "★"
                                        } else {
                                            "☆"
                                        },
                                        fontSize = 24.sp
                                    )
                                }
                            }
                        }

                        Text(
                            "Calificación: ${state.rating} de 5"
                        )

                        OutlinedTextField(
                            value = state.comment,
                            onValueChange =
                                reviewsViewModel::onCommentChange,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isSaving,
                            label = {
                                Text("Tu comentario")
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { keyboardController?.hide() }
                            ),
                            supportingText = {
                                Text("${state.comment.length}/500")
                            },
                            minLines = 3,
                            maxLines = 6
                        )

                        Button(
                            onClick = reviewsViewModel::onSaveClick,
                            enabled = state.canSave
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (ownReview == null) {
                                        "Publicar reseña"
                                    } else {
                                        "Guardar cambios"
                                    }
                                )
                            }
                        }
                    }
                }

                state.saveErrorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                state.notice?.let { message ->
                    Text(message)
                }

                state.reviews.forEach { review ->
                    HorizontalDivider()

                    Column(
                        modifier = Modifier.padding(
                            vertical = 4.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(
                            4.dp
                        )
                    ) {
                        Text(
                            text = if (review.userId == userId) {
                                "${review.authorName} (vos)"
                            } else {
                                review.authorName
                            },
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            "★".repeat(review.rating) +
                                    "☆".repeat(5 - review.rating)
                        )

                        Text(
                            text = dateFormat.format(
                                Date(review.updatedAt)
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(review.comment)
                    }
                }
            }
        }
    }
}
