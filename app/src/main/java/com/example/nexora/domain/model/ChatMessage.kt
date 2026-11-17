package com.example.nexora.domain.model

data class ChatMessage(
    val id: String = "",
    val authorId: String = "",
    val text: String = "",
    val createdAt: Long = 0L
)