package com.example.nexora

import com.example.nexora.presentation.account.accountErrorCode
import com.example.nexora.presentation.account.accountErrorMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountErrorsTest {

    @Test
    fun missingConfigurationDoesNotReportInvalidCredentials() {
        val errorCode = accountErrorCode(
            firebaseCode = "ERROR_INTERNAL_ERROR",
            detail = "An internal error has occurred. [ CONFIGURATION_NOT_FOUND ]"
        )

        assertEquals("CONFIGURATION_NOT_FOUND", errorCode)
        assertTrue(
            accountErrorMessage(errorCode)
                .contains("configuración de Authentication")
        )
    }

    @Test
    fun existingAccountMessageSuggestsSigningIn() {
        val message = accountErrorMessage("ERROR_EMAIL_ALREADY_IN_USE")

        assertTrue(message.contains("pestaña Ingresar"))
    }

    @Test
    fun networkErrorDoesNotReportAnIncorrectPassword() {
        val message = accountErrorMessage("ERROR_NETWORK_REQUEST_FAILED")

        assertTrue(message.contains("conexión a internet"))
        assertFalse(message.contains("contraseña"))
    }

    @Test
    fun unknownErrorDoesNotExposeInternalDetails() {
        val internalDetail = "Internal account information"

        val errorCode = accountErrorCode(
            firebaseCode = null,
            detail = internalDetail
        )

        val message = accountErrorMessage(errorCode)

        assertEquals("ERROR_UNKNOWN", errorCode)
        assertFalse(message.contains(internalDetail))
    }
}