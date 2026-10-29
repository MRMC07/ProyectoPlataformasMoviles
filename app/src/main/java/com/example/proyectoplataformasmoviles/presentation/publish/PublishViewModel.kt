
package com.example.proyectoplataformasmoviles.presentation.publish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.domain.repository.ListingRepository
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PublishUiState(
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val category: String = "",
    val titleTouched: Boolean = false,
    val descriptionTouched: Boolean = false,
    val priceTouched: Boolean = false,
    val categoryTouched: Boolean = false,
    val isSaving: Boolean = false,
    val isPublished: Boolean = false,
    val errorMessage: String? = null
) {
    val titleError: String?
        get() = when {
            title.isBlank() ->
                "El título es obligatorio."
            title.trim().length < 3 ->
                "El título debe tener al menos 3 caracteres."
            else -> null
        }

    val descriptionError: String?
        get() = when {
            description.isBlank() ->
                "La descripción es obligatoria."
            description.trim().length < 10 ->
                "La descripción debe tener al menos 10 caracteres."
            else -> null
        }

    val priceError: String?
        get() {
            val amount = price.toDoubleOrNull()

            return when {
                price.isBlank() ->
                    "El precio es obligatorio."
                amount == null ||
                    !amount.isFinite() ||
                    amount <= 0.0 ->
                    "Ingresa un precio válido mayor a cero."
                else -> null
            }
        }

    val categoryError: String?
        get() = if (category.isBlank()) {
            "Selecciona una categoría."
        } else {
            null
        }

    val isValid: Boolean
        get() = titleError == null &&
            descriptionError == null &&
            priceError == null &&
            categoryError == null

    val canPublish: Boolean
        get() = isValid && !isSaving && !isPublished
}

class PublishViewModel(
    private val repository: ListingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublishUiState())

    val uiState: StateFlow<PublishUiState> =
        _uiState.asStateFlow()

    private val listingId = UUID.randomUUID().toString()

    fun onTitleChange(value: String) {
        _uiState.update { state ->
            state.copy(
                title = value,
                titleTouched = true,
                errorMessage = null
            )
        }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { state ->
            state.copy(
                description = value,
                descriptionTouched = true,
                errorMessage = null
            )
        }
    }

    fun onPriceChange(value: String) {
        _uiState.update { state ->
            state.copy(
                price = value,
                priceTouched = true,
                errorMessage = null
            )
        }
    }

    fun onCategoryChange(value: String) {
        _uiState.update { state ->
            state.copy(
                category = value,
                categoryTouched = true,
                errorMessage = null
            )
        }
    }

    fun onPublishClick() {
        val currentState = _uiState.value

        if (!currentState.canPublish) {
            _uiState.update { state ->
                state.copy(
                    titleTouched = true,
                    descriptionTouched = true,
                    priceTouched = true,
                    categoryTouched = true
                )
            }
            return
        }

        val amount = currentState.price.toDoubleOrNull()
            ?: return

        val listing = Listing(
            id = listingId,
            title = currentState.title.trim(),
            description = currentState.description.trim(),
            price = amount,
            category = currentState.category,
            sellerName = "Usuario",
            imageUrl = ""
        )

        _uiState.update { state ->
            state.copy(
                isSaving = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                repository.saveListing(listing)

                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        isPublished = true
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isSaving = false,
                        errorMessage =
                            "No pudimos confirmar la publicación. " +
                            "Revisa tu conexión e intenta de nuevo. " +
                            "Si quedó pendiente, podría " +
                            "sincronizarse al recuperar la conexión."
                    )
                }
            }
        }
    }
}
