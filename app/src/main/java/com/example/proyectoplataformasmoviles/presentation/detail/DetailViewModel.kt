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
    val errorMessage: String? = null
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
        loadListing()
    }

    private fun loadListing() {
        observationJob?.cancel()

        observationJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null)
            }

            try {
                repository.observeListings().collect { listings ->
                    val selectedListing = listings.find { listing ->
                        listing.id == listingId
                    }

                    _uiState.value = DetailUiState(
                        listing = selectedListing,
                        isLoading = false
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo cargar la publicación. " +
                                "Revisa tu conexión e intenta de nuevo."
                    )
                }
            }
        }
    }
}