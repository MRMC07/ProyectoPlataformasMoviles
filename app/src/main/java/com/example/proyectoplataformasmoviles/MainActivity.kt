
package com.example.proyectoplataformasmoviles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.room.Room
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ProyectoPlataformasMovilesTheme {
                AppNavigation(
                    draftRepository = draftRepository
                )
            }
        }
    }
}
