package com.example.nexora.presentation.request

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexora.data.repository.RequestRepository
import com.example.nexora.domain.model.Listing
import com.example.nexora.domain.model.RequestStatus
import com.example.nexora.domain.model.ServiceRequest
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RequestsUiState(
    val requests: List<ServiceRequest> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val updatingId: String? = null,
    val actionError: String? = null
)

class RequestsViewModel(
    private val repository: RequestRepository,
    private val userId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(RequestsUiState())
    val uiState = _uiState.asStateFlow()

    private var observationJob: Job? = null

    init {
        onRetryClick()
    }

    fun onRetryClick() {
        if (_uiState.value.updatingId != null) return

        observationJob?.cancel()

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        observationJob = viewModelScope.launch {
            try {
                repository.observeRequests(userId).collect { requests ->
                    _uiState.update {
                        it.copy(
                            requests = requests,
                            isLoading = false
                        )
                    }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No se pudieron cargar las solicitudes. Revisá tu conexión."
                    )
                }
            }
        }
    }

    fun changeStatus(
        request: ServiceRequest,
        status: RequestStatus
    ) {
        if (_uiState.value.updatingId != null) return

        _uiState.update {
            it.copy(
                updatingId = request.id,
                actionError = null
            )
        }

        viewModelScope.launch {
            try {
                repository.changeStatus(
                    request = request,
                    userId = userId,
                    status = status
                )

                _uiState.update {
                    it.copy(updatingId = null)
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        updatingId = null,
                        actionError = "No pudimos confirmar el cambio. Revisá la conexión y el estado actual. Si quedó pendiente, podría completarse al recuperar la conexión."
                    )
                }
            }
        }
    }
}

data class NewRequestUiState(
    val modality: String = "Virtual",
    val schedule: String = "",
    val note: String = "",
    val isSending: Boolean = false,
    val isSent: Boolean = false,
    val errorMessage: String? = null
) {
    val canSend: Boolean
        get() = !isSending &&
                !isSent &&
                modality.trim().length in 1..80 &&
                schedule.trim().length in 3..120 &&
                note.trim().length in 10..1000
}

class NewRequestViewModel(
    private val repository: RequestRepository,
    private val userId: String,
    private val requestId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewRequestUiState())
    val uiState = _uiState.asStateFlow()

    fun onModalityChange(value: String) {
        if (_uiState.value.isSending || _uiState.value.isSent) return

        _uiState.update {
            it.copy(
                modality = value,
                errorMessage = null
            )
        }
    }

    fun onScheduleChange(value: String) {
        if (
            _uiState.value.isSending ||
            _uiState.value.isSent ||
            value.length > 120
        ) {
            return
        }

        _uiState.update {
            it.copy(
                schedule = value,
                errorMessage = null
            )
        }
    }

    fun onNoteChange(value: String) {
        if (
            _uiState.value.isSending ||
            _uiState.value.isSent ||
            value.length > 1000
        ) {
            return
        }

        _uiState.update {
            it.copy(
                note = value,
                errorMessage = null
            )
        }
    }

    fun send(listing: Listing) {
        val currentState = _uiState.value

        if (!currentState.canSend) return

        _uiState.update {
            it.copy(
                isSending = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                repository.createRequest(
                    requestId = requestId,
                    userId = userId,
                    listing = listing,
                    modality = currentState.modality,
                    schedule = currentState.schedule,
                    note = currentState.note
                )

                _uiState.update {
                    it.copy(
                        isSending = false,
                        isSent = true
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isSending = false,
                        errorMessage = when (error) {
                            is FirebaseFirestoreException -> when (error.code) {
                                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                                    "Firebase rechazó la solicitud. Revisá las reglas de Firestore para la colección requests."

                                FirebaseFirestoreException.Code.UNAVAILABLE ->
                                    "Firestore no está disponible. Revisá la conexión e intentá de nuevo."

                                else -> error.localizedMessage
                                    ?: "Firestore no pudo guardar la solicitud."
                            }

                            else -> error.localizedMessage
                                ?: "No se pudo guardar la solicitud. Revisá Mis solicitudes antes de reintentar."
                        }
                    )
                }
            }
        }
    }
}