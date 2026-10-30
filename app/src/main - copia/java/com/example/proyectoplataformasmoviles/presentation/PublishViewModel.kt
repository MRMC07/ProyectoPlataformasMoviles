
package com.example.proyectoplataformasmoviles.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val isSubmitting: Boolean = false,
    val notice: String? = null
) {
    val titleError: String?
        get() = when {
            title.isBlank() -> "El título es obligatorio."
            title.trim().length < 3 ->
                "Debe tener al menos 3 caracteres."
            else -> null
        }

    val descriptionError: String?
        get() = when {
            description.isBlank() ->
                "La descripción es obligatoria."
            description.trim().length < 10 ->
                "Debe tener al menos 10 caracteres."
            else -> null
        }

    val priceError: String?
        get() {
            val amount = price.toDoubleOrNull()

            return when {
                price.isBlank() ->
                    "El precio es obligatorio."
                amount == null || !amount.isFinite() ||
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

    val canSubmit: Boolean
        get() = isValid && !isSubmitting
}

class PublishViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PublishUiState())

    val uiState: StateFlow<PublishUiState> =
        _uiState.asStateFlow()

    fun onTitleChange(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                title = value,
                titleTouched = true,
                notice = null
            )
        }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                description = value,
                descriptionTouched = true,
                notice = null
            )
        }
    }

    fun onPriceChange(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                price = value,
                priceTouched = true,
                notice = null
            )
        }
    }

    fun onCategoryChange(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                category = value,
                notice = null
            )
        }
    }

    fun onSubmitClick() {
        val currentState = _uiState.value

        if (!currentState.canSubmit) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSubmitting = true, notice = null)
            }

            // No persistence is performed here.
            // The repository integration belongs to Persona 1.
            _uiState.update {
                it.copy(
                    isSubmitting = false,
                    notice = "El formulario es válido. " +
                            "El guardado estará disponible " +
                            "cuando se conecte el repositorio."
                )
            }
        }
    }
}
