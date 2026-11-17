package com.example.nexora.data.repository

import com.example.nexora.domain.model.Listing

fun parseListing(
    listingId: String,
    fields: Map<String, Any>
): Listing? {
    val title = fields["title"] as? String ?: return null
    val price = (fields["price"] as? Number)?.toDouble() ?: return null

    if (title.isBlank() || !price.isFinite() || price < 0) {
        return null
    }

    return Listing(
        id = listingId,
        title = title,
        description = fields["description"] as? String ?: "Sin descripción",
        price = price,
        category = fields["category"] as? String ?: "Otros",
        sellerName = fields["sellerName"] as? String ?: "Sin especificar",
        imageUrl = fields["imageUrl"] as? String ?: "",
        rating = 0.0,
        reviewCount = 0,
        faculty = "Sin especificar",
        deliveryMode = "A convenir",
        turnaroundTime = "A convenir",
        includes = emptyList(),
        sellerEmail = fields["sellerEmail"] as? String ?: "",
        ownerId = fields["ownerId"] as? String ?: "",
        university = fields["university"] as? String ?: "Sin especificar",
        isActive = fields["isActive"] as? Boolean ?: true,
        hasImage = fields["hasImage"] as? Boolean ?: false
    )
}