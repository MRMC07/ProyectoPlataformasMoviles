package com.example.proyectoplataformasmoviles.presentation.home

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

data class HomeUiState(
    val listings: List<Listing> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class HomeViewModel(
    private val repository: ListingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    private var observationJob: Job? = null

    init {
        loadListings()
    }

    fun onRetryClick() {
        loadListings()
    }

    private fun loadListings() {
        observationJob?.cancel()

        observationJob = viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                repository.observeListings().collect { listings ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            listings = listings,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = "No se pudieron cargar las publicaciones. " +
                                "Revisa tu conexión e intenta de nuevo."
                    )
                }
            }
        }
    }
}