package com.example.proyectoplataformasmoviles.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.domain.repository.ListingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val listing: Listing? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isUpdatingStatus: Boolean = false,
    val statusErrorMessage: String? = null
)

class DetailViewModel(
    private val listingId: String,
    private val repository: ListingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())

    val uiState = _uiState.asStateFlow()

    private var observationJob: Job? = null

    init {
        loadListing()
    }

    fun onRetryClick() {
        if (_uiState.value.isUpdatingStatus) return
        loadListing()
    }

    fun setListingActive(isActive: Boolean) {
        val currentState = _uiState.value
        val listing = currentState.listing ?: return

        if (
            currentState.isLoading ||
            currentState.isUpdatingStatus ||
            currentState.errorMessage != null
        ) {
            return
        }

        _uiState.update {
            it.copy(
                isUpdatingStatus = true,
                statusErrorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                repository.setListingActive(
                    listingId = listing.id,
                    isActive = isActive
                )

                _uiState.update {
                    it.copy(
                        isUpdatingStatus = false,
                        statusErrorMessage = null
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isUpdatingStatus = false,
                        statusErrorMessage = "No pudimos confirmar el cambio. Revisá la conexión. Si el envío quedó pendiente, podría completarse al recuperar la conexión."
                    )
                }
            }
        }
    }

    private fun loadListing() {
        observationJob?.cancel()

        observationJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                repository.observeListings().collect { listings ->
                    val selectedListing = listings.firstOrNull {
                        it.id == listingId
                    }

                    _uiState.update {
                        it.copy(
                            listing = selectedListing,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo cargar la publicación. Revisá tu conexión e intentá de nuevo."
                    )
                }
            }
        }
    }
}