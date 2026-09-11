package com.ecommerce.review.event

import com.ecommerce.review.entity.IdempotencyKey
import com.ecommerce.review.entity.VerifiedPurchase
import com.ecommerce.review.repository.IdempotencyKeyRepository
import com.ecommerce.review.repository.VerifiedPurchaseRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyList
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.kafka.support.Acknowledgment
import java.math.BigDecimal
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class OrderPlacedConsumerTest {
    @Mock
    private lateinit var verifiedPurchaseRepository: VerifiedPurchaseRepository

    @Mock
    private lateinit var idempotencyKeyRepository: IdempotencyKeyRepository

    @Mock
    private lateinit var ack: Acknowledgment

    @InjectMocks
    private lateinit var consumer: OrderPlacedConsumer

    private fun event(eventId: String = "evt-1") =
        OrderPlacedEvent(
            eventId = eventId,
            orderId = 100L,
            orderNumber = "ORD-100",
            userId = 5L,
            totalAmount = BigDecimal("99.98"),
            items =
                listOf(
                    OrderPlacedEvent.OrderItem(10L, 1, BigDecimal("49.99"), BigDecimal("49.99")),
                    OrderPlacedEvent.OrderItem(11L, 1, BigDecimal("49.99"), BigDecimal("49.99")),
                ),
            occurredAt = LocalDateTime.now(),
        )

    @Test
    fun `new event stores verified purchases and acknowledges`() {
        `when`(idempotencyKeyRepository.existsByEventId("evt-1")).thenReturn(false)

        consumer.onOrderPlaced(event(), ack)

        verify(verifiedPurchaseRepository).saveAll(anyList<VerifiedPurchase>())
        verify(idempotencyKeyRepository).save(any(IdempotencyKey::class.java))
        verify(ack).acknowledge()
    }

    @Test
    fun `duplicate event is skipped but acknowledged`() {
        `when`(idempotencyKeyRepository.existsByEventId("evt-1")).thenReturn(true)

        consumer.onOrderPlaced(event(), ack)

        verify(verifiedPurchaseRepository, never()).saveAll(anyList<VerifiedPurchase>())
        verify(idempotencyKeyRepository, never()).save(any(IdempotencyKey::class.java))
        verify(ack).acknowledge()
    }
}
