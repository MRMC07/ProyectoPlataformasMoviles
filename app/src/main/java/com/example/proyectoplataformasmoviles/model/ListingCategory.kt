package com.example.proyectoplataformasmoviles.model

enum class ListingCategory(
    val id: String,
    val label: String
) {
    TUTORING("tutoring", "Tutorías"),
    FOOD("food", "Comida"),
    TECHNOLOGY("technology", "Tecnología"),
    DESIGN("design", "Diseño");

    companion object {
        fun fromId(id: String): ListingCategory? {
            return entries.firstOrNull { category ->
                category.id == id
            }
        }
    }
}