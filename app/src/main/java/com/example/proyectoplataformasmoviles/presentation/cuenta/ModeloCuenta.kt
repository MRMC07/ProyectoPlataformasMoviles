package com.example.proyectoplataformasmoviles.presentation.cuenta

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class ModeloCuenta : ViewModel() {
    private var vigente = true
    private val autenticacion = FirebaseAuth.getInstance()
    var ocupado by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    fun acceder(correo: String, contrasena: String, nombre: String, crear: Boolean, alAcceder: (String) -> Unit) {
        if (ocupado) return
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches() || contrasena.isBlank()) {
            error = "Ingresá tu correo electrónico y contraseña."
            return
        }
        ocupado = true
        error = null
        val tarea = if (crear) autenticacion.createUserWithEmailAndPassword(correo.trim(), contrasena)
            else autenticacion.signInWithEmailAndPassword(correo.trim(), contrasena)
        tarea.addOnCompleteListener { resultado ->
            if (!vigente) return@addOnCompleteListener
            val usuario = if (resultado.isSuccessful) resultado.result?.user else null
            if (!resultado.isSuccessful || usuario == null) {
                ocupado = false
                val causa = resultado.exception
                val codigo = when (causa) {
                    is FirebaseNetworkException -> "ERROR_NETWORK_REQUEST_FAILED"
                    is FirebaseTooManyRequestsException -> "ERROR_TOO_MANY_REQUESTS"
                    else -> codigoErrorCuenta((causa as? FirebaseAuthException)?.errorCode, causa?.message)
                }
                // Se registra solo el código, sin correo, contraseña ni tokens.
                Log.w("NexoraCuenta", "Acceso rechazado: $codigo")
                error = mensajeErrorCuenta(codigo)
            } else if (crear) {
                usuario.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(nombre.trim()).build())
                    .addOnCompleteListener { perfil ->
                        if (!vigente) return@addOnCompleteListener
                        ocupado = false
                        if (perfil.isSuccessful) alAcceder(usuario.displayName ?: nombre.trim())
                        else error = "La cuenta fue creada, pero no se pudo guardar el nombre. Podés iniciar sesión con tu correo."
                    }
            } else {
                ocupado = false
                alAcceder(usuario.displayName ?: usuario.email.orEmpty())
            }
        }
    }
    override fun onCleared() { vigente = false }
}
