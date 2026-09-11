package com.ecommerce.review.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class InternalApiKeyFilter(
    @Value("\${app.internal.api-key:internal-service-key-12345}") private val internalApiKey: String,
) : OncePerRequestFilter() {
    companion object {
        private const val HEADER_NAME = "X-Internal-API-Key"
        private val ADMIN_AUTHORITIES = listOf(SimpleGrantedAuthority("ROLE_ADMIN"))
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val apiKey = request.getHeader(HEADER_NAME)
        if (internalApiKey == apiKey) {
            val auth = UsernamePasswordAuthenticationToken("internal-service", null, ADMIN_AUTHORITIES)
            SecurityContextHolder.getContext().authentication = auth
        }
        filterChain.doFilter(request, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return !path.startsWith("/api/v1/reviews") &&
            !path.startsWith("/api/v1/products") &&
            !path.startsWith("/api/v1/admin")
    }
}
