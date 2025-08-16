package com.hanihome.hanihome_au_api.config;

import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryOptions;
import io.sentry.protocol.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * Sentry 에러 추적 및 모니터링 설정
 * 
 * 주요 기능:
 * - Sentry 초기화 및 구성
 * - 커스텀 에러 처리 및 컨텍스트 설정
 * - 사용자 정보 및 요청 컨텍스트 추가
 */
@Configuration
@Profile("!test")
public class SentryConfig {

    private static final Logger logger = LoggerFactory.getLogger(SentryConfig.class);

    @Value("${sentry.dsn:}")
    private String sentryDsn;
    
    @Value("${spring.application.name:hanihome-au-api}")
    private String applicationName;
    
    @Value("${spring.profiles.active:dev}")
    private String environment;
    
    @Value("${app.version:1.0.0}")
    private String applicationVersion;
    
    @Value("${sentry.sample-rate:1.0}")
    private Double sampleRate;
    
    @Value("${sentry.traces-sample-rate:0.1}")
    private Double tracesSampleRate;

    @PostConstruct
    public void initSentry() {
        if (sentryDsn == null || sentryDsn.trim().isEmpty()) {
            logger.warn("Sentry DSN not configured - error tracking disabled");
            return;
        }

        try {
            Sentry.init(options -> {
                options.setDsn(sentryDsn);
                options.setEnvironment(environment);
                options.setRelease(applicationVersion);
                options.setSampleRate(sampleRate);
                options.setTracesSampleRate(tracesSampleRate);
                options.setDebug(logger.isDebugEnabled());
                
                // 서버 이름 설정
                options.setServerName(applicationName);
                
                // 태그 설정
                options.setTags(Map.of(
                    "application", applicationName,
                    "environment", environment,
                    "version", applicationVersion
                ));
                
                // 요청 필터링 - 헬스체크 제외
                options.setBeforeSend((event, hint) -> {
                    return filterEvent(event, hint);
                });
                
                // 커스텀 트랜잭션 네이밍
                options.setBeforeTransaction((transaction, hint) -> {
                    return filterTransaction(transaction, hint);
                });
            });
            
            logger.info("Sentry initialized successfully for environment: {}", environment);
            
            // 초기화 테스트
            testSentryIntegration();
            
        } catch (Exception e) {
            logger.error("Failed to initialize Sentry", e);
        }
    }

    private SentryEvent filterEvent(SentryEvent event, Object hint) {
        try {
            // 헬스체크 관련 에러 필터링
            String message = event.getMessage() != null ? event.getMessage().getFormatted() : "";
            String loggerName = event.getLogger() != null ? event.getLogger() : "";
            
            if (isHealthCheckRelated(message, loggerName)) {
                logger.debug("Filtering out health check related error: {}", message);
                return null; // 이벤트 제외
            }
            
            // 사용자 컨텍스트 추가
            addUserContext(event);
            
            // 커스텀 태그 추가
            addCustomTags(event);
            
            // 추가 컨텍스트 정보
            addApplicationContext(event);
            
            return event;
        } catch (Exception e) {
            logger.warn("Error processing Sentry event", e);
            return event; // 에러가 있어도 원본 이벤트는 전송
        }
    }

    private io.sentry.protocol.TransactionNameSource filterTransaction(
            io.sentry.protocol.TransactionNameSource transaction, Object hint) {
        // 트랜잭션 필터링 로직 (필요시 구현)
        return transaction;
    }

    private boolean isHealthCheckRelated(String message, String loggerName) {
        if (message == null && loggerName == null) return false;
        
        String[] healthCheckKeywords = {
            "actuator/health",
            "health-check",
            "readiness",
            "liveness",
            "/health",
            "HealthIndicator"
        };
        
        for (String keyword : healthCheckKeywords) {
            if ((message != null && message.toLowerCase().contains(keyword.toLowerCase())) ||
                (loggerName != null && loggerName.toLowerCase().contains(keyword.toLowerCase()))) {
                return true;
            }
        }
        
        return false;
    }

