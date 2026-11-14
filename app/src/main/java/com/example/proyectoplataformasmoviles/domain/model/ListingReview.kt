package com.example.proyectoplataformasmoviles.domain.model

data class ListingReview(
    val userId: String,
    val authorName: String,
    val rating: Int,
    val comment: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class ReviewSummary(
    val rating: Double,
    val reviewCount: Int
)

fun summarizeReviews(reviews: List<ListingReview>): ReviewSummary {
    return ReviewSummary(
        rating = if (reviews.isEmpty()) {
            0.0
        } else {
            reviews.sumOf { it.rating }.toDouble() / reviews.size
        },
        reviewCount = reviews.size
    )
}