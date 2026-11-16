package com.example.proyectoplataformasmoviles.presentation.chat


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoplataformasmoviles.data.repository.ChatRepository
import com.example.proyectoplataformasmoviles.domain.model.MensajeChat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EstadoEnvioMensaje {
    ENVIANDO,
    ENVIADO,
    ERROR
}

data class MensajeChatUi(
    val mensaje: MensajeChat,
    val estadoEnvio: EstadoEnvioMensaje? = null
)

data class ChatUiState(
    val mensajes: List<MensajeChatUi> = emptyList(),
    val isLoading: Boolean = true,
    val isConnected: Boolean? = null,
    val loadError: String? = null,
    val sendError: String? = null
) {
    val isEmpty: Boolean
        get() = !isLoading && loadError == null && mensajes.isEmpty()

    val isOffline: Boolean
        get() = isConnected == false
}

private data class MensajePendiente(
    val mensaje: MensajeChat,
    val estado: EstadoEnvioMensaje
)

class ChatViewModel(
    private val repository: ChatRepository,
    private val userId: String,
    private val requestId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    private var mensajesRecibidos = emptyList<MensajeChat>()
    private var mensajePendiente: MensajePendiente? = null
    private var observacionJob: Job? = null

    init {
        observarConexion()
        reintentarCarga()
    }

    fun reintentarCarga() {
        observacionJob?.cancel()

        _uiState.update {
            it.copy(isLoading = true, loadError = null)
        }

        observacionJob = viewModelScope.launch {
            try {
                repository.observarMensajes(requestId).collect { mensajes ->
                    mensajesRecibidos = mensajes

                    val pendiente = mensajePendiente
                    if (
                        pendiente?.estado == EstadoEnvioMensaje.ENVIADO &&
                        mensajes.any { it.id == pendiente.mensaje.id }
                    ) {
                        mensajePendiente = null
                    }

                    actualizarMensajes()
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
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

    fun enviarMensaje(texto: String) {
        if (mensajePendiente != null) return

        val textoValidado = texto.trim()
        if (textoValidado.length !in 1..1000) {
            _uiState.update {
                it.copy(sendError = "El mensaje debe tener entre 1 y 1000 caracteres.")
            }
            return
        }

        val mensaje = try {
            MensajeChat(
                id = repository.crearIdMensaje(requestId),
                autorId = userId,
                texto = textoValidado
            )
        } catch (_: Exception) {
            _uiState.update {
                it.copy(sendError = "No pudimos preparar el mensaje.")
            }
            return
        }

        mensajePendiente = MensajePendiente(
            mensaje = mensaje,
            estado = EstadoEnvioMensaje.ENVIANDO
        )
        _uiState.update { it.copy(sendError = null) }
        actualizarMensajes()
        ejecutarEnvio(mensajePendiente ?: return)
    }

    fun reintentarEnvio() {
        val pendiente = mensajePendiente ?: return
        if (pendiente.estado != EstadoEnvioMensaje.ERROR) return

        ejecutarEnvio(pendiente)
    }

    private fun ejecutarEnvio(pendiente: MensajePendiente) {
        mensajePendiente = pendiente.copy(estado = EstadoEnvioMensaje.ENVIANDO)
        _uiState.update { it.copy(sendError = null) }
        actualizarMensajes()

        viewModelScope.launch {
            try {
                repository.enviarMensaje(
                    requestId = requestId,
                    mensajeId = pendiente.mensaje.id,
                    userId = userId,
                    texto = pendiente.mensaje.texto
                )

                val actual = mensajePendiente
                if (actual?.mensaje?.id == pendiente.mensaje.id) {
                    mensajePendiente =
                        if (mensajesRecibidos.any { it.id == pendiente.mensaje.id }) {
                            null
                        } else {
                            actual.copy(estado = EstadoEnvioMensaje.ENVIADO)
                        }
                    actualizarMensajes()
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                val actual = mensajePendiente
                if (actual?.mensaje?.id == pendiente.mensaje.id) {
                    mensajePendiente = actual.copy(
                        estado = EstadoEnvioMensaje.ERROR
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
                    actualizarMensajes()
                }
            }
        }
    }

    private fun observarConexion() {
        viewModelScope.launch {
            try {
                repository.observarConexion().collect { conectado ->
                    _uiState.update { it.copy(isConnected = conectado) }
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                _uiState.update { it.copy(isConnected = null) }
            }
        }
    }

    private fun actualizarMensajes() {
        val pendiente = mensajePendiente
        val mensajesUi = mensajesRecibidos.map { mensaje ->
            MensajeChatUi(
                mensaje = mensaje,
                estadoEnvio = pendiente
                    ?.takeIf { it.mensaje.id == mensaje.id }
                    ?.estado
            )
        }.toMutableList()

        if (pendiente != null && mensajesUi.none {
                it.mensaje.id == pendiente.mensaje.id
            }
        ) {
            mensajesUi += MensajeChatUi(
                mensaje = pendiente.mensaje,
                estadoEnvio = pendiente.estado
            )
        }

        _uiState.update { it.copy(mensajes = mensajesUi) }
    }
}