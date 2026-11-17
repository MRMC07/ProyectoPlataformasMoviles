package com.example.nexora.domain.model

import java.util.Locale

fun formatPrice(price: Double): String =
    String.format(Locale.forLanguageTag("es-GT"), "%.2f", price)

fun findListing(listings: List<Listing>, listingId: String): Listing? =
    listings.firstOrNull { it.id == listingId }

fun amountInCents(text: String): Long? {
    val normalizedAmount = text.trim().replace(',', '.')

    if (!normalizedAmount.matches(Regex("""\d{1,6}(\.\d{1,2})?"""))) {
        return null
    }

    return normalizedAmount
        .toBigDecimalOrNull()
        ?.movePointRight(2)
        ?.longValueExact()
        ?.takeIf { it > 0 }
}