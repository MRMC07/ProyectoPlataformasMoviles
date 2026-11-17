package com.example.nexora.di

import android.content.Context
import androidx.room.Room
import com.example.nexora.data.monitoring.CrashlyticsErrorReporter
import com.example.nexora.data.repository.AuthRepository
import com.example.nexora.data.repository.ChatRepository
import com.example.nexora.data.repository.FirebaseListingRepository
import com.example.nexora.data.repository.ProfileRepository
import com.example.nexora.data.repository.RequestRepository
import com.example.nexora.data.repository.ReviewRepository
import com.example.nexora.domain.repository.ListingRepository
import com.example.nexora.local.AppDatabase
import com.example.nexora.local.DraftRepository
import com.example.nexora.network.ConnectionStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore

class AppContainer(context: Context) {

    private val applicationContext = context.applicationContext

    private val firestore = FirebaseFirestore.getInstance()
    private val authentication = FirebaseAuth.getInstance()

    private val connectionStatus = ConnectionStatus(
        applicationContext
    )

    private val errorReporter = CrashlyticsErrorReporter(
        crashlytics = FirebaseCrashlytics.getInstance()
    )

    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "project_local_database"
        ).build()
    }

    val authRepository = AuthRepository(
        authentication = authentication,
        errorReporter = errorReporter
    )

    val reviewRepository = ReviewRepository(
        firestore = firestore,
        authentication = authentication,
        errorReporter = errorReporter
    )

    val listingRepository: ListingRepository =
        FirebaseListingRepository(
            firestore = firestore,
            authentication = authentication,
            reviewRepository = reviewRepository,
            errorReporter = errorReporter
        )

    val chatRepository = ChatRepository(
        firestore = firestore,
        authentication = authentication,
        connectionStatus = connectionStatus,
        errorReporter = errorReporter
    )

    val profileRepository = ProfileRepository(
        firestore = firestore,
        authentication = authentication,
        errorReporter = errorReporter
    )

    val requestRepository = RequestRepository(
        firestore = firestore,
        authentication = authentication,
        errorReporter = errorReporter
    )

    val draftRepository by lazy {
        DraftRepository(
            draftDao = database.draftDao(),
            errorReporter = errorReporter
        )
    }
}