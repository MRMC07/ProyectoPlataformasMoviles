package com.example.proyectoplataformasmoviles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.proyectoplataformasmoviles.navigation.AppNavigation
import com.example.proyectoplataformasmoviles.ui.theme.ProyectoPlataformasMovilesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ProyectoPlataformasMovilesTheme {
                AppNavigation()
            }
        }
    }
}