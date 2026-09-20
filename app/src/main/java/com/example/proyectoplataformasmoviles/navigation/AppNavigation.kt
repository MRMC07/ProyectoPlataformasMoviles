package com.example.proyectoplataformasmoviles.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyectoplataformasmoviles.screens.ContactarScreen
import com.example.proyectoplataformasmoviles.screens.DetalleScreen
import com.example.proyectoplataformasmoviles.screens.InicioScreen
import com.example.proyectoplataformasmoviles.screens.PublicarScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "inicio"
    ) {

        composable("inicio") {

            InicioScreen(
                onPublicacionClick = {
                    navController.navigate("detalle")
                },
                onPublicarClick = {
                    navController.navigate("publicar")
                }
            )
        }
        composable("detalle") {

            DetalleScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onContactarClick = {
                    navController.navigate("contactar")
                }
            )
        }

        composable("publicar") {

            PublicarScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPublicarClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("contactar") {

            ContactarScreen()
        }
    }
}