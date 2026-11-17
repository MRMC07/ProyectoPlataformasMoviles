package com.example.nexora.data.repository

import com.example.nexora.data.monitoring.CrashlyticsErrorReporter
import com.example.nexora.domain.model.ListingReview
import com.example.nexora.domain.model.ReviewSummary
import com.example.nexora.domain.model.summarizeReviews
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class ReviewRepository(
    firestore: FirebaseFirestore,
    private val authentication: FirebaseAuth,
    private val errorReporter: CrashlyticsErrorReporter
) {
    private val reviews = firestore.collection("reviews")
    private val listings = firestore.collection("listings")
    private val requests = firestore.collection("requests")

    fun observeReviews(listingId: String): Flow<List<ListingReview>> {
        val query = reviews
            .document(listingId)
            .collection("entries")
            .orderBy("updatedAt", Query.Direction.DESCENDING)

        return observeSnapshot(query).map { snapshot ->
            snapshot.documents.mapNotNull(::parseReview)
        }.catch { error ->
            errorReporter.record(
                error = error,
                operation = "reviews.observe"
            )

            throw error
        }
    }

    fun observeSummaries(): Flow<Map<String, ReviewSummary>> {
        return observeSnapshot(
            reviews.firestore.collectionGroup("entries")
        ).map { snapshot ->
            snapshot.documents
                .mapNotNull { document ->
                    val listingId = document.reference.parent.parent?.id
                        ?: return@mapNotNull null

                    val review = parseReview(document)
                        ?: return@mapNotNull null

                    listingId to review
                }
                .groupBy(
                    keySelector = { it.first },
                    valueTransform = { it.second }
                )
                .mapValues { (_, listingReviews) ->
                    summarizeReviews(listingReviews)
                }
        }.catch { error ->
            errorReporter.record(
                error = error,
                operation = "reviews.observeSummaries"
            )

            throw error
        }
    }

    fun observeCompletedRequestId(
        listingId: String,
        userId: String
    ): Flow<String?> {
        val query = requests
            .whereEqualTo("customerId", userId)
            .whereEqualTo("listingId", listingId)
            .whereEqualTo("status", "COMPLETED")
            .limit(1)

        return observeSnapshot(query).map { snapshot ->
            snapshot.documents.firstOrNull()?.id
        }.catch { error ->
            errorReporter.record(
                error = error,
                operation = "reviews.observeCompletedRequest"
            )

            throw error
        }
    }

    suspend fun saveReview(
        listingId: String,
        userId: String,
        rating: Int,
        comment: String
    ) {
        try {
            val user = authentication.currentUser
                ?.takeUnless { it.isAnonymous || it.email == null }
                ?: error(
                    "Iniciá sesión con tu cuenta para escribir una reseña."
                )

            check(user.uid == userId) {
                "La sesión cambió. Volvé a abrir la publicación."
            }

            require(rating in 1..5) {
                "Seleccioná una calificación de 1 a 5 estrellas."
            }

            val normalizedComment = comment.trim()

            require(normalizedComment.length in 1..500) {
                "El comentario debe tener entre 1 y 500 caracteres."
            }

            val listingSnapshot = listings
                .document(listingId)
                .get(Source.SERVER)
                .await()

            check(listingSnapshot.exists()) {
                "La publicación ya no está disponible."
            }

            val ownerId = listingSnapshot.getString("ownerId")

            check(!ownerId.isNullOrBlank() && ownerId != userId) {
                "No podés reseñar tus propias publicaciones."
            }

            val completedRequest = requests
                .whereEqualTo("customerId", userId)
                .whereEqualTo("listingId", listingId)
                .whereEqualTo("status", "COMPLETED")
                .limit(1)
                .get(Source.SERVER)
                .await()
                .documents
                .firstOrNull()
                ?: error(
                    "Necesitás una solicitud completada de este servicio para escribir una reseña."
                )

            check(completedRequest.getString("providerId") == ownerId) {
                "La solicitud completada no corresponde a esta persona vendedora."
            }

            check(authentication.currentUser?.uid == userId) {
                "La sesión cambió. Volvé a abrir la publicación."
            }

            val reviewReference = reviews
                .document(listingId)
                .collection("entries")
                .document(userId)

            val existingReview = reviewReference
                .get(Source.SERVER)
                .await()

            val reviewFields = mutableMapOf<String, Any>(
                "requestId" to completedRequest.id,
                "authorName" to user.displayName
                    .orEmpty()
                    .trim()
                    .take(80)
                    .ifBlank { "Usuario" },
                "rating" to rating,
                "comment" to normalizedComment,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            reviewFields["createdAt"] = if (existingReview.exists()) {
                existingReview.getTimestamp("createdAt")
                    ?: error(
                        "La reseña guardada no tiene una fecha válida."
                    )
            } else {
                FieldValue.serverTimestamp()
            }

            val isConfirmed = withTimeoutOrNull(10_000L) {
                reviewReference.set(reviewFields).await()
                true
            } ?: false

            check(isConfirmed) {
                "No pudimos confirmar el guardado. Puede completarse al recuperar la conexión."
            }
        } catch (timeout: TimeoutCancellationException) {
            errorReporter.record(
                error = timeout,
                operation = "reviews.save"
            )

            throw timeout
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            errorReporter.record(
                error = error,
                operation = "reviews.save"
            )

            throw error
        }
    }

    private fun parseReview(
        document: DocumentSnapshot
    ): ListingReview? {
        val authorName = document.getString("authorName") ?: return null
        val rating = document.getLong("rating")?.toInt() ?: return null
        val comment = document.getString("comment") ?: return null
        val createdAt = document.getTimestamp("createdAt")?.toDate()?.time
            ?: return null
        val updatedAt = document.getTimestamp("updatedAt")?.toDate()?.time
            ?: return null

        if (
            authorName.isBlank() ||
            authorName.length > 80 ||
            rating !in 1..5 ||
            comment.isBlank() ||
            comment.length > 500 ||
            createdAt <= 0 ||
            updatedAt <= 0
        ) {
            return null
        }

        return ListingReview(
            userId = document.id,
            authorName = authorName,
            rating = rating,
            comment = comment,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun observeSnapshot(
        query: Query
    ): Flow<QuerySnapshot> = callbackFlow {
        val registration = query.addSnapshotListener { snapshot, error ->
            when {
                error != null -> close(error)
                snapshot != null -> trySend(snapshot)
                else -> close(
                    IllegalStateException(
                        "No se pudieron cargar las reseñas."
                    )
                )
            }
        }

        awaitClose {
            registration.remove()
        }
    }
}