package com.example.proyectoplataformasmoviles.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.proyectoplataformasmoviles.data.repository.ChatRepository
import com.example.proyectoplataformasmoviles.data.repository.ProfileRepository
import com.example.proyectoplataformasmoviles.data.repository.RequestRepository
import com.example.proyectoplataformasmoviles.data.repository.ReviewRepository
import com.example.proyectoplataformasmoviles.domain.repository.ListingRepository
import com.example.proyectoplataformasmoviles.local.DraftRepository
import com.example.proyectoplataformasmoviles.presentation.MyProductsViewModel
import com.example.proyectoplataformasmoviles.presentation.chat.ChatViewModel
import com.example.proyectoplataformasmoviles.presentation.detail.DetailViewModel
import com.example.proyectoplataformasmoviles.presentation.home.HomeViewModel
import com.example.proyectoplataformasmoviles.presentation.profile.ProfileViewModel
import com.example.proyectoplataformasmoviles.presentation.publish.PublishViewModel
import com.example.proyectoplataformasmoviles.screens.ChatScreen
import com.example.proyectoplataformasmoviles.screens.ContactoConectado as ContactSummaryScreen
import com.example.proyectoplataformasmoviles.screens.DetailScreen
import com.example.proyectoplataformasmoviles.screens.EditListingRoute
import com.example.proyectoplataformasmoviles.screens.GuestProfileScreen
import com.example.proyectoplataformasmoviles.screens.HomeScreen
import com.example.proyectoplataformasmoviles.screens.ListingImageEditor
import com.example.proyectoplataformasmoviles.screens.ListingImageView
import com.example.proyectoplataformasmoviles.screens.ListingReviews
import com.example.proyectoplataformasmoviles.screens.ListingStatusControls
import com.example.proyectoplataformasmoviles.screens.MyProductsScreen
import com.example.proyectoplataformasmoviles.screens.NewRequestRoute
import com.example.proyectoplataformasmoviles.screens.ProfileRoute
import com.example.proyectoplataformasmoviles.screens.PublishScreen
import com.example.proyectoplataformasmoviles.screens.RegisterScreen
import com.example.proyectoplataformasmoviles.screens.RequestsRoute
import com.example.proyectoplataformasmoviles.screens.borradores.PublishScreen as DraftPublishScreen
import com.example.proyectoplataformasmoviles.presentation.PublishViewModel as DraftPublishViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object Routes {
    const val REGISTER = "register"
    const val HOME = "home"
    const val PUBLISH = "publish"
    const val PROFILE = "profile"
    const val DRAFTS = "drafts"
    const val NEW_DRAFT = "new_draft"
    const val REQUESTS = "requests"
    const val CHAT = "chat/{requestId}"
    const val DETAIL = "detail/{listingId}"
    const val EDIT = "edit/{listingId}"
    const val ORDER = "order/{listingId}"
    const val CONTACT =
        "contact/{listingId}?modality={modality}&note={note}&schedule={schedule}"

    fun detail(listingId: String): String {
        return "detail/${Uri.encode(listingId)}"
    }

    fun edit(listingId: String): String {
        return "edit/${Uri.encode(listingId)}"
    }

    fun order(listingId: String): String {
        return "order/${Uri.encode(listingId)}"
    }

    fun chat(requestId: String): String {
        return "chat/${Uri.encode(requestId)}"
    }

    fun contact(
        listingId: String,
        modality: String = "",
        note: String = "",
        schedule: String = ""
    ): String {
        return "contact/${Uri.encode(listingId)}" +
                "?modality=${Uri.encode(modality)}" +
                "&note=${Uri.encode(note)}" +
                "&schedule=${Uri.encode(schedule)}"
    }
}

