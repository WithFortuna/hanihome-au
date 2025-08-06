package com.hanihome.hanihome_au_api.testutil;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests using Testcontainers.
 * 
 * This class provides:
 * - PostgreSQL container setup
 * - Spring Boot test configuration
 * - Common test utilities and setup
 * - Transaction rollback for test isolation
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@Import(PostgreSQLContainerConfig.class)
@Testcontainers
@Transactional // Rollback transactions after each test for isolation
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @Container
    protected static final PostgreSQLContainer<?> postgres = 
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass")
                    .withInitScript("init-integration-test-db.sql");

    /**
     * Configure Spring Boot to use the Testcontainer database.
     * This method dynamically sets the datasource properties based on the running container.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Database configuration
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        
        // JPA configuration for integration tests
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "false"); // Less verbose for integration tests
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.PostgreSQLDialect");
        
        // Enable Flyway for integration tests to test migrations
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.baseline-on-migrate", () -> "true");
        
        // JWT configuration
        registry.add("jwt.secret", () -> "integration-test-jwt-secret-key");
        registry.add("jwt.expiration", () -> "3600000");
        
        // Disable external services for integration tests
        registry.add("fcm.enabled", () -> "false");
        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> "1025");
        
        // Rate limiting configuration
        registry.add("rate-limit.requests-per-minute", () -> "500");
        registry.add("rate-limit.burst-size", () -> "50");
    }

    @BeforeEach
    void setUp() {
        // Common setup for all integration tests
        // This method can be overridden by subclasses for additional setup
    }

    /**
     * Get the base URL for the running application.
     * Useful for making HTTP requests in integration tests.
     */
    protected String getBaseUrl() {
        return "http://localhost:" + port;
    }

    /**
     * Get the PostgreSQL container instance for direct database access if needed.
     */
    protected PostgreSQLContainer<?> getPostgresContainer() {
        return postgres;
    }
}