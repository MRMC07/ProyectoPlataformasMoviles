package com.example.proyectoplataformasmoviles.di

import com.example.proyectoplataformasmoviles.data.repository.FirebaseListingRepository
import com.example.proyectoplataformasmoviles.data.repository.ProfileRepository
import com.example.proyectoplataformasmoviles.domain.repository.ListingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AppContainer {
    private val database = FirebaseDatabase.getInstance(
        "https://proyectoplataformasmovil-86f6e-default-rtdb.firebaseio.com/"
    )

    private val authentication = FirebaseAuth.getInstance()

    val listingRepository: ListingRepository =
        FirebaseListingRepository(
            database,
            authentication
        )

    val profileRepository = ProfileRepository(
        database,
        authentication
    )
}