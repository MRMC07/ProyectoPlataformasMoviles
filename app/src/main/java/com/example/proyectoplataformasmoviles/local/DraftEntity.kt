package com.example.proyectoplataformasmoviles.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "publication_drafts")
data class DraftEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val price: String,
    val category: String,
    val updatedAt: Long
)
