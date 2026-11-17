package com.example.nexora.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexora.data.repository.ChatRepository
import com.example.nexora.domain.model.ChatMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MessageSendStatus {
    SENDING,
    SENT,
    ERROR
}

data class ChatMessageUi(
    val message: ChatMessage,
    val sendStatus: MessageSendStatus? = null
)

data class ChatUiState(
    val messages: List<ChatMessageUi> = emptyList(),
    val isLoading: Boolean = true,
    val isConnected: Boolean? = null,
    val loadError: String? = null,
    val sendError: String? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && loadError == null && messages.isEmpty()

    val isOffline: Boolean
        get() = isConnected == false
}

private data class PendingMessage(
    val message: ChatMessage,
    val status: MessageSendStatus
)

class ChatViewModel(
    private val repository: ChatRepository,
    private val userId: String,
    private val requestId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    private var receivedMessages = emptyList<ChatMessage>()
    private var pendingMessage: PendingMessage? = null
    private var observationJob: Job? = null

    init {
        observeConnection()
        retryLoading()
    }

    fun retryLoading() {
        observationJob?.cancel()

        _uiState.update {
            it.copy(isLoading = true, loadError = null)
        }

        observationJob = viewModelScope.launch {
            try {
                repository.observeMessages(requestId).collect { messages ->
                    receivedMessages = messages

                    val pending = pendingMessage
                    if (
                        pending?.status == MessageSendStatus.SENT &&
                        messages.any { it.id == pending.message.id }
                    ) {
                        pendingMessage = null
                    }

                    updateMessages()
                    _uiState.update {
                        it.copy(isLoading = false)
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadError = "No se pudieron cargar los mensajes. Revisá tu conexión y tu acceso a esta solicitud."
                    )
                }
            }
        }
    }

    fun sendMessage(text: String) {
        if (pendingMessage != null) return

        val trimmedText = text.trim()
        if (trimmedText.length !in 1..1000) {
            _uiState.update {
                it.copy(
                    sendError = "El mensaje debe tener entre 1 y 1000 caracteres."
                )
            }
            return
        }

        val message = try {
            ChatMessage(
                id = repository.createMessageId(requestId),
                authorId = userId,
                text = trimmedText
            )
        } catch (_: Exception) {
            _uiState.update {
                it.copy(sendError = "No pudimos preparar el mensaje.")
            }
            return
        }

        val pending = PendingMessage(
            message = message,
            status = MessageSendStatus.SENDING
        )

        pendingMessage = pending
        _uiState.update {
            it.copy(sendError = null)
        }
        updateMessages()
        executeSend(pending)
    }

    fun retrySending() {
        val pending = pendingMessage ?: return
        if (pending.status != MessageSendStatus.ERROR) return

        executeSend(pending)
    }

    private fun executeSend(pending: PendingMessage) {
        pendingMessage = pending.copy(status = MessageSendStatus.SENDING)
        _uiState.update {
            it.copy(sendError = null)
        }
        updateMessages()

        viewModelScope.launch {
            try {
                repository.sendMessage(
                    requestId = requestId,
                    messageId = pending.message.id,
                    userId = userId,
                    text = pending.message.text
                )

                val currentPending = pendingMessage
                if (currentPending?.message?.id == pending.message.id) {
                    pendingMessage = if (
                        receivedMessages.any { it.id == pending.message.id }
                    ) {
                        null
                    } else {
                        currentPending.copy(status = MessageSendStatus.SENT)
                    }
                    updateMessages()
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                val currentPending = pendingMessage
                if (currentPending?.message?.id == pending.message.id) {
                    pendingMessage = currentPending.copy(
                        status = MessageSendStatus.ERROR
                    )

                    _uiState.update {
                        it.copy(
                            sendError = if (it.isOffline) {
                                "Sin conexión. El mensaje no está confirmado; reintentá con el mismo mensaje cuando vuelva la conexión."
                            } else {
                                "No se pudo confirmar el mensaje. Revisá el acceso a la solicitud y reintentá."
                            }
                        )
                    }
                    updateMessages()
                }
            }
        }
    }

    private fun observeConnection() {
        viewModelScope.launch {
            try {
                repository.observeConnection().collect { isConnected ->
                    _uiState.update {
                        it.copy(isConnected = isConnected)
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isConnected = null)
                }
            }
        }
    }

    private fun updateMessages() {
        val pending = pendingMessage
        val uiMessages = receivedMessages.map { message ->
            ChatMessageUi(
                message = message,
                sendStatus = pending
                    ?.takeIf { it.message.id == message.id }
                    ?.status
            )
        }.toMutableList()

        if (
            pending != null &&
            uiMessages.none { it.message.id == pending.message.id }
        ) {
            uiMessages += ChatMessageUi(
                message = pending.message,
                sendStatus = pending.status
            )
        }

        _uiState.update {
            it.copy(messages = uiMessages)
        }
    }
}