@Composable
fun AppNavigation(
    homeViewModel: HomeViewModel,
    listingRepository: ListingRepository,
    draftRepository: DraftRepository,
    profileRepository: ProfileRepository,
    chatRepository: ChatRepository,
    requestRepository: RequestRepository
) {
    val navController = rememberNavController()
    val authentication = FirebaseAuth.getInstance()

    val usuarioAutenticado = authentication.currentUser?.let { usuario ->
        !usuario.isAnonymous && usuario.email != null
    } == true

    val destinoInicial = if (usuarioAutenticado) {
        Routes.HOME
    } else {
        Routes.REGISTER
    }

    val reviewRepository = remember(authentication) {
        ReviewRepository(
            firestore = FirebaseFirestore.getInstance(),
            authentication = authentication
        )
    }

    val catalogState by homeViewModel.uiState
        .collectAsStateWithLifecycle()

    val activeListings = catalogState.listings.filter {
        it.isActive
    }

    var currentUserName by rememberSaveable {
        mutableStateOf(
            authentication.currentUser?.displayName ?: "Visitante"
        )
    }

    var currentUserRole by rememberSaveable {
        mutableStateOf("Estudiante")
    }

    NavHost(
        navController = navController,
        startDestination = destinoInicial
    ) {
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = { name, role ->
                    homeViewModel.onRetryClick()
                    currentUserName = name
                    currentUserRole = role

                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.REGISTER) {
                            inclusive = true
                        }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                },
                mostrarRegresar = false
            )
        }

        composable(Routes.HOME) {
            val usuario = authentication.currentUser

            if (
                usuario == null ||
                usuario.isAnonymous ||
                usuario.email == null
            ) {
                LaunchedEffect(usuario?.uid) {
                    navController.navigate(Routes.REGISTER) {
                        popUpTo(Routes.HOME) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            } else {
                HomeScreen(
                    necesitaAcceso = false,
                    state = catalogState.copy(
                        listings = activeListings
                    ),
                    userName = currentUserName,
                    userRole = currentUserRole,
                    onListingClick = { listingId ->
                        navController.navigate(
                            Routes.detail(listingId)
                        )
                    },
                    onPublishClick = {
                        navController.navigate(Routes.PUBLISH)
                    },
                    onProfileClick = {
                        navController.navigate(Routes.PROFILE)
                    },
                    onRetryClick = homeViewModel::onRetryClick,
                    listingImageContent = { listing, imageModifier ->
                        ListingImageView(
                            listing = listing,
                            repository = listingRepository,
                            modifier = imageModifier
                        )
                    }
                )
            }
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

            val currentUser = authentication.currentUser

            val isOwner = currentUser != null &&
                    !currentUser.isAnonymous &&
                    detailState.listing?.propietario == currentUser.uid

            DetailScreen(
                state = detailState,
                onBackClick = {
                    navController.popBackStack()
                },
                onContactClick = {
                    navController.navigate(
                        Routes.order(listingId)
                    )
                },
                onRetryClick = detailViewModel::onRetryClick,
                canEdit = isOwner,
                onEditClick = {
                    navController.navigate(
                        Routes.edit(listingId)
                    )
                },
                managementContent = {
                    detailState.listing?.let { listing ->
                        ListingStatusControls(
                            listing = listing,
                            canManage = isOwner,
                            isUpdating = detailState.isUpdatingStatus,
                            errorMessage = detailState.statusErrorMessage,
                            onChangeStatus = detailViewModel::setListingActive
                        )
                    }
                },
                listingImageContent = { listing, imageModifier ->
                    ListingImageView(
                        listing = listing,
                        repository = listingRepository,
                        modifier = imageModifier
                    )
                },
                reviewContent = {
                    detailState.listing?.let { listing ->
                        ListingReviews(
                            listing = listing,
                            repository = reviewRepository
                        )
                    }
                }
            )
        }

        composable(
            route = Routes.EDIT,
            arguments = listOf(
                navArgument("listingId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments
                ?.getString("listingId")
                .orEmpty()

            EditListingRoute(
                listingId = listingId,
                repository = listingRepository,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.REQUESTS) {
            RequestsRoute(
                repository = requestRepository,
                onBackClick = {
                    navController.popBackStack()
                },
                onSignInClick = {
                    navController.navigate(Routes.REGISTER)
                },
                onChatClick = { request ->
                    navController.navigate(Routes.chat(request.id))
                }
            )
        }

        composable(
            route = Routes.CHAT,
            arguments = listOf(
                navArgument("requestId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val requestId = backStackEntry.arguments
                ?.getString("requestId")
                .orEmpty()

            val user = authentication.currentUser?.takeUnless {
                it.isAnonymous || it.email == null
            }

            if (user == null) {
                LaunchedEffect(requestId) {
                    navController.popBackStack()
                    navController.navigate(Routes.REGISTER)
                }
            } else {
                val chatViewModel: ChatViewModel = viewModel(
                    key = "chat:${user.uid}:$requestId",
                    factory = viewModelFactory {
                        initializer {
                            ChatViewModel(
                                repository = chatRepository,
                                userId = user.uid,
                                requestId = requestId
                            )
                        }
                    }
                )

                ChatScreen(
                    viewModel = chatViewModel,
                    userId = user.uid,
                    onVolver = {
                        navController.popBackStack()
                    }
                )
            }
        }

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

            NewRequestRoute(
                listing = activeListings.firstOrNull {
                    it.id == listingId
                },
                repository = requestRepository,
                onBackClick = {
                    navController.popBackStack()
                },
                onSent = {
                    navController.navigate(Routes.REQUESTS) {
                        popUpTo(Routes.ORDER) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onRequestsClick = {
                    navController.navigate(Routes.REQUESTS)
                },
                onSignInClick = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

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

            ContactSummaryScreen(
                publicacion = activeListings.firstOrNull {
                    it.id == listingId
                },
                alVolver = {
                    navController.popBackStack()
                },
                modalidad = modality,
                horario = schedule,
                nota = note
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
                },
                imageContent = {
                    ListingImageEditor(
                        state = publishState,
                        repository = listingRepository,
                        onImageSelected =
                            publishViewModel::onImageSelected,
                        onRemoveImageClick =
                            publishViewModel::onRemoveImageClick
                    )
                }
            )
        }

        composable(Routes.DRAFTS) {
            val draftsViewModel: MyProductsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        MyProductsViewModel(draftRepository)
                    }
                }
            )

            val draftsState by draftsViewModel.uiState
                .collectAsStateWithLifecycle()

            MyProductsScreen(
                state = draftsState,
                onBackClick = {
                    navController.popBackStack()
                },
                onPublishClick = {
                    navController.navigate(Routes.NEW_DRAFT)
                },
                onDeleteDraft = draftsViewModel::deleteDraft
            )
        }

        composable(Routes.NEW_DRAFT) {
            val draftViewModel: DraftPublishViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        DraftPublishViewModel(draftRepository)
                    }
                }
            )

            val draftState by draftViewModel.uiState
                .collectAsStateWithLifecycle()

            LaunchedEffect(draftState.isSaved) {
                if (draftState.isSaved) {
                    navController.popBackStack()
                }
            }

            DraftPublishScreen(
                state = draftState,
                onTitleChange = draftViewModel::onTitleChange,
                onDescriptionChange =
                    draftViewModel::onDescriptionChange,
                onPriceChange = draftViewModel::onPriceChange,
                onCategoryChange =
                    draftViewModel::onCategoryChange,
                onPublishClick = draftViewModel::onSubmitClick,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.PROFILE) {
            val user = authentication.currentUser

            if (user == null || user.isAnonymous) {
                GuestProfileScreen(
                    onSignInClick = {
                        navController.navigate(Routes.REGISTER)
                    },
                    onDraftsClick = {
                        navController.navigate(Routes.DRAFTS)
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            } else {
                val profileViewModel: ProfileViewModel = viewModel(
                    key = user.uid,
                    factory = viewModelFactory {
                        initializer {
                            ProfileViewModel(
                                repository = profileRepository,
                                userId = user.uid,
                                initialName = user.displayName.orEmpty()
                            )
                        }
                    }
                )

                ProfileRoute(
                    viewModel = profileViewModel,
                    listings = catalogState.listings.filter {
                        it.propietario == user.uid
                    },
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onListingClick = { listingId ->
                        navController.navigate(
                            Routes.detail(listingId)
                        )
                    },
                    onPublishClick = {
                        navController.navigate(Routes.PUBLISH)
                    },
                    onLogoutClick = {
                        authentication.signOut()
                        homeViewModel.onRetryClick()

                        currentUserName = "Visitante"
                        currentUserRole = "Estudiante"

                        navController.navigate(Routes.REGISTER) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    },
                    onDraftsClick = {
                        navController.navigate(Routes.DRAFTS)
                    },
                    onNameChanged = { name ->
                        currentUserName = name
                    },
                    onRequestsClick = {
                        navController.navigate(Routes.REQUESTS)
                    }
                )
            }
        }
    }
}