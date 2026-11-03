package com.example.proyectoplataformasmoviles.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoplataformasmoviles.model.Listing
import com.example.proyectoplataformasmoviles.remote.ListingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val listing: Listing? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class DetailViewModel(
    private val repository: ListingRepository,
    private val listingId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())

    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        observeListing()
    }

    private fun observeListing() {
        viewModelScope.launch {
            try {
                repository.observeListing(listingId).collect { listing ->
                    _uiState.update {
                        it.copy(
                            listing = listing,
                            isLoading = false,
                            errorMessage = if (listing == null) {
                                "Esta publicación ya no está disponible."
                            } else {
                                null
                            }
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudo cargar la publicación."
                    )
                }
            }
        }
    }
}