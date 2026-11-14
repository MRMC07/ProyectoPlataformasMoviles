package com.example.proyectoplataformasmoviles.data.repository

import com.example.proyectoplataformasmoviles.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import kotlinx.coroutines.tasks.await

class ProfileRepository(
    database: FirebaseDatabase,
    private val authentication: FirebaseAuth
) {
    private val profiles = database.getReference("perfiles")

    private fun requireCurrentUser(userId: String) =
        requireNotNull(authentication.currentUser) {
            "Se necesita una cuenta para consultar el perfil."
        }.also { user ->
            require(!user.isAnonymous && user.uid == userId) {
                "La sesión cambió. Volvé a ingresar."
            }
        }

    suspend fun load(userId: String): UserProfile {
        val user = requireCurrentUser(userId)
        val snapshot = profiles.child(userId).get().await()

        requireCurrentUser(userId)

        return UserProfile(
            name = snapshot.child("nombre")
                .getValue(String::class.java)
                ?: user.displayName.orEmpty(),
            university = snapshot.child("universidad")
                .getValue(String::class.java).orEmpty(),
            description = snapshot.child("descripcion")
                .getValue(String::class.java).orEmpty(),
            photoBase64 = snapshot.child("fotoBase64")
                .getValue(String::class.java).orEmpty()
        )
    }

    suspend fun save(userId: String, profile: UserProfile) {
        requireCurrentUser(userId)

        val normalizedProfile = profile.normalized()
        val validationError = normalizedProfile.validationError()

        require(validationError == null) {
            validationError.orEmpty()
        }

        profiles.child(userId).updateChildren(
            mapOf(
                "nombre" to normalizedProfile.name,
                "universidad" to normalizedProfile.university,
                "descripcion" to normalizedProfile.description,
                "fotoBase64" to normalizedProfile.photoBase64,
                "actualizadoEn" to ServerValue.TIMESTAMP
            )
        ).await()

        requireCurrentUser(userId)
    }

    suspend fun updateAccountName(userId: String, name: String) {
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        requireCurrentUser(userId)
            .updateProfile(profileUpdates)
            .await()
    }
}