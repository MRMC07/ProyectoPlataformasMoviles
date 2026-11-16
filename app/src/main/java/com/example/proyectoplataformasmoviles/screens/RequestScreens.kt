package com.example.proyectoplataformasmoviles.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyectoplataformasmoviles.data.repository.RequestRepository
import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.domain.model.RequestStatus
import com.example.proyectoplataformasmoviles.domain.model.ServiceRequest
import com.example.proyectoplataformasmoviles.presentation.request.NewRequestViewModel
import com.example.proyectoplataformasmoviles.presentation.request.RequestsViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.UUID

@Composable
private fun currentRequestUserId(): String? {
    val authentication = remember {
        FirebaseAuth.getInstance()
    }

    var user by remember {
        mutableStateOf(authentication.currentUser)
    }

    DisposableEffect(authentication) {
        val listener = FirebaseAuth.AuthStateListener {
            user = it.currentUser
        }

        authentication.addAuthStateListener(listener)

        onDispose {
            authentication.removeAuthStateListener(listener)
        }
    }

    return user?.takeUnless {
        it.isAnonymous || it.email == null
    }?.uid
}

@Composable
fun NewRequestRoute(
    listing: Listing?,
    repository: RequestRepository,
    onBackClick: () -> Unit,
    onSent: () -> Unit,
    onRequestsClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    val userId = currentRequestUserId()

    if (
        userId == null ||
        listing == null ||
        !listing.isActive ||
        listing.propietario == userId
    ) {
        RequestMessage(
            message = when {
                userId == null ->
                    "Iniciá sesión con tu cuenta para enviar solicitudes."

                listing?.propietario == userId ->
                    "No podés solicitar tu propio servicio."

                else ->
                    "Esta publicación ya no está disponible."
            },
            onBackClick = onBackClick,
            onSignInClick = if (userId == null) {
                onSignInClick
            } else {
                null
            }
        )
        return
    }

    val requestId = rememberSaveable(listing.id, userId) {
        UUID.randomUUID().toString()
    }

    val requestViewModel: NewRequestViewModel = viewModel(
        key = "new-request:$userId:$requestId",
        factory = viewModelFactory {
            initializer {
                NewRequestViewModel(
                    repository = repository,
                    userId = userId,
                    requestId = requestId
                )
            }
        }
    )

    val state by requestViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSent) {
        if (state.isSent) {
            onSent()
        }
    }

    BackHandler(enabled = state.isSending) {}

    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onBackClick,
                enabled = !state.isSending
            ) {
                Text("Regresar")
            }

            Text(
                text = "Solicitar servicio",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = listing.title,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                "La persona que publica podrá aceptar o rechazar tu solicitud."
            )

            Text("Modalidad")

            Row {
                listOf("Virtual", "Presencial").forEach { modality ->
                    TextButton(
                        onClick = {
                            requestViewModel.onModalityChange(modality)
                        },
                        enabled = !state.isSending && !state.isSent
                    ) {
                        Text(
                            if (state.modality == modality) {
                                "✓ $modality"
                            } else {
                                modality
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = state.schedule,
                onValueChange = requestViewModel::onScheduleChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSending && !state.isSent,
                label = {
                    Text("Horario propuesto")
                },
                supportingText = {
                    Text("Entre 3 y 120 caracteres")
                },
                singleLine = true
            )

            OutlinedTextField(
                value = state.note,
                onValueChange = requestViewModel::onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSending && !state.isSent,
                label = {
                    Text("Mensaje para quien publica")
                },
                supportingText = {
                    Text(
                        "Entre 10 y 1000 caracteres · ${state.note.length}/1000"
                    )
                },
                minLines = 3,
                maxLines = 6
            )

            if (state.isSending) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Enviando solicitud…")
            }

            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    requestViewModel.send(listing)
                },
                enabled = state.canSend
            ) {
                Text("Enviar solicitud")
            }

            TextButton(
                onClick = onRequestsClick,
                enabled = !state.isSending
            ) {
                Text("Mis solicitudes")
            }
        }
    }
}

