package com.ecommerce.review.repository

import com.ecommerce.review.entity.Review
import com.ecommerce.review.entity.ReviewStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ReviewRepository : JpaRepository<Review, Long> {
    fun findByProductIdAndStatus(
        productId: Long,
        status: ReviewStatus,
        pageable: Pageable,
    ): Page<Review>

    fun findByProductIdAndUserId(
        productId: Long,
        userId: Long,
    ): Review?

    fun findByUserId(
        userId: Long,
        pageable: Pageable,
    ): Page<Review>

    fun findByStatus(
        status: ReviewStatus,
        pageable: Pageable,
    ): Page<Review>

    @Query("SELECT r FROM Review r WHERE r.id = :id AND r.userId = :userId")
    fun findByIdAndUserId(
        @Param("id") id: Long,
        @Param("userId") userId: Long,
    ): Review?

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = :status")
    fun countByProductIdAndStatus(
        @Param("productId") productId: Long,
        @Param("status") status: ReviewStatus,
    ): Long

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.status = :status")
    fun averageRatingByProductIdAndStatus(
        @Param("productId") productId: Long,
        @Param("status") status: ReviewStatus,
    ): Double?

    @Query(
        "SELECT r.rating, COUNT(r) FROM Review r " +
            "WHERE r.productId = :productId AND r.status = :status GROUP BY r.rating",
    )
    fun ratingDistributionByProductIdAndStatus(
        @Param("productId") productId: Long,
        @Param("status") status: ReviewStatus,
    ): List<Array<Any>>
}
