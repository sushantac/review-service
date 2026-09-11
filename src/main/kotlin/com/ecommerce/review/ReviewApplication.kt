package com.ecommerce.review

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka

@SpringBootApplication
@EnableKafka
class ReviewApplication

fun main(args: Array<String>) {
    runApplication<ReviewApplication>(*args)
}
