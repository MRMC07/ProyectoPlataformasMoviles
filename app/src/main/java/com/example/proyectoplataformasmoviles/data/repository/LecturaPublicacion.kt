package com.example.proyectoplataformasmoviles.data.repository

import com.example.proyectoplataformasmoviles.domain.model.Listing

fun leerPublicacion(identificador: String, campos: Map<String, Any>): Listing? {
    val nombre = (campos["nombre"] ?: campos["title"]) as? String ?: return null
    val precio = (campos["precio"] ?: campos["price"]) as? Number ?: return null
    if (nombre.isBlank() || !precio.toDouble().isFinite() || precio.toDouble() < 0) return null
    return Listing(
        id = identificador, title = nombre,
        description = (campos["descripcion"] ?: campos["description"]) as? String ?: "Sin descripción",
        price = precio.toDouble(), category = (campos["categoria"] ?: campos["category"]) as? String ?: "Otros",
        sellerName = (campos["nombreVendedor"] ?: campos["sellerName"]) as? String ?: "Sin especificar",
        imageUrl = campos["imageUrl"] as? String ?: "",
        rating = 0.0, reviewCount = 0, faculty = "Sin especificar",
        deliveryMode = "A convenir", turnaroundTime = "A convenir", includes = emptyList(),
        correoVendedor = campos["correoVendedor"] as? String ?: "",
        propietario = (campos["propietario"] ?: campos["ownerId"]) as? String ?: "",
        universidad = campos["universidad"] as? String ?: "Sin especificar",
        isActive = campos["isActive"] as? Boolean ?: true
    )
}

