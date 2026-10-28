package com.example.proyectoplataformasmoviles.domain.model

data class Listing(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val sellerName: String,
    val imageUrl: String
)