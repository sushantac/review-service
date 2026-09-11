package com.ecommerce.review.event

import com.ecommerce.review.entity.IdempotencyKey
import com.ecommerce.review.entity.VerifiedPurchase
import com.ecommerce.review.repository.IdempotencyKeyRepository
import com.ecommerce.review.repository.VerifiedPurchaseRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OrderPlacedConsumer(
    private val verifiedPurchaseRepository: VerifiedPurchaseRepository,
    private val idempotencyKeyRepository: IdempotencyKeyRepository,
) {
    private val log = LoggerFactory.getLogger(OrderPlacedConsumer::class.java)

    @KafkaListener(topics = ["\${app.kafka.topics.order-placed:order.placed}"], groupId = "review-service")
    @Transactional
    fun onOrderPlaced(
        event: OrderPlacedEvent,
        ack: Acknowledgment,
    ) {
        if (event.eventId.isNullOrBlank() || idempotencyKeyRepository.existsByEventId(event.eventId)) {
            log.debug("Duplicate or null eventId, skipping: {}", event.eventId)
            ack.acknowledge()
            return
        }

        val purchases =
            event.items.map { item ->
                VerifiedPurchase(
                    eventId = event.eventId,
                    userId = event.userId,
                    productId = item.productId,
                    orderId = event.orderId,
                    orderNumber = event.orderNumber,
                )
            }

        verifiedPurchaseRepository.saveAll(purchases)
        idempotencyKeyRepository.save(IdempotencyKey(event.eventId, "order.placed"))
        ack.acknowledge()
        log.info("Processed order.placed event for orderId={}, {} items", event.orderId, purchases.size)
    }
}
