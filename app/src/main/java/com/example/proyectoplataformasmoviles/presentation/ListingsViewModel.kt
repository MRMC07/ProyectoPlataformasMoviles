package com.example.proyectoplataformasmoviles.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoplataformasmoviles.model.Listing
import com.example.proyectoplataformasmoviles.remote.ListingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ListingsUiState(
    val listings: List<Listing> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val isEmpty: Boolean
        get() = !isLoading &&
                errorMessage == null &&
                listings.isEmpty()
}

class ListingsViewModel(
    private val repository: ListingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListingsUiState())

    val uiState: StateFlow<ListingsUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeListings()
    }

    fun onRetryClick() {
        observeListings()
    }

    private fun observeListings() {
        observeJob?.cancel()

        observeJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                repository.observeListings().collect { listings ->
                    _uiState.update {
                        it.copy(
                            listings = listings,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudieron cargar las publicaciones."
                    )
                }
            }
        }
    }
}