package com.example.nexora

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.nexora.presentation.account.AccountUiState
import com.example.nexora.screens.GuestProfileScreen
import com.example.nexora.screens.RegisterScreen
import com.example.nexora.ui.theme.NexoraTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AppScreensTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun registrationRequiresCredentialsBeforeSubmission() {
        composeRule.setContent {
            NexoraTheme {
                RegisterScreen(
                    state = AccountUiState(),
                    onNameChange = {},
                    onEmailChange = {},
                    onPasswordChange = {},
                    onCreatingAccountChange = {},
                    onSubmitClick = {},
                    onBackClick = {},
                    showBackButton = false
                )
            }
        }

        composeRule
            .onNodeWithText("Tu nombre")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("Correo electrónico")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("Contraseña")
            .assertIsDisplayed()

        composeRule
            .onNodeWithText("Crear mi cuenta")
            .assertIsNotEnabled()
    }

    @Test
    fun guestProfileProvidesLocalDraftsAccess() {
        var wasDraftsRequested = false

        composeRule.setContent {
            NexoraTheme {
                GuestProfileScreen(
                    onSignInClick = {},
                    onDraftsClick = {
                        wasDraftsRequested = true
                    },
                    onBackClick = {}
                )
            }
        }

        composeRule
            .onNodeWithText("Borradores de este dispositivo")
            .assertIsDisplayed()
            .performClick()

        composeRule.waitForIdle()

        assertTrue(wasDraftsRequested)
    }
}