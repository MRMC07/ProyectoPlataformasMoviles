package com.example.proyectoplataformasmoviles.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.proyectoplataformasmoviles.presentation.chat.ChatViewModel
import com.example.proyectoplataformasmoviles.presentation.chat.EstadoEnvioMensaje

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    userId: String,
    onVolver: () -> Unit
) {
    val estado by viewModel.uiState.collectAsState()
    var texto by remember { mutableStateOf("") }

    val hayEnvioPendiente = estado.mensajes.any {
        it.estadoEnvio != null
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onVolver) {
                    Text("Volver")
                }
                Text(
                    text = "Chat de la solicitud",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (estado.isOffline) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Sin conexión con Firebase. Los mensajes necesitan confirmación del servidor.",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            when {
                estado.isLoading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                estado.loadError != null -> {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(estado.loadError.orEmpty())
                        TextButton(onClick = viewModel::reintentarCarga) {
                            Text("Reintentar")
                        }
                    }
                }

                estado.isEmpty -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Todavía no hay mensajes.")
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = estado.mensajes,
                            key = { it.mensaje.id }
                        ) { mensajeUi ->
                            val esMio = mensajeUi.mensaje.autorId == userId

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (esMio) {
                                    Alignment.End
                                } else {
                                    Alignment.Start
                                }
                            ) {
                                Surface(
                                    color = if (esMio) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(mensajeUi.mensaje.texto)

                                        when (mensajeUi.estadoEnvio) {
                                            EstadoEnvioMensaje.ENVIANDO -> {
                                                Text(
                                                    "Enviando…",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }

                                            EstadoEnvioMensaje.ENVIADO -> {
                                                Text(
                                                    "Enviado",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }

                                            EstadoEnvioMensaje.ERROR -> {
                                                Text(
                                                    "No se pudo confirmar el envío.",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                                TextButton(
                                                    onClick = viewModel::reintentarEnvio
                                                ) {
                                                    Text("Reintentar envío")
                                                }
                                            }

                                            null -> Unit
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            estado.sendError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(4.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { nuevoTexto ->
                        if (nuevoTexto.length <= 1000) texto = nuevoTexto
                    },
                    modifier = Modifier.weight(1f),
                    label = { Text("Mensaje") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    maxLines = 4
                )

                Button(
                    onClick = {
                        viewModel.enviarMensaje(texto)
                        texto = ""
                    },
                    enabled = texto.isNotBlank() && !hayEnvioPendiente,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Enviar")
                }
            }
        }
    }
}
