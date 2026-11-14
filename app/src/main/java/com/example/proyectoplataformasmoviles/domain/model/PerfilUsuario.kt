package com.example.proyectoplataformasmoviles.domain.model

data class UserProfile(
    val name: String = "",
    val university: String = "",
    val description: String = "",
    val photoBase64: String = ""
) {
    fun normalized(): UserProfile = copy(
        name = name.trim(),
        university = university.trim(),
        description = description.trim()
    )

    fun validationError(): String? = when {
        name.trim().length !in 3..80 ->
            "El nombre debe tener entre 3 y 80 caracteres."

        university.trim().length > 120 ->
            "La universidad admite hasta 120 caracteres."

        description.trim().length > 300 ->
            "La descripción admite hasta 300 caracteres."

        photoBase64.length > 32768 ->
            "La foto supera el tamaño permitido. Seleccioná otra imagen."

        else -> null
    }
}