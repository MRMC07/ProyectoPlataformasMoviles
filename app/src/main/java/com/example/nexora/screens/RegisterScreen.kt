package com.example.nexora.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.nexora.R
import com.example.nexora.presentation.account.AccountUiState
import com.example.nexora.ui.theme.Spacing

@Composable
fun RegisterScreen(
    state: AccountUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onCreatingAccountChange: (Boolean) -> Unit,
    onSubmitClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true
) {
    var isPasswordVisible by rememberSaveable {
        mutableStateOf(false)
    }

    val nameError = state.nameError.takeIf {
        state.name.isNotEmpty()
    }

    val emailError = state.emailError.takeIf {
        state.email.isNotEmpty()
    }

    val passwordError = state.passwordError.takeIf {
        state.password.isNotEmpty()
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(Spacing.ExtraLarge),
            verticalArrangement = Arrangement.spacedBy(
                Spacing.ExtraLarge
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    Spacing.Small
                )
            ) {
                if (showBackButton) {
                    IconButton(
                        onClick = onBackClick,
                        enabled = !state.isLoading
                    ) {
                        Icon(
                            painter = painterResource(
                                R.drawable.ic_back
                            ),
                            contentDescription = "Regresar"
                        )
                    }
                }

                NexoraBrand()
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(
                    Spacing.Small
                )
            ) {
                Text(
                    text = if (state.isCreatingAccount) {
                        "Tu talento merece\nsu propio espacio."
                    } else {
                        "Qué bueno\nverte de nuevo."
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Una comunidad para descubrir, publicar y conectar.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TabRow(
                selectedTabIndex = if (state.isCreatingAccount) 0 else 1,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                Tab(
                    selected = state.isCreatingAccount,
                    onClick = {
                        onCreatingAccountChange(true)
                    },
                    enabled = !state.isLoading,
                    text = {
                        Text("Crear cuenta")
                    }
                )

                Tab(
                    selected = !state.isCreatingAccount,
                    onClick = {
                        onCreatingAccountChange(false)
                    },
                    enabled = !state.isLoading,
                    text = {
                        Text("Ingresar")
                    }
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(
                    Spacing.Medium
                )
            ) {
                if (state.isCreatingAccount) {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = onNameChange,
                        label = {
                            Text("Tu nombre")
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isLoading,
                        isError = nameError != null,
                        supportingText = {
                            nameError?.let {
                                Text(it)
                            }
                        },
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    label = {
                        Text("Correo electrónico")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                    isError = emailError != null,
                    supportingText = {
                        emailError?.let {
                            Text(it)
                        }
                    },
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    label = {
                        Text("Contraseña")
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                    isError = passwordError != null,
                    supportingText = {
                        if (passwordError != null) {
                            Text(passwordError)
                        } else if (state.isCreatingAccount) {
                            Text("Usá al menos 6 caracteres.")
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation = if (isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                isPasswordVisible = !isPasswordVisible
                            },
                            enabled = !state.isLoading
                        ) {
                            Text(
                                if (isPasswordVisible) {
                                    "Ocultar"
                                } else {
                                    "Ver"
                                }
                            )
                        }
                    }
                )

                state.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (state.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = onSubmitClick,
                    enabled = state.isFormValid && !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        if (state.isCreatingAccount) {
                            "Crear mi cuenta"
                        } else {
                            "Ingresar a Nexora"
                        }
                    )
                }
            }

            Text(
                text = "Para estudiantes de todas las universidades de Guatemala.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}