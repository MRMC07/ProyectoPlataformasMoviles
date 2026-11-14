package com.example.proyectoplataformasmoviles.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.presentation.profile.ProfileViewModel

@Composable
fun GuestProfileScreen(
    onSignInClick: () -> Unit,
    onDraftsClick: () -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Ingresá para ver y editar tu perfil.")

            Button(onClick = onSignInClick) {
                Text("Ingresar o crear cuenta")
            }

            TextButton(onClick = onDraftsClick) {
                Text("Borradores de este dispositivo")
            }

            TextButton(onClick = onBackClick) {
                Text("Regresar")
            }
        }
    }
}

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel,
    listings: List<Listing>,
    onBackClick: () -> Unit,
    onListingClick: (String) -> Unit,
    onPublishClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDraftsClick: () -> Unit,
    onNameChanged: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.profile.name, state.isLoaded) {
        if (state.isLoaded) {
            onNameChanged(state.profile.name)
        }
    }

    ProfileScreen(
        userName = state.profile.name,
        userUniversity = state.profile.university.ifBlank {
            "Universidad sin indicar"
        },
        userListings = listings,
        onBackClick = onBackClick,
        onListingClick = onListingClick,
        onPublishClick = onPublishClick,
        onLogoutClick = onLogoutClick,
        onDraftsClick = onDraftsClick,
        photoBase64 = state.profile.photoBase64,
        description = state.profile.description,
        onEditClick = viewModel::startEditing,
        canEdit = state.isLoaded && !state.isLoading,
        statusContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Cargando perfil…")
                }

                if (state.editedProfile == null) {
                    state.errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error
                        )

                        TextButton(
                            onClick = viewModel::loadProfile,
                            enabled = !state.isLoading
                        ) {
                            Text("Reintentar")
                        }
                    }

                    state.statusMessage?.let { message ->
                        Text(message)
                    }
                }
            }
        }
    )

    val editedProfile = state.editedProfile

    if (editedProfile != null) {
        val context = LocalContext.current
        val isBusy = state.isSaving || state.isProcessingPhoto

        val photoPicker = rememberLauncherForActivityResult(
            contract = PickVisualMedia()
        ) { uri ->
            if (uri != null) {
                viewModel.selectPhoto(
                    contentResolver = context.contentResolver,
                    uri = uri
                )
            }
        }

        AlertDialog(
            onDismissRequest = viewModel::cancelEditing,
            properties = DialogProperties(
                dismissOnBackPress = !isBusy,
                dismissOnClickOutside = !isBusy
            ),
            title = {
                Text("Editar perfil")
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileAvatar(
                        photoBase64 = editedProfile.photoBase64,
                        name = editedProfile.name,
                        modifier = Modifier.size(80.dp)
                    )

                    TextButton(
                        onClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(
                                    PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        enabled = !isBusy
                    ) {
                        Text("Seleccionar foto")
                    }

                    if (editedProfile.photoBase64.isNotBlank()) {
                        TextButton(
                            onClick = viewModel::removePhoto,
                            enabled = !isBusy
                        ) {
                            Text("Quitar foto")
                        }
                    }

                    OutlinedTextField(
                        value = editedProfile.name,
                        onValueChange = { name ->
                            viewModel.updateProfileFields(
                                name = name.take(80),
                                university = editedProfile.university,
                                description = editedProfile.description
                            )
                        },
                        label = {
                            Text("Nombre")
                        },
                        supportingText = {
                            Text("Entre 3 y 80 caracteres")
                        },
                        singleLine = true,
                        enabled = !isBusy,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editedProfile.university,
                        onValueChange = { university ->
                            viewModel.updateProfileFields(
                                name = editedProfile.name,
                                university = university.take(120),
                                description = editedProfile.description
                            )
                        },
                        label = {
                            Text("Universidad (opcional)")
                        },
                        singleLine = true,
                        enabled = !isBusy,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editedProfile.description,
                        onValueChange = { description ->
                            viewModel.updateProfileFields(
                                name = editedProfile.name,
                                university = editedProfile.university,
                                description = description.take(300)
                            )
                        },
                        label = {
                            Text("Sobre mí (opcional)")
                        },
                        supportingText = {
                            Text("${editedProfile.description.length}/300")
                        },
                        minLines = 2,
                        maxLines = 4,
                        enabled = !isBusy,
                        modifier = Modifier.fillMaxWidth()
                    )

                    state.errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    if (isBusy) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = if (state.isProcessingPhoto) {
                                "Preparando foto…"
                            } else {
                                "Guardando perfil…"
                            }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::saveProfile,
                    enabled = !isBusy &&
                            editedProfile.validationError() == null
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = viewModel::cancelEditing,
                    enabled = !isBusy
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ProfileAvatar(
    photoBase64: String,
    name: String,
    modifier: Modifier = Modifier
) {
    val image = remember(photoBase64) {
        if (photoBase64.isBlank() || photoBase64.length > 32768) {
            null
        } else {
            try {
                val imageBytes = Base64.decode(
                    photoBase64,
                    Base64.NO_WRAP
                )

                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }

                BitmapFactory.decodeByteArray(
                    imageBytes,
                    0,
                    imageBytes.size,
                    options
                )

                if (
                    options.outWidth <= 0 ||
                    options.outHeight <= 0
                ) {
                    null
                } else {
                    options.inJustDecodeBounds = false

                    while (
                        maxOf(options.outWidth, options.outHeight) /
                        options.inSampleSize > 256
                    ) {
                        options.inSampleSize *= 2
                    }

                    BitmapFactory.decodeByteArray(
                        imageBytes,
                        0,
                        imageBytes.size,
                        options
                    )?.asImageBitmap()
                }
            } catch (_: IllegalArgumentException) {
                null
            }
        }
    }

    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = "Foto de perfil",
            modifier = modifier.clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "U",
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}