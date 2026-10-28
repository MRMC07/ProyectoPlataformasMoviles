package com.example.proyectoplataformasmoviles.di

import com.example.proyectoplataformasmoviles.data.repository.FirestoreListingRepository
import com.example.proyectoplataformasmoviles.domain.repository.ListingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer {

    val listingRepository: ListingRepository = FirestoreListingRepository(
        firestore = FirebaseFirestore.getInstance(),
        auth = FirebaseAuth.getInstance()
    )
}