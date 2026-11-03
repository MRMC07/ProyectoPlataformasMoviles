package com.example.proyectoplataformasmoviles.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.proyectoplataformasmoviles.model.ListingCategory

fun categoryIcon(category: ListingCategory): ImageVector {
    return when (category) {
        ListingCategory.TUTORING -> Icons.Filled.School
        ListingCategory.FOOD -> Icons.Filled.Restaurant
        ListingCategory.TECHNOLOGY -> Icons.Filled.Computer
        ListingCategory.DESIGN -> Icons.Filled.Palette
    }
}