    private void addUserContext(SentryEvent event) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && 
                !"anonymousUser".equals(authentication.getName())) {
                
                User user = new User();
                user.setId(authentication.getName());
                user.setUsername(authentication.getName());
                
                // 권한 정보 추가
                if (authentication.getAuthorities() != null) {
                    user.setData(Map.of(
                        "roles", authentication.getAuthorities().toString()
                    ));
                }
                
                event.setUser(user);
            }
        } catch (Exception e) {
            logger.debug("Could not add user context to Sentry event", e);
        }
    }

    private void addCustomTags(SentryEvent event) {
        Map<String, String> tags = new HashMap<>();
        
        // JVM 정보
        Runtime runtime = Runtime.getRuntime();
        tags.put("jvm.memory.used", 
                 String.valueOf((runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024));
        tags.put("jvm.memory.max", 
                 String.valueOf(runtime.maxMemory() / 1024 / 1024));
        
        // 스레드 정보
        tags.put("thread.count", String.valueOf(Thread.activeCount()));
        
        event.setTags(tags);
    }

    private void addApplicationContext(SentryEvent event) {
        Map<String, Object> extra = new HashMap<>();
        
        // 애플리케이션 정보
        extra.put("application.name", applicationName);
        extra.put("application.version", applicationVersion);
        extra.put("environment", environment);
        
        // 시스템 정보
        extra.put("os.name", System.getProperty("os.name"));
        extra.put("java.version", System.getProperty("java.version"));
        
        event.setExtra(extra);
    }

    private void testSentryIntegration() {
        if (logger.isDebugEnabled()) {
            logger.debug("Testing Sentry integration...");
            
            // 테스트 이벤트 전송 (DEBUG 레벨에서만)
            Sentry.withScope(scope -> {
                scope.setLevel(io.sentry.SentryLevel.INFO);
                scope.setTag("test", "sentry-integration");
                Sentry.captureMessage("Sentry integration test - application started successfully");
            });
        }
    }

    /**
     * Sentry 연결 상태를 확인하는 Health Indicator
     */
    @Bean
    public HealthIndicator sentryHealthIndicator() {
        return () -> {
            try {
                if (sentryDsn == null || sentryDsn.trim().isEmpty()) {
                    return Health.down()
                        .withDetail("reason", "Sentry DSN not configured")
                        .build();
                }
                
                // Sentry 초기화 상태 확인
                boolean isInitialized = Sentry.isEnabled();
                
                return isInitialized ? 
                    Health.up()
                        .withDetail("dsn.configured", true)
                        .withDetail("environment", environment)
                        .withDetail("sample.rate", sampleRate)
                        .build() :
                    Health.down()
                        .withDetail("reason", "Sentry not properly initialized")
                        .build();
                        
            } catch (Exception e) {
                return Health.down()
                    .withDetail("reason", "Error checking Sentry status")
                    .withException(e)
                    .build();
            }
        };
    }

    /**
     * 커스텀 에러 캡처 메서드들
     */
    public static void captureBusinessException(String message, Exception exception, Map<String, String> extra) {
        Sentry.withScope(scope -> {
            scope.setLevel(io.sentry.SentryLevel.WARNING);
            scope.setTag("error.type", "business");
            
            if (extra != null) {
                extra.forEach(scope::setExtra);
            }
            
            if (exception != null) {
                Sentry.captureException(exception);
            } else {
                Sentry.captureMessage(message);
            }
        });
    }
    
    public static void captureSecurityEvent(String message, String userId, String action) {
        Sentry.withScope(scope -> {
            scope.setLevel(io.sentry.SentryLevel.WARNING);
            scope.setTag("event.type", "security");
            scope.setTag("security.action", action);
            
            if (userId != null) {
                User user = new User();
                user.setId(userId);
                scope.setUser(user);
            }
            
            scope.setExtra("security.details", Map.of(
                "action", action,
                "timestamp", System.currentTimeMillis()
            ));
            
            Sentry.captureMessage(message);
        });
    }
    
    public static void capturePerformanceIssue(String operation, long durationMs, Map<String, Object> context) {
        if (durationMs > 5000) { // 5초 이상인 경우만
            Sentry.withScope(scope -> {
                scope.setLevel(io.sentry.SentryLevel.WARNING);
                scope.setTag("issue.type", "performance");
                scope.setTag("operation", operation);
                
                Map<String, Object> extra = new HashMap<>();
                extra.put("duration.ms", durationMs);
                extra.put("operation", operation);
                
                if (context != null) {
                    extra.putAll(context);
                }
                
                extra.forEach((key, value) -> scope.setExtra(key, value));
                
                Sentry.captureMessage(String.format("Slow operation detected: %s (%dms)", operation, durationMs));
            });
        }
    }
}