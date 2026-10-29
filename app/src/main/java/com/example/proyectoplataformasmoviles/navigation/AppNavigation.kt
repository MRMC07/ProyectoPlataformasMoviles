
package com.example.proyectoplataformasmoviles.navigation

import android.net.Uri
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

private object Routes {
    const val HOME = "home"
    const val PUBLISH = "publish"
    const val DETAIL = "detail/{listingId}"
    const val CONTACT = "contact/{listingId}"

    fun detail(id: String): String {
        return "detail/${Uri.encode(id)}"
    }

    fun contact(id: String): String {
        return "contact/${Uri.encode(id)}"
    }
}

@Composable
fun AppNavigation(
    homeViewModel: HomeViewModel,
    listingRepository: ListingRepository
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            val state by homeViewModel.uiState
                .collectAsStateWithLifecycle()

            HomeScreen(
                state = state,
                onListingClick = { listingId ->
                    navController.navigate(
                        Routes.detail(listingId)
                    )
                },
                onPublishClick = {
                    navController.navigate(Routes.PUBLISH)
                },
                onRetryClick = homeViewModel::onRetryClick
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(
                navArgument("listingId") {
                    type = NavType.StringType
                }
            )
        ) { entry ->
            val listingId = entry.arguments
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

            val state by detailViewModel.uiState
                .collectAsStateWithLifecycle()

            DetailScreen(
                state = state,
                onBackClick = {
                    navController.popBackStack()
                },
                onContactClick = {
                    navController.navigate(
                        Routes.contact(listingId)
                    )
                },
                onRetryClick = detailViewModel::onRetryClick
            )
        }

        composable(Routes.PUBLISH) {
            val publishViewModel: PublishViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        PublishViewModel(
                            repository = listingRepository
                        )
                    }
                }
            )

            val state by publishViewModel.uiState
                .collectAsStateWithLifecycle()

            LaunchedEffect(state.isPublished) {
                if (state.isPublished) {
                    navController.popBackStack()
                }
            }

            PublishScreen(
                state = state,
                onTitleChange = publishViewModel::onTitleChange,
                onDescriptionChange =
                    publishViewModel::onDescriptionChange,
                onPriceChange = publishViewModel::onPriceChange,
                onCategoryChange =
                    publishViewModel::onCategoryChange,
                onPublishClick = publishViewModel::onPublishClick,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.CONTACT,
            arguments = listOf(
                navArgument("listingId") {
                    type = NavType.StringType
                }
            )
        ) { entry ->
            val listingId = entry.arguments
                ?.getString("listingId")
                .orEmpty()

            ContactScreen(
                listingId = listingId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
