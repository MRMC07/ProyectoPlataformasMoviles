package com.example.proyectoplataformasmoviles.presentation.review

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoplataformasmoviles.data.repository.ReviewRepository
import com.example.proyectoplataformasmoviles.domain.model.ListingReview
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewsUiState(
    val reviews: List<ListingReview> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val rating: Int = 0,
    val comment: String = "",
    val isSaving: Boolean = false,
    val saveErrorMessage: String? = null,
    val notice: String? = null
) {
    val canSave: Boolean
        get() = !isLoading &&
                errorMessage == null &&
                !isSaving &&
                rating in 1..5 &&
                comment.trim().length in 1..500
}

class ReviewsViewModel(
    private val listingId: String,
    private val repository: ReviewRepository,
    private val userId: String?
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewsUiState())

    val uiState = _uiState.asStateFlow()

    private var observationJob: Job? = null
    private var isFormChanged = false

    init {
        loadReviews()
    }

    fun onRatingChange(value: Int) {
        if (_uiState.value.isSaving || value !in 1..5) return

        isFormChanged = true

        _uiState.update {
            it.copy(
                rating = value,
                saveErrorMessage = null,
                notice = null
            )
        }
    }

    fun onCommentChange(value: String) {
        if (_uiState.value.isSaving || value.length > 500) return

        isFormChanged = true

        _uiState.update {
            it.copy(
                comment = value,
                saveErrorMessage = null,
                notice = null
            )
        }
    }

    fun onRetryClick() {
        if (!_uiState.value.isSaving) {
            loadReviews()
        }
    }

    fun onSaveClick() {
        val authorId = userId ?: return
        val currentState = _uiState.value

        if (!currentState.canSave) return

        _uiState.update {
            it.copy(
                isSaving = true,
                saveErrorMessage = null,
                notice = null
            )
        }

        viewModelScope.launch {
            try {
                repository.saveReview(
                    listingId = listingId,
                    userId = authorId,
                    rating = currentState.rating,
                    comment = currentState.comment
                )

                isFormChanged = false

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        comment = currentState.comment.trim(),
                        notice = "Reseña guardada."
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveErrorMessage = "No pudimos confirmar la reseña. Revisá tu conexión y que la publicación siga activa. Si el envío quedó pendiente, podría completarse al recuperar la conexión."
                    )
                }
            }
        }
    }

    private fun loadReviews() {
        observationJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        observationJob = viewModelScope.launch {
            try {
                repository.observeReviews(listingId).collect { reviews ->
                    val ownReview = reviews.firstOrNull {
                        it.userId == userId
                    }

                    _uiState.update { state ->
                        state.copy(
                            reviews = reviews,
                            isLoading = false,
                            errorMessage = null,
                            rating = if (
                                !isFormChanged && !state.isSaving
                            ) {
                                ownReview?.rating ?: 0
                            } else {
                                state.rating
                            },
                            comment = if (
                                !isFormChanged && !state.isSaving
                            ) {
                                ownReview?.comment.orEmpty()
                            } else {
                                state.comment
                            }
                        )
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudieron cargar las reseñas. Revisá tu conexión."
                    )
                }
            }
        }
    }
}