package com.example.nexora.data.repository

import com.example.nexora.domain.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class ProfileRepository(
    firestore: FirebaseFirestore,
    private val authentication: FirebaseAuth
) {
    private val profiles = firestore.collection("users")

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
        val snapshot = profiles.document(userId).get().await()

        requireCurrentUser(userId)

        return UserProfile(
            name = (snapshot.get("name") as? String)
                ?: user.displayName.orEmpty(),
            university = (snapshot.get("university") as? String).orEmpty(),
            description = (snapshot.get("description") as? String).orEmpty(),
            photoBase64 = (snapshot.get("photoBase64") as? String).orEmpty()
        )
    }

    suspend fun save(userId: String, profile: UserProfile) {
        requireCurrentUser(userId)

        val normalizedProfile = profile.normalized()
        val validationError = normalizedProfile.validationError()

        require(validationError == null) {
            validationError.orEmpty()
        }

        profiles.document(userId).set(
            mapOf(
                "name" to normalizedProfile.name,
                "university" to normalizedProfile.university,
                "description" to normalizedProfile.description,
                "photoBase64" to normalizedProfile.photoBase64,
                "updatedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
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