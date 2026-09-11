package com.ecommerce.review.repository

import com.ecommerce.review.entity.VerifiedPurchase
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VerifiedPurchaseRepository : JpaRepository<VerifiedPurchase, Long> {
    fun findByEventId(eventId: String): VerifiedPurchase?

    fun existsByUserIdAndProductId(
        userId: Long,
        productId: Long,
    ): Boolean
}
