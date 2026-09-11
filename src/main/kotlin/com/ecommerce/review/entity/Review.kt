package com.ecommerce.review.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.OffsetDateTime

@Entity
@Table(
    name = "reviews",
    schema = "review",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_reviews_product_user", columnNames = ["product_id", "user_id"]),
    ],
)
data class Review(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "product_id", nullable = false)
    var productId: Long = 0,
    @Column(name = "user_id", nullable = false)
    var userId: Long = 0,
    @Column(nullable = false)
    var rating: Int = 0,
    @Column(length = 160)
    var title: String? = null,
    @Column(columnDefinition = "TEXT")
    var body: String? = null,
    @Column(name = "verified_purchase", nullable = false)
    var verifiedPurchase: Boolean = false,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ReviewStatus = ReviewStatus.PENDING,
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),
    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now(),
) {
    @PreUpdate
    fun preUpdate() {
        updatedAt = OffsetDateTime.now()
    }
}
