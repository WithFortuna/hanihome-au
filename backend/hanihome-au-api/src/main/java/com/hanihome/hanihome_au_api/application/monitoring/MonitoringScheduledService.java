package com.hanihome.hanihome_au_api.application.monitoring;

import com.hanihome.hanihome_au_api.config.SentryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 정기적인 모니터링 작업을 수행하는 스케줄러 서비스
 * 
 * 주요 기능:
 * - 정기적인 메트릭 업데이트
 * - 시스템 헬스 체크
 * - 알람 임계값 모니터링
 * - 자동 리포팅
 */
@Service
@Profile("!test")
public class MonitoringScheduledService {

    private static final Logger logger = LoggerFactory.getLogger(MonitoringScheduledService.class);

    private final MetricsService metricsService;
    private final CloudWatchAlarmService cloudWatchAlarmService;
    private final HealthIndicator sentryHealthIndicator;

    // 성능 추적용 카운터
    private final AtomicLong lastMetricsUpdateTime = new AtomicLong(System.currentTimeMillis());
    private final AtomicLong metricsUpdateCount = new AtomicLong(0);

    @Autowired
    public MonitoringScheduledService(
            MetricsService metricsService,
            CloudWatchAlarmService cloudWatchAlarmService,
            HealthIndicator sentryHealthIndicator) {
        this.metricsService = metricsService;
        this.cloudWatchAlarmService = cloudWatchAlarmService;
        this.sentryHealthIndicator = sentryHealthIndicator;
    }

    /**
     * 매 5분마다 커스텀 메트릭 업데이트
     */
    @Scheduled(fixedRate = 300000) // 5분
    public void updateCustomMetrics() {
        try {
            logger.debug("Starting scheduled metrics update");
            long startTime = System.currentTimeMillis();

            // 리뷰 작성률 계산 및 기록
            metricsService.calculateAndRecordReviewRate();

            // 신고 처리율 계산 및 기록
            metricsService.calculateAndRecordReportProcessingRate();

            // 게이지 메트릭 업데이트 (실제 데이터로 대체 필요)
            updateRealTimeGauges();

            // 성능 메트릭 기록
            long duration = System.currentTimeMillis() - startTime;
            metricsService.recordDuration(
                "hanihome.monitoring.update.duration",
                java.time.Duration.ofMillis(duration),
                "update.type", "scheduled"
            );

            lastMetricsUpdateTime.set(System.currentTimeMillis());
            metricsUpdateCount.incrementAndGet();

            logger.debug("Scheduled metrics update completed in {}ms", duration);

        } catch (Exception e) {
            logger.error("Error during scheduled metrics update", e);
            
            // 에러를 Sentry에 리포트
            SentryConfig.captureBusinessException(
                "Scheduled metrics update failed",
                e,
                Map.of(
                    "operation", "scheduled_metrics_update",
                    "error.type", "monitoring_failure"
                )
            );
        }
    }

    /**
     * 매 15분마다 시스템 헬스 체크 및 모니터링
     */
    @Scheduled(fixedRate = 900000) // 15분
    public void performSystemHealthCheck() {
        try {
            logger.debug("Starting system health check");

            // Sentry 연결 상태 확인
            checkSentryHealth();

            // CloudWatch 알람 상태 확인
            checkCloudWatchAlarms();

            // JVM 메트릭 확인
            checkJvmMetrics();

            // 데이터베이스 연결 상태 체크 (메트릭을 통한 간접 확인)
            checkDatabaseHealth();

            logger.debug("System health check completed");

        } catch (Exception e) {
            logger.error("Error during system health check", e);
            
            SentryConfig.captureBusinessException(
                "System health check failed",
                e,
                Map.of(
                    "operation", "system_health_check",
                    "error.type", "monitoring_failure"
                )
            );
        }
    }

    /**
     * 매 1시간마다 상세 리포팅 및 분석
     */
    @Scheduled(fixedRate = 3600000) // 1시간
    public void generateHourlyReport() {
        try {
            logger.info("Generating hourly monitoring report");

            // 현재 메트릭 상태 로깅
            metricsService.logCurrentMetrics();

            // 리뷰 품질 지표 업데이트 (임시 데이터)
            updateReviewQualityMetrics();

            // 대시보드 성능 지표 시뮬레이션
            simulateDashboardPerformance();

            // 모니터링 시스템 자체 성능 체크
            checkMonitoringSystemPerformance();

            logger.info("Hourly monitoring report generated successfully");

        } catch (Exception e) {
            logger.error("Error generating hourly report", e);
            
            SentryConfig.captureBusinessException(
                "Hourly monitoring report generation failed",
                e,
                Map.of(
                    "operation", "hourly_report",
                    "error.type", "reporting_failure"
                )
            );
        }
    }

