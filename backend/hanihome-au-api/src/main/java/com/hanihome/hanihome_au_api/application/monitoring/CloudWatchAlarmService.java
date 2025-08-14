package com.hanihome.hanihome_au_api.application.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.*;
import software.amazon.awssdk.services.sns.SnsClient;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CloudWatch 알람 설정 및 관리 서비스
 * 
 * 주요 기능:
 * - 커스텀 메트릭 알람 생성
 * - SNS 연동 알림 설정
 * - 임계값 모니터링
 */
@Service
@Profile({"staging", "production"})
public class CloudWatchAlarmService {

    private static final Logger logger = LoggerFactory.getLogger(CloudWatchAlarmService.class);

    private final CloudWatchClient cloudWatchClient;
    private final SnsClient snsClient;

    @Value("${management.metrics.export.cloudwatch.namespace:HaniHome/AU}")
    private String namespace;

    @Value("${aws.cloudwatch.sns-topic-arn:}")
    private String snsTopicArn;

    @Value("${spring.profiles.active:development}")
    private String environment;

    @Autowired
    public CloudWatchAlarmService(CloudWatchClient cloudWatchClient, SnsClient snsClient) {
        this.cloudWatchClient = cloudWatchClient;
        this.snsClient = snsClient;
    }

    @PostConstruct
    public void initializeAlarms() {
        if (snsTopicArn == null || snsTopicArn.trim().isEmpty()) {
            logger.warn("SNS Topic ARN not configured - CloudWatch alarms will be created without notifications");
        }

        try {
            createApplicationAlarms();
            logger.info("CloudWatch alarms initialized successfully for environment: {}", environment);
        } catch (Exception e) {
            logger.error("Failed to initialize CloudWatch alarms", e);
        }
    }

    private void createApplicationAlarms() {
        // 1. 높은 에러율 알람
        createHighErrorRateAlarm();
        
        // 2. 느린 응답 시간 알람
        createSlowResponseTimeAlarm();
        
        // 3. 높은 메모리 사용률 알람
        createHighMemoryUsageAlarm();
        
        // 4. 대기 중인 신고 수 알람
        createPendingReportsAlarm();
        
        // 5. 리뷰 작성률 저하 알람
        createLowReviewRateAlarm();
        
        // 6. 데이터베이스 연결 실패 알람
        createDatabaseConnectionAlarm();
    }

    private void createHighErrorRateAlarm() {
        try {
            PutMetricAlarmRequest request = PutMetricAlarmRequest.builder()
                .alarmName(environment + "-HaniHome-HighErrorRate")
                .alarmDescription("High error rate detected in HaniHome application")
                .metricName("http_server_requests")
                .namespace(namespace)
                .statistic(Statistic.SUM)
                .dimensions(Dimension.builder()
                    .name("status")
                    .value("5xx")
                    .build())
                .period(300) // 5분
                .evaluationPeriods(2)
                .threshold(10.0) // 5분간 10개 이상의 5xx 에러
                .comparisonOperator(ComparisonOperator.GREATER_THAN_THRESHOLD)
                .alarmActions(getAlarmActions())
                .treatMissingData("notBreaching")
                .build();

            cloudWatchClient.putMetricAlarm(request);
            logger.info("Created high error rate alarm: {}", request.alarmName());
        } catch (Exception e) {
            logger.error("Failed to create high error rate alarm", e);
        }
    }

    private void createSlowResponseTimeAlarm() {
        try {
            PutMetricAlarmRequest request = PutMetricAlarmRequest.builder()
                .alarmName(environment + "-HaniHome-SlowResponseTime")
                .alarmDescription("Slow response time detected in HaniHome application")
                .metricName("http_server_requests")
                .namespace(namespace)
                .statistic(Statistic.AVERAGE)
                .period(300) // 5분
                .evaluationPeriods(3)
                .threshold(5.0) // 5초 평균 응답시간
                .comparisonOperator(ComparisonOperator.GREATER_THAN_THRESHOLD)
                .alarmActions(getAlarmActions())
                .treatMissingData("notBreaching")
                .build();

            cloudWatchClient.putMetricAlarm(request);
            logger.info("Created slow response time alarm: {}", request.alarmName());
        } catch (Exception e) {
            logger.error("Failed to create slow response time alarm", e);
        }
    }

    private void createHighMemoryUsageAlarm() {
        try {
            PutMetricAlarmRequest request = PutMetricAlarmRequest.builder()
                .alarmName(environment + "-HaniHome-HighMemoryUsage")
                .alarmDescription("High memory usage detected in HaniHome application")
                .metricName("jvm_memory_used_bytes")
                .namespace(namespace)
                .statistic(Statistic.AVERAGE)
                .period(300) // 5분
                .evaluationPeriods(3)
                .threshold(0.85) // 85% 메모리 사용률
                .comparisonOperator(ComparisonOperator.GREATER_THAN_THRESHOLD)
                .alarmActions(getAlarmActions())
                .treatMissingData("notBreaching")
                .build();

            cloudWatchClient.putMetricAlarm(request);
            logger.info("Created high memory usage alarm: {}", request.alarmName());
        } catch (Exception e) {
            logger.error("Failed to create high memory usage alarm", e);
        }
    }

