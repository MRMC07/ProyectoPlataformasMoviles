package com.example.proyectoplataformasmoviles.navigation
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.proyectoplataformasmoviles.presentation.MyProductsViewModel
import com.example.proyectoplataformasmoviles.presentation.PublishViewModel
import com.example.proyectoplataformasmoviles.screens.ConnectionBanner
import com.example.proyectoplataformasmoviles.screens.ContactarScreen
import com.example.proyectoplataformasmoviles.screens.DetalleScreen
import com.example.proyectoplataformasmoviles.screens.InicioScreen
import com.example.proyectoplataformasmoviles.screens.MyProductsScreen
import com.example.proyectoplataformasmoviles.screens.ProfileScreen
import com.example.proyectoplataformasmoviles.screens.PublishScreen

private object Routes {
    const val HOME = "inicio"
    const val DETAIL = "detalle/{listingId}"
    const val CONTACT = "contactar/{listingId}"
    const val PUBLISH = "publish"
    const val MY_PRODUCTS = "my_products"
    const val PROFILE = "profile"

    fun detail(listingId: String): String {
        return "detalle/${Uri.encode(listingId)}"
    }

    fun contact(listingId: String): String {
        return "contactar/${Uri.encode(listingId)}"
    }
}

@Composable
fun AppNavigation(
    draftRepository: DraftRepository
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val connectionStatus = remember(context) {
        ConnectionStatus(context.applicationContext)
    }

    val isConnected by connectionStatus.isConnected
        .collectAsStateWithLifecycle(initialValue = true)

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
                InicioScreen(
                    onPublicacionClick = {
                        navController.navigate(
                            Routes.detail("demo_listing")
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

                DetalleScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onContactarClick = {
                        navController.navigate(
                            Routes.contact(listingId)
                        )
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
            ) {
                ContactarScreen()
            }

            composable(Routes.PUBLISH) {
                val publishViewModel: PublishViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            PublishViewModel(
                                repository = draftRepository
                            )
                        }
                    }
                )

                val publishState by publishViewModel.uiState
                    .collectAsStateWithLifecycle()

                LaunchedEffect(publishState.isSaved) {
                    if (publishState.isSaved) {
                        navController.popBackStack()
                    }
                }

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
                    onDeleteDraft =
                        myProductsViewModel::deleteDraft
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
