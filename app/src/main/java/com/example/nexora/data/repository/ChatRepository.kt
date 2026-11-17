package com.example.nexora.data.repository

import com.example.nexora.data.monitoring.CrashlyticsErrorReporter
import com.example.nexora.domain.model.ChatMessage
import com.example.nexora.network.ConnectionStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class ChatRepository(
    firestore: FirebaseFirestore,
    private val authentication: FirebaseAuth,
    private val connectionStatus: ConnectionStatus,
    private val errorReporter: CrashlyticsErrorReporter
) {
    private val conversations = firestore.collection("conversations")

    fun createMessageId(requestId: String): String {
        return try {
            require(requestId.isNotBlank()) {
                "La solicitud no es válida."
            }

            conversations
                .document(requestId)
                .collection("messages")
                .document()
                .id
        } catch (error: Exception) {
            errorReporter.record(
                error = error,
                operation = "chat.createMessageId"
            )

            throw error
        }
    }

    fun observeConnection(): Flow<Boolean> {
        return connectionStatus.isConnected.catch { error ->
            errorReporter.record(
                error = error,
                operation = "chat.observeConnection"
            )

            throw error
        }
    }

    fun observeMessages(
        requestId: String
    ): Flow<List<ChatMessage>> {
        return callbackFlow<List<ChatMessage>> {
            require(requestId.isNotBlank()) {
                "La solicitud no es válida."
            }

            val query = conversations
                .document(requestId)
                .collection("messages")
                .orderBy("createdAt", Query.Direction.ASCENDING)

            val registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    close(
                        IllegalStateException(
                            "No se pudieron cargar los mensajes."
                        )
                    )
                    return@addSnapshotListener
                }

                try {
                    val messages = snapshot.documents
                        .mapNotNull(::parseMessage)

                    trySend(messages)
                } catch (parsingError: Exception) {
                    close(parsingError)
                }
            }

            awaitClose {
                registration.remove()
            }
        }.catch { error ->
            errorReporter.record(
                error = error,
                operation = "chat.observeMessages"
            )

            throw error
        }
    }

    suspend fun sendMessage(
        requestId: String,
        messageId: String,
        userId: String,
        text: String
    ) {
        try {
            val user = authentication.currentUser?.takeIf {
                it.uid == userId &&
                        !it.isAnonymous &&
                        it.email != null
            } ?: error(
                "La sesión cambió. Iniciá sesión con tu cuenta."
            )

            val trimmedText = text.trim()

            require(trimmedText.length in 1..1000) {
                "El mensaje debe tener entre 1 y 1000 caracteres."
            }

            require(requestId.isNotBlank() && messageId.isNotBlank()) {
                "La solicitud o el mensaje no son válidos."
            }

            val messageReference = conversations
                .document(requestId)
                .collection("messages")
                .document(messageId)

            val existingMessage = messageReference
                .get(Source.SERVER)
                .await()

            if (existingMessage.exists()) {
                check(
                    matchesSentMessage(
                        document = existingMessage,
                        userId = user.uid,
                        text = trimmedText
                    )
                ) {
                    "El identificador ya corresponde a otro mensaje."
                }

                return
            }

            try {
                val isConfirmed = withTimeoutOrNull(10_000L) {
                    messageReference.set(
                        mapOf(
                            "authorId" to user.uid,
                            "text" to trimmedText,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    ).await()

                    true
                } ?: false

                check(isConfirmed) {
                    "No pudimos confirmar el envío. Revisá tu conexión y reintentá."
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error

                val confirmedMessage = try {
                    withTimeoutOrNull(3_000L) {
                        messageReference.get(Source.SERVER).await()
                    }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (confirmationError: Exception) {
                    errorReporter.record(
                        error = confirmationError,
                        operation = "chat.confirmMessage"
                    )

                    null
                }

                if (
                    confirmedMessage != null &&
                    matchesSentMessage(
                        document = confirmedMessage,
                        userId = user.uid,
                        text = trimmedText
                    )
                ) {
                    errorReporter.record(
                        error = error,
                        operation = "chat.sendMessageRecovered"
                    )

                    return
                }

                throw error
            }
        } catch (timeout: TimeoutCancellationException) {
            errorReporter.record(
                error = timeout,
                operation = "chat.sendMessage"
            )

            throw timeout
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            errorReporter.record(
                error = error,
                operation = "chat.sendMessage"
            )

            throw error
        }
    }

    private fun matchesSentMessage(
        document: DocumentSnapshot,
        userId: String,
        text: String
    ): Boolean {
        return document.getString("authorId") == userId &&
                document.getString("text") == text &&
                document.getTimestamp("createdAt") != null
    }

    private fun parseMessage(
        document: DocumentSnapshot
    ): ChatMessage? {
        val createdAt = document.getTimestamp("createdAt")
            ?: return null

        return ChatMessage(
            id = document.id,
            authorId = document.getString("authorId") ?: return null,
            text = document.getString("text") ?: return null,
            createdAt = createdAt.toDate().time
        )
    }
}