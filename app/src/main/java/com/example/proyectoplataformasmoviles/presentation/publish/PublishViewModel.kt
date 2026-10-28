package com.example.proyectoplataformasmoviles.presentation.publish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.domain.repository.ListingRepository
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PublishUiState(
    val title: String = "",
    val description: String = "",
    val price: String = "",
    val category: String = "",
    val isSaving: Boolean = false,
    val isPublished: Boolean = false,
    val errorMessage: String? = null
) {
    val titleError: String?
        get() = if (title.isBlank()) {
            "Ingresa un título."
        } else {
            null
        }

    val descriptionError: String?
        get() = if (description.isBlank()) {
            "Ingresa una descripción."
        } else {
            null
        }

    val priceError: String?
        get() {
            val amount = price.toDoubleOrNull()

            return if (
                amount == null ||
                !amount.isFinite() ||
                amount <= 0
            ) {
                "Ingresa un precio válido mayor a cero."
            } else {
                null
            }
        }

    val categoryError: String?
        get() = if (category.isBlank()) {
            "Selecciona una categoría."
        } else {
            null
        }

    val canPublish: Boolean
        get() = titleError == null &&
                descriptionError == null &&
                priceError == null &&
                categoryError == null &&
                !isSaving &&
                !isPublished
}

class PublishViewModel(
    private val repository: ListingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublishUiState())
    val uiState = _uiState.asStateFlow()

    private val listingId = UUID.randomUUID().toString()

    fun onTitleChange(value: String) {
        _uiState.update {
            it.copy(title = value, errorMessage = null)
        }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update {
            it.copy(description = value, errorMessage = null)
        }
    }

    fun onPriceChange(value: String) {
        _uiState.update {
            it.copy(price = value, errorMessage = null)
        }
    }

    fun onCategoryChange(value: String) {
        _uiState.update {
            it.copy(category = value, errorMessage = null)
        }
    }

    fun onPublishClick() {
        val formState = _uiState.value

        if (!formState.canPublish) {
            return
        }

        val amount = formState.price.toDoubleOrNull() ?: return

        val listing = Listing(
            id = listingId,
            title = formState.title.trim(),
            description = formState.description.trim(),
            price = amount,
            category = formState.category,
            sellerName = "Usuario",
            imageUrl = ""
        )

        _uiState.update {
            it.copy(isSaving = true, errorMessage = null)
        }

        viewModelScope.launch {
            try {
                repository.saveListing(listing)

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isPublished = true
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "No se pudo confirmar el guardado. " +
                                "Revisa tu conexión y vuelve a intentar. " +
                                "Si la escritura quedó pendiente, podría enviarse " +
                                "al recuperar la conexión."
                    )
                }
            }
        }
    }
}