package com.example.proyectoplataformasmoviles.data.repository

import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.domain.model.RequestStatus
import com.example.proyectoplataformasmoviles.domain.model.ServiceRequest
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class RequestRepository(
    database: FirebaseDatabase,
    private val authentication: FirebaseAuth
) {
    private val requests = database.getReference("requests")

    fun observeRequests(userId: String): Flow<List<ServiceRequest>> {
        return combine(
            observeQuery(
                requests.orderByChild("customerId").equalTo(userId)
            ),
            observeQuery(
                requests.orderByChild("providerId").equalTo(userId)
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

        require(listing.isActive && listing.propietario.isNotBlank()) {
            "La publicación no está disponible."
        }

        require(listing.propietario != userId) {
            "No podés solicitar tu propio servicio."
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

        confirmWrite(
            requests.child(requestId).setValue(
                mapOf(
                    "listingId" to listing.id,
                    "listingTitle" to listing.title,
                    "customerId" to userId,
                    "customerName" to user.displayName.orEmpty()
                        .trim()
                        .take(80)
                        .ifBlank { "Usuario" },
                    "providerId" to listing.propietario,
                    "providerName" to listing.sellerName.take(200),
                    "modality" to modality.trim(),
                    "schedule" to schedule.trim(),
                    "note" to note.trim(),
                    "status" to RequestStatus.PENDING.name,
                    "createdAt" to ServerValue.TIMESTAMP,
                    "updatedAt" to ServerValue.TIMESTAMP
                )
            )
        )
    }

    suspend fun changeStatus(
        request: ServiceRequest,
        userId: String,
        status: RequestStatus
    ) {
        requireUser(userId)

        require(status in request.availableStatuses(userId)) {
            "Este cambio de estado no está permitido."
        }

        confirmWrite(
            requests.child(request.id).updateChildren(
                mapOf(
                    "status" to status.name,
                    "updatedAt" to ServerValue.TIMESTAMP
                )
            )
        )
    }

    private fun requireUser(userId: String) =
        authentication.currentUser?.takeIf {
            it.uid == userId &&
                    !it.isAnonymous &&
                    it.email != null
        } ?: error(
            "La sesión cambió. Iniciá sesión con tu cuenta."
        )

    private fun observeQuery(
        query: Query
    ): Flow<List<ServiceRequest>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(
                    snapshot.children.mapNotNull(::parseRequest)
                )
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        query.addValueEventListener(listener)

        awaitClose {
            query.removeEventListener(listener)
        }
    }

    private fun parseRequest(
        snapshot: DataSnapshot
    ): ServiceRequest? {
        fun text(field: String) =
            snapshot.child(field).value as? String

        val status = RequestStatus.entries.firstOrNull {
            it.name == text("status")
        } ?: return null

        return ServiceRequest(
            id = snapshot.key ?: return null,
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
            createdAt = (
                    snapshot.child("createdAt").value as? Number
                    )?.toLong() ?: return null,
            updatedAt = (
                    snapshot.child("updatedAt").value as? Number
                    )?.toLong() ?: return null
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