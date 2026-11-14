package com.example.proyectoplataformasmoviles.presentation.publish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.domain.repository.ListingRepository
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.proyectoplataformasmoviles.domain.model.importeEnCentavos as parsePriceInCents

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
    val errorMessage: String? = null,
    val isEditing: Boolean = false
) {
    val titleError: String?
        get() = when {
            title.isBlank() ->
                "El título es obligatorio."

            title.trim().length !in 3..80 ->
                "El título debe tener entre 3 y 80 caracteres."

            else -> null
        }

    val descriptionError: String?
        get() = when {
            description.isBlank() ->
                "La descripción es obligatoria."

            description.trim().length !in 10..1500 ->
                "La descripción debe tener entre 10 y 1500 caracteres."

            else -> null
        }

    val priceError: String?
        get() = if (parsePriceInCents(price) == null) {
            "Ingresá un precio mayor a cero, de hasta 999999.99, con un máximo de dos decimales."
        } else {
            null
        }

    val categoryError: String?
        get() = if (category.trim().length !in 1..80) {
            "Seleccioná una categoría válida."
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
    private val repository: ListingRepository,
    private val initialListing: Listing? = null
) : ViewModel() {

    private val listingId = initialListing?.id
        ?: UUID.randomUUID().toString()

    private val _uiState = MutableStateFlow(
        PublishUiState(
            title = initialListing?.title.orEmpty(),
            description = initialListing?.description.orEmpty(),
            price = initialListing?.let { listing ->
                String.format(
                    Locale.US,
                    "%.2f",
                    listing.price
                )
            }.orEmpty(),
            category = initialListing?.category.orEmpty(),
            isEditing = initialListing != null
        )
    )

    val uiState = _uiState.asStateFlow()

    fun onTitleChange(value: String) {
        if (_uiState.value.isSaving || _uiState.value.isPublished) {
            return
        }

        _uiState.update {
            it.copy(
                title = value,
                titleTouched = true,
                errorMessage = null
            )
        }
    }

    fun onDescriptionChange(value: String) {
        if (_uiState.value.isSaving || _uiState.value.isPublished) {
            return
        }

        _uiState.update {
            it.copy(
                description = value,
                descriptionTouched = true,
                errorMessage = null
            )
        }
    }

    fun onPriceChange(value: String) {
        if (_uiState.value.isSaving || _uiState.value.isPublished) {
            return
        }

        _uiState.update {
            it.copy(
                price = value,
                priceTouched = true,
                errorMessage = null
            )
        }
    }

    fun onCategoryChange(value: String) {
        if (_uiState.value.isSaving || _uiState.value.isPublished) {
            return
        }

        _uiState.update {
            it.copy(
                category = value,
                categoryTouched = true,
                errorMessage = null
            )
        }
    }

    fun onPublishClick() {
        val currentState = _uiState.value

        if (currentState.isSaving || currentState.isPublished) {
            return
        }

        if (!currentState.isValid) {
            _uiState.update {
                it.copy(
                    titleTouched = true,
                    descriptionTouched = true,
                    priceTouched = true,
                    categoryTouched = true
                )
            }
            return
        }

        val priceInCents = parsePriceInCents(
            currentState.price
        ) ?: return

        val listing = initialListing?.copy(
            title = currentState.title.trim(),
            description = currentState.description.trim(),
            price = priceInCents / 100.0,
            category = currentState.category.trim()
        ) ?: Listing(
            id = listingId,
            title = currentState.title.trim(),
            description = currentState.description.trim(),
            price = priceInCents / 100.0,
            category = currentState.category.trim(),
            sellerName = "",
            imageUrl = ""
        )

        _uiState.update {
            it.copy(
                isSaving = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                if (initialListing == null) {
                    repository.saveListing(listing)
                } else {
                    repository.updateListing(listing)
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isPublished = true
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "No pudimos confirmar el guardado. Revisá tu conexión y reintentá. Si el envío quedó pendiente, podría completarse al recuperar la conexión."
                    )
                }
            }
        }
    }
}