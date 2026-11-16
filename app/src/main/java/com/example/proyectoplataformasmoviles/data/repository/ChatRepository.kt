package com.example.proyectoplataformasmoviles.data.repository

import com.example.proyectoplataformasmoviles.domain.model.MensajeChat
import com.example.proyectoplataformasmoviles.network.ConnectionStatus
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.FieldValue
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
    private val conversaciones = firestore.collection("conversaciones")

    fun crearIdMensaje(requestId: String): String {
        require(requestId.isNotBlank()) {
            "La solicitud no es válida."
        }

        return conversaciones
            .document(requestId)
            .collection("mensajes")
            .document()
            .id
    }

    fun observarConexion(): Flow<Boolean> = connectionStatus.isConnected

    fun observarMensajes(requestId: String): Flow<List<MensajeChat>> {
        require(requestId.isNotBlank()) {
            "La solicitud no es válida."
        }

        val consulta = conversaciones
            .document(requestId)
            .collection("mensajes")
            .orderBy("creadaEn", Query.Direction.ASCENDING)

        return callbackFlow {
            val registro = consulta.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                } else if (snapshot == null) {
                    close(IllegalStateException("No se pudieron cargar los mensajes."))
                } else {
                    trySend(snapshot.documents.mapNotNull(::convertirMensaje))
                }
            }

            awaitClose {
                registro.remove()
            }
        }
    }

    suspend fun enviarMensaje(
        requestId: String,
        mensajeId: String,
        userId: String,
        texto: String
    ) {
        val usuario = authentication.currentUser?.takeIf {
            it.uid == userId && !it.isAnonymous && it.email != null
        } ?: error("La sesión cambió. Iniciá sesión con tu cuenta.")

        val textoValidado = texto.trim()
        require(textoValidado.length in 1..1000) {
            "El mensaje debe tener entre 1 y 1000 caracteres."
        }
        require(requestId.isNotBlank() && mensajeId.isNotBlank()) {
            "La solicitud o el mensaje no son válidos."
        }

        val referencia = conversaciones
            .document(requestId)
            .collection("mensajes")
            .document(mensajeId)

        val mensajeExistente = referencia.get(Source.SERVER).await()
        if (mensajeExistente.exists()) {
            check(coincideConEnvio(mensajeExistente, usuario.uid, textoValidado)) {
                "El identificador ya corresponde a otro mensaje."
            }
            return
        }

        try {
            val confirmada = withTimeoutOrNull(10_000L) {
                referencia.set(
                    mapOf(
                        "autorId" to usuario.uid,
                        "texto" to textoValidado,
                        "creadaEn" to FieldValue.serverTimestamp()
                    )
                ).await()
                true
            } ?: false

            if (!confirmada) {
                error("No pudimos confirmar el envío. Revisá tu conexión y reintentá.")
            }
        } catch (error: Exception) {
            if (error is CancellationException) throw error

            val mensajeConfirmado = try {
                withTimeoutOrNull(3_000L) {
                    referencia.get(Source.SERVER).await()
                }
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (_: Exception) {
                null
            }

            if (
                mensajeConfirmado != null &&
                coincideConEnvio(mensajeConfirmado, usuario.uid, textoValidado)
            ) {
                return
            }

            throw error
        }
    }

    private fun coincideConEnvio(
        documento: com.google.firebase.firestore.DocumentSnapshot,
        userId: String,
        texto: String
    ): Boolean {
        return documento.getString("autorId") == userId
                && documento.getString("texto") == texto
                && documento.getTimestamp("creadaEn") != null
    }

    private fun convertirMensaje(
        documento: com.google.firebase.firestore.DocumentSnapshot
    ): MensajeChat? {
        val creadaEn: Timestamp = documento.getTimestamp("creadaEn") ?: return null

        return MensajeChat(
            id = documento.id,
            autorId = documento.getString("autorId") ?: return null,
            texto = documento.getString("texto") ?: return null,
            creadaEn = creadaEn.toDate().time
        )
    }
}