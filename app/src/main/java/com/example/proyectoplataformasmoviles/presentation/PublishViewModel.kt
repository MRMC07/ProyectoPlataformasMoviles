package com.example.proyectoplataformasmoviles.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoplataformasmoviles.local.DraftEntity
import com.example.proyectoplataformasmoviles.local.DraftRepository
import com.example.proyectoplataformasmoviles.remote.ListingRepository
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
    val isSubmitting: Boolean = false,
    val isSaved: Boolean = false,
    val isQueued: Boolean = false,
    val notice: String? = null,
    val errorMessage: String? = null
) {
    val titleError: String?
        get() = when {
            title.isBlank() ->
                "El título es obligatorio."
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

    val canSubmit: Boolean
        get() = isValid &&
                !isSubmitting &&
                !isSaved &&
                !isQueued
}

class PublishViewModel(
    private val draftRepository: DraftRepository,
    private val listingRepository: ListingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PublishUiState())

    val uiState: StateFlow<PublishUiState> = _uiState.asStateFlow()

    private val draftId = UUID.randomUUID().toString()

    fun onTitleChange(value: String) {
        _uiState.update { state ->
            state.copy(
                title = value,
                titleTouched = true,
                notice = null,
                errorMessage = null
            )
        }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { state ->
            state.copy(
                description = value,
                descriptionTouched = true,
                notice = null,
                errorMessage = null
            )
        }
    }

    fun onPriceChange(value: String) {
        _uiState.update { state ->
            state.copy(
                price = value,
                priceTouched = true,
                notice = null,
                errorMessage = null
            )
        }
    }

    fun onCategoryChange(value: String) {
        _uiState.update { state ->
            state.copy(
                category = value,
                notice = null,
                errorMessage = null
            )
        }
    }

    fun onSubmitClick() {
        val currentState = _uiState.value

        if (!currentState.canSubmit) return

        val price = currentState.price.trim().toDoubleOrNull() ?: return

        val draft = DraftEntity(
            id = draftId,
            title = currentState.title.trim(),
            description = currentState.description.trim(),
            price = currentState.price.trim(),
            category = currentState.category,
            updatedAt = System.currentTimeMillis()
        )

        _uiState.update { state ->
            state.copy(
                isSubmitting = true,
                errorMessage = null,
                notice = null
            )
        }

        viewModelScope.launch {
            try {
                draftRepository.saveDraft(draft)

                val confirmed = listingRepository.createListing(
                    title = draft.title,
                    description = draft.description,
                    price = price,
                    category = draft.category,
                    sellerName = DEFAULT_SELLER_NAME,
                    imageUrl = DEFAULT_IMAGE_URL
                )

                if (confirmed) {
                    draftRepository.deleteDraft(draftId)

                    _uiState.update { state ->
                        state.copy(
                            isSubmitting = false,
                            isSaved = true,
                            notice = "Publicación enviada correctamente."
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            isSubmitting = false,
                            isQueued = true,
                            notice = "Sin conexión. Tu publicación quedó " +
                                    "guardada en este dispositivo y se " +
                                    "enviará al recuperar la conexión."
                        )
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isSubmitting = false,
                        errorMessage = "No se pudo publicar. Intenta de nuevo."
                    )
                }
            }
        }
    }

    private companion object {
        const val DEFAULT_SELLER_NAME = "Estudiante emprendedor"
        const val DEFAULT_IMAGE_URL =
            "https://images.unsplash.com/photo-1522202176988-66273c2fd55f" +
                    "?auto=format&fit=crop&w=600&q=80"
    }
}