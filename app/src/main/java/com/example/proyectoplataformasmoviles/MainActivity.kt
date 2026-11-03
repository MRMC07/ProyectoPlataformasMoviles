package com.example.proyectoplataformasmoviles

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.room.Room
import com.example.proyectoplataformasmoviles.local.AppDatabase
import com.example.proyectoplataformasmoviles.local.DraftRepository
import com.example.proyectoplataformasmoviles.navigation.AppNavigation
import com.example.proyectoplataformasmoviles.remote.ListingRepository
import com.example.proyectoplataformasmoviles.ui.theme.ProyectoPlataformasMovilesTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "project_local_database"
        ).build()
    }

    private val draftRepository by lazy {
        DraftRepository(
            draftDao = database.draftDao()
        )
    }

    private val listingRepository by lazy {
        ListingRepository()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userRef = db.collection("users").document(userId)

                    userRef.get().addOnSuccessListener { document ->
                        if (!document.exists()) {
                            val userData = hashMapOf(
                                "name" to "Usuario ${userId.take(5)}",
                                "descripcion" to "Nuevo usuario en la app",
                                "imageurl" to ""
                            )
                            userRef.set(userData)
                        }
                    }
                }
            }
        }

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            ProyectoPlataformasMovilesTheme {
                AppNavigation(
                    draftRepository = draftRepository,
                    listingRepository = listingRepository
                )
            }
        }
    }
}