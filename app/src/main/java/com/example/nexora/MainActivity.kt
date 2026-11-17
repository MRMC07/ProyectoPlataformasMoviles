package com.example.nexora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.Room
import com.example.nexora.di.AppContainer
import com.example.nexora.local.AppDatabase
import com.example.nexora.local.DraftRepository
import com.example.nexora.navigation.AppNavigation
import com.example.nexora.presentation.home.HomeViewModel
import com.example.nexora.ui.theme.NexoraTheme

class MainActivity : ComponentActivity() {

    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "project_local_database"
        ).build()
    }

    private val draftRepository by lazy {
        DraftRepository(
            draftDao = database.draftDao()
        )
    }

    private val appContainer by lazy {
        AppContainer(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val homeViewModelFactory = viewModelFactory {
            initializer {
                HomeViewModel(appContainer.listingRepository)
            }
        }

        val homeViewModel = ViewModelProvider(
            this,
            homeViewModelFactory
        )[HomeViewModel::class.java]

        setContent {
            NexoraTheme(darkTheme = true) {
                AppNavigation(
                    homeViewModel = homeViewModel,
                    listingRepository = appContainer.listingRepository,
                    draftRepository = draftRepository,
                    profileRepository = appContainer.profileRepository,
                    requestRepository = appContainer.requestRepository,
                    chatRepository = appContainer.chatRepository
                )
            }
        }
    }
}