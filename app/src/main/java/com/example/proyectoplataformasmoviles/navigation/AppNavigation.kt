package com.example.proyectoplataformasmoviles.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.proyectoplataformasmoviles.screens.OnboardingScreen
import com.example.proyectoplataformasmoviles.screens.ProfileScreen
import com.example.proyectoplataformasmoviles.screens.PublishScreen
import com.example.proyectoplataformasmoviles.screens.RegisterScreen
import com.example.proyectoplataformasmoviles.screens.ServiceOrderScreen

object Routes {
    const val ONBOARDING = "onboarding"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PUBLISH = "publish"
    const val PROFILE = "profile"
    const val BORRADORES = "borradores"
    const val BORRADOR_NUEVO = "borrador_nuevo"
    const val DETAIL = "detail/{listingId}"
    const val ORDER = "order/{listingId}"
    const val CONTACT = "contact/{listingId}?modality={modality}&note={note}&schedule={schedule}"

    fun detail(listingId: String): String {
        return "detail/${Uri.encode(listingId)}"
    }

    fun order(listingId: String): String {
        return "order/${Uri.encode(listingId)}"
    }

    fun contact(
        listingId: String,
        modality: String = "",
        note: String = "",
        schedule: String = ""
    ): String {
        return "contact/${Uri.encode(listingId)}?modality=${Uri.encode(modality)}&note=${Uri.encode(note)}&schedule=${Uri.encode(schedule)}"
    }
}

@Composable
fun AppNavigation(
    homeViewModel: HomeViewModel,
    listingRepository: ListingRepository,
    draftRepository: com.example.proyectoplataformasmoviles.local.DraftRepository
) {
    val navController = rememberNavController()
    val catalogo by homeViewModel.uiState.collectAsStateWithLifecycle()

    // Estado de sesión en memoria para unificar todo el trayecto unilineal
    var currentUserName by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.displayName ?: "Visitante") }
    var currentUserRole by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("Estudiante") }

    NavHost(
        navController = navController,
        startDestination = Routes.ONBOARDING
    ) {
        // 1. Bienvenida a Nexora
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onStartRegistration = {
                    navController.navigate(Routes.REGISTER)
                },
                onExploreAsGuest = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // 2. Creación de Usuario / Registro
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { name, role ->
                    homeViewModel.onRetryClick()
                    currentUserName = name
                    currentUserRole = role
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // 3. Catálogo de emprendimientos universitarios
        composable(Routes.HOME) {
            val homeState by homeViewModel.uiState
                .collectAsStateWithLifecycle()

            HomeScreen(
                necesitaAcceso = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.isAnonymous != false,
                state = homeState,
                userName = currentUserName,
                userRole = currentUserRole,
                onListingClick = { listingId ->
                    navController.navigate(Routes.detail(listingId))
                },
                onPublishClick = {
                    if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.isAnonymous == false) navController.navigate(Routes.PUBLISH) else navController.navigate(Routes.REGISTER)
                },
                onProfileClick = {
                    navController.navigate(Routes.PROFILE)
                },
                onRetryClick = homeViewModel::onRetryClick
            )
        }

        // 4. Detalle del Servicio
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
                    navController.navigate(Routes.order(listingId))
                },
                onRetryClick = detailViewModel::onRetryClick
            )
        }

        // 5. Preparación de la consulta al emprendedor
        composable(
            route = Routes.ORDER,
            arguments = listOf(
                navArgument("listingId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments
                ?.getString("listingId")
                .orEmpty()

            ServiceOrderScreen(
                listings = catalogo.listings,
                listingId = listingId,
                onBackClick = {
                    navController.popBackStack()
                },
                onProceedToChat = { modality, note, schedule ->
                    navController.navigate(
                        Routes.contact(
                            listingId = listingId,
                            modality = modality,
                            note = note,
                            schedule = schedule
                        )
                    )
                }
            )
        }

        // 6. Revisión de la consulta y contacto por correo
        composable(
            route = Routes.CONTACT,
            arguments = listOf(
                navArgument("listingId") {
                    type = NavType.StringType
                },
                navArgument("modality") {
                    type = NavType.StringType
                    defaultValue = "Lugar a convenir"
                },
                navArgument("note") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("schedule") {
                    type = NavType.StringType
                    defaultValue = "Horario a convenir"
                }
            )
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments
                ?.getString("listingId")
                .orEmpty()
            val modality = backStackEntry.arguments
                ?.getString("modality")
                .orEmpty()
            val note = backStackEntry.arguments
                ?.getString("note")
                .orEmpty()
            val schedule = backStackEntry.arguments
                ?.getString("schedule")
                .orEmpty()

            com.example.proyectoplataformasmoviles.screens.ContactoConectado(
                publicacion = catalogo.listings.firstOrNull { it.id == listingId },
                alVolver = { navController.popBackStack() },
                modalidad = modality, horario = schedule, nota = note
            )
        }
        // 7. Publicación de productos y servicios
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
                onDescriptionChange = publishViewModel::onDescriptionChange,
                onPriceChange = publishViewModel::onPriceChange,
                onCategoryChange = publishViewModel::onCategoryChange,
                onPublishClick = publishViewModel::onPublishClick,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.BORRADORES) {
            val modelo: com.example.proyectoplataformasmoviles.presentation.MyProductsViewModel = viewModel(
                factory = viewModelFactory { initializer {
                    com.example.proyectoplataformasmoviles.presentation.MyProductsViewModel(draftRepository)
                } }
            )
            val estado by modelo.uiState.collectAsStateWithLifecycle()
            com.example.proyectoplataformasmoviles.screens.MyProductsScreen(
                state = estado, onBackClick = { navController.popBackStack() },
                onPublishClick = { navController.navigate(Routes.BORRADOR_NUEVO) },
                onDeleteDraft = modelo::deleteDraft
            )
        }
        composable(Routes.BORRADOR_NUEVO) {
            val modelo: com.example.proyectoplataformasmoviles.presentation.PublishViewModel = viewModel(
                factory = viewModelFactory { initializer {
                    com.example.proyectoplataformasmoviles.presentation.PublishViewModel(draftRepository)
                } }
            )
            val estado by modelo.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(estado.isSaved) {
                if (estado.isSaved) navController.popBackStack()
            }
            com.example.proyectoplataformasmoviles.screens.borradores.PublishScreen(
                state = estado, onTitleChange = modelo::onTitleChange,
                onDescriptionChange = modelo::onDescriptionChange, onPriceChange = modelo::onPriceChange,
                onCategoryChange = modelo::onCategoryChange, onPublishClick = modelo::onSubmitClick,
                onBackClick = { navController.popBackStack() }
            )
        }
        // 8. Perfil de Usuario ("Perfil E." en el pizarrón)
        composable(Routes.PROFILE) {
            ProfileScreen(
                userName = currentUserName,
                userRole = currentUserRole,
                alVerBorradores = { navController.navigate(Routes.BORRADORES) },
                userListings = catalogo.listings.filter { it.propietario == com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid },
                onBackClick = {
                    navController.popBackStack()
                },
                onListingClick = { listingId ->
                    navController.navigate(Routes.detail(listingId))
                },
                onPublishClick = {
                    if (com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.isAnonymous == false) navController.navigate(Routes.PUBLISH) else navController.navigate(Routes.REGISTER)
                },
                onLogoutClick = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    homeViewModel.onRetryClick()
                    currentUserName = "Visitante"
                    currentUserRole = "Estudiante"
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
