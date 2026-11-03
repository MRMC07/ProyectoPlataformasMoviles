package com.example.proyectoplataformasmoviles.remote

import com.example.proyectoplataformasmoviles.model.Listing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.map

class ListingRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val listingsCollection =
        firestore.collection(LISTINGS_COLLECTION)

    private suspend fun ensureSignedIn(): String {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            return currentUser.uid
        }

        val result = auth.signInAnonymously().await()

        return result.user?.uid
            ?: throw IllegalStateException(
                "Anonymous sign-in returned no user."
            )
    }

    suspend fun createListing(
        title: String,
        description: String,
        price: Double,
        category: String,
        sellerName: String,
        imageUrl: String
    ): Boolean {
        val ownerId = ensureSignedIn()

        val data = hashMapOf<String, Any>(
            "title" to title,
            "description" to description,
            "price" to price,
            "category" to category,
            "sellerName" to sellerName,
            "imageUrl" to imageUrl,
            "ownerId" to ownerId
        )

        val document = listingsCollection.document()

        val confirmed = withTimeoutOrNull(SERVER_TIMEOUT_MILLIS) {
            document.set(data).await()
            true
        }

        return confirmed ?: false
    }

    fun observeListings(): Flow<List<Listing>> = callbackFlow {
        ensureSignedIn()

        val registration = listingsCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val listings = snapshot?.documents
                    .orEmpty()
                    .mapNotNull { document -> document.toListing() }

                trySend(listings)
            }

        awaitClose { registration.remove() }
    }

    fun observeListing(listingId: String): Flow<Listing?> {
        return observeListings().map { listings ->
            listings.firstOrNull { listing ->
                listing.id == listingId
            }
        }
    }

    private fun DocumentSnapshot.toListing(): Listing? {
        val title = getString("title") ?: return null
        val description = getString("description") ?: return null
        val price = getDouble("price") ?: return null
        val category = getString("category") ?: return null
        val sellerName = getString("sellerName") ?: return null
        val imageUrl = getString("imageUrl") ?: return null
        val ownerId = getString("ownerId") ?: return null

        return Listing(
            id = id,
            title = title,
            description = description,
            price = price,
            category = category,
            sellerName = sellerName,
            imageUrl = imageUrl,
            ownerId = ownerId
        )
    }

    private companion object {
        const val LISTINGS_COLLECTION = "listings"
        const val SERVER_TIMEOUT_MILLIS = 5_000L
    }
}