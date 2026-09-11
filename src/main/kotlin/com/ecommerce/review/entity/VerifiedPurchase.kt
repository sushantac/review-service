package com.ecommerce.review.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.OffsetDateTime

@Entity
@Table(
    name = "verified_purchases",
    schema = "review",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_verified_purchases_event_product", columnNames = ["event_id", "product_id"]),
    ],
)
data class VerifiedPurchase(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "event_id", nullable = false)
    var eventId: String = "",
    @Column(name = "user_id", nullable = false)
    var userId: Long = 0,
    @Column(name = "product_id", nullable = false)
    var productId: Long = 0,
    @Column(name = "order_id", nullable = false)
    var orderId: Long = 0,
    @Column(name = "order_number", nullable = false)
    var orderNumber: String = "",
    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),
)
