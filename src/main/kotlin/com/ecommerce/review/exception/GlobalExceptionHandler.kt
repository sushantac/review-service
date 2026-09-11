package com.ecommerce.review.exception

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.OffsetDateTime
import java.util.LinkedHashMap

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ApiException::class)
    fun handleApiException(
        ex: ApiException,
        request: HttpServletRequest,
    ): ResponseEntity<Any> {
        val message = ex.message ?: "API error"
        log.warn("API exception: {} - {}", ex.code, message)
        val body =
            LinkedHashMap<String, Any>().apply {
                put("timestamp", OffsetDateTime.now().toString())
                put("status", ex.status)
                put("error", ex.code)
                put("message", message)
                put("path", request.requestURI)
            }
        return ResponseEntity.status(ex.status).body(body)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest,
    ): ResponseEntity<Any> {
        val errors =
            ex.bindingResult.allErrors
                .map { error ->
                    val field = if (error is FieldError) error.field else error.objectName
                    val message = error.defaultMessage ?: "Invalid value"
                    "$field: $message"
                }.joinToString(", ")

        log.warn("Validation failed: {}", errors)
        val body =
            LinkedHashMap<String, Any>().apply {
                put("timestamp", OffsetDateTime.now().toString())
                put("status", HttpStatus.BAD_REQUEST.value())
                put("error", "ValidationError")
                put("message", errors)
                put("path", request.requestURI)
            }
        return ResponseEntity.badRequest().body(body)
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneral(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<Any> {
        log.error("Unexpected error: {}", ex.message, ex)
        val body =
            LinkedHashMap<String, Any>().apply {
                put("timestamp", OffsetDateTime.now().toString())
                put("status", HttpStatus.INTERNAL_SERVER_ERROR.value())
                put("error", "Internal Server Error")
                put("message", "An unexpected error occurred")
                put("path", request.requestURI)
            }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
    }
}
