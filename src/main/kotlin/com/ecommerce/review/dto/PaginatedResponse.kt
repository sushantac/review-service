package com.ecommerce.review.dto

data class PaginatedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean,
) {
    companion object {
        fun <T> fromPage(page: org.springframework.data.domain.Page<T>): PaginatedResponse<T> =
            PaginatedResponse(
                content = page.content,
                page = page.number,
                size = page.size,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                last = page.isLast,
            )
    }
}
