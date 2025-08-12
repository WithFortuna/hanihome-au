package com.hanihome.hanihome_au_api.testutil;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class to verify that test profiles are correctly configured
 * and that database connections work as expected.
 */
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
@Sql(scripts = "classpath:cleanup-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TestProfileConfigurationTest {

    @Autowired
    private Environment environment;

    @Autowired
    private DataSource dataSource;

    @Test
    void shouldLoadTestProfile() {
        // Verify that test profile is active
        String[] activeProfiles = environment.getActiveProfiles();
        assertThat(activeProfiles).contains("test");
        
        // Verify application name for test profile
        String appName = environment.getProperty("spring.application.name");
        assertThat(appName).isEqualTo("hanihome-au-api-test");
    }

    @Test
    void shouldUseH2DatabaseForTests() throws Exception {
        // Verify that we're using H2 database for tests
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            String url = metaData.getURL();
            
            assertThat(databaseProductName).isEqualTo("H2");
            assertThat(url).contains("jdbc:h2:mem:testdb");
        }
    }

    @Test
    void shouldHaveTestSpecificConfiguration() {
        // Verify JWT configuration
        String jwtSecret = environment.getProperty("jwt.secret");
        assertThat(jwtSecret).isEqualTo("test-jwt-secret-key-for-testing-only-do-not-use-in-production");
        
        // Verify Flyway is disabled for tests
        String flywayEnabled = environment.getProperty("spring.flyway.enabled");
        assertThat(flywayEnabled).isEqualTo("false");
        
        // Verify file storage configuration
        String uploadDir = environment.getProperty("app.file-storage.upload-dir");
        assertThat(uploadDir).isEqualTo("test-uploads");
        
        // Verify Redis configuration (different port for test)
        String redisPort = environment.getProperty("spring.data.redis.port");
        assertThat(redisPort).isEqualTo("6370");
    }

    @Test
    void shouldHaveTestLoggingConfiguration() {
        // Verify logging level for application package
        String loggingLevel = environment.getProperty("logging.level.com.hanihome");
        assertThat(loggingLevel).isEqualTo("DEBUG");
        
        // Verify SQL logging is enabled
        String sqlLogging = environment.getProperty("logging.level.org.hibernate.SQL");
        assertThat(sqlLogging).isEqualTo("DEBUG");
    }

    @Test
    @Sql(scripts = {"classpath:schema.sql", "classpath:data.sql"})
    void shouldLoadTestDataSuccessfully() throws Exception {
        // Verify that test data can be loaded successfully
        try (Connection connection = dataSource.getConnection()) {
            // Check that tables exist and have data
            var statement = connection.createStatement();
            
            // Check users table
            var userResult = statement.executeQuery("SELECT COUNT(*) FROM users");
            userResult.next();
            int userCount = userResult.getInt(1);
            assertThat(userCount).isGreaterThan(0);
            
            // Check properties table
            var propertyResult = statement.executeQuery("SELECT COUNT(*) FROM properties");
            propertyResult.next();
            int propertyCount = propertyResult.getInt(1);
            assertThat(propertyCount).isGreaterThan(0);
            
            // Check property images table
            var imageResult = statement.executeQuery("SELECT COUNT(*) FROM property_images");
            imageResult.next();
            int imageCount = imageResult.getInt(1);
            assertThat(imageCount).isGreaterThan(0);
        }
    }

    @Test
    void shouldHaveManagementEndpointsExposed() {
        // Verify that all management endpoints are exposed for testing
        String exposedEndpoints = environment.getProperty("management.endpoints.web.exposure.include");
        assertThat(exposedEndpoints).isEqualTo("*");
        
        // Verify health details are shown
        String healthDetails = environment.getProperty("management.endpoint.health.show-details");
        assertThat(healthDetails).isEqualTo("always");
    }
}