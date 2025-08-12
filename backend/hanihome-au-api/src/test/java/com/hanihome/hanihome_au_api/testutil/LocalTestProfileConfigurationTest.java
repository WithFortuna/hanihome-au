package com.hanihome.hanihome_au_api.testutil;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class to verify that the local-test profile is correctly configured.
 * This profile is used for development testing with local PostgreSQL database.
 * 
 * Note: This test assumes a local PostgreSQL database is available.
 * If not available, the test will fail with connection errors, which is expected.
 */
@SpringBootTest
@ActiveProfiles("local-test")
@TestPropertySource(locations = "classpath:application-local-test.yml")
class LocalTestProfileConfigurationTest {

    @Autowired
    private Environment environment;

    @Test
    void shouldLoadLocalTestProfile() {
        // Verify that local-test profile is active
        String[] activeProfiles = environment.getActiveProfiles();
        assertThat(activeProfiles).contains("local-test");
        
        // Verify application name for local-test profile
        String appName = environment.getProperty("spring.application.name");
        assertThat(appName).isEqualTo("hanihome-au-api-local-test");
    }

    @Test
    void shouldUseLocalPostgreSQLConfiguration() {
        // Verify PostgreSQL configuration
        String jdbcUrl = environment.getProperty("spring.datasource.url");
        String username = environment.getProperty("spring.datasource.username");
        String driverClass = environment.getProperty("spring.datasource.driver-class-name");
        
        assertThat(jdbcUrl).isEqualTo("jdbc:postgresql://localhost:5432/hanihome_test");
        assertThat(username).isEqualTo("hanihome_user"); // Default value
        assertThat(driverClass).isEqualTo("org.postgresql.Driver");
        
        // Verify JPA configuration
        String ddlAuto = environment.getProperty("spring.jpa.hibernate.ddl-auto");
        String showSql = environment.getProperty("spring.jpa.show-sql");
        assertThat(ddlAuto).isEqualTo("validate");
        assertThat(showSql).isEqualTo("true");
    }

    @Test
    void shouldHaveFlywayEnabled() {
        // Verify Flyway is enabled for local-test
        String flywayEnabled = environment.getProperty("spring.flyway.enabled");
        assertThat(flywayEnabled).isEqualTo("true");
        
        String cleanDisabled = environment.getProperty("spring.flyway.clean-disabled");
        assertThat(cleanDisabled).isEqualTo("false"); // Allow clean for local testing
    }

    @Test
    void shouldHaveLocalTestSpecificConfiguration() {
        // Verify JWT configuration with longer expiration for development
        String jwtExpiration = environment.getProperty("jwt.expiration");
        assertThat(jwtExpiration).isEqualTo("86400000"); // 24 hours
        
        String refreshExpiration = environment.getProperty("jwt.refresh-expiration");
        assertThat(refreshExpiration).isEqualTo("604800000"); // 7 days
        
        // Verify file storage configuration
        String uploadDir = environment.getProperty("app.file-storage.upload-dir");
        String maxSize = environment.getProperty("app.file-storage.max-file-size");
        assertThat(uploadDir).isEqualTo("local-test-uploads");
        assertThat(maxSize).isEqualTo("20MB");
    }

    @Test
    void shouldHaveDetailedLoggingConfiguration() {
        // Verify detailed logging for development
        String appLogging = environment.getProperty("logging.level.com.hanihome");
        String sqlLogging = environment.getProperty("logging.level.org.hibernate.SQL");
        String bindingLogging = environment.getProperty("logging.level.org.hibernate.type.descriptor.sql.BasicBinder");
        
        assertThat(appLogging).isEqualTo("DEBUG");
        assertThat(sqlLogging).isEqualTo("DEBUG");
        assertThat(bindingLogging).isEqualTo("TRACE");
        
        // Verify log file configuration
        String logFileName = environment.getProperty("logging.file.name");
        assertThat(logFileName).isEqualTo("logs/local-test.log");
    }

    @Test
    void shouldHaveRedisConfiguration() {
        // Verify Redis configuration with separate database for local tests
        String redisHost = environment.getProperty("spring.data.redis.host");
        String redisPort = environment.getProperty("spring.data.redis.port");
        String redisDatabase = environment.getProperty("spring.data.redis.database");
        
        assertThat(redisHost).isEqualTo("localhost");
        assertThat(redisPort).isEqualTo("6379");
        assertThat(redisDatabase).isEqualTo("2");
    }

    @Test
    void shouldHaveManagementEndpointsFullyExposed() {
        // Verify all management endpoints are exposed for local development
        String exposedEndpoints = environment.getProperty("management.endpoints.web.exposure.include");
        assertThat(exposedEndpoints).isEqualTo("*");
        
        String healthDetails = environment.getProperty("management.endpoint.health.show-details");
        assertThat(healthDetails).isEqualTo("always");
    }

    @Test
    void shouldHaveExternalServiceConfiguration() {
        // Verify external service configurations with environment variable support
        String googleClientId = environment.getProperty("security.oauth2.client.registration.google.client-id");
        String smtpHost = environment.getProperty("spring.mail.host");
        String fcmEnabled = environment.getProperty("fcm.enabled");
        
        assertThat(googleClientId).isEqualTo("your-google-client-id"); // Default fallback
        assertThat(smtpHost).isEqualTo("smtp.gmail.com"); // Default fallback
        assertThat(fcmEnabled).isEqualTo("false"); // Disabled by default for safety
    }

    @Test
    void shouldHaveCacheConfiguration() {
        // Verify cache configuration
        String cacheType = environment.getProperty("spring.cache.type");
        String cachePrefix = environment.getProperty("spring.cache.redis.key-prefix");
        String cacheTtl = environment.getProperty("spring.cache.redis.time-to-live");
        
        assertThat(cacheType).isEqualTo("redis");
        assertThat(cachePrefix).isEqualTo("local-test:");
        assertThat(cacheTtl).isEqualTo("600000"); // 10 minutes
    }
}