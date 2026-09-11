package com.ecommerce.review.controller

import com.ecommerce.review.dto.PaginatedResponse
import com.ecommerce.review.dto.ReviewRequest
import com.ecommerce.review.dto.ReviewResponse
import com.ecommerce.review.dto.ReviewSummary
import com.ecommerce.review.service.ReviewService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class ReviewController(
    private val reviewService: ReviewService,
) {
    @GetMapping("/products/{productId}/reviews")
    fun getProductReviews(
        @PathVariable productId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PaginatedResponse<ReviewResponse> = reviewService.getReviewsByProduct(productId, page, size)

    @GetMapping("/products/{productId}/reviews/summary")
    fun getProductReviewSummary(
        @PathVariable productId: Long,
    ): ReviewSummary = reviewService.getReviewSummary(productId)

    @PostMapping("/reviews")
    fun createReview(
        @AuthenticationPrincipal jwt: Jwt,
        @Valid @RequestBody request: ReviewRequest,
    ): ReviewResponse {
        val userId = jwt.subject.toLong()
        return reviewService.createReview(userId, request)
    }

    @GetMapping("/reviews/my")
    fun getMyReviews(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PaginatedResponse<ReviewResponse> {
        val userId = jwt.subject.toLong()
        return reviewService.getMyReviews(userId, page, size)
    }

    @GetMapping("/reviews/{id}")
    fun getReview(
        @PathVariable id: Long,
    ): ReviewResponse = reviewService.getReview(id)

    @PutMapping("/reviews/{id}")
    fun updateReview(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: Long,
        @Valid @RequestBody request: ReviewRequest,
    ): ReviewResponse {
        val userId = jwt.subject.toLong()
        return reviewService.updateReview(userId, id, request)
    }

    @DeleteMapping("/reviews/{id}")
    fun deleteReview(
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: Long,
    ): ResponseEntity<Void> {
        val userId = jwt.subject.toLong()
        reviewService.deleteReview(userId, id)
        return ResponseEntity.noContent().build()
    }

    // Admin endpoints
    @GetMapping("/admin/reviews")
    fun getAllReviewsAdmin(
        @RequestParam(required = false) status: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): PaginatedResponse<ReviewResponse> = reviewService.getAllReviews(status, page, size)

    @PostMapping("/admin/reviews/{id}/approve")
    fun approveReview(
        @PathVariable id: Long,
    ): ReviewResponse = reviewService.moderateReview(id, true)

    @PostMapping("/admin/reviews/{id}/reject")
    fun rejectReview(
        @PathVariable id: Long,
    ): ReviewResponse = reviewService.moderateReview(id, false)
}
