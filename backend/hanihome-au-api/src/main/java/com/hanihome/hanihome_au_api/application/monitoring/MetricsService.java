package com.hanihome.hanihome_au_api.application.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 커스텀 메트릭 수집 및 관리를 위한 서비스
 * 
 * 주요 기능:
 * - 비즈니스 메트릭 수집 (리뷰 작성률, 신고 처리율 등)
 * - 성능 메트릭 모니터링
 * - 사용자 활동 추적
 */
@Service
public class MetricsService {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricsService.class);
    
    private final MeterRegistry meterRegistry;
    
    // 카운터 메트릭들
    private final Counter propertyViewCounter;
    private final Counter propertyCreatedCounter;
    private final Counter userRegistrationCounter;
    private final Counter reviewSubmittedCounter;
    private final Counter reportProcessedCounter;
    private final Counter searchRequestCounter;
    private final Counter favoriteAddedCounter;
    private final Counter viewingScheduledCounter;
    
    // 타이머 메트릭들
    private final Timer propertySearchDuration;
    private final Timer databaseQueryDuration;
    private final Timer externalApiCallDuration;
    
    // 게이지를 위한 저장소
    private final ConcurrentHashMap<String, AtomicLong> gaugeValues = new ConcurrentHashMap<>();

    @Autowired
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // 카운터 초기화
        this.propertyViewCounter = Counter.builder("hanihome.property.views")
            .description("Number of property views")
            .register(meterRegistry);
            
        this.propertyCreatedCounter = Counter.builder("hanihome.property.created")
            .description("Number of properties created")
            .register(meterRegistry);
            
        this.userRegistrationCounter = Counter.builder("hanihome.user.registrations")
            .description("Number of user registrations")
            .register(meterRegistry);
            
        this.reviewSubmittedCounter = Counter.builder("hanihome.review.submitted")
            .description("Number of reviews submitted")
            .register(meterRegistry);
            
        this.reportProcessedCounter = Counter.builder("hanihome.report.processed")
            .description("Number of reports processed")
            .register(meterRegistry);
            
        this.searchRequestCounter = Counter.builder("hanihome.search.requests")
            .description("Number of search requests")
            .register(meterRegistry);
            
        this.favoriteAddedCounter = Counter.builder("hanihome.favorite.added")
            .description("Number of favorites added")
            .register(meterRegistry);
            
        this.viewingScheduledCounter = Counter.builder("hanihome.viewing.scheduled")
            .description("Number of viewings scheduled")
            .register(meterRegistry);
        
        // 타이머 초기화
        this.propertySearchDuration = Timer.builder("hanihome.search.duration")
            .description("Property search execution time")
            .register(meterRegistry);
            
        this.databaseQueryDuration = Timer.builder("hanihome.database.query.duration")
            .description("Database query execution time")
            .register(meterRegistry);
            
        this.externalApiCallDuration = Timer.builder("hanihome.external.api.duration")
            .description("External API call duration")
            .register(meterRegistry);
        
        // 게이지 초기화
        initializeGauges();
    }
    
    private void initializeGauges() {
        // 활성 사용자 수
        gaugeValues.put("active.users", new AtomicLong(0));
        Gauge.builder("hanihome.users.active")
            .description("Number of active users")
            .register(meterRegistry, gaugeValues.get("active.users"), AtomicLong::get);
        
        // 활성 매물 수
        gaugeValues.put("active.properties", new AtomicLong(0));
        Gauge.builder("hanihome.properties.active")
            .description("Number of active properties")
            .register(meterRegistry, gaugeValues.get("active.properties"), AtomicLong::get);
        
        // 대기 중인 신고 수
        gaugeValues.put("pending.reports", new AtomicLong(0));
        Gauge.builder("hanihome.reports.pending")
            .description("Number of pending reports")
            .register(meterRegistry, gaugeValues.get("pending.reports"), AtomicLong::get);
            
        // 예정된 viewing 수
        gaugeValues.put("scheduled.viewings", new AtomicLong(0));
        Gauge.builder("hanihome.viewings.scheduled")
            .description("Number of scheduled viewings")
            .register(meterRegistry, gaugeValues.get("scheduled.viewings"), AtomicLong::get);
    }
    
    // === 카운터 메트릭 메서드들 ===
    
    public void incrementPropertyView(String propertyType, String location) {
        propertyViewCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "property.type", propertyType,
                "location", location
            )
        );
        logger.debug("Property view recorded: type={}, location={}", propertyType, location);
    }
    
    public void incrementPropertyCreated(String propertyType, String ownerRole) {
        propertyCreatedCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "property.type", propertyType,
                "owner.role", ownerRole
            )
        );
        logger.debug("Property creation recorded: type={}, owner={}", propertyType, ownerRole);
    }
    
    public void incrementUserRegistration(String userRole, String provider) {
        userRegistrationCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "user.role", userRole,
                "oauth.provider", provider
            )
        );
        logger.debug("User registration recorded: role={}, provider={}", userRole, provider);
    }
    
    public void incrementReviewSubmitted(String reviewType, int rating) {
        reviewSubmittedCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "review.type", reviewType,
                "rating.range", getRatingRange(rating)
            )
        );
        logger.debug("Review submission recorded: type={}, rating={}", reviewType, rating);
    }
    
    public void incrementReportProcessed(String reportType, String action, boolean resolved) {
        reportProcessedCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "report.type", reportType,
                "action", action,
                "resolved", String.valueOf(resolved)
            )
        );
        logger.debug("Report processing recorded: type={}, action={}, resolved={}", 
                   reportType, action, resolved);
    }
    
    public void incrementSearchRequest(String searchType, int resultCount) {
        searchRequestCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "search.type", searchType,
                "result.range", getResultCountRange(resultCount)
            )
        );
        logger.debug("Search request recorded: type={}, results={}", searchType, resultCount);
    }
    
    public void incrementFavoriteAdded(String propertyType, String userRole) {
        favoriteAddedCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "property.type", propertyType,
                "user.role", userRole
            )
        );
        logger.debug("Favorite addition recorded: property={}, user={}", propertyType, userRole);
    }
    
    public void incrementViewingScheduled(String propertyType, String timeSlot) {
        viewingScheduledCounter.increment(
            io.micrometer.core.instrument.Tags.of(
                "property.type", propertyType,
                "time.slot", timeSlot
            )
        );
        logger.debug("Viewing scheduling recorded: property={}, slot={}", propertyType, timeSlot);
    }
    
    // === 타이머 메트릭 메서드들 ===
    
    public Timer.Sample startPropertySearchTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordPropertySearchDuration(Timer.Sample sample, String searchType, boolean cached) {
        sample.stop(Timer.builder("hanihome.search.duration")
            .description("Property search execution time")
            .tags("search.type", searchType, "cached", String.valueOf(cached))
            .register(meterRegistry));
        logger.debug("Search duration recorded: type={}, cached={}", searchType, cached);
    }
    
    public Timer.Sample startDatabaseQueryTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordDatabaseQueryDuration(Timer.Sample sample, String queryType, String table) {
        sample.stop(Timer.builder("hanihome.database.query.duration")
            .description("Database query execution time")
            .tags("query.type", queryType, "table", table)
            .register(meterRegistry));
        logger.debug("Database query duration recorded: type={}, table={}", queryType, table);
    }
    
    public Timer.Sample startExternalApiCallTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void recordExternalApiCallDuration(Timer.Sample sample, String apiName, String operation, boolean success) {
        sample.stop(Timer.builder("hanihome.external.api.duration")
            .description("External API call duration")
            .tags("api.name", apiName, "operation", operation, "success", String.valueOf(success))
            .register(meterRegistry));
        logger.debug("External API call duration recorded: api={}, operation={}, success={}", 
                   apiName, operation, success);
    }
    
    public void recordDuration(String metricName, Duration duration, String... tags) {
        Timer.builder(metricName)
            .description("Custom duration metric")
            .tags(tags)
            .register(meterRegistry)
            .record(duration);
        logger.debug("Custom duration recorded: metric={}, duration={}ms", metricName, duration.toMillis());
    }
    
    // === 게이지 메트릭 메서드들 ===
    
    public void updateActiveUsers(long count) {
        gaugeValues.get("active.users").set(count);
        logger.debug("Active users updated: {}", count);
    }
    
    public void updateActiveProperties(long count) {
        gaugeValues.get("active.properties").set(count);
        logger.debug("Active properties updated: {}", count);
    }
    
    public void updatePendingReports(long count) {
        gaugeValues.get("pending.reports").set(count);
        logger.debug("Pending reports updated: {}", count);
    }
    
    public void updateScheduledViewings(long count) {
        gaugeValues.get("scheduled.viewings").set(count);
        logger.debug("Scheduled viewings updated: {}", count);
    }
    
    // === 유틸리티 메서드들 ===
    
    private String getRatingRange(int rating) {
        if (rating <= 2) return "low";
        if (rating <= 4) return "medium";
        return "high";
    }
    
    private String getResultCountRange(int count) {
        if (count == 0) return "empty";
        if (count <= 5) return "few";
        if (count <= 20) return "some";
        return "many";
    }
    
    /**
     * 커스텀 카운터 메트릭 생성 및 증가
     */
    public void incrementCustomCounter(String name, String description, String... tags) {
        Counter.builder(name)
            .description(description)
            .tags(tags)
            .register(meterRegistry)
            .increment();
        logger.debug("Custom counter incremented: {}", name);
    }
    
    /**
     * 커스텀 게이지 메트릭 설정
     */
    public void setCustomGauge(String name, String description, double value, String... tags) {
        Gauge.builder(name)
            .description(description)
            .tags(tags)
            .register(meterRegistry, value, v -> v);
        logger.debug("Custom gauge set: {} = {}", name, value);
    }
    
    /**
     * 현재 메트릭 상태 로깅 (디버그용)
     */
    public void logCurrentMetrics() {
        logger.info("=== Current Metrics State ===");
        logger.info("Property views: {}", propertyViewCounter.count());
        logger.info("Properties created: {}", propertyCreatedCounter.count());
        logger.info("User registrations: {}", userRegistrationCounter.count());
        logger.info("Reviews submitted: {}", reviewSubmittedCounter.count());
        logger.info("Reports processed: {}", reportProcessedCounter.count());
        logger.info("Search requests: {}", searchRequestCounter.count());
        logger.info("Active users: {}", gaugeValues.get("active.users").get());
        logger.info("Active properties: {}", gaugeValues.get("active.properties").get());
        logger.info("Pending reports: {}", gaugeValues.get("pending.reports").get());
        logger.info("==============================");
    }
}