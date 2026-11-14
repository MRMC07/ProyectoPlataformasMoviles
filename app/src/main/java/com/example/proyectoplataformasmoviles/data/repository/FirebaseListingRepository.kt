package com.example.proyectoplataformasmoviles.data.repository

import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.domain.model.MAX_LISTING_IMAGE_LENGTH
import com.example.proyectoplataformasmoviles.domain.repository.ListingRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import java.math.RoundingMode
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import com.example.proyectoplataformasmoviles.data.repository.leerPublicacion as parseListing

class FirebaseListingRepository(
    database: FirebaseDatabase,
    private val authentication: FirebaseAuth
) : ListingRepository {

    private val databaseRoot = database.reference
    private val listings = databaseRoot.child("products")
    private val listingImages = databaseRoot.child("listingImages")

    private val reviewRepository = ReviewRepository(
        database = database,
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
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val availableListings = snapshot.children.mapNotNull { listingSnapshot ->
                    val fields = listingSnapshot.children.mapNotNull { field ->
                        val key = field.key
                        val value = field.value

                        if (key != null && value != null) {
                            key to value
                        } else {
                            null
                        }
                    }.toMap()

                    parseListing(
                        listingSnapshot.key.orEmpty(),
                        fields
                    )
                }

                trySend(availableListings)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        listings.addValueEventListener(listener)

        awaitClose {
            listings.removeEventListener(listener)
        }
    }

    override fun observeListingImage(
        listingId: String
    ): Flow<String?> = callbackFlow {
        val reference = listingImages.child(listingId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val imageData = snapshot.value

                when {
                    imageData == null -> {
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

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        reference.addValueEventListener(listener)

        awaitClose {
            reference.removeEventListener(listener)
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

        val changes = mutableMapOf<String, Any?>(
            "products/${listing.id}" to mapOf(
                "id" to listing.id,
                "nombre" to listing.title.trim(),
                "descripcion" to listing.description.trim(),
                "precio" to priceInCents / 100.0,
                "precioCentavos" to priceInCents,
                "categoria" to listing.category.trim(),
                "nombreVendedor" to (
                        user.displayName ?: user.email.orEmpty()
                        ),
                "correoVendedor" to user.email.orEmpty(),
                "propietario" to user.uid,
                "imageUrl" to listing.imageUrl,
                "creadaEn" to ServerValue.TIMESTAMP,
                "isActive" to true,
                "hasImage" to (imageBase64 != null)
            )
        )

        if (imageBase64 != null) {
            changes["listingImages/${listing.id}"] = imageBase64
        }

        confirmWrite(
            databaseRoot.updateChildren(changes)
        )
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

        val ownerId = existingListing.child("propietario")
            .getValue(String::class.java)

        check(ownerId == user.uid) {
            "Solo podés editar tus propias publicaciones."
        }

        check(authentication.currentUser?.uid == user.uid) {
            "La sesión cambió. Volvé a ingresar."
        }

        val listingPath = "products/${listing.id}"

        val changes = mutableMapOf<String, Any?>(
            "$listingPath/nombre" to listing.title.trim(),
            "$listingPath/descripcion" to listing.description.trim(),
            "$listingPath/precio" to priceInCents / 100.0,
            "$listingPath/precioCentavos" to priceInCents,
            "$listingPath/categoria" to listing.category.trim()
        )

        if (imageBase64 != null) {
            changes["$listingPath/hasImage"] = true
            changes["$listingPath/imageUrl"] = ""
            changes["listingImages/${listing.id}"] = imageBase64
        } else if (removeImage) {
            changes["$listingPath/hasImage"] = false
            changes["$listingPath/imageUrl"] = ""
            changes["listingImages/${listing.id}"] = null
        }

        confirmWrite(
            databaseRoot.updateChildren(changes)
        )
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

        val ownerId = existingListing.child("propietario")
            .getValue(String::class.java)

        check(ownerId == user.uid) {
            "Solo podés administrar tus propias publicaciones."
        }

        check(authentication.currentUser?.uid == user.uid) {
            "La sesión cambió. Volvé a ingresar."
        }

        confirmWrite(
            listings.child(listingId).updateChildren(
                mapOf(
                    "isActive" to isActive
                )
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
                    imageBase64.matches(
                        Regex("[A-Za-z0-9+/]+={0,2}")
                    )
        ) {
            "La imagen supera el tamaño permitido o no tiene un formato válido."
        }
    }

    private suspend fun readListingData(
        listingId: String
    ): DataSnapshot {
        return withTimeoutOrNull(10_000L) {
            listings.child(listingId).get().await()
        } ?: error(
            "No pudimos consultar la publicación. Revisá tu conexión."
        )
    }

    private suspend fun confirmWrite(task: Task<Void>) {
        val isConfirmed = withTimeoutOrNull(10_000L) {
            task.await()
            true
        } ?: false

        check(isConfirmed) {
            "No pudimos confirmar el guardado. Puede completarse al recuperar la conexión."
        }
    }
}