package com.example.nexora.domain.repository

import com.example.nexora.domain.model.Listing
import kotlinx.coroutines.flow.Flow

interface ListingRepository {
    fun observeListings(): Flow<List<Listing>>

    fun observeListingImage(listingId: String): Flow<String?>

    suspend fun saveListing(
        listing: Listing,
        imageBase64: String? = null
    )

    suspend fun updateListing(
        listing: Listing,
        imageBase64: String? = null,
        removeImage: Boolean = false
    )

    suspend fun setListingActive(
        listingId: String,
        isActive: Boolean
    )
}