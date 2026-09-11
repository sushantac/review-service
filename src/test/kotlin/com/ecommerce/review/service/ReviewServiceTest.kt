package com.ecommerce.review.service

import com.ecommerce.review.dto.ReviewRequest
import com.ecommerce.review.entity.Review
import com.ecommerce.review.entity.ReviewStatus
import com.ecommerce.review.event.ReviewEvent
import com.ecommerce.review.event.ReviewEventPublisher
import com.ecommerce.review.exception.ApiException
import com.ecommerce.review.repository.ReviewRepository
import com.ecommerce.review.repository.VerifiedPurchaseRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ReviewServiceTest {
    @Mock
    private lateinit var reviewRepository: ReviewRepository

    @Mock
    private lateinit var verifiedPurchaseRepository: VerifiedPurchaseRepository

    @Mock
    private lateinit var reviewEventPublisher: ReviewEventPublisher

    @InjectMocks
    private lateinit var reviewService: ReviewService

    private lateinit var review: Review

    @BeforeEach
    fun setUp() {
        review =
            Review(
                id = 1L,
                productId = 10L,
                userId = 5L,
                rating = 4,
                title = "Great product",
                body = "Works as expected",
                verifiedPurchase = true,
                status = ReviewStatus.APPROVED,
            )
    }

    @Test
    fun `createReview saves pending review with verified flag`() {
        whenever(reviewRepository.findByProductIdAndUserId(10L, 5L)).thenReturn(null)
        whenever(verifiedPurchaseRepository.existsByUserIdAndProductId(5L, 10L)).thenReturn(true)
        whenever(reviewRepository.save(any<Review>())).thenAnswer {
            it.getArgument<Review>(0).apply { id = 1L }
        }

        val response = reviewService.createReview(5L, ReviewRequest(10L, 5, "Excellent", "Loved it"))

        assertEquals(10L, response.productId)
        assertEquals(5L, response.userId)
        assertEquals(5, response.rating)
        assertTrue(response.verifiedPurchase)
        assertEquals(ReviewStatus.PENDING, response.status)
        verify(reviewEventPublisher, never()).publishReviewCreated(any<ReviewEvent>())
    }

    @Test
    fun `createReview rejects duplicate review with 409`() {
        whenever(reviewRepository.findByProductIdAndUserId(10L, 5L)).thenReturn(review)

        val ex =
            assertThrows(ApiException::class.java) {
                reviewService.createReview(5L, ReviewRequest(10L, 5, "Again", "Duplicate"))
            }
        assertEquals(409, ex.status)
    }

    @Test
    fun `getReview throws 404 when missing`() {
        whenever(reviewRepository.findById(99L)).thenReturn(Optional.empty())

        val ex = assertThrows(ApiException::class.java) { reviewService.getReview(99L) }
        assertEquals(404, ex.status)
    }

    @Test
    fun `updateReview resets status to pending`() {
        whenever(reviewRepository.findByIdAndUserId(1L, 5L)).thenReturn(review)
        whenever(reviewRepository.save(any<Review>())).thenAnswer { it.getArgument<Review>(0) }

        val response = reviewService.updateReview(5L, 1L, ReviewRequest(10L, 3, "Updated", "Changed mind"))

        assertEquals(3, response.rating)
        assertEquals(ReviewStatus.PENDING, response.status)
    }

    @Test
    fun `updateReview throws 404 for non-owner`() {
        whenever(reviewRepository.findByIdAndUserId(1L, 7L)).thenReturn(null)

        val ex =
            assertThrows(ApiException::class.java) {
                reviewService.updateReview(7L, 1L, ReviewRequest(10L, 3, "Hijack", "Nope"))
            }
        assertEquals(404, ex.status)
    }

    @Test
    fun `deleteReview removes owned review`() {
        whenever(reviewRepository.findByIdAndUserId(1L, 5L)).thenReturn(review)

        reviewService.deleteReview(5L, 1L)

        verify(reviewRepository).delete(review)
    }

    @Test
    fun `getReviewSummary aggregates approved reviews`() {
        whenever(reviewRepository.averageRatingByProductIdAndStatus(10L, ReviewStatus.APPROVED))
            .thenReturn(4.5)
        whenever(reviewRepository.countByProductIdAndStatus(10L, ReviewStatus.APPROVED))
            .thenReturn(2L)
        whenever(reviewRepository.ratingDistributionByProductIdAndStatus(10L, ReviewStatus.APPROVED))
            .thenReturn(listOf(arrayOf<Any>(5, 1L), arrayOf<Any>(4, 1L)))

        val summary = reviewService.getReviewSummary(10L)

        assertEquals(10L, summary.productId)
        assertEquals(4.5, summary.averageRating)
        assertEquals(2L, summary.totalReviews)
        assertEquals(mapOf(5 to 1L, 4 to 1L), summary.ratingDistribution)
    }

    @Test
    fun `getReviewSummary returns zeros when no reviews`() {
        whenever(reviewRepository.averageRatingByProductIdAndStatus(10L, ReviewStatus.APPROVED))
            .thenReturn(null)
        whenever(reviewRepository.countByProductIdAndStatus(10L, ReviewStatus.APPROVED))
            .thenReturn(0L)
        whenever(reviewRepository.ratingDistributionByProductIdAndStatus(10L, ReviewStatus.APPROVED))
            .thenReturn(emptyList())

        val summary = reviewService.getReviewSummary(10L)

        assertEquals(0.0, summary.averageRating)
        assertEquals(0L, summary.totalReviews)
        assertTrue(summary.ratingDistribution.isEmpty())
    }

    @Test
    fun `getAllReviews filters by status`() {
        val pageable = PageRequest.of(0, 20)
        whenever(reviewRepository.findByStatus(ReviewStatus.PENDING, pageable))
            .thenReturn(PageImpl(listOf(review)))

        val page = reviewService.getAllReviews("pending", 0, 20)

        assertEquals(1L, page.totalElements)
    }

    @Test
    fun `getAllReviews rejects invalid status with 400`() {
        val ex =
            assertThrows(ApiException::class.java) {
                reviewService.getAllReviews("bogus", 0, 20)
            }
        assertEquals(400, ex.status)
    }

    @Test
    fun `moderateReview approve sets approved and publishes event`() {
        val pending = review.copy(status = ReviewStatus.PENDING)
        whenever(reviewRepository.findById(1L)).thenReturn(Optional.of(pending))
        whenever(reviewRepository.save(any<Review>())).thenAnswer { it.getArgument<Review>(0) }

        val response = reviewService.moderateReview(1L, true)

        assertEquals(ReviewStatus.APPROVED, response.status)
        verify(reviewEventPublisher).publishReviewCreated(any<ReviewEvent>())
    }

    @Test
    fun `moderateReview reject sets rejected without publishing`() {
        val pending = review.copy(status = ReviewStatus.PENDING)
        whenever(reviewRepository.findById(1L)).thenReturn(Optional.of(pending))
        whenever(reviewRepository.save(any<Review>())).thenAnswer { it.getArgument<Review>(0) }

        val response = reviewService.moderateReview(1L, false)

        assertEquals(ReviewStatus.REJECTED, response.status)
        verify(reviewEventPublisher, never()).publishReviewCreated(any<ReviewEvent>())
    }
}
