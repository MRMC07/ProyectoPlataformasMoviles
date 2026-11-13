package com.example.proyectoplataformasmoviles.domain.repository

import com.example.proyectoplataformasmoviles.domain.model.Listing
import kotlinx.coroutines.flow.Flow

interface ListingRepository {
    fun observeListings(): Flow<List<Listing>>

    suspend fun saveListing(listing: Listing)
}