@Composable
fun RequestsRoute(
    repository: RequestRepository,
    onBackClick: () -> Unit,
    onSignInClick: () -> Unit,
    onChatClick: (ServiceRequest) -> Unit = {}
) {
    val userId = currentRequestUserId()

    if (userId == null) {
        RequestMessage(
            message = "Iniciá sesión con tu cuenta para ver tus solicitudes.",
            onBackClick = onBackClick,
            onSignInClick = onSignInClick
        )
        return
    }

    val requestsViewModel: RequestsViewModel = viewModel(
        key = "requests:$userId",
        factory = viewModelFactory {
            initializer {
                RequestsViewModel(
                    repository = repository,
                    userId = userId
                )
            }
        }
    )

    val state by requestsViewModel.uiState.collectAsStateWithLifecycle()

    var showReceived by rememberSaveable {
        mutableStateOf(false)
    }

    var pendingChange by remember(userId) {
        mutableStateOf<Pair<ServiceRequest, RequestStatus>?>(null)
    }

    val visibleRequests = state.requests.filter {
        if (showReceived) {
            it.providerId == userId
        } else {
            it.customerId == userId
        }
    }

    Scaffold { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TextButton(onClick = onBackClick) {
                    Text("Regresar")
                }

                Text(
                    text = "Mis solicitudes",
                    style = MaterialTheme.typography.headlineSmall
                )

                Row {
                    TextButton(
                        onClick = {
                            showReceived = false
                        }
                    ) {
                        Text(
                            if (!showReceived) {
                                "✓ Enviadas"
                            } else {
                                "Enviadas"
                            }
                        )
                    }

                    TextButton(
                        onClick = {
                            showReceived = true
                        }
                    ) {
                        Text(
                            if (showReceived) {
                                "✓ Recibidas"
                            } else {
                                "Recibidas"
                            }
                        )
                    }
                }

                if (state.isLoading || state.updatingId != null) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                state.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )

                    TextButton(
                        onClick = requestsViewModel::onRetryClick
                    ) {
                        Text("Reintentar")
                    }
                }

                state.actionError?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (
                    !state.isLoading &&
                    state.errorMessage == null &&
                    visibleRequests.isEmpty()
                ) {
                    Text(
                        "Todavía no hay solicitudes en esta sección."
                    )
                }
            }

            items(
                items = visibleRequests,
                key = { it.id }
            ) { request ->
                Card(onClick = {
                    onChatClick(request)
                },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(
                            8.dp
                        )
                    ) {
                        Text(
                            text = request.listingTitle,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            "Estado: ${request.status.label}"
                        )

                        Text(
                            if (showReceived) {
                                "Solicita: ${request.customerName}"
                            } else {
                                "Ofrece: ${request.providerName}"
                            }
                        )

                        Text("Modalidad: ${request.modality}")
                        Text("Horario: ${request.schedule}")
                        Text(request.note)

                        request.availableStatuses(userId).forEach { status ->
                            TextButton(
                                onClick = {
                                    pendingChange = request to status
                                },
                                enabled = state.updatingId == null &&
                                        state.errorMessage == null
                            ) {
                                Text(
                                    when (status) {
                                        RequestStatus.ACCEPTED ->
                                            "Aceptar"

                                        RequestStatus.REJECTED ->
                                            "Rechazar"

                                        RequestStatus.CANCELLED ->
                                            "Cancelar solicitud"

                                        RequestStatus.COMPLETED ->
                                            "Marcar completada"

                                        RequestStatus.PENDING ->
                                            "Pendiente"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    pendingChange?.let { (request, status) ->
        AlertDialog(
            onDismissRequest = {
                pendingChange = null
            },
            title = {
                Text("Cambiar estado")
            },
            text = {
                Text(
                    "¿Cambiar la solicitud a «${status.label}»?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingChange = null
                        requestsViewModel.changeStatus(
                            request,
                            status
                        )
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        pendingChange = null
                    }
                ) {
                    Text("Volver")
                }
            }
        )
    }
}

@Composable
private fun RequestMessage(
    message: String,
    onBackClick: () -> Unit,
    onSignInClick: (() -> Unit)? = null
) {
    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(message)

            if (onSignInClick != null) {
                Button(onClick = onSignInClick) {
                    Text("Ingresar")
                }
            }

            TextButton(onClick = onBackClick) {
                Text("Regresar")
            }
        }
    }
}
