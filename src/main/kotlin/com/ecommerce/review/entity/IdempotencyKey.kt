package com.ecommerce.review.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(name = "idempotency_keys", schema = "review")
data class IdempotencyKey(
    @Id
    @Column(name = "event_id", nullable = false)
    var eventId: String = "",
    @Column(name = "topic", nullable = false)
    var topic: String = "",
    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now(),
)
