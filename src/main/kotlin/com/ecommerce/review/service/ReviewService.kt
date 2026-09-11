package com.ecommerce.review.service

import com.ecommerce.review.dto.PaginatedResponse
import com.ecommerce.review.dto.ReviewRequest
import com.ecommerce.review.dto.ReviewResponse
import com.ecommerce.review.dto.ReviewSummary
import com.ecommerce.review.entity.Review
import com.ecommerce.review.entity.ReviewStatus
import com.ecommerce.review.event.ReviewEvent
import com.ecommerce.review.event.ReviewEventPublisher
import com.ecommerce.review.exception.ApiException
import com.ecommerce.review.repository.ReviewRepository
import com.ecommerce.review.repository.VerifiedPurchaseRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val verifiedPurchaseRepository: VerifiedPurchaseRepository,
    private val reviewEventPublisher: ReviewEventPublisher,
) {
    @Transactional
    fun createReview(
        userId: Long,
        request: ReviewRequest,
    ): ReviewResponse {
        val existing = reviewRepository.findByProductIdAndUserId(request.productId, userId)
        if (existing != null) {
            throw ApiException("DUPLICATE_REVIEW", "You have already reviewed this product", 409)
        }

        val verified = verifiedPurchaseRepository.existsByUserIdAndProductId(userId, request.productId)

        val review =
            Review(
                productId = request.productId,
                userId = userId,
                rating = request.rating,
                title = request.title,
                body = request.body,
                verifiedPurchase = verified,
                status = ReviewStatus.PENDING,
            )

        val saved = reviewRepository.save(review)
        return ReviewResponse.fromEntity(saved)
    }

    @Transactional(readOnly = true)
    fun getReview(id: Long): ReviewResponse {
        val review =
            reviewRepository
                .findById(id)
                .orElseThrow { ApiException("REVIEW_NOT_FOUND", "Review not found", 404) }
        return ReviewResponse.fromEntity(review)
    }

    @Transactional
    fun updateReview(
        userId: Long,
        id: Long,
        request: ReviewRequest,
    ): ReviewResponse {
        val review =
            reviewRepository.findByIdAndUserId(id, userId)
                ?: throw ApiException("REVIEW_NOT_FOUND", "Review not found", 404)

        review.rating = request.rating
        review.title = request.title
        review.body = request.body
        review.status = ReviewStatus.PENDING

        val saved = reviewRepository.save(review)
        return ReviewResponse.fromEntity(saved)
    }

    @Transactional
    fun deleteReview(
        userId: Long,
        id: Long,
    ) {
        val review =
            reviewRepository.findByIdAndUserId(id, userId)
                ?: throw ApiException("REVIEW_NOT_FOUND", "Review not found", 404)
        reviewRepository.delete(review)
    }

    @Transactional(readOnly = true)
    fun getReviewsByProduct(
        productId: Long,
        page: Int,
        size: Int,
    ): PaginatedResponse<ReviewResponse> {
        val pageable = PageRequest.of(page, size)
        val pageResult = reviewRepository.findByProductIdAndStatus(productId, ReviewStatus.APPROVED, pageable)
        return PaginatedResponse.fromPage(pageResult.map { ReviewResponse.fromEntity(it) })
    }

    @Transactional(readOnly = true)
    fun getReviewSummary(productId: Long): ReviewSummary {
        val avgRating = reviewRepository.averageRatingByProductIdAndStatus(productId, ReviewStatus.APPROVED) ?: 0.0
        val totalReviews = reviewRepository.countByProductIdAndStatus(productId, ReviewStatus.APPROVED)
        val distribution =
            reviewRepository
                .ratingDistributionByProductIdAndStatus(productId, ReviewStatus.APPROVED)
                .associate { (it[0] as Int) to (it[1] as Long) }
        return ReviewSummary(productId, avgRating, totalReviews, distribution)
    }

    @Transactional(readOnly = true)
    fun getMyReviews(
        userId: Long,
        page: Int,
        size: Int,
    ): PaginatedResponse<ReviewResponse> {
        val pageable = PageRequest.of(page, size)
        val pageResult = reviewRepository.findByUserId(userId, pageable)
        return PaginatedResponse.fromPage(pageResult.map { ReviewResponse.fromEntity(it) })
    }

    @Transactional(readOnly = true)
    fun getAllReviews(
        status: String?,
        page: Int,
        size: Int,
    ): PaginatedResponse<ReviewResponse> {
        val pageable = PageRequest.of(page, size)
        val pageResult =
            if (status.isNullOrBlank()) {
                reviewRepository.findAll(pageable)
            } else {
                val reviewStatus =
                    try {
                        ReviewStatus.valueOf(status.uppercase())
                    } catch (ex: IllegalArgumentException) {
                        throw ApiException("INVALID_STATUS", "Invalid status: $status. Use PENDING, APPROVED or REJECTED", 400)
                    }
                reviewRepository.findByStatus(reviewStatus, pageable)
            }
        return PaginatedResponse.fromPage(pageResult.map { ReviewResponse.fromEntity(it) })
    }

    @Transactional
    fun moderateReview(
        id: Long,
        approve: Boolean,
    ): ReviewResponse {
        val review =
            reviewRepository
                .findById(id)
                .orElseThrow { ApiException("REVIEW_NOT_FOUND", "Review not found", 404) }
        review.status = if (approve) ReviewStatus.APPROVED else ReviewStatus.REJECTED
        val saved = reviewRepository.save(review)
        if (approve) {
            reviewEventPublisher.publishReviewCreated(ReviewEvent.fromReview(saved))
        }
        return ReviewResponse.fromEntity(saved)
    }
}
