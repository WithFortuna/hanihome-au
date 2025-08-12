package com.hanihome.hanihome_au_api.testutil;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Configuration class for PostgreSQL Testcontainer.
 * This provides a shared PostgreSQL container instance for integration tests.
 */
@TestConfiguration(proxyBeanMethods = false)
public class PostgreSQLContainerConfig {

    /**
     * PostgreSQL container for integration tests.
     * Uses the @ServiceConnection annotation for automatic Spring Boot integration.
     * 
     * @return configured PostgreSQL container
     */
    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpass")
                .withInitScript("init-integration-test-db.sql")
                .withReuse(true); // Reuse container across tests for better performance
    }
}