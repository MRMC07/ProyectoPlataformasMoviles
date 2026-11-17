package com.example.nexora.presentation.account

import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest

class AccountViewModel : ViewModel() {

    private var isActive = true
    private val authentication = FirebaseAuth.getInstance()

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun authenticate(
        email: String,
        password: String,
        name: String,
        createAccount: Boolean,
        onAuthenticated: (String) -> Unit
    ) {
        if (isLoading) return

        val trimmedEmail = email.trim()

        if (
            !Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches() ||
            password.isBlank()
        ) {
            errorMessage = "Ingresá tu correo electrónico y contraseña."
            return
        }

        if (createAccount) {
            val institutionalDomains = setOf(
                "uvg.edu.gt",
                "correoe.usac.edu.gt",
                "mail.usac.edu.gt",
                "miumg.edu.gt",
                "galileo.edu",
                "estudiante.udv.edu.gt",
                "upana.edu.gt",
                "unis.edu.gt",
                "ufm.edu"
            )

            val emailDomain = trimmedEmail
                .substringAfterLast("@", "")
                .lowercase()

            if (emailDomain !in institutionalDomains) {
                errorMessage = "Usá tu correo institucional de estudiante."
                return
            }
        }

        isLoading = true
        errorMessage = null

        val authenticationTask = if (createAccount) {
            authentication.createUserWithEmailAndPassword(
                trimmedEmail,
                password
            )
        } else {
            authentication.signInWithEmailAndPassword(
                trimmedEmail,
                password
            )
        }

        authenticationTask.addOnCompleteListener { authenticationResult ->
            if (!isActive) return@addOnCompleteListener

            val user = if (authenticationResult.isSuccessful) {
                authenticationResult.result?.user
            } else {
                null
            }

            if (!authenticationResult.isSuccessful || user == null) {
                isLoading = false

                val cause = authenticationResult.exception
                val errorCode = when (cause) {
                    is FirebaseNetworkException ->
                        "ERROR_NETWORK_REQUEST_FAILED"

                    is FirebaseTooManyRequestsException ->
                        "ERROR_TOO_MANY_REQUESTS"

                    else -> accountErrorCode(
                        (cause as? FirebaseAuthException)?.errorCode,
                        cause?.message
                    )
                }

                errorMessage = accountErrorMessage(errorCode)
            } else if (createAccount) {
                val trimmedName = name.trim()
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(trimmedName)
                    .build()

                user.updateProfile(profileUpdates)
                    .addOnCompleteListener onProfileUpdated@{ profileResult ->
                        if (!isActive) return@onProfileUpdated

                        isLoading = false

                        if (profileResult.isSuccessful) {
                            onAuthenticated(user.displayName ?: trimmedName)
                        } else {
                            errorMessage =
                                "La cuenta fue creada, pero no se pudo guardar el nombre. Podés iniciar sesión con tu correo."
                        }
                    }
            } else {
                isLoading = false
                onAuthenticated(user.displayName ?: user.email.orEmpty())
            }
        }
    }

    override fun onCleared() {
        isActive = false
        super.onCleared()
    }
}