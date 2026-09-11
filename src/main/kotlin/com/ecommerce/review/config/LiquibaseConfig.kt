package com.ecommerce.review.config

import liquibase.integration.spring.SpringLiquibase
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@Configuration
@EnableConfigurationProperties(LiquibaseProperties::class)
class LiquibaseConfig(
    @Value("\${spring.liquibase.default-schema:review}") private val schema: String,
) {
    @Bean
    fun springLiquibase(
        dataSource: DataSource,
        properties: LiquibaseProperties,
    ): SpringLiquibase {
        val liquibase =
            object : SpringLiquibase() {
                override fun afterPropertiesSet() {
                    try {
                        JdbcTemplate(dataSource).execute("CREATE SCHEMA IF NOT EXISTS $schema")
                        super.afterPropertiesSet()
                    } catch (e: liquibase.exception.LiquibaseException) {
                        throw IllegalStateException("Failed to initialize Liquibase", e)
                    }
                }
            }
        liquibase.setDataSource(dataSource)
        liquibase.changeLog = properties.changeLog
        liquibase.defaultSchema = schema
        liquibase.contexts = properties.contexts?.let { java.lang.String.join(",", it) } ?: ""
        liquibase.isDropFirst = properties.isDropFirst
        return liquibase
    }
}
