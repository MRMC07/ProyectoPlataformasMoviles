package com.example.proyectoplataformasmoviles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyectoplataformasmoviles.di.AppContainer
import com.example.proyectoplataformasmoviles.navigation.AppNavigation
import com.example.proyectoplataformasmoviles.presentation.home.HomeViewModel
import com.example.proyectoplataformasmoviles.ui.theme.ProyectoPlataformasMovilesTheme

class MainActivity : ComponentActivity() {

    private val appContainer by lazy {
        AppContainer()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val factory = viewModelFactory {
            initializer {
                HomeViewModel(
                    repository = appContainer.listingRepository
                )
            }
        }

        val homeViewModel = ViewModelProvider(
            this,
            factory
        )[HomeViewModel::class.java]

        setContent {
            ProyectoPlataformasMovilesTheme {
                AppNavigation(
                    homeViewModel = homeViewModel,
                    listingRepository = appContainer.listingRepository
                )
            }
        }
    }
}