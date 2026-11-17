package com.example.nexora.domain.model

class AuthenticationException(
    val errorCode: String,
    val detail: String? = null,
    cause: Throwable? = null
) : Exception("Authentication failed: $errorCode", cause)