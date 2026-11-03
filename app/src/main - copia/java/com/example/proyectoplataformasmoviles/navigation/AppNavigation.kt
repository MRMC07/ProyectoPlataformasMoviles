
package com.example.proyectoplataformasmoviles.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.proyectoplataformasmoviles.presentation.PublishViewModel
import com.example.proyectoplataformasmoviles.screens.ContactarScreen
import com.example.proyectoplataformasmoviles.screens.DetalleScreen
import com.example.proyectoplataformasmoviles.screens.InicioScreen
import com.example.proyectoplataformasmoviles.screens.MyProductsScreen
import com.example.proyectoplataformasmoviles.screens.ProfileScreen
import com.example.proyectoplataformasmoviles.screens.PublishScreen

private object Routes {
    const val HOME = "inicio"
    const val DETAIL = "detalle"
    const val CONTACT = "contactar"
    const val PUBLISH = "publish"
    const val MY_PRODUCTS = "my_products"
    const val PROFILE = "profile"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            InicioScreen(
                onPublicacionClick = {
                    navController.navigate(Routes.DETAIL)
                },
                onPublishClick = {
                    navController.navigate(Routes.PUBLISH)
                },
                onMyProductsClick = {
                    navController.navigate(Routes.MY_PRODUCTS)
                },
                onProfileClick = {
                    navController.navigate(Routes.PROFILE)
                }
            )
        }

        composable(Routes.DETAIL) {
            DetalleScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onContactarClick = {
                    navController.navigate(Routes.CONTACT)
                }
            )
        }

        composable(Routes.CONTACT) {
            ContactarScreen()
        }

        composable(Routes.PUBLISH) {
            val publishViewModel: PublishViewModel = viewModel()

            val publishState by publishViewModel.uiState
                .collectAsStateWithLifecycle()

            PublishScreen(
                state = publishState,
                onTitleChange =
                    publishViewModel::onTitleChange,
                onDescriptionChange =
                    publishViewModel::onDescriptionChange,
                onPriceChange =
                    publishViewModel::onPriceChange,
                onCategoryChange =
                    publishViewModel::onCategoryChange,
                onPublishClick =
                    publishViewModel::onSubmitClick,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.MY_PRODUCTS) {
            MyProductsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onPublishClick = {
                    navController.navigate(Routes.PUBLISH)
                }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onMyProductsClick = {
                    navController.navigate(Routes.MY_PRODUCTS)
                }
            )
        }
    }
}
