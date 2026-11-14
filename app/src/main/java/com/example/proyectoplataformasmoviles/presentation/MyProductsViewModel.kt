
package com.example.proyectoplataformasmoviles.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoplataformasmoviles.local.DraftEntity
import com.example.proyectoplataformasmoviles.local.DraftRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MyProductsUiState(
    val drafts: List<DraftEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
) {
    val isEmpty: Boolean
        get() = !isLoading &&
                errorMessage == null &&
                drafts.isEmpty()
}

class MyProductsViewModel(
    private val repository: DraftRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow(MyProductsUiState())

    val uiState: StateFlow<MyProductsUiState> =
        _uiState.asStateFlow()

    init {
        observeDrafts()
    }

    private fun observeDrafts() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                repository.observeDrafts().collect { drafts ->
                    _uiState.update {
                        it.copy(
                            drafts = drafts,
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
                        errorMessage =
                            "No se pudieron cargar los borradores."
                    )
                }
            }
        }
    }

    fun deleteDraft(draftId: String) {
        viewModelScope.launch {
            try {
                repository.deleteDraft(draftId)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage =
                            "No se pudo eliminar el borrador."
                    )
                }
            }
        }
    }
}