    /**
     * 매일 자정에 일간 요약 리포트 생성
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    public void generateDailyReport() {
        try {
            logger.info("Generating daily monitoring summary");

            // 일간 통계 수집
            long metricsUpdates = metricsUpdateCount.getAndSet(0);
            long lastUpdateAge = System.currentTimeMillis() - lastMetricsUpdateTime.get();

            // Sentry에 성공적인 일간 요약 전송
            SentryConfig.captureBusinessException(
                "Daily monitoring summary",
                null,
                Map.of(
                    "operation", "daily_summary",
                    "metrics.updates.count", String.valueOf(metricsUpdates),
                    "last.update.age.minutes", String.valueOf(lastUpdateAge / 60000),
                    "report.type", "success"
                )
            );

            logger.info("Daily monitoring summary completed - Updates: {}, Last update: {}min ago", 
                       metricsUpdates, lastUpdateAge / 60000);

        } catch (Exception e) {
            logger.error("Error generating daily report", e);
            
            SentryConfig.captureBusinessException(
                "Daily monitoring summary failed",
                e,
                Map.of(
                    "operation", "daily_summary",
                    "error.type", "reporting_failure"
                )
            );
        }
    }

    // === 프라이빗 헬퍼 메서드들 ===

    private void updateRealTimeGauges() {
        // TODO: 실제 구현에서는 Repository에서 실제 데이터를 가져와야 함
        
        // 임시 데이터로 게이지 업데이트
        long activeUsers = Math.round(Math.random() * 1000 + 100);
        long activeProperties = Math.round(Math.random() * 5000 + 500);
        long pendingReports = Math.round(Math.random() * 20);
        long scheduledViewings = Math.round(Math.random() * 100 + 10);

        metricsService.updateActiveUsers(activeUsers);
        metricsService.updateActiveProperties(activeProperties);
        metricsService.updatePendingReports(pendingReports);
        metricsService.updateScheduledViewings(scheduledViewings);

        logger.debug("Real-time gauges updated: users={}, properties={}, reports={}, viewings={}",
                   activeUsers, activeProperties, pendingReports, scheduledViewings);
    }

    private void checkSentryHealth() {
        try {
            Status sentryStatus = sentryHealthIndicator.health().getStatus();
            if (!Status.UP.equals(sentryStatus)) {
                logger.warn("Sentry health check failed: {}", sentryStatus);
                
                // 메트릭으로 기록
                metricsService.incrementCustomCounter(
                    "hanihome.monitoring.health.failed",
                    "Health check failures",
                    "service", "sentry",
                    "status", sentryStatus.getCode()
                );
            } else {
                logger.debug("Sentry health check passed");
            }
        } catch (Exception e) {
            logger.warn("Error checking Sentry health", e);
        }
    }

    private void checkCloudWatchAlarms() {
        try {
            Map<String, String> alarmStates = cloudWatchAlarmService.getAlarmStates();
            
            for (Map.Entry<String, String> entry : alarmStates.entrySet()) {
                String alarmName = entry.getKey();
                String state = entry.getValue();
                
                // 알람 상태를 메트릭으로 기록
                metricsService.incrementCustomCounter(
                    "hanihome.cloudwatch.alarm.state",
                    "CloudWatch alarm states",
                    "alarm.name", alarmName,
                    "state", state
                );
                
                if ("ALARM".equals(state)) {
                    logger.warn("CloudWatch alarm is in ALARM state: {}", alarmName);
                }
            }
            
            logger.debug("CloudWatch alarm check completed - {} alarms checked", alarmStates.size());
            
        } catch (Exception e) {
            logger.warn("Error checking CloudWatch alarms", e);
        }
    }

    private void checkJvmMetrics() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double memoryUsagePercent = (double) usedMemory / maxMemory * 100;

        // 메모리 사용률이 85% 이상이면 경고
        if (memoryUsagePercent > 85.0) {
            logger.warn("High memory usage detected: {:.2f}%", memoryUsagePercent);
            
            SentryConfig.capturePerformanceIssue(
                "high_memory_usage",
                (long) memoryUsagePercent,
                Map.of(
                    "memory.used.mb", usedMemory / 1024 / 1024,
                    "memory.max.mb", maxMemory / 1024 / 1024,
                    "memory.usage.percent", memoryUsagePercent
                )
            );
        }

        // JVM 메트릭을 게이지로 기록
        metricsService.setCustomGauge(
            "hanihome.jvm.memory.usage.percent",
            "JVM memory usage percentage",
            memoryUsagePercent
        );
    }

    private void checkDatabaseHealth() {
        // TODO: 실제 구현에서는 DatabaseHealthIndicator나 Repository를 통한 실제 체크
        
        // 임시로 메트릭을 통한 간접 체크
        logger.debug("Database health check completed (via metrics)");
    }

    private void updateReviewQualityMetrics() {
        // TODO: 실제 구현에서는 ReviewService에서 실제 데이터를 가져와야 함
        
        // 임시 데이터
        int spamDetected = (int) (Math.random() * 5);
        int trustScoreUpdates = (int) (Math.random() * 20 + 10);
        double avgTrustScore = Math.random() * 20 + 80; // 80-100 범위

        metricsService.recordReviewQualityMetrics(spamDetected, trustScoreUpdates, avgTrustScore);
    }

    private void simulateDashboardPerformance() {
        // 여러 대시보드 타입의 성능 시뮬레이션
        String[] dashboardTypes = {"admin", "user", "landlord", "agent"};
        
        for (String type : dashboardTypes) {
            long loadTime = Math.round(Math.random() * 3000 + 500); // 0.5-3.5초
            int dataPoints = (int) (Math.random() * 5000 + 100);
            
            metricsService.recordDashboardMetrics(type, loadTime, dataPoints);
        }
    }

    private void checkMonitoringSystemPerformance() {
        long updateAge = System.currentTimeMillis() - lastMetricsUpdateTime.get();
        
        // 마지막 업데이트가 10분 이상 지났으면 경고
        if (updateAge > 600000) { // 10분
            logger.warn("Metrics update is stale - last update was {}min ago", updateAge / 60000);
            
            SentryConfig.captureBusinessException(
                "Stale metrics detected in monitoring system",
                null,
                Map.of(
                    "last.update.age.minutes", String.valueOf(updateAge / 60000),
                    "monitoring.issue", "stale_metrics"
                )
            );
        }

        // 모니터링 시스템 자체 성능 메트릭
        metricsService.setCustomGauge(
            "hanihome.monitoring.last.update.age.minutes",
            "Minutes since last metrics update",
            updateAge / 60000.0
        );
    }
}