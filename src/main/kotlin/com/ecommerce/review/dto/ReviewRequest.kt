package com.ecommerce.review.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class ReviewRequest(
    @field:NotNull(message = "productId is required")
    val productId: Long,
    @field:NotNull(message = "rating is required")
    @field:Min(value = 1, message = "rating must be at least 1")
    @field:Max(value = 5, message = "rating must be at most 5")
    val rating: Int,
    @field:Size(max = 160, message = "title must not exceed 160 characters")
    val title: String? = null,
    @field:Size(max = 5000, message = "body must not exceed 5000 characters")
    val body: String? = null,
)
