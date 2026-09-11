package com.ecommerce.review.repository

import com.ecommerce.review.entity.IdempotencyKey
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface IdempotencyKeyRepository : JpaRepository<IdempotencyKey, String> {
    fun existsByEventId(eventId: String): Boolean
}
