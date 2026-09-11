package com.ecommerce.review.dto

import com.ecommerce.review.entity.Review
import com.ecommerce.review.entity.ReviewStatus
import java.time.OffsetDateTime

data class ReviewResponse(
    val id: Long,
    val productId: Long,
    val userId: Long,
    val rating: Int,
    val title: String?,
    val body: String?,
    val verifiedPurchase: Boolean,
    val status: ReviewStatus,
    val createdAt: OffsetDateTime,
    val updatedAt: OffsetDateTime,
) {
    companion object {
        fun fromEntity(entity: Review): ReviewResponse =
            ReviewResponse(
                id = entity.id!!,
                productId = entity.productId,
                userId = entity.userId,
                rating = entity.rating,
                title = entity.title,
                body = entity.body,
                verifiedPurchase = entity.verifiedPurchase,
                status = entity.status,
                createdAt = entity.createdAt,
                updatedAt = entity.updatedAt,
            )
    }
}
