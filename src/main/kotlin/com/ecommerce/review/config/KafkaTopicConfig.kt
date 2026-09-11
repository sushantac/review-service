package com.ecommerce.review.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaTopicConfig {
    @Value("\${app.kafka.topics.review-created:review.created}")
    private lateinit var reviewCreatedTopic: String

    @Bean
    fun reviewCreatedTopic(): NewTopic =
        TopicBuilder
            .name(reviewCreatedTopic)
            .partitions(3)
            .replicas(1)
            .build()
}
