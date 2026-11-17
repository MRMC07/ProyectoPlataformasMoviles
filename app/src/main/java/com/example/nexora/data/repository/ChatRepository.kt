package com.example.nexora.data.repository

import com.example.nexora.domain.model.ChatMessage
import com.example.nexora.network.ConnectionStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class ChatRepository(
    firestore: FirebaseFirestore,
    private val authentication: FirebaseAuth,
    private val connectionStatus: ConnectionStatus
) {
    private val conversations = firestore.collection("conversations")

    fun createMessageId(requestId: String): String {
        require(requestId.isNotBlank()) {
            "La solicitud no es válida."
        }

        return conversations
            .document(requestId)
            .collection("messages")
            .document()
            .id
    }

    fun observeConnection(): Flow<Boolean> = connectionStatus.isConnected

    fun observeMessages(requestId: String): Flow<List<ChatMessage>> {
        require(requestId.isNotBlank()) {
            "La solicitud no es válida."
        }

        val query = conversations
            .document(requestId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)

        return callbackFlow {
            val registration = query.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else if (snapshot == null) {
                    close(
                        IllegalStateException(
                            "No se pudieron cargar los mensajes."
                        )
                    )
                } else {
                    trySend(snapshot.documents.mapNotNull(::parseMessage))
                }
            }

            awaitClose {
                registration.remove()
            }
        }
    }

    suspend fun sendMessage(
        requestId: String,
        messageId: String,
        userId: String,
        text: String
    ) {
        val user = authentication.currentUser?.takeIf {
            it.uid == userId && !it.isAnonymous && it.email != null
        } ?: error("La sesión cambió. Iniciá sesión con tu cuenta.")

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

        val existingMessage = messageReference.get(Source.SERVER).await()
        if (existingMessage.exists()) {
            check(matchesSentMessage(existingMessage, user.uid, trimmedText)) {
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

            if (!isConfirmed) {
                error(
                    "No pudimos confirmar el envío. Revisá tu conexión y reintentá."
                )
            }
        } catch (error: Exception) {
            if (error is CancellationException) throw error

            val confirmedMessage = try {
                withTimeoutOrNull(3_000L) {
                    messageReference.get(Source.SERVER).await()
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                null
            }

            if (
                confirmedMessage != null &&
                matchesSentMessage(confirmedMessage, user.uid, trimmedText)
            ) {
                return
            }

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

    private fun parseMessage(document: DocumentSnapshot): ChatMessage? {
        val createdAt = document.getTimestamp("createdAt") ?: return null

        return ChatMessage(
            id = document.id,
            authorId = document.getString("authorId") ?: return null,
            text = document.getString("text") ?: return null,
            createdAt = createdAt.toDate().time
        )
    }
}