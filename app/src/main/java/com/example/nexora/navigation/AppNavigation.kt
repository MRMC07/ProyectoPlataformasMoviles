package com.example.nexora.navigation

import android.net.Uri
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.nexora.data.repository.AuthRepository
import com.example.nexora.data.repository.ChatRepository
import com.example.nexora.data.repository.ProfileRepository
import com.example.nexora.data.repository.RequestRepository
import com.example.nexora.data.repository.ReviewRepository
import com.example.nexora.domain.model.AuthenticationException
import com.example.nexora.domain.repository.ListingRepository
import com.example.nexora.local.DraftRepository
import com.example.nexora.presentation.MyProductsViewModel
import com.example.nexora.presentation.account.AccountViewModel
import com.example.nexora.presentation.chat.ChatViewModel
import com.example.nexora.presentation.detail.DetailViewModel
import com.example.nexora.presentation.home.HomeViewModel
import com.example.nexora.presentation.profile.ProfileViewModel
import com.example.nexora.presentation.publish.PublishViewModel
import com.example.nexora.screens.ChatScreen
import com.example.nexora.screens.ContactSummaryScreen
import com.example.nexora.screens.DetailScreen
import com.example.nexora.screens.EditListingRoute
import com.example.nexora.screens.GuestProfileScreen
import com.example.nexora.screens.HomeScreen
import com.example.nexora.screens.ListingImageEditor
import com.example.nexora.screens.ListingImageView
import com.example.nexora.screens.ListingReviews
import com.example.nexora.screens.ListingStatusControls
import com.example.nexora.screens.MyProductsScreen
import com.example.nexora.screens.NewRequestRoute
import com.example.nexora.screens.ProfileRoute
import com.example.nexora.screens.PublishScreen
import com.example.nexora.screens.RegisterScreen
import com.example.nexora.screens.RequestsRoute
import kotlinx.coroutines.launch
import com.example.nexora.presentation.PublishViewModel as DraftPublishViewModel
import com.example.nexora.screens.drafts.PublishScreen as DraftPublishScreen

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
    requestRepository: RequestRepository,
    authRepository: AuthRepository,
    reviewRepository: ReviewRepository
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    val accountFlow = remember(authRepository) {
        authRepository.observeUser()
    }

    val currentAccount by accountFlow.collectAsStateWithLifecycle(
        initialValue = authRepository.currentUser
    )

    val startDestination = remember {
        if (authRepository.currentUser != null) {
            Routes.HOME
        } else {
            Routes.REGISTER
        }
    }

    val catalogState by homeViewModel.uiState
        .collectAsStateWithLifecycle()

    val activeListings = catalogState.listings.filter {
        it.isActive
    }

    var currentUserName by rememberSaveable {
        mutableStateOf(
            authRepository.currentUser?.let { user ->
                user.name.ifBlank { user.email }
            } ?: "Visitante"
        )
    }

    var currentUserRole by rememberSaveable {
        mutableStateOf("Estudiante")
    }

    var isSigningOut by remember {
        mutableStateOf(false)
    }

    var signOutError by remember {
        mutableStateOf<String?>(null)
    }

    signOutError?.let { message ->
        AlertDialog(
            onDismissRequest = {
                signOutError = null
            },
            title = {
                Text("No pudimos cerrar la sesión")
            },
            text = {
                Text(message)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        signOutError = null
                    }
                ) {
                    Text("Aceptar")
                }
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.REGISTER) {
            val accountViewModel: AccountViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        AccountViewModel(
                            authRepository = authRepository
                        )
                    }
                }
            )

            val accountState by accountViewModel.uiState
                .collectAsStateWithLifecycle()

            LaunchedEffect(accountState.authenticatedUser) {
                val user = accountState.authenticatedUser

                if (user != null) {
                    currentUserName = user.name.ifBlank {
                        user.email
                    }
                    currentUserRole = "Estudiante"

                    accountViewModel.clearAuthenticatedUser()
                    homeViewModel.onRetryClick()

                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.REGISTER) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }

            RegisterScreen(
                state = accountState,
                onNameChange = accountViewModel::updateName,
                onEmailChange = accountViewModel::updateEmail,
                onPasswordChange = accountViewModel::updatePassword,
                onCreatingAccountChange =
                    accountViewModel::setCreatingAccount,
                onSubmitClick = accountViewModel::authenticate,
                onBackClick = {
                    navController.popBackStack()
                },
                showBackButton = false
            )
        }

        composable(Routes.HOME) {
            val user = currentAccount

            if (user == null) {
                LaunchedEffect(Unit) {
                    navController.navigate(Routes.REGISTER) {
                        popUpTo(Routes.HOME) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            } else {
                HomeScreen(
                    requiresSignIn = false,
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

            val user = currentAccount

            val isOwner = user != null &&
                    detailState.listing?.ownerId == user.id

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
                            onChangeStatus =
                                detailViewModel::setListingActive
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
                            userId = currentAccount?.id,
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
                userId = currentAccount?.id,
                repository = listingRepository,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.REQUESTS) {
            RequestsRoute(
                userId = currentAccount?.id,
                repository = requestRepository,
                onBackClick = {
                    navController.popBackStack()
                },
                onSignInClick = {
                    navController.navigate(Routes.REGISTER)
                },
                onChatClick = { request ->
                    navController.navigate(
                        Routes.chat(request.id)
                    )
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

            val user = currentAccount

            if (user == null) {
                LaunchedEffect(requestId) {
                    navController.popBackStack()
                    navController.navigate(Routes.REGISTER) {
                        launchSingleTop = true
                    }
                }
            } else {
                val chatViewModel: ChatViewModel = viewModel(
                    key = "chat:${user.id}:$requestId",
                    factory = viewModelFactory {
                        initializer {
                            ChatViewModel(
                                repository = chatRepository,
                                userId = user.id,
                                requestId = requestId
                            )
                        }
                    }
                )

                ChatScreen(
                    viewModel = chatViewModel,
                    userId = user.id,
                    onBack = {
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
                userId = currentAccount?.id,
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
                listing = activeListings.firstOrNull {
                    it.id == listingId
                },
                onBack = {
                    navController.popBackStack()
                },
                modality = modality,
                schedule = schedule,
                note = note
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
            val user = currentAccount

            if (user == null) {
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
                    key = user.id,
                    factory = viewModelFactory {
                        initializer {
                            ProfileViewModel(
                                repository = profileRepository,
                                userId = user.id,
                                initialName = user.name
                            )
                        }
                    }
                )

                ProfileRoute(
                    viewModel = profileViewModel,
                    listings = catalogState.listings.filter {
                        it.ownerId == user.id
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
                        if (!isSigningOut) {
                            isSigningOut = true

                            coroutineScope.launch {
                                try {
                                    authRepository.signOut()

                                    currentUserName = "Visitante"
                                    currentUserRole = "Estudiante"

                                    homeViewModel.onRetryClick()

                                    navController.navigate(
                                        Routes.REGISTER
                                    ) {
                                        popUpTo(0) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                } catch (_: AuthenticationException) {
                                    signOutError =
                                        "Volvé a intentar cerrar la sesión."
                                } finally {
                                    isSigningOut = false
                                }
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