package com.ecommerce.review.event

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderPlacedEvent(
    @JsonProperty("eventId") val eventId: String,
    @JsonProperty("orderId") val orderId: Long,
    @JsonProperty("orderNumber") val orderNumber: String,
    @JsonProperty("userId") val userId: Long,
    @JsonProperty("totalAmount") val totalAmount: BigDecimal,
    @JsonProperty("items") val items: List<OrderItem>,
    @JsonProperty("occurredAt")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    val occurredAt: LocalDateTime,
) {
    data class OrderItem(
        @JsonProperty("productId") val productId: Long,
        @JsonProperty("quantity") val quantity: Int,
        @JsonProperty("unitPrice") val unitPrice: BigDecimal,
        @JsonProperty("totalPrice") val totalPrice: BigDecimal,
    )
}
