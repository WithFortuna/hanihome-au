package com.hanihome.hanihome_au_api.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.sentry.Sentry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * CloudWatch 및 모니터링 설정을 담당하는 구성 클래스
 * 
 * 주요 기능:
 * - CloudWatch 메트릭 레지스트리 설정
 * - Sentry 에러 추적 커스터마이징
 * - 커스텀 메트릭 태그 및 네이밍 설정
 * - 애플리케이션별 메트릭 구성
 */
@Configuration
@Profile("!test") // 테스트 환경에서는 비활성화
public class MonitoringConfig {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringConfig.class);

    @Value("${spring.application.name:hanihome-au-api}")
    private String applicationName;
    
    @Value("${management.metrics.export.cloudwatch.namespace:HaniHome/AU}")
    private String cloudwatchNamespace;
    
    @Value("${management.metrics.export.cloudwatch.step:PT1M}")
    private Duration step;
    
    @Value("${aws.region:ap-southeast-2}")
    private String awsRegion;
    
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Value("${sentry.dsn:}")
    private String sentryDsn;

    @Value("${sentry.environment:development}")
    private String sentryEnvironment;

    @PostConstruct
    public void initializeMonitoring() {
        logger.info("Initializing monitoring for environment: {}", activeProfile);
        
        // Sentry 커스터마이징
        if (sentryDsn != null && !sentryDsn.trim().isEmpty()) {
            configureSentry();
            logger.info("Sentry monitoring initialized successfully");
        } else {
            logger.warn("Sentry DSN not configured - error tracking disabled");
        }
    }

    /**
     * CloudWatch 메트릭 레지스트리 구성
     */
    @Bean
    @Profile("!local")
    public CloudWatchMeterRegistry cloudWatchMeterRegistry() {
        CloudWatchConfig cloudWatchConfig = new CloudWatchConfig() {
            @Override
            public String get(String key) {
                return null; // 기본값 사용
            }

            @Override
            public String namespace() {
                return cloudwatchNamespace;
            }

            @Override
            public Duration step() {
                return step;
            }
        };

        return new CloudWatchMeterRegistry(
            cloudWatchConfig,
            Clock.SYSTEM,
            cloudWatchAsyncClient()
        );
    }

    /**
     * CloudWatch 비동기 클라이언트 구성
     */
    @Bean
    @Profile("!local")
    public CloudWatchAsyncClient cloudWatchAsyncClient() {
        return CloudWatchAsyncClient.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    /**
     * 공통 메트릭 태그 설정
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags(
                "application", applicationName,
                "environment", activeProfile,
                "region", awsRegion
            );
    }

    /**
     * 커스텀 메트릭 이름 변환 설정
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsNamingConvention() {
        return registry -> registry.config()
            .namingConvention(new CloudWatchNamingConvention());
    }

    /**
     * Sentry 설정 커스터마이징
     */
    private void configureSentry() {
        Sentry.configureScope(scope -> {
            // 기본 컨텍스트 정보 설정
            scope.setTag("environment", sentryEnvironment);
            scope.setTag("application", applicationName);
            scope.setTag("region", awsRegion);
            
            // 애플리케이션 컨텍스트
            scope.setContext("application", Map.of(
                "name", applicationName,
                "version", System.getProperty("app.version", "unknown"),
                "environment", sentryEnvironment
            ));
            
            // 서버 정보
            scope.setContext("server", Map.of(
                "region", awsRegion,
                "java_version", System.getProperty("java.version"),
                "os", System.getProperty("os.name")
            ));
        });

        // 커스텀 에러 필터링 및 샘플링
        Sentry.configureOptions(options -> {
            // PII 데이터 필터링
            options.setBeforeSend((event, hint) -> {
                if (event.getRequest() != null) {
                    event.getRequest().setData(null);
                    
                    if (event.getRequest().getHeaders() != null) {
                        event.getRequest().getHeaders().remove("authorization");
                        event.getRequest().getHeaders().remove("cookie");
                        event.getRequest().getHeaders().remove("x-api-key");
                    }
                }
                
                // 환경별 에러 레벨 필터링
                if ("development".equals(sentryEnvironment)) {
                    return event;
                } else {
                    if (event.getLevel() != null && 
                        (event.getLevel().ordinal() >= io.sentry.SentryLevel.ERROR.ordinal())) {
                        return event;
                    }
                    return null;
                }
            });
            
            // 성능 모니터링 샘플 레이트 설정
            if ("production".equals(sentryEnvironment)) {
                options.setTracesSampleRate(0.05); // 5% 샘플링
            } else if ("staging".equals(sentryEnvironment)) {
                options.setTracesSampleRate(0.1);  // 10% 샘플링
            } else {
                options.setTracesSampleRate(1.0);  // 개발환경은 100%
            }
            
            options.setRelease(applicationName + "@" + System.getProperty("app.version", "unknown"));
            options.setFingerprint(List.of("{{ default }}", "{{ error.type }}", "{{ error.value }}"));
        });
    }

    /**
     * Sentry 헬스 체크 지표
     */
    @Bean
    public HealthIndicator sentryHealthIndicator() {
        return () -> {
            try {
                if (sentryDsn != null && !sentryDsn.trim().isEmpty()) {
                    Sentry.captureMessage("Health check", io.sentry.SentryLevel.DEBUG);
                    return org.springframework.boot.actuate.health.Health.up()
                        .withDetail("sentry.environment", sentryEnvironment)
                        .withDetail("sentry.enabled", true)
                        .build();
                } else {
                    return org.springframework.boot.actuate.health.Health.down()
                        .withDetail("sentry.enabled", false)
                        .withDetail("reason", "DSN not configured")
                        .build();
                }
            } catch (Exception e) {
                return org.springframework.boot.actuate.health.Health.down()
                    .withDetail("sentry.error", e.getMessage())
                    .build();
            }
        };
    }

    /**
     * CloudWatch 헬스 체크 지표
     */
    @Bean
    @Profile({"staging", "production"})
    public HealthIndicator cloudWatchHealthIndicator() {
        return () -> {
            try {
                return org.springframework.boot.actuate.health.Health.up()
                    .withDetail("cloudwatch.namespace", cloudwatchNamespace)
                    .withDetail("cloudwatch.enabled", true)
                    .withDetail("cloudwatch.region", awsRegion)
                    .build();
            } catch (Exception e) {
                return org.springframework.boot.actuate.health.Health.down()
                    .withDetail("cloudwatch.error", e.getMessage())
                    .build();
            }
        };
    }

    /**
     * CloudWatch용 커스텀 네이밍 컨벤션
     */
    private static class CloudWatchNamingConvention implements io.micrometer.core.instrument.config.NamingConvention {
        @Override
        public String name(String name, io.micrometer.core.instrument.Meter.Type type, String baseUnit) {
            // 점(.)을 언더스코어(_)로 변환하여 CloudWatch와 호환성 개선
            return name.replace('.', '_');
        }

        @Override
        public String tagKey(String key) {
            return key.replace('.', '_');
        }

        @Override
        public String tagValue(String value) {
            return value;
        }
    }
}