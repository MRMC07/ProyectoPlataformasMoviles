package com.example.nexora.data.repository

import com.example.nexora.domain.model.Listing
import com.example.nexora.domain.model.RequestStatus
import com.example.nexora.domain.model.ServiceRequest
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class RequestRepository(
    firestore: FirebaseFirestore,
    private val authentication: FirebaseAuth
) {
    private val requests = firestore.collection("requests")
    private val listings = firestore.collection("listings")

    fun observeRequests(userId: String): Flow<List<ServiceRequest>> {
        return combine(
            observeQuery(
                requests.whereEqualTo("customerId", userId)
            ),
            observeQuery(
                requests.whereEqualTo("providerId", userId)
            )
        ) { sentRequests, receivedRequests ->
            (sentRequests + receivedRequests)
                .distinctBy { it.id }
                .sortedByDescending { it.updatedAt }
        }
    }

    suspend fun createRequest(
        requestId: String,
        userId: String,
        listing: Listing,
        modality: String,
        schedule: String,
        note: String
    ) {
        val user = requireUser(userId)

        require(requestId.isNotBlank() && '/' !in requestId) {
            "El identificador de la solicitud no es válido."
        }

        require(modality.trim().length in 1..80) {
            "Seleccioná una modalidad."
        }

        require(schedule.trim().length in 3..120) {
            "Indicá un horario de entre 3 y 120 caracteres."
        }

        require(note.trim().length in 10..1000) {
            "Escribí un mensaje de entre 10 y 1000 caracteres."
        }

        val listingSnapshot = listings.document(listing.id)
            .get(Source.SERVER)
            .await()

        check(listingSnapshot.exists()) {
            "La publicación no está disponible."
        }

        check(listingSnapshot.getBoolean("isActive") != false) {
            "La publicación está pausada. Pedile a quien la publicó que la active."
        }

        val providerId = listingSnapshot.getString("ownerId")
            ?: listing.ownerId.takeIf { it.isNotBlank() }
            ?: error("La publicación no tiene un vendedor válido.")

        require(providerId != userId) {
            "No podés solicitar tu propio servicio."
        }

        val listingTitle = listingSnapshot.getString("title")
            ?: listing.title.takeIf { it.isNotBlank() }
            ?: error("La publicación no tiene un título válido.")

        val providerName = listingSnapshot.getString("sellerName")
            ?: listing.sellerName.takeIf { it.isNotBlank() }
            ?: error("La publicación no tiene un vendedor válido.")

        val customerName = user.displayName
            .orEmpty()
            .trim()
            .take(80)
            .ifBlank { "Usuario" }

        val requestFields = mapOf(
            "listingId" to listing.id,
            "listingTitle" to listingTitle,
            "customerId" to userId,
            "customerName" to customerName,
            "providerId" to providerId,
            "providerName" to providerName,
            "modality" to modality.trim(),
            "schedule" to schedule.trim(),
            "note" to note.trim(),
            "status" to RequestStatus.PENDING.name,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        confirmWrite(
            requests.document(requestId).set(requestFields)
        )
    }

    suspend fun changeStatus(
        request: ServiceRequest,
        userId: String,
        status: RequestStatus
    ) {
        requireUser(userId)

        val requestDocument = requests.document(request.id)
        val snapshot = requestDocument.get().await()

        check(snapshot.exists()) {
            "La solicitud ya no está disponible."
        }

        val currentRequest = parseRequest(snapshot)
            ?: error("La solicitud guardada no tiene un formato válido.")

        require(status in currentRequest.availableStatuses(userId)) {
            "Este cambio de estado no está permitido."
        }

        confirmWrite(
            requestDocument.update(
                mapOf(
                    "status" to status.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        )
    }

    private fun requireUser(userId: String): FirebaseUser {
        return authentication.currentUser?.takeIf {
            it.uid == userId &&
                    !it.isAnonymous &&
                    it.email != null
        } ?: error(
            "La sesión cambió. Iniciá sesión con tu cuenta."
        )
    }

    private fun observeQuery(
        query: Query
    ): Flow<List<ServiceRequest>> = callbackFlow {
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            trySend(
                snapshot?.documents
                    .orEmpty()
                    .mapNotNull(::parseRequest)
            )
        }

        awaitClose {
            registration.remove()
        }
    }

    private fun parseRequest(
        snapshot: DocumentSnapshot
    ): ServiceRequest? {
        fun text(field: String): String? =
            snapshot.get(field) as? String

        fun timestampMillis(field: String): Long? {
            return when (val value = snapshot.get(field)) {
                is Timestamp -> value.toDate().time
                is Number -> value.toLong()
                else -> null
            }
        }

        val status = RequestStatus.entries.firstOrNull {
            it.name == text("status")
        } ?: return null

        return ServiceRequest(
            id = snapshot.id,
            listingId = text("listingId") ?: return null,
            listingTitle = text("listingTitle") ?: return null,
            customerId = text("customerId") ?: return null,
            customerName = text("customerName") ?: return null,
            providerId = text("providerId") ?: return null,
            providerName = text("providerName") ?: return null,
            modality = text("modality") ?: return null,
            schedule = text("schedule") ?: return null,
            note = text("note") ?: return null,
            status = status,
            createdAt = timestampMillis("createdAt") ?: return null,
            updatedAt = timestampMillis("updatedAt") ?: return null
        )
    }

    private suspend fun confirmWrite(task: Task<Void>) {
        val confirmed = withTimeoutOrNull(10_000L) {
            task.await()
            true
        } ?: false

        check(confirmed) {
            "No pudimos confirmar el guardado. Puede completarse al recuperar la conexión."
        }
    }
}