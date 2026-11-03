package com.example.proyectoplataformasmoviles.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoplataformasmoviles.data.ChatRepository
import com.example.proyectoplataformasmoviles.model.Message
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ChatViewModel : ViewModel() {
    private val repository = ChatRepository()
    private val auth = FirebaseAuth.getInstance()

    val currentUserId: String
        get() = auth.currentUser?.uid ?: "guest_user"

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun loadMessages(chatId: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        repository.getMessagesForChat(chatId)
            .onEach { messages ->
                _uiState.value = ChatUiState(messages = messages, isLoading = false)
            }
            .catch { e ->
                _uiState.value = ChatUiState(isLoading = false, errorMessage = e.localizedMessage)
            }
            .launchIn(viewModelScope)
    }

    fun sendMessage(chatId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            try {
                repository.sendMessage(chatId, currentUserId, text.trim())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage)
            }
        }
    }
}