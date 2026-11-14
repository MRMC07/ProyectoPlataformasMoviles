package com.example.proyectoplataformasmoviles.data.repository

import com.example.proyectoplataformasmoviles.domain.model.ListingReview
import com.example.proyectoplataformasmoviles.domain.model.ReviewSummary
import com.example.proyectoplataformasmoviles.domain.model.summarizeReviews
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull

class ReviewRepository(
    database: FirebaseDatabase,
    private val authentication: FirebaseAuth
) {
    private val reviews = database.getReference("reviews")
    private val listings = database.getReference("products")

    fun observeReviews(listingId: String): Flow<List<ListingReview>> {
        return observeSnapshot(reviews.child(listingId)).map { snapshot ->
            snapshot.children.mapNotNull(::parseReview)
                .sortedByDescending { it.updatedAt }
        }
    }

    fun observeSummaries(): Flow<Map<String, ReviewSummary>> {
        return observeSnapshot(reviews).map { snapshot ->
            snapshot.children.associate { listingSnapshot ->
                listingSnapshot.key.orEmpty() to summarizeReviews(
                    listingSnapshot.children.mapNotNull(::parseReview)
                )
            }
        }
    }

    suspend fun saveReview(
        listingId: String,
        userId: String,
        rating: Int,
        comment: String
    ) {
        val user = authentication.currentUser
            ?.takeUnless { it.isAnonymous || it.email == null }
            ?: error("Iniciá sesión con tu cuenta para escribir una reseña.")

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

        val reference = reviews.child(listingId).child(userId)

        val listingSnapshot = withTimeoutOrNull(10_000L) {
            listings.child(listingId).get().await()
        } ?: error(
            "No pudimos consultar la publicación. Revisá tu conexión."
        )

        check(listingSnapshot.exists()) {
            "La publicación ya no está disponible."
        }

        val ownerId = listingSnapshot.child("propietario")
            .getValue(String::class.java)

        check(!ownerId.isNullOrBlank() && ownerId != userId) {
            "No podés reseñar tus propias publicaciones."
        }

        check(listingSnapshot.child("isActive").value != false) {
            "No se pueden guardar reseñas en una publicación retirada."
        }

        val existingReview = withTimeoutOrNull(10_000L) {
            reference.get().await()
        } ?: error(
            "No pudimos consultar tu reseña. Revisá tu conexión."
        )

        check(authentication.currentUser?.uid == userId) {
            "La sesión cambió. Volvé a abrir la publicación."
        }

        val changes = mutableMapOf<String, Any>(
            "authorName" to user.displayName.orEmpty()
                .trim()
                .take(80)
                .ifBlank { "Usuario" },
            "rating" to rating,
            "comment" to normalizedComment,
            "updatedAt" to ServerValue.TIMESTAMP
        )

        if (!existingReview.exists()) {
            changes["createdAt"] = ServerValue.TIMESTAMP
        }

        val task = reference.updateChildren(changes)

        val isConfirmed = withTimeoutOrNull(10_000L) {
            task.await()
            true
        } ?: false

        check(isConfirmed) {
            "No pudimos confirmar el guardado. Puede completarse al recuperar la conexión."
        }
    }

    private fun parseReview(snapshot: DataSnapshot): ListingReview? {
        val userId = snapshot.key ?: return null

        val authorName = snapshot.child("authorName").value as? String
            ?: return null

        val rating = snapshot.child("rating").value as? Number
            ?: return null

        val comment = snapshot.child("comment").value as? String
            ?: return null

        val createdAt = snapshot.child("createdAt").value as? Number
            ?: return null

        val updatedAt = snapshot.child("updatedAt").value as? Number
            ?: return null

        if (
            authorName.isBlank() ||
            authorName.length > 80 ||
            rating.toDouble() !in 1.0..5.0 ||
            rating.toDouble() % 1.0 != 0.0 ||
            comment.isBlank() ||
            comment.length > 500 ||
            createdAt.toLong() <= 0 ||
            updatedAt.toLong() <= 0
        ) {
            return null
        }

        return ListingReview(
            userId = userId,
            authorName = authorName,
            rating = rating.toInt(),
            comment = comment,
            createdAt = createdAt.toLong(),
            updatedAt = updatedAt.toLong()
        )
    }

    private fun observeSnapshot(
        reference: DatabaseReference
    ): Flow<DataSnapshot> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        reference.addValueEventListener(listener)

        awaitClose {
            reference.removeEventListener(listener)
        }
    }
}