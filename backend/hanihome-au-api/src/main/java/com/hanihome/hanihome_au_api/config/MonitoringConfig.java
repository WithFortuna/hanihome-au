package com.hanihome.hanihome_au_api.config;

import io.micrometer.cloudwatch2.CloudWatchConfig;
import io.micrometer.cloudwatch2.CloudWatchMeterRegistry;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;

import java.time.Duration;
import java.util.Map;

/**
 * CloudWatch 및 모니터링 설정을 담당하는 구성 클래스
 * 
 * 주요 기능:
 * - CloudWatch 메트릭 레지스트리 설정
 * - 커스텀 메트릭 태그 및 네이밍 설정
 * - 애플리케이션별 메트릭 구성
 */
@Configuration
@Profile("!test") // 테스트 환경에서는 비활성화
public class MonitoringConfig {

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