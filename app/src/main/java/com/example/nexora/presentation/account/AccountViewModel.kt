package com.example.nexora.presentation.account

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexora.data.repository.AuthRepository
import com.example.nexora.domain.model.AccountUser
import com.example.nexora.domain.model.AuthenticationException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val institutionalDomains = setOf(
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

data class AccountUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val isCreatingAccount: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val authenticatedUser: AccountUser? = null
) {
    val nameError: String?
        get() = when {
            !isCreatingAccount -> null
            name.trim().length < 3 ->
                "Ingresá un nombre de al menos 3 caracteres."
            name.trim().length > 80 ->
                "El nombre debe tener como máximo 80 caracteres."
            else -> null
        }

    val emailError: String?
        get() {
            val trimmedEmail = email.trim()

            if (!Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
                return "Ingresá un correo electrónico válido."
            }

            val domain = trimmedEmail
                .substringAfterLast("@", "")
                .lowercase()

            return if (isCreatingAccount && domain !in institutionalDomains) {
                "Usá tu correo institucional de estudiante."
            } else {
                null
            }
        }

    val passwordError: String?
        get() = when {
            password.isBlank() ->
                "Ingresá tu contraseña."
            isCreatingAccount && password.length < 6 ->
                "Usá al menos 6 caracteres."
            else -> null
        }

    val isFormValid: Boolean
        get() = nameError == null &&
                emailError == null &&
                passwordError == null
}

class AccountViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState = _uiState.asStateFlow()

    fun updateName(name: String) {
        if (_uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                name = name.take(80),
                errorMessage = null
            )
        }
    }

    fun updateEmail(email: String) {
        if (_uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                email = email,
                errorMessage = null
            )
        }
    }

    fun updatePassword(password: String) {
        if (_uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                password = password,
                errorMessage = null
            )
        }
    }

    fun setCreatingAccount(isCreatingAccount: Boolean) {
        if (_uiState.value.isLoading) return

        _uiState.update {
            it.copy(
                isCreatingAccount = isCreatingAccount,
                errorMessage = null
            )
        }
    }

    fun authenticate() {
        val form = _uiState.value

        if (form.isLoading || form.authenticatedUser != null) return

        if (!form.isFormValid) {
            _uiState.update {
                it.copy(
                    errorMessage = form.nameError
                        ?: form.emailError
                        ?: form.passwordError
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            var accountCreated = false

            try {
                val user = if (form.isCreatingAccount) {
                    val createdUser = authRepository.createAccount(
                        email = form.email.trim(),
                        password = form.password
                    )

                    accountCreated = true

                    authRepository.updateDisplayName(
                        userId = createdUser.id,
                        name = form.name.trim()
                    )
                } else {
                    authRepository.signIn(
                        email = form.email.trim(),
                        password = form.password
                    )
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        password = "",
                        authenticatedUser = user
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: AuthenticationException) {
                val message = when {
                    accountCreated ->
                        "La cuenta fue creada, pero no pudimos guardar el nombre. Ingresá con tu correo y contraseña."

                    error.errorCode == "ERROR_AUTH_TIMEOUT" ->
                        "La operación tardó demasiado. Revisá tu conexión y volvé a intentar."

                    error.errorCode == "ERROR_SESSION_CHANGED" ->
                        "La sesión cambió. Volvé a ingresar."

                    else ->
                        accountErrorMessage(
                            accountErrorCode(
                                firebaseCode = error.errorCode,
                                detail = error.detail
                            )
                        )
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isCreatingAccount = if (accountCreated) {
                            false
                        } else {
                            it.isCreatingAccount
                        },
                        errorMessage = message
                    )
                }
            }
        }
    }

    fun clearAuthenticatedUser() {
        _uiState.update {
            it.copy(authenticatedUser = null)
        }
    }
}