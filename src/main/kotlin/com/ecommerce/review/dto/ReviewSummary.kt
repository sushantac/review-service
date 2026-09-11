package com.ecommerce.review.dto

data class ReviewSummary(
    val productId: Long,
    val averageRating: Double,
    val totalReviews: Long,
    val ratingDistribution: Map<Int, Long>,
)
