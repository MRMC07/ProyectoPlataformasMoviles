package com.example.proyectoplataformasmoviles.presentation.profile

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoplataformasmoviles.data.repository.ProfileRepository
import com.example.proyectoplataformasmoviles.domain.model.UserProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val editedProfile: UserProfile? = null,
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val isSaving: Boolean = false,
    val isProcessingPhoto: Boolean = false,
    val errorMessage: String? = null,
    val statusMessage: String? = null
)

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val userId: String,
    initialName: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ProfileUiState(
            profile = UserProfile(name = initialName)
        )
    )

    val uiState = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        val currentState = _uiState.value

        if (
            currentState.isLoading ||
            currentState.isSaving ||
            currentState.editedProfile != null
        ) {
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val profile = withTimeout(20_000L) {
                    repository.load(userId)
                }

                _uiState.update {
                    it.copy(
                        profile = profile,
                        isLoading = false,
                        isLoaded = true
                    )
                }
            } catch (_: TimeoutCancellationException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "La carga está tardando demasiado. Volvé a intentar."
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No pudimos cargar tu perfil. Revisá la conexión y volvé a intentar."
                    )
                }
            }
        }
    }

    fun startEditing() {
        val currentState = _uiState.value

        if (
            !currentState.isLoaded ||
            currentState.isLoading ||
            currentState.editedProfile != null
        ) {
            return
        }

        _uiState.update {
            it.copy(
                editedProfile = it.profile,
                errorMessage = null,
                statusMessage = null
            )
        }
    }

    fun updateProfileFields(
        name: String,
        university: String,
        description: String
    ) {
        val currentState = _uiState.value

        if (
            currentState.isSaving ||
            currentState.isProcessingPhoto ||
            currentState.editedProfile == null
        ) {
            return
        }

        _uiState.update {
            it.copy(
                editedProfile = it.editedProfile?.copy(
                    name = name,
                    university = university,
                    description = description
                ),
                errorMessage = null
            )
        }
    }

    fun cancelEditing() {
        if (
            _uiState.value.isSaving ||
            _uiState.value.isProcessingPhoto
        ) {
            return
        }

        _uiState.update {
            it.copy(
                editedProfile = null,
                errorMessage = null
            )
        }
    }

    fun removePhoto() {
        if (
            _uiState.value.isSaving ||
            _uiState.value.isProcessingPhoto
        ) {
            return
        }

        _uiState.update {
            it.copy(
                editedProfile = it.editedProfile?.copy(
                    photoBase64 = ""
                ),
                errorMessage = null
            )
        }
    }

    fun selectPhoto(
        contentResolver: ContentResolver,
        uri: Uri
    ) {
        val currentState = _uiState.value

        if (
            currentState.isSaving ||
            currentState.isProcessingPhoto ||
            currentState.editedProfile == null
        ) {
            return
        }

        _uiState.update {
            it.copy(
                isProcessingPhoto = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                val photo = withContext(Dispatchers.IO) {
                    prepareProfilePhoto(contentResolver, uri)
                }

                _uiState.update {
                    it.copy(
                        isProcessingPhoto = false,
                        editedProfile = it.editedProfile?.copy(
                            photoBase64 = photo
                        )
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessingPhoto = false,
                        errorMessage = "No pudimos abrir esa foto. Seleccioná otra imagen."
                    )
                }
            }
        }
    }

    fun saveProfile() {
        val currentState = _uiState.value

        if (
            currentState.isSaving ||
            currentState.isProcessingPhoto
        ) {
            return
        }

        val profile = currentState.editedProfile
            ?.normalized()
            ?: return

        profile.validationError()?.let { message ->
            _uiState.update {
                it.copy(errorMessage = message)
            }
            return
        }

        _uiState.update {
            it.copy(
                isSaving = true,
                errorMessage = null,
                statusMessage = null
            )
        }

        viewModelScope.launch {
            try {
                withTimeout(20_000L) {
                    repository.save(userId, profile)
                }


                val statusMessage = try {
                    withTimeout(20_000L) {
                        repository.updateAccountName(
                            userId,
                            profile.name
                        )
                    }

                    "Perfil guardado."
                } catch (_: TimeoutCancellationException) {
                    "Perfil guardado. No se pudo actualizar el nombre de la cuenta; guardá de nuevo cuando tengas conexión."
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    "Perfil guardado. No se pudo actualizar el nombre de la cuenta; guardá de nuevo cuando tengas conexión."
                }

                _uiState.update {
                    it.copy(
                        profile = profile,
                        editedProfile = null,
                        isSaving = false,
                        statusMessage = statusMessage
                    )
                }
            } catch (_: TimeoutCancellationException) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "No pudimos confirmar el guardado. Podría sincronizarse al recuperar la conexión. Conservamos tus cambios para que reintentés."
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "No pudimos confirmar el guardado. Conservamos tus cambios aquí para que reintentés."
                    )
                }
            }
        }
    }
}