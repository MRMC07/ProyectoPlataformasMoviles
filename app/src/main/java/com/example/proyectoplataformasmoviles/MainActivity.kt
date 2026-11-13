
package com.example.proyectoplataformasmoviles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.room.Room
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.proyectoplataformasmoviles.di.AppContainer
import com.example.proyectoplataformasmoviles.presentation.home.HomeViewModel
import com.example.proyectoplataformasmoviles.local.AppDatabase
import com.example.proyectoplataformasmoviles.local.DraftRepository
import com.example.proyectoplataformasmoviles.navigation.AppNavigation
import com.example.proyectoplataformasmoviles.ui.theme.ProyectoPlataformasMovilesTheme

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

    private val contenedor by lazy { AppContainer() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val fabrica = viewModelFactory {
            initializer { HomeViewModel(contenedor.listingRepository) }
        }
        val modeloInicio = ViewModelProvider(this, fabrica)[HomeViewModel::class.java]
        setContent {
            ProyectoPlataformasMovilesTheme {
                AppNavigation(
                    homeViewModel = modeloInicio,
                    listingRepository = contenedor.listingRepository,
                    draftRepository = draftRepository
                )
            }
        }
    }
}
