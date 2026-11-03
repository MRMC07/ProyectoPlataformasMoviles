package com.example.proyectoplataformasmoviles.model

data class ChatMessage(
    val id: Int,
    val text: String,
    val time: String,
    val isMine: Boolean
)