package com.example.proyectoplataformasmoviles.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyectoplataformasmoviles.domain.repository.ListingRepository
import com.example.proyectoplataformasmoviles.presentation.detail.DetailViewModel
import com.example.proyectoplataformasmoviles.presentation.home.HomeViewModel
import com.example.proyectoplataformasmoviles.presentation.publish.PublishViewModel
import com.example.proyectoplataformasmoviles.screens.ContactScreen
import com.example.proyectoplataformasmoviles.screens.DetailScreen
import com.example.proyectoplataformasmoviles.screens.HomeScreen
import com.example.proyectoplataformasmoviles.screens.PublishScreen

@Composable
fun AppNavigation(
    homeViewModel: HomeViewModel,
    listingRepository: ListingRepository
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            val homeState by homeViewModel.uiState
                .collectAsStateWithLifecycle()

            HomeScreen(
                state = homeState,
                onListingClick = { listingId ->
                    navController.navigate("detail/$listingId")
                },
                onPublishClick = {
                    navController.navigate("publish")
                },
                onRetryClick = homeViewModel::onRetryClick
            )
        }

        composable(
            route = "detail/{listingId}",
            arguments = listOf(
                navArgument("listingId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments
                ?.getString("listingId")
                .orEmpty()

            val detailViewModel: DetailViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        DetailViewModel(
                            listingId = listingId,
                            repository = listingRepository
                        )
                    }
                }
            )

            val detailState by detailViewModel.uiState
                .collectAsStateWithLifecycle()

            DetailScreen(
                state = detailState,
                onBackClick = {
                    navController.popBackStack()
                },
                onContactClick = {
                    navController.navigate("contact")
                },
                onRetryClick = detailViewModel::onRetryClick
            )
        }

        composable("publish") {
            val publishViewModel: PublishViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        PublishViewModel(
                            repository = listingRepository
                        )
                    }
                }
            )

            val publishState by publishViewModel.uiState
                .collectAsStateWithLifecycle()

            LaunchedEffect(publishState.isPublished) {
                if (publishState.isPublished) {
                    navController.popBackStack()
                }
            }

            PublishScreen(
                state = publishState,
                onTitleChange = publishViewModel::onTitleChange,
                onDescriptionChange = publishViewModel::onDescriptionChange,
                onPriceChange = publishViewModel::onPriceChange,
                onCategoryChange = publishViewModel::onCategoryChange,
                onPublishClick = publishViewModel::onPublishClick,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("contact") {
            ContactScreen()
        }
    }
}