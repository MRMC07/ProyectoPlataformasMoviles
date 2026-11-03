package com.example.proyectoplataformasmoviles.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyectoplataformasmoviles.local.DraftRepository
import com.example.proyectoplataformasmoviles.network.ConnectionStatus
import com.example.proyectoplataformasmoviles.presentation.DetailViewModel
import com.example.proyectoplataformasmoviles.presentation.ListingsViewModel
import com.example.proyectoplataformasmoviles.presentation.MyProductsViewModel
import com.example.proyectoplataformasmoviles.presentation.PublishViewModel
import com.example.proyectoplataformasmoviles.remote.ListingRepository
import com.example.proyectoplataformasmoviles.screens.ChatDetailScreen
import com.example.proyectoplataformasmoviles.screens.ChatsScreen
import com.example.proyectoplataformasmoviles.screens.ConnectionBanner
import com.example.proyectoplataformasmoviles.screens.DetailScreen
import com.example.proyectoplataformasmoviles.screens.HomeScreen
import com.example.proyectoplataformasmoviles.screens.MyProductsScreen
import com.example.proyectoplataformasmoviles.screens.ProfileScreen
import com.example.proyectoplataformasmoviles.screens.PublishScreen

private object Routes {
    const val HOME = "home"
    const val CHATS = "chats"
    const val DETAIL = "detail/{listingId}"
    const val CONTACT = "contact/{listingId}/{sellerName}"
    const val PUBLISH = "publish"
    const val MY_PRODUCTS = "my_products"
    const val PROFILE = "profile"

    fun detail(listingId: String): String {
        return "detail/${Uri.encode(listingId)}"
    }

    fun contact(listingId: String, sellerName: String): String {
        return "contact/${Uri.encode(listingId)}/${Uri.encode(sellerName)}"
    }
}

@Composable
fun AppNavigation(
    draftRepository: DraftRepository,
    listingRepository: ListingRepository
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val connectionStatus = remember(context) {
        ConnectionStatus(context.applicationContext)
    }

    val isConnected by connectionStatus.isConnected
        .collectAsStateWithLifecycle(initialValue = true)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            ConnectionBanner(
                isConnected = isConnected
            )

            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.weight(1f)
            ) {
                composable(Routes.HOME) {
                    val listingsViewModel: ListingsViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer {
                                ListingsViewModel(
                                    repository = listingRepository
                                )
                            }
                        }
                    )

                    val listingsState by listingsViewModel.uiState
                        .collectAsStateWithLifecycle()

                    HomeScreen(
                        state = listingsState,
                        onListingClick = { listingId ->
                            navController.navigate(
                                Routes.detail(listingId)
                            )
                        },
                        onPublishClick = {
                            navController.navigate(Routes.PUBLISH)
                        },
                        onMyProductsClick = {
                            navController.navigate(Routes.MY_PRODUCTS)
                        },
                        onProfileClick = {
                            navController.navigate(Routes.PROFILE)
                        },
                        onChatsClick = {
                            navController.navigate(Routes.CHATS)
                        },
                        onRetryClick = listingsViewModel::onRetryClick
                    )
                }

                composable(Routes.CHATS) {
                    ChatsScreen(
                        onChatClick = { listingId ->
                            navController.navigate(
                                Routes.contact(listingId, "Vendedor")
                            )
                        },
                        onHomeClick = {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                            }
                        },
                        onMyProductsClick = {
                            navController.navigate(Routes.MY_PRODUCTS)
                        },
                        onProfileClick = {
                            navController.navigate(Routes.PROFILE)
                        }
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
                        key = "detail_$listingId",
                        factory = viewModelFactory {
                            initializer {
                                DetailViewModel(
                                    repository = listingRepository,
                                    listingId = listingId
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
                            val sellerName = detailState.listing?.sellerName ?: "Vendedor"
                            navController.navigate(
                                Routes.contact(listingId, sellerName)
                            )
                        }
                    )
                }

                composable(
                    route = Routes.CONTACT,
                    arguments = listOf(
                        navArgument("listingId") {
                            type = NavType.StringType
                        },
                        navArgument("sellerName") {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val listingId = backStackEntry.arguments
                        ?.getString("listingId")
                        .orEmpty()

                    val sellerName = backStackEntry.arguments
                        ?.getString("sellerName")
                        ?: "Vendedor"

                    ChatDetailScreen(
                        chatId = listingId,
                        sellerName = sellerName,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Routes.PUBLISH) {
                    val publishViewModel: PublishViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer {
                                PublishViewModel(
                                    draftRepository = draftRepository,
                                    listingRepository = listingRepository
                                )
                            }
                        }
                    )

                    val publishState by publishViewModel.uiState
                        .collectAsStateWithLifecycle()

                    PublishScreen(
                        state = publishState,
                        onTitleChange = publishViewModel::onTitleChange,
                        onDescriptionChange = publishViewModel::onDescriptionChange,
                        onPriceChange = publishViewModel::onPriceChange,
                        onCategoryChange = publishViewModel::onCategoryChange,
                        onPublishClick = publishViewModel::onSubmitClick,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Routes.MY_PRODUCTS) {
                    val myProductsViewModel: MyProductsViewModel = viewModel(
                        factory = viewModelFactory {
                            initializer {
                                MyProductsViewModel(
                                    repository = draftRepository
                                )
                            }
                        }
                    )

                    val productsState by myProductsViewModel.uiState
                        .collectAsStateWithLifecycle()

                    MyProductsScreen(
                        state = productsState,
                        onBackClick = {
                            navController.popBackStack()
                        },
                        onPublishClick = {
                            navController.navigate(Routes.PUBLISH)
                        },
                        onDeleteDraft = myProductsViewModel::deleteDraft
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
    }
}