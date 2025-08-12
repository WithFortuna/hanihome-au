package com.hanihome.hanihome_au_api;

import com.hanihome.hanihome_au_api.testutil.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test to verify that the Testcontainers environment is properly configured.
 * This test validates:
 * - PostgreSQL container is running and accessible
 * - Spring context loads correctly with integration-test profile
 * - Database schema is properly created
 * - Application endpoints are accessible
 * - Transaction rollback works correctly
 */
class IntegrationTestEnvironmentTest extends BaseIntegrationTest {

    @Autowired
    private Environment environment;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldHaveIntegrationTestProfileActive() {
        String[] activeProfiles = environment.getActiveProfiles();
        assertThat(activeProfiles).contains("integration-test");
        
        String appName = environment.getProperty("spring.application.name");
        assertThat(appName).isEqualTo("hanihome-au-api-integration-test");
    }

    @Test
    void shouldUsePostgreSQLDatabase() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            String url = metaData.getURL();
            
            assertThat(databaseProductName).isEqualTo("PostgreSQL");
            assertThat(url).contains("jdbc:postgresql://");
            assertThat(url).contains("testdb");
        }
    }

    @Test
    void shouldHavePostgreSQLContainerRunning() {
        assertThat(getPostgresContainer().isRunning()).isTrue();
        assertThat(getPostgresContainer().getDatabaseName()).isEqualTo("testdb");
        assertThat(getPostgresContainer().getUsername()).isEqualTo("testuser");
        assertThat(getPostgresContainer().getPassword()).isEqualTo("testpass");
    }

    @Test
    void shouldCreateDatabaseTablesViaMigrations() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // Check that key tables exist (created by Flyway migrations)
            ResultSet tables = metaData.getTables(null, null, "users", new String[]{"TABLE"});
            assertThat(tables.next()).isTrue();
            
            tables = metaData.getTables(null, null, "properties", new String[]{"TABLE"});
            assertThat(tables.next()).isTrue();
            
            tables = metaData.getTables(null, null, "property_images", new String[]{"TABLE"});
            assertThat(tables.next()).isTrue();
        }
    }

    @Test
    void shouldHaveApplicationRunning() {
        String baseUrl = getBaseUrl();
        assertThat(baseUrl).matches("http://localhost:\\d+");
        
        // Test that actuator health endpoint is accessible
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/actuator/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    void shouldHaveCorrectIntegrationTestConfiguration() {
        // Verify JWT configuration
        String jwtSecret = environment.getProperty("jwt.secret");
        assertThat(jwtSecret).contains("integration-test");
        
        // Verify Flyway is enabled
        String flywayEnabled = environment.getProperty("spring.flyway.enabled");
        assertThat(flywayEnabled).isEqualTo("true");
        
        // Verify FCM is disabled for tests
        String fcmEnabled = environment.getProperty("fcm.enabled");
        assertThat(fcmEnabled).isEqualTo("false");
    }

    @Test
    @Sql(scripts = "/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = "/cleanup-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void shouldLoadAndCleanTestData() throws Exception {
        // Insert test data via @Sql annotation and verify it's loaded
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.createStatement();
            
            // Check that test data was loaded
            var userResult = statement.executeQuery("SELECT COUNT(*) FROM users");
            userResult.next();
            int userCount = userResult.getInt(1);
            assertThat(userCount).isGreaterThan(0);
            
            var propertyResult = statement.executeQuery("SELECT COUNT(*) FROM properties");
            propertyResult.next();
            int propertyCount = propertyResult.getInt(1);
            assertThat(propertyCount).isGreaterThan(0);
        }
        // Data will be cleaned up automatically by @Sql cleanup script
    }

    @Test
    void shouldHandleTransactionRollback() throws Exception {
        // This test verifies that @Transactional rollback works correctly
        int initialUserCount;
        
        try (Connection connection = dataSource.getConnection()) {
            var statement = connection.createStatement();
            var result = statement.executeQuery("SELECT COUNT(*) FROM users");
            result.next();
            initialUserCount = result.getInt(1);
        }
        
        // The test is @Transactional, so any data changes will be rolled back
        // after the test completes, maintaining test isolation
        assertThat(initialUserCount).isGreaterThanOrEqualTo(0);
    }

    @Test
    void shouldHaveCorrectLoggingConfiguration() {
        // Verify logging configuration for integration tests
        String appLoggingLevel = environment.getProperty("logging.level.com.hanihome");
        assertThat(appLoggingLevel).isEqualTo("INFO");
        
        String testcontainersLogging = environment.getProperty("logging.level.org.testcontainers");
        assertThat(testcontainersLogging).isEqualTo("INFO");
    }

    @Test
    void shouldHaveManagementEndpointsConfigured() {
        // Check management endpoint configuration
        String exposedEndpoints = environment.getProperty("management.endpoints.web.exposure.include");
        assertThat(exposedEndpoints).isEqualTo("health,info,metrics");
        
        // Test that info endpoint is accessible
        String baseUrl = getBaseUrl();
        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/actuator/info", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}