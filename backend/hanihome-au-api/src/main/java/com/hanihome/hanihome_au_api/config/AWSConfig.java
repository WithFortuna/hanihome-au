package com.hanihome.hanihome_au_api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.SnsAsyncClient;

/**
 * AWS 서비스 클라이언트 설정
 * 
 * 주요 기능:
 * - CloudWatch 클라이언트 설정
 * - SNS 클라이언트 설정 (알림용)
 * - AWS 리전 및 자격증명 관리
 */
@Configuration
@Profile("!test")
public class AWSConfig {

    @Value("${aws.region:ap-southeast-2}")
    private String awsRegion;

    /**
     * CloudWatch 동기 클라이언트
     */
    @Bean
    @Profile("!local")
    public CloudWatchClient cloudWatchClient() {
        return CloudWatchClient.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    /**
     * CloudWatch 비동기 클라이언트 (이미 MonitoringConfig에 있지만 여기서도 관리)
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
     * SNS 동기 클라이언트 (알림용)
     */
    @Bean
    @Profile("!local")
    public SnsClient snsClient() {
        return SnsClient.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }

    /**
     * SNS 비동기 클라이언트 (대량 알림용)
     */
    @Bean
    @Profile("!local")
    public SnsAsyncClient snsAsyncClient() {
        return SnsAsyncClient.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
    }
}