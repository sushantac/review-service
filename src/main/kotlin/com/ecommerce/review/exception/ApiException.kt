package com.ecommerce.review.exception

class ApiException(
    val code: String,
    message: String,
    val status: Int = 400,
) : RuntimeException(message)
