package com.ecommerce.review.config

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import javax.sql.DataSource

@Component
class SchemaInitializer(
    private val dataSource: DataSource,
) {
    @Value("\${spring.liquibase.default-schema:review}")
    private var schema: String = "review"

    private val jdbcTemplate = JdbcTemplate(dataSource)

    @PostConstruct
    fun createSchema() {
        jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS $schema")
    }
}
