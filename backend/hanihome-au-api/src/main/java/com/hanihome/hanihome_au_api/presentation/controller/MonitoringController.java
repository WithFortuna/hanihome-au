package com.hanihome.hanihome_au_api.presentation.controller;

import com.hanihome.hanihome_au_api.application.monitoring.MetricsService;
import com.hanihome.hanihome_au_api.config.SentryConfig;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 모니터링 테스트 및 관리를 위한 컨트롤러
 * 
 * 주요 기능:
 * - 메트릭 테스트 엔드포인트
 * - Sentry 테스트 엔드포인트  
 * - 모니터링 상태 확인
 * 
 * Note: 이 컨트롤러는 개발/테스트 환경에서만 활성화됩니다.
 */
@RestController
@RequestMapping("/monitoring")
@Tag(name = "Monitoring", description = "모니터링 테스트 및 관리 API")
@Profile({"development", "staging", "!production"}) // 프로덕션에서는 비활성화
public class MonitoringController {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringController.class);
    
    private final MetricsService metricsService;
    private final CloudWatchAlarmService cloudWatchAlarmService;
    private final Random random = new Random();

    @Autowired
    public MonitoringController(MetricsService metricsService, CloudWatchAlarmService cloudWatchAlarmService) {
        this.metricsService = metricsService;
        this.cloudWatchAlarmService = cloudWatchAlarmService;
    }

    /**
     * 메트릭 테스트 - 다양한 메트릭 생성
     */
    @PostMapping("/test/metrics")
    @Operation(summary = "메트릭 테스트", description = "다양한 메트릭을 생성하여 모니터링 시스템을 테스트합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testMetrics(
            @Parameter(description = "테스트할 메트릭 타입") 
            @RequestParam(defaultValue = "all") String type,
            @Parameter(description = "생성할 메트릭 수")
            @RequestParam(defaultValue = "10") int count) {
        
        logger.info("Testing metrics - type: {}, count: {}", type, count);
        
        try {
            switch (type.toLowerCase()) {
                case "property":
                    testPropertyMetrics(count);
                    break;
                case "user":
                    testUserMetrics(count);
                    break;
                case "search":
                    testSearchMetrics(count);
                    break;
                case "performance":
                    testPerformanceMetrics(count);
                    break;
                case "all":
                default:
                    testPropertyMetrics(count / 4);
                    testUserMetrics(count / 4);
                    testSearchMetrics(count / 4);
                    testPerformanceMetrics(count / 4);
                    break;
            }
            
            // 현재 메트릭 상태 로깅
            metricsService.logCurrentMetrics();
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Metrics test completed",
                "type", type,
                "count", count
            ));
            
        } catch (Exception e) {
            logger.error("Error testing metrics", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to test metrics: " + e.getMessage()
            ));
        }
    }

    /**
     * Sentry 에러 테스트
     */
    @PostMapping("/test/sentry")
    @Operation(summary = "Sentry 에러 테스트", description = "다양한 타입의 에러를 발생시켜 Sentry 연동을 테스트합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testSentry(
            @Parameter(description = "에러 타입 (exception, message, security, performance)")
            @RequestParam(defaultValue = "message") String errorType) {
        
        logger.info("Testing Sentry error reporting - type: {}", errorType);
        
        try {
            switch (errorType.toLowerCase()) {
                case "exception":
                    try {
                        throw new RuntimeException("Test exception for Sentry integration");
                    } catch (RuntimeException e) {
                        SentryConfig.captureBusinessException(
                            "Test exception from monitoring controller", 
                            e, 
                            Map.of("test_type", "exception")
                        );
                        throw e; // 재던져서 ExceptionHandler가 처리하도록
                    }
                    
                case "security":
                    SentryConfig.captureSecurityEvent(
                        "Test security event from monitoring controller",
                        "test-user-123",
                        "TEST_SECURITY_ACTION"
                    );
                    break;
                    
                case "performance":
                    SentryConfig.capturePerformanceIssue(
                        "test_slow_operation",
                        6000L, // 6초 (임계값 5초 초과)
                        Map.of(
                            "operation_details", "Test performance issue",
                            "test_type", "performance"
                        )
                    );
                    break;
                    
                case "message":
                default:
                    SentryConfig.captureBusinessException(
                        "Test message for Sentry integration - monitoring controller",
                        null,
                        Map.of(
                            "test_type", "message",
                            "controller", "MonitoringController"
                        )
                    );
                    break;
            }
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Sentry test completed",
                "errorType", errorType
            ));
            
        } catch (Exception e) {
            // 이 경우 ExceptionHandler에서 처리됨
            throw e;
        }
    }

    /**
     * 성능 메트릭 테스트 - 의도적으로 느린 작업 시뮬레이션
     */
    @PostMapping("/test/performance")
    @Operation(summary = "성능 메트릭 테스트", description = "느린 작업을 시뮬레이션하여 성능 메트릭을 테스트합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testPerformance(
            @Parameter(description = "시뮬레이션할 지연 시간 (밀리초)")
            @RequestParam(defaultValue = "1000") long delayMs) {
        
        Timer.Sample sample = metricsService.startDatabaseQueryTimer();
        
        try {
            logger.info("Simulating slow operation - delay: {}ms", delayMs);
            
            // 의도적 지연
            Thread.sleep(delayMs);
            
            // 성능 이슈가 있는 경우 Sentry에 리포트
            if (delayMs > 5000) {
                SentryConfig.capturePerformanceIssue(
                    "test_performance_endpoint",
                    delayMs,
                    Map.of(
                        "endpoint", "/monitoring/test/performance",
                        "requested_delay", delayMs
                    )
                );
            }
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Performance test completed",
                "delayMs", delayMs
            ));
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Performance test interrupted", e);
        } finally {
            // 수행 시간 기록
            metricsService.recordDatabaseQueryDuration(
                sample, 
                "performance_test", 
                "test_table"
            );
        }
    }

    /**
     * 게이지 메트릭 업데이트
     */
    @PostMapping("/test/gauges")
    @Operation(summary = "게이지 메트릭 테스트", description = "게이지 메트릭 값들을 업데이트합니다.")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateGauges() {
        
        logger.info("Updating gauge metrics with random values");
        
        // 랜덤 값으로 게이지 업데이트
        long activeUsers = ThreadLocalRandom.current().nextLong(50, 1000);
        long activeProperties = ThreadLocalRandom.current().nextLong(100, 5000);
        long pendingReports = ThreadLocalRandom.current().nextLong(0, 50);
        long scheduledViewings = ThreadLocalRandom.current().nextLong(10, 200);
        
        metricsService.updateActiveUsers(activeUsers);
        metricsService.updateActiveProperties(activeProperties);
        metricsService.updatePendingReports(pendingReports);
        metricsService.updateScheduledViewings(scheduledViewings);
        
        return ResponseEntity.ok(Map.of(
            "status", "success",
            "message", "Gauge metrics updated",
            "values", Map.of(
                "activeUsers", activeUsers,
                "activeProperties", activeProperties,
                "pendingReports", pendingReports,
                "scheduledViewings", scheduledViewings
            )
        ));
    }

    /**
     * 모니터링 상태 확인
     */
    @GetMapping("/status")
    @Operation(summary = "모니터링 상태 확인", description = "현재 모니터링 시스템의 상태를 확인합니다.")
    public ResponseEntity<Map<String, Object>> getMonitoringStatus() {
        
        // 기본 상태 정보 수집
        Map<String, Object> status = Map.of(
            "metrics_service", "active",
            "sentry_enabled", isSentryConfigured(),
            "cloudwatch_enabled", isCloudWatchConfigured(),
            "timestamp", System.currentTimeMillis(),
            "jvm", Map.of(
                "memory_used_mb", (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024,
                "memory_max_mb", Runtime.getRuntime().maxMemory() / 1024 / 1024,
                "threads", Thread.activeCount()
            )
        );
        
        return ResponseEntity.ok(status);
    }

    // === 프라이빗 메서드들 ===

    private void testPropertyMetrics(int count) {
        String[] propertyTypes = {"APARTMENT", "HOUSE", "TOWNHOUSE", "STUDIO"};
        String[] locations = {"Melbourne", "Sydney", "Brisbane", "Perth"};
        String[] ownerRoles = {"LANDLORD", "AGENT"};
        
        for (int i = 0; i < count; i++) {
            String propertyType = propertyTypes[random.nextInt(propertyTypes.length)];
            String location = locations[random.nextInt(locations.length)];
            String ownerRole = ownerRoles[random.nextInt(ownerRoles.length)];
            
            metricsService.incrementPropertyView(propertyType, location);
            
            if (random.nextBoolean()) {
                metricsService.incrementPropertyCreated(propertyType, ownerRole);
            }
            
            if (random.nextInt(10) < 3) { // 30% 확률
                metricsService.incrementFavoriteAdded(propertyType, "USER");
            }
        }
    }
    
    private void testUserMetrics(int count) {
        String[] userRoles = {"USER", "LANDLORD", "AGENT"};
        String[] providers = {"google", "kakao", "email"};
        
        for (int i = 0; i < count; i++) {
            String userRole = userRoles[random.nextInt(userRoles.length)];
            String provider = providers[random.nextInt(providers.length)];
            
            if (random.nextInt(10) < 2) { // 20% 확률
                metricsService.incrementUserRegistration(userRole, provider);
            }
        }
    }
    
    private void testSearchMetrics(int count) {
        String[] searchTypes = {"location", "price_range", "property_type", "features"};
        
        for (int i = 0; i < count; i++) {
            String searchType = searchTypes[random.nextInt(searchTypes.length)];
            int resultCount = random.nextInt(50);
            
            metricsService.incrementSearchRequest(searchType, resultCount);
        }
    }
    
    private void testPerformanceMetrics(int count) {
        for (int i = 0; i < count; i++) {
            Timer.Sample sample = metricsService.startPropertySearchTimer();
            
            try {
                // 랜덤 지연 시뮬레이션 (1-100ms)
                Thread.sleep(random.nextInt(100) + 1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            boolean cached = random.nextBoolean();
            metricsService.recordPropertySearchDuration(sample, "test_search", cached);
        }
    }
    
    private boolean isSentryConfigured() {
        // Sentry DSN이 설정되어 있는지 확인
        try {
            return io.sentry.Sentry.isEnabled();
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isCloudWatchConfigured() {
        // CloudWatch 설정 여부 확인 (실제 환경에서는 더 정확한 체크 필요)
        String region = System.getenv("AWS_REGION");
        String accessKey = System.getenv("AWS_ACCESS_KEY_ID");
        return region != null && accessKey != null;
    }
}