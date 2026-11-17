package com.example.nexora.di

import android.content.Context
import com.example.nexora.data.repository.ChatRepository
import com.example.nexora.data.repository.FirebaseListingRepository
import com.example.nexora.data.repository.ProfileRepository
import com.example.nexora.data.repository.RequestRepository
import com.example.nexora.data.repository.ReviewRepository
import com.example.nexora.domain.repository.ListingRepository
import com.example.nexora.network.ConnectionStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer(context: Context) {
    private val firestore = FirebaseFirestore.getInstance()
    private val authentication = FirebaseAuth.getInstance()
    private val connectionStatus = ConnectionStatus(context.applicationContext)

    val reviewRepository = ReviewRepository(
        firestore = firestore,
        authentication = authentication
    )

    val listingRepository: ListingRepository =
        FirebaseListingRepository(
            firestore = firestore,
            authentication = authentication
        )

    val chatRepository = ChatRepository(
        firestore = firestore,
        authentication = authentication,
        connectionStatus = connectionStatus
    )

    val profileRepository = ProfileRepository(
        firestore = firestore,
        authentication = authentication
    )

    val requestRepository = RequestRepository(
        firestore = firestore,
        authentication = authentication
    )
}