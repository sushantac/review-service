package com.ecommerce.review.event

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class ReviewEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) {
    private val log = LoggerFactory.getLogger(ReviewEventPublisher::class.java)

    @Value("\${app.kafka.topics.review-created:review.created}")
    private lateinit var reviewCreatedTopic: String

    fun publishReviewCreated(event: ReviewEvent) {
        kafkaTemplate
            .send(reviewCreatedTopic, event.productId.toString(), event)
            .whenComplete { result, ex ->
                if (ex != null) {
                    log.error("Failed to publish review.created event for reviewId={}", event.reviewId, ex)
                } else {
                    log.info("Published review.created event for reviewId={}", event.reviewId)
                }
            }
    }
}
