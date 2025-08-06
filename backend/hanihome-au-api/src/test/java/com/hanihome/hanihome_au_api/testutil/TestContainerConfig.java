package com.hanihome.hanihome_au_api.testutil;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainers 설정을 위한 테스트 구성 클래스
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestContainerConfig {

    /**
     * PostgreSQL 테스트 컨테이너 설정
     * @return PostgreSQL 컨테이너 인스턴스
     */
    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("hanihome_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true); // 컨테이너 재사용으로 테스트 속도 향상
    }

    /**
     * Redis 테스트 컨테이너 설정
     * @return Redis 컨테이너 인스턴스
     */
    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379)
                .withReuse(true);
    }

    /**
     * 모든 컨테이너의 공통 설정
     */
    public static class ContainerSettings {
        
        /**
         * 컨테이너 시작 대기 시간 설정
         */
        public static final int CONTAINER_STARTUP_TIMEOUT_SECONDS = 60;
        
        /**
         * 테스트 실행 전 컨테이너 상태 확인
         */
        public static void waitForContainersToStart() {
            // 컨테이너가 완전히 시작될 때까지 대기
            try {
                Thread.sleep(2000); // 2초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 데이터베이스 초기화를 위한 유틸리티
     */
    public static class DatabaseUtils {
        
        /**
         * 테스트 후 데이터베이스 정리
         */
        public static void cleanupDatabase(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
            // 외래 키 제약 조건을 일시적으로 비활성화
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            
            // 테스트 데이터 정리 (역순으로)
            String[] tablesToClean = {
                "transaction_activities",
                "transaction_financial_infos", 
                "transactions",
                "viewings",
                "reports",
                "report_actions",
                "search_histories",
                "property_favorites",
                "property_images",
                "property_status_histories",
                "fcm_tokens",
                "properties",
                "users"
            };
            
            for (String table : tablesToClean) {
                try {
                    jdbcTemplate.execute("TRUNCATE TABLE " + table + " RESTART IDENTITY CASCADE");
                } catch (Exception e) {
                    // 테이블이 없거나 다른 오류가 발생해도 계속 진행
                    System.out.println("Warning: Could not truncate table " + table + ": " + e.getMessage());
                }
            }
            
            // 외래 키 제약 조건 다시 활성화
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
        
        /**
         * 테스트용 기본 데이터 삽입
         */
        public static void insertTestData(org.springframework.jdbc.core.JdbcTemplate jdbcTemplate) {
            // 기본 사용자 데이터
            jdbcTemplate.execute("""
                INSERT INTO users (id, email, first_name, last_name, phone_number, is_active, role, created_at, updated_at)
                VALUES ('test-user-1', 'test@example.com', 'Test', 'User', '0412345678', true, 'USER', NOW(), NOW())
                """);
                
            // 기본 부동산 데이터
            jdbcTemplate.execute("""
                INSERT INTO properties (id, title, description, property_type, rental_type, 
                    price_amount, price_currency, bedrooms, bathrooms, parking_spaces,
                    street_address, suburb, state, postcode, country, latitude, longitude,
                    is_available, status, owner_id, created_at, updated_at)
                VALUES ('test-property-1', 'Test Property', 'A test property', 'APARTMENT', 'LONG_TERM',
                    500.00, 'AUD', 2, 1, 1,
                    '123 Test Street', 'Test Suburb', 'NSW', '2000', 'Australia', -33.8688, 151.2093,
                    true, 'ACTIVE', 'test-user-1', NOW(), NOW())
                """);
        }
    }

    /**
     * Redis 캐시 관리를 위한 유틸리티
     */
    public static class CacheUtils {
        
        /**
         * Redis 캐시 정리
         */
        public static void clearCache(org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate) {
            try {
                redisTemplate.getConnectionFactory().getConnection().flushAll();
            } catch (Exception e) {
                System.out.println("Warning: Could not clear Redis cache: " + e.getMessage());
            }
        }
        
        /**
         * 특정 패턴의 키 삭제
         */
        public static void deleteKeysByPattern(
            org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate, 
            String pattern) {
            try {
                java.util.Set<String> keys = redisTemplate.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            } catch (Exception e) {
                System.out.println("Warning: Could not delete Redis keys with pattern " + pattern + ": " + e.getMessage());
            }
        }
    }

    /**
     * 테스트 환경 설정 검증
     */
    public static class EnvironmentValidator {
        
        /**
         * Docker 환경 확인
         */
        public static boolean isDockerAvailable() {
            try {
                ProcessBuilder pb = new ProcessBuilder("docker", "version");
                Process process = pb.start();
                return process.waitFor() == 0;
            } catch (Exception e) {
                return false;
            }
        }
        
        /**
         * 테스트 환경 검증
         */
        public static void validateTestEnvironment() {
            if (!isDockerAvailable()) {
                throw new IllegalStateException(
                    "Docker is not available. Please install Docker to run integration tests.");
            }
        }
        
        /**
         * 필요한 포트 확인
         */
        public static boolean isPortAvailable(int port) {
            try (java.net.ServerSocket socket = new java.net.ServerSocket(port)) {
                return true;
            } catch (java.io.IOException e) {
                return false;
            }
        }
    }

    /**
     * 테스트 프로파일별 설정
     */
    public static class ProfileConfigs {
        
        /**
         * 빠른 테스트를 위한 설정 (H2 인메모리 DB 사용)
         */
        @TestConfiguration
        static class FastTestConfig {
            // H2 데이터베이스 설정은 application-test.yml에서 관리
        }
        
        /**
         * 완전한 통합 테스트를 위한 설정 (실제 DB 컨테이너 사용)
         */
        @TestConfiguration
        static class FullIntegrationTestConfig {
            
            @Bean
            @ServiceConnection
            PostgreSQLContainer<?> fullIntegrationPostgres() {
                return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"))
                        .withDatabaseName("hanihome_integration_test")
                        .withUsername("integration_test")
                        .withPassword("integration_test")
                        .withInitScript("test-data.sql"); // 초기 데이터 스크립트
            }
        }
    }
}