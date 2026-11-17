package com.example.nexora.data.repository

import com.example.nexora.domain.model.Listing
import com.example.nexora.domain.model.MAX_LISTING_IMAGE_LENGTH
import com.example.nexora.domain.repository.ListingRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.math.RoundingMode

class FirebaseListingRepository(
    firestore: FirebaseFirestore,
    private val authentication: FirebaseAuth
) : ListingRepository {

    private val listings = firestore.collection("listings")
    private val listingImages = firestore.collection("listingImages")

    private val reviewRepository = ReviewRepository(
        firestore = firestore,
        authentication = authentication
    )

    override fun observeListings(): Flow<List<Listing>> {
        return combine(
            observeListingData(),
            reviewRepository.observeSummaries()
        ) { availableListings, summaries ->
            availableListings.map { listing ->
                val summary = summaries[listing.id]

                listing.copy(
                    rating = summary?.rating ?: 0.0,
                    reviewCount = summary?.reviewCount ?: 0
                )
            }
        }
    }

    private fun observeListingData(): Flow<List<Listing>> = callbackFlow {
        val registration = listings.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val availableListings = snapshot
                ?.documents
                .orEmpty()
                .mapNotNull { document ->
                    document.data?.let { fields ->
                        parseListing(document.id, fields)
                    }
                }

            trySend(availableListings)
        }

        awaitClose {
            registration.remove()
        }
    }

    override fun observeListingImage(
        listingId: String
    ): Flow<String?> = callbackFlow {
        val registration = listingImages.document(listingId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val imageData = snapshot?.get("imageBase64")

                when {
                    snapshot == null || !snapshot.exists() -> {
                        trySend(null)
                    }

                    imageData is String &&
                            imageData.isNotBlank() &&
                            imageData.length <= MAX_LISTING_IMAGE_LENGTH -> {
                        trySend(imageData)
                    }

                    else -> {
                        close(
                            IllegalStateException(
                                "La imagen guardada no tiene un formato válido."
                            )
                        )
                    }
                }
            }

        awaitClose {
            registration.remove()
        }
    }

    override suspend fun saveListing(
        listing: Listing,
        imageBase64: String?
    ) {
        val user = requireRegisteredUser()
        val priceInCents = validateListing(listing)
        validateImage(imageBase64)

        val existingListing = readListingData(listing.id)

        if (existingListing.exists()) {
            updateListing(
                listing = listing,
                imageBase64 = imageBase64,
                removeImage = false
            )
            return
        }

        check(authentication.currentUser?.uid == user.uid) {
            "La sesión cambió. Volvé a ingresar."
        }

        val sellerName = user.displayName
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: "Estudiante"

        val listingFields = mapOf(
            "title" to listing.title.trim(),
            "description" to listing.description.trim(),
            "price" to priceInCents / 100.0,
            "priceCents" to priceInCents,
            "category" to listing.category.trim(),
            "sellerName" to sellerName,
            "imageUrl" to listing.imageUrl,
            "ownerId" to user.uid,
            "createdAt" to FieldValue.serverTimestamp(),
            "isActive" to true,
            "hasImage" to (imageBase64 != null)
        )

        val batch = listings.firestore.batch()
        batch.set(listings.document(listing.id), listingFields)

        if (imageBase64 != null) {
            batch.set(
                listingImages.document(listing.id),
                mapOf(
                    "ownerId" to user.uid,
                    "imageBase64" to imageBase64
                )
            )
        }

        confirmWrite(batch.commit())
    }

    override suspend fun updateListing(
        listing: Listing,
        imageBase64: String?,
        removeImage: Boolean
    ) {
        val user = requireRegisteredUser()
        val priceInCents = validateListing(listing)
        validateImage(imageBase64)

        require(imageBase64 == null || !removeImage) {
            "No se puede cambiar y quitar la imagen al mismo tiempo."
        }

        val existingListing = readListingData(listing.id)

        check(existingListing.exists()) {
            "La publicación ya no está disponible."
        }

        val ownerId = existingListing.getString("ownerId")

        check(ownerId == user.uid) {
            "Solo podés editar tus propias publicaciones."
        }

        check(authentication.currentUser?.uid == user.uid) {
            "La sesión cambió. Volvé a ingresar."
        }

        val listingDocument = listings.document(listing.id)
        val imageDocument = listingImages.document(listing.id)

        val changes = mutableMapOf<String, Any>(
            "title" to listing.title.trim(),
            "description" to listing.description.trim(),
            "price" to priceInCents / 100.0,
            "priceCents" to priceInCents,
            "category" to listing.category.trim()
        )

        val batch = listings.firestore.batch()

        if (imageBase64 != null) {
            changes["hasImage"] = true
            changes["imageUrl"] = ""

            batch.set(
                imageDocument,
                mapOf(
                    "ownerId" to user.uid,
                    "imageBase64" to imageBase64
                )
            )
        } else if (removeImage) {
            changes["hasImage"] = false
            changes["imageUrl"] = ""
            batch.delete(imageDocument)
        }

        batch.update(listingDocument, changes)
        confirmWrite(batch.commit())
    }

    override suspend fun setListingActive(
        listingId: String,
        isActive: Boolean
    ) {
        val user = requireRegisteredUser()

        require(listingId.isNotBlank() && '/' !in listingId) {
            "El identificador de la publicación no es válido."
        }

        val existingListing = readListingData(listingId)

        check(existingListing.exists()) {
            "La publicación ya no está disponible."
        }

        check(existingListing.getString("ownerId") == user.uid) {
            "Solo podés administrar tus propias publicaciones."
        }

        check(authentication.currentUser?.uid == user.uid) {
            "La sesión cambió. Volvé a ingresar."
        }

        confirmWrite(
            listings.document(listingId).update(
                "isActive",
                isActive
            )
        )
    }

    private fun requireRegisteredUser(): FirebaseUser {
        return authentication.currentUser
            ?.takeUnless { it.isAnonymous }
            ?: error(
                "Iniciá sesión con tu cuenta para publicar o administrar tus publicaciones."
            )
    }

    private fun validateListing(listing: Listing): Long {
        require(listing.id.isNotBlank() && '/' !in listing.id) {
            "El identificador de la publicación no es válido."
        }

        require(listing.title.trim().length in 3..80) {
            "El título debe tener entre 3 y 80 caracteres."
        }

        require(listing.description.trim().length in 10..1500) {
            "La descripción debe tener entre 10 y 1500 caracteres."
        }

        require(listing.category.trim().length in 1..80) {
            "Seleccioná una categoría válida."
        }

        require(
            listing.price.isFinite() &&
                    listing.price > 0 &&
                    listing.price <= 999999.99
        ) {
            "Ingresá un precio mayor a cero y de hasta 999999.99."
        }

        return try {
            listing.price.toBigDecimal()
                .setScale(2, RoundingMode.UNNECESSARY)
                .movePointRight(2)
                .longValueExact()
        } catch (_: ArithmeticException) {
            throw IllegalArgumentException(
                "El precio debe tener un máximo de dos decimales."
            )
        }
    }

    private fun validateImage(imageBase64: String?) {
        if (imageBase64 == null) return

        require(
            imageBase64.isNotBlank() &&
                    imageBase64.length <= MAX_LISTING_IMAGE_LENGTH &&
                    imageBase64.length % 4 == 0 &&
                    imageBase64.matches(Regex("[A-Za-z0-9+/]+={0,2}"))
        ) {
            "La imagen supera el tamaño permitido o no tiene un formato válido."
        }
    }

    private suspend fun readListingData(
        listingId: String
    ) = withTimeoutOrNull(10_000L) {
        listings.document(listingId).get().await()
    } ?: error(
        "No pudimos consultar la publicación. Revisá tu conexión."
    )

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