    private void createPendingReportsAlarm() {
        try {
            PutMetricAlarmRequest request = PutMetricAlarmRequest.builder()
                .alarmName(environment + "-HaniHome-PendingReports")
                .alarmDescription("High number of pending reports detected")
                .metricName("hanihome_reports_pending")
                .namespace(namespace)
                .statistic(Statistic.MAXIMUM)
                .period(900) // 15분
                .evaluationPeriods(2)
                .threshold(50.0) // 50개 이상의 대기 중인 신고
                .comparisonOperator(ComparisonOperator.GREATER_THAN_THRESHOLD)
                .alarmActions(getAlarmActions())
                .treatMissingData("notBreaching")
                .build();

            cloudWatchClient.putMetricAlarm(request);
            logger.info("Created pending reports alarm: {}", request.alarmName());
        } catch (Exception e) {
            logger.error("Failed to create pending reports alarm", e);
        }
    }

    private void createLowReviewRateAlarm() {
        try {
            PutMetricAlarmRequest request = PutMetricAlarmRequest.builder()
                .alarmName(environment + "-HaniHome-LowReviewRate")
                .alarmDescription("Low review submission rate detected")
                .metricName("hanihome_review_submitted")
                .namespace(namespace)
                .statistic(Statistic.SUM)
                .period(3600) // 1시간
                .evaluationPeriods(2)
                .threshold(5.0) // 1시간에 5개 미만의 리뷰
                .comparisonOperator(ComparisonOperator.LESS_THAN_THRESHOLD)
                .alarmActions(getAlarmActions())
                .treatMissingData("breaching") // 데이터가 없으면 알람
                .build();

            cloudWatchClient.putMetricAlarm(request);
            logger.info("Created low review rate alarm: {}", request.alarmName());
        } catch (Exception e) {
            logger.error("Failed to create low review rate alarm", e);
        }
    }

    private void createDatabaseConnectionAlarm() {
        try {
            PutMetricAlarmRequest request = PutMetricAlarmRequest.builder()
                .alarmName(environment + "-HaniHome-DatabaseConnection")
                .alarmDescription("Database connection issues detected")
                .metricName("hanihome_database_query_duration")
                .namespace(namespace)
                .statistic(Statistic.AVERAGE)
                .period(300) // 5분
                .evaluationPeriods(2)
                .threshold(10.0) // 10초 이상의 평균 쿼리 시간
                .comparisonOperator(ComparisonOperator.GREATER_THAN_THRESHOLD)
                .alarmActions(getAlarmActions())
                .treatMissingData("notBreaching")
                .build();

            cloudWatchClient.putMetricAlarm(request);
            logger.info("Created database connection alarm: {}", request.alarmName());
        } catch (Exception e) {
            logger.error("Failed to create database connection alarm", e);
        }
    }

    private List<String> getAlarmActions() {
        List<String> actions = new ArrayList<>();
        
        if (snsTopicArn != null && !snsTopicArn.trim().isEmpty()) {
            actions.add(snsTopicArn);
        }
        
        return actions;
    }

    /**
     * 동적으로 커스텀 알람 생성
     */
    public void createCustomAlarm(String alarmName, String metricName, double threshold, 
                                 ComparisonOperator operator, int evaluationPeriods) {
        try {
            PutMetricAlarmRequest request = PutMetricAlarmRequest.builder()
                .alarmName(environment + "-HaniHome-" + alarmName)
                .alarmDescription("Custom alarm: " + alarmName)
                .metricName(metricName)
                .namespace(namespace)
                .statistic(Statistic.AVERAGE)
                .period(300)
                .evaluationPeriods(evaluationPeriods)
                .threshold(threshold)
                .comparisonOperator(operator)
                .alarmActions(getAlarmActions())
                .treatMissingData("notBreaching")
                .build();

            cloudWatchClient.putMetricAlarm(request);
            logger.info("Created custom alarm: {}", request.alarmName());
        } catch (Exception e) {
            logger.error("Failed to create custom alarm: {}", alarmName, e);
        }
    }

    /**
     * 알람 상태 조회
     */
    public Map<String, String> getAlarmStates() {
        try {
            DescribeAlarmsRequest request = DescribeAlarmsRequest.builder()
                .alarmNamePrefix(environment + "-HaniHome-")
                .build();

            DescribeAlarmsResponse response = cloudWatchClient.describeAlarms(request);
            
            return response.metricAlarms().stream()
                .collect(java.util.stream.Collectors.toMap(
                    MetricAlarm::alarmName,
                    alarm -> alarm.stateValue().toString()
                ));
        } catch (Exception e) {
            logger.error("Failed to get alarm states", e);
            return Map.of();
        }
    }

    /**
     * 알람 삭제 (정리용)
     */
    public void deleteAlarm(String alarmName) {
        try {
            DeleteAlarmsRequest request = DeleteAlarmsRequest.builder()
                .alarmNames(environment + "-HaniHome-" + alarmName)
                .build();

            cloudWatchClient.deleteAlarms(request);
            logger.info("Deleted alarm: {}", alarmName);
        } catch (Exception e) {
            logger.error("Failed to delete alarm: {}", alarmName, e);
        }
    }
}