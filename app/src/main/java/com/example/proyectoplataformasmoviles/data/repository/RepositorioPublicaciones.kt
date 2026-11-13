package com.example.proyectoplataformasmoviles.data.repository

import com.example.proyectoplataformasmoviles.domain.model.Listing
import com.example.proyectoplataformasmoviles.domain.repository.ListingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.math.RoundingMode

class RepositorioPublicaciones(
    private val base: FirebaseDatabase,
    private val autenticacion: FirebaseAuth
) : ListingRepository {
    private val publicaciones = base.getReference("products")

    override fun observeListings(): Flow<List<Listing>> = callbackFlow {
        val escucha = object : ValueEventListener {
            override fun onDataChange(resultado: DataSnapshot) {
                val ofertas = resultado.children.mapNotNull { nodo ->
                    val campos = nodo.children.mapNotNull { campo ->
                        val clave = campo.key
                        val valor = campo.value
                        if (clave != null && valor != null) clave to valor else null
                    }.toMap()
                    leerPublicacion(nodo.key.orEmpty(), campos)
                }
                trySend(ofertas)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        publicaciones.addValueEventListener(escucha)
        awaitClose { publicaciones.removeEventListener(escucha) }
    }

    override suspend fun saveListing(listing: Listing) {
        val usuario = autenticacion.currentUser?.takeUnless { it.isAnonymous }
            ?: error("Iniciá sesión con tu cuenta para publicar.")
        require(listing.id.isNotBlank() && '/' !in listing.id)
        require(listing.title.trim().length in 3..80 && listing.description.trim().length in 10..1500)
        require(listing.price.isFinite() && listing.price > 0 && listing.price <= 999999.99)
        val centavos = listing.price.toBigDecimal().setScale(2, RoundingMode.UNNECESSARY)
            .movePointRight(2).longValueExact()
        val tarea = publicaciones.child(listing.id).setValue(mapOf(
            "id" to listing.id, "nombre" to listing.title.trim(),
            "descripcion" to listing.description.trim(), "precio" to listing.price,
            "precioCentavos" to centavos, "categoria" to listing.category,
            "nombreVendedor" to (usuario.displayName ?: usuario.email.orEmpty()),
            "correoVendedor" to usuario.email.orEmpty(), "propietario" to usuario.uid,
            "imageUrl" to listing.imageUrl, "creadaEn" to ServerValue.TIMESTAMP
        ))
        val confirmado = withTimeoutOrNull(10_000) { tarea.await(); true } ?: false
        check(confirmado) { "El envío sigue sin confirmación. Puede completarse al volver la conexión; reintentá desde este formulario para conservar el mismo identificador." }
    }
}
