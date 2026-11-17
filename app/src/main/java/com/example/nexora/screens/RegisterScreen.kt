package com.example.nexora.screens

import android.util.Patterns
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nexora.R
import com.example.nexora.presentation.account.AccountViewModel
import com.example.nexora.ui.theme.Spacing

@Composable
fun RegisterScreen(
    onRegisterSuccess: (userName: String, role: String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    accountViewModel: AccountViewModel = viewModel(),
    showBackButton: Boolean = true
) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    var userName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val isCreatingAccount = selectedTabIndex == 0
    val isFormValid =
        Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() &&
                password.length >= 6 &&
                (!isCreatingAccount || userName.trim().length >= 3)

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
            verticalArrangement = Arrangement.spacedBy(Spacing.ExtraLarge)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                if (showBackButton) {
                    IconButton(
                        onClick = onBackClick,
                        enabled = !accountViewModel.isLoading
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Regresar"
                        )
                    }
                }

                NexoraBrand()
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                Text(
                    text = if (isCreatingAccount) {
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
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    enabled = !accountViewModel.isLoading,
                    text = { Text("Crear cuenta") }
                )

                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    enabled = !accountViewModel.isLoading,
                    text = { Text("Ingresar") }
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                if (isCreatingAccount) {
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it.take(80) },
                        label = { Text("Tu nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !accountViewModel.isLoading,
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo electrónico") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !accountViewModel.isLoading,
                    shape = RoundedCornerShape(14.dp)
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !accountViewModel.isLoading,
                    shape = RoundedCornerShape(14.dp),
                    supportingText = {
                        if (isCreatingAccount) {
                            Text("Usá al menos 6 caracteres.")
                        }
                    },
                    visualTransformation = if (isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        TextButton(
                            onClick = {
                                isPasswordVisible = !isPasswordVisible
                            }
                        ) {
                            Text(
                                if (isPasswordVisible) "Ocultar" else "Ver"
                            )
                        }
                    }
                )

                accountViewModel.errorMessage?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (accountViewModel.isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = {
                        accountViewModel.authenticate(
                            email = email,
                            password = password,
                            name = userName,
                            createAccount = isCreatingAccount,
                            onAuthenticated = { authenticatedUserName ->
                                onRegisterSuccess(
                                    authenticatedUserName,
                                    "Estudiante"
                                )
                            }
                        )
                    },
                    enabled = isFormValid && !accountViewModel.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        if (isCreatingAccount) {
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