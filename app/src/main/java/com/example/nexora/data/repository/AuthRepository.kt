package com.example.nexora.data.repository

import com.example.nexora.data.monitoring.CrashlyticsErrorReporter
import com.example.nexora.domain.model.AccountUser
import com.example.nexora.domain.model.AuthenticationException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

private const val AUTH_TIMEOUT_MILLIS = 20_000L

class AuthRepository(
    private val authentication: FirebaseAuth,
    private val errorReporter: CrashlyticsErrorReporter
) {
    val currentUser: AccountUser?
        get() = authentication.currentUser?.toAccountUser()

    fun observeUser(): Flow<AccountUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAccountUser())
        }

        authentication.addAuthStateListener(listener)

        awaitClose {
            authentication.removeAuthStateListener(listener)
        }
    }.distinctUntilChanged()

    suspend fun signIn(
        email: String,
        password: String
    ): AccountUser = runAuthOperation("auth.signIn") {
        val result = authentication.signInWithEmailAndPassword(
            email.trim(),
            password
        ).await()

        requireNotNull(result.user?.toAccountUser()) {
            "No se pudo recuperar la cuenta."
        }
    }

    suspend fun createAccount(
        email: String,
        password: String
    ): AccountUser = runAuthOperation("auth.createAccount") {
        val result = authentication.createUserWithEmailAndPassword(
            email.trim(),
            password
        ).await()

        requireNotNull(result.user?.toAccountUser()) {
            "No se pudo recuperar la cuenta creada."
        }
    }

    suspend fun updateDisplayName(
        userId: String,
        name: String
    ): AccountUser = runAuthOperation("auth.updateDisplayName") {
        val user = requireCurrentUser(userId)

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name.trim())
            .build()

        user.updateProfile(profileUpdates).await()

        requireNotNull(
            requireCurrentUser(userId).toAccountUser()
        ) {
            "No se pudo recuperar la cuenta actualizada."
        }
    }

    suspend fun signOut() {
        runAuthOperation("auth.signOut") {
            authentication.signOut()
        }
    }

    private fun requireCurrentUser(userId: String): FirebaseUser {
        return authentication.currentUser?.takeIf {
            it.uid == userId &&
                    !it.isAnonymous &&
                    it.email != null
        } ?: throw AuthenticationException(
            errorCode = "ERROR_SESSION_CHANGED"
        )
    }

    private fun FirebaseUser.toAccountUser(): AccountUser? {
        val accountEmail = email ?: return null

        if (isAnonymous) return null

        return AccountUser(
            id = uid,
            name = displayName.orEmpty(),
            email = accountEmail
        )
    }

    private suspend fun <T> runAuthOperation(
        operationName: String,
        operation: suspend () -> T
    ): T {
        return try {
            withTimeout(AUTH_TIMEOUT_MILLIS) {
                operation()
            }
        } catch (timeout: TimeoutCancellationException) {
            errorReporter.record(
                error = timeout,
                operation = operationName
            )

            throw AuthenticationException(
                errorCode = "ERROR_AUTH_TIMEOUT",
                cause = timeout
            )
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: AuthenticationException) {
            errorReporter.record(
                error = error,
                operation = operationName
            )

            throw error
        } catch (error: Exception) {
            errorReporter.record(
                error = error,
                operation = operationName
            )

            val errorCode = when (error) {
                is FirebaseNetworkException ->
                    "ERROR_NETWORK_REQUEST_FAILED"

                is FirebaseTooManyRequestsException ->
                    "ERROR_TOO_MANY_REQUESTS"

                is FirebaseAuthException ->
                    error.errorCode

                else ->
                    "ERROR_UNKNOWN"
            }

            throw AuthenticationException(
                errorCode = errorCode,
                detail = error.message,
                cause = error
            )
        }
    }
}