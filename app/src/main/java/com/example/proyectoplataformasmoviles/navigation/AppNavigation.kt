
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

    fun detail(listingId: String): String {
        return "detail/${Uri.encode(listingId)}"
    }

    fun contact(listingId: String): String {
        return "contact/${Uri.encode(listingId)}"
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
            val homeState by homeViewModel.uiState
                .collectAsStateWithLifecycle()

            HomeScreen(
                state = homeState,
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
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments
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
