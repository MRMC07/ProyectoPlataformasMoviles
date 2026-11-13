package com.example.proyectoplataformasmoviles.di

import com.example.proyectoplataformasmoviles.data.repository.RepositorioPublicaciones
import com.example.proyectoplataformasmoviles.domain.repository.ListingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AppContainer {
    val listingRepository: ListingRepository = RepositorioPublicaciones(
        FirebaseDatabase.getInstance("https://proyectoplataformasmovil-86f6e-default-rtdb.firebaseio.com/"),
        FirebaseAuth.getInstance()
    )
}
