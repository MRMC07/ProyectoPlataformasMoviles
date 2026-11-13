package com.example.proyectoplataformasmoviles.domain.model

import java.util.Locale

fun formatearImporte(precio: Double): String = String.format(Locale.forLanguageTag("es-GT"), "%.2f", precio)

fun buscarPublicacion(publicaciones: List<Listing>, identificador: String): Listing? =
    publicaciones.firstOrNull { it.id == identificador }

fun importeEnCentavos(texto: String): Long? {
    val normalizado = texto.trim().replace(',', '.')
    if (!normalizado.matches(Regex("\\d{1,6}(\\.\\d{1,2})?"))) return null
    return normalizado.toBigDecimalOrNull()?.movePointRight(2)?.longValueExact()?.takeIf { it > 0 }
}
