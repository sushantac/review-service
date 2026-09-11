package com.ecommerce.review.event

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.OffsetDateTime

data class ReviewEvent(
    @JsonProperty("eventId") val eventId: String,
    @JsonProperty("reviewId") val reviewId: Long,
    @JsonProperty("productId") val productId: Long,
    @JsonProperty("userId") val userId: Long,
    @JsonProperty("rating") val rating: Int,
    @JsonProperty("title") val title: String?,
    @JsonProperty("body") val body: String?,
    @JsonProperty("verifiedPurchase") val verifiedPurchase: Boolean,
    @JsonProperty("occurredAt") val occurredAt: OffsetDateTime,
) {
    companion object {
        fun fromReview(review: com.ecommerce.review.entity.Review): ReviewEvent =
            ReviewEvent(
                eventId =
                    java.util.UUID
                        .randomUUID()
                        .toString(),
                reviewId = review.id!!,
                productId = review.productId,
                userId = review.userId,
                rating = review.rating,
                title = review.title,
                body = review.body,
                verifiedPurchase = review.verifiedPurchase,
                occurredAt = review.createdAt,
            )
    }
}
