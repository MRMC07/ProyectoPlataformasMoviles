package com.example.proyectoplataformasmoviles.data.repository

import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.domain.repository.ListingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class FirestoreListingRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ListingRepository {

    private val sessionMutex = Mutex()

    override fun observeListings(): Flow<List<Listing>> = flow {
        ensureSession()

        emitAll(
            callbackFlow {
                val registration = firestore.collection("listings")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            close(error)
                            return@addSnapshotListener
                        }

                        if (snapshot == null) {
                            close(
                                IllegalStateException(
                                    "No se recibieron publicaciones."
                                )
                            )
                            return@addSnapshotListener
                        }

                        try {
                            val listings = snapshot.documents.map { document ->
                                document.toListing()
                            }
                            trySend(listings)
                        } catch (exception: Exception) {
                            close(exception)
                        }
                    }

                awaitClose {
                    registration.remove()
                }
            }
        )
    }

    override suspend fun saveListing(listing: Listing) {
        require(listing.id.isNotBlank() && '/' !in listing.id) {
            "La publicación necesita un identificador válido."
        }
        require(listing.title.isNotBlank()) {
            "El título no puede estar vacío."
        }
        require(listing.description.isNotBlank()) {
            "La descripción no puede estar vacía."
        }
        require(listing.price.isFinite() && listing.price > 0) {
            "El precio debe ser mayor a cero."
        }
        require(listing.category.isNotBlank()) {
            "Selecciona una categoría."
        }

        val ownerId = ensureSession()

        val listingFields = mapOf(
            "title" to listing.title.trim(),
            "description" to listing.description.trim(),
            "price" to listing.price,
            "category" to listing.category,
            "sellerName" to listing.sellerName,
            "imageUrl" to listing.imageUrl,
            "ownerId" to ownerId
        )

        try {
            withTimeout(OPERATION_TIMEOUT_MILLIS) {
                firestore.collection("listings")
                    .document(listing.id)
                    .set(listingFields)
                    .await()
            }
        } catch (exception: TimeoutCancellationException) {
            throw IllegalStateException(
                "No se pudo confirmar el guardado. " +
                        "La escritura puede seguir pendiente y enviarse " +
                        "al recuperar la conexión.",
                exception
            )
        }
    }

    private suspend fun ensureSession(): String {
        return sessionMutex.withLock {
            val currentUser = auth.currentUser

            if (currentUser != null) {
                currentUser.uid
            } else {
                try {
                    val authResult = withTimeout(OPERATION_TIMEOUT_MILLIS) {
                        auth.signInAnonymously().await()
                    }

                    requireNotNull(authResult.user) {
                        "No se pudo iniciar la sesión."
                    }.uid
                } catch (exception: TimeoutCancellationException) {
                    throw IllegalStateException(
                        "No se pudo iniciar la sesión. " +
                                "Conéctate a internet e intenta de nuevo.",
                        exception
                    )
                }
            }
        }
    }

    private fun DocumentSnapshot.toListing(): Listing {
        return Listing(
            id = id,
            title = requireNotNull(getString("title")) {
                "La publicación no tiene título."
            },
            description = requireNotNull(getString("description")) {
                "La publicación no tiene descripción."
            },
            price = requireNotNull(getDouble("price")) {
                "La publicación no tiene un precio válido."
            },
            category = requireNotNull(getString("category")) {
                "La publicación no tiene categoría."
            },
            sellerName = getString("sellerName").orEmpty(),
            imageUrl = getString("imageUrl").orEmpty()
        )
    }

    private companion object {
        const val OPERATION_TIMEOUT_MILLIS = 10_000L
    }
}