package com.example.proyectoplataformasmoviles.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ProfileUiState(
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.update { it.copy(isLoading = false, errorMessage = "No hay sesión activa") }
            return
        }

        val userId = currentUser.uid

        viewModelScope.launch {
            try {
                val docRef = firestore.collection("users").document(userId)
                val doc = docRef.get().await()

                if (doc.exists()) {
                    val name = doc.getString("name") ?: ""
                    val description = doc.getString("descripcion") ?: ""
                    val imageUrl = doc.getString("imageurl") ?: ""

                    _uiState.update {
                        it.copy(
                            name = name,
                            description = description,
                            imageUrl = imageUrl,
                            isLoading = false
                        )
                    }
                } else {
                    // Si no existe el documento para este UID real, lo creamos de una vez
                    val initialData = mapOf(
                        "name" to "Usuario",
                        "descripcion" to "",
                        "imageurl" to ""
                    )
                    docRef.set(initialData).await()
                    _uiState.update {
                        it.copy(
                            name = "",
                            description = "",
                            imageUrl = "",
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.localizedMessage) }
            }
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName) }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun saveProfile() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _uiState.update {it.copy(errorMessage = "Usuario no autenticado")}
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val userData = mapOf(
                    "name" to _uiState.value.name,
                    "descripcion" to _uiState.value.description,
                    "imageurl" to _uiState.value.imageUrl
                )
                // Usamos SetOptions.merge() para actualizar sin romper la estructura
                firestore.collection("users")
                    .document(userId)
                    .set(userData, SetOptions.merge())
                    .await()

                _uiState.update { it.copy(isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = e.localizedMessage) }
            }
        }
    }
}