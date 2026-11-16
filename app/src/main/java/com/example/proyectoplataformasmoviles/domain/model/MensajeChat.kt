package com.example.proyectoplataformasmoviles.domain.model

data class MensajeChat(
    val id: String = "",
    val autorId: String = "",
    val texto: String = "",
    val creadaEn: Long = 0L
)