package com.hanihome.api.scheduler;

import com.hanihome.api.service.PerformanceMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

@Slf4j
@Component
@RequiredArgsConstructor
public class PerformanceReportScheduler {

    private final PerformanceMonitoringService performanceMonitoringService;

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void recordMemoryUsage() {
        try {
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            
            long usedMemory = heapUsage.getUsed();
            long maxMemory = heapUsage.getMax();
            
            performanceMonitoringService.recordMemoryUsage(usedMemory, maxMemory);
            
            // Log if memory usage is high
            double usagePercentage = (double) usedMemory / maxMemory * 100;
            if (usagePercentage > 70) {
                log.warn("High memory usage: {:.2f}% ({} MB / {} MB)", 
                        usagePercentage, usedMemory / 1048576, maxMemory / 1048576);
            }
        } catch (Exception e) {
            log.error("Error recording memory usage", e);
        }
    }

    @Scheduled(cron = "0 0 * * * *") // Every hour
    public void generateHourlyPerformanceReport() {
        try {
            log.info("Generating hourly performance report...");
            performanceMonitoringService.generatePerformanceReport();
        } catch (Exception e) {
            log.error("Error generating performance report", e);
        }
    }

    @Scheduled(cron = "0 0 0 * * *") // Daily at midnight
    public void generateDailyPerformanceReport() {
        try {
            log.info("Generating daily performance report...");
            // This could generate a more comprehensive daily report
            performanceMonitoringService.generatePerformanceReport();
            
            // Could also send email reports, store in database, etc.
            generatePerformanceAlerts();
        } catch (Exception e) {
            log.error("Error generating daily performance report", e);
        }
    }

    @Scheduled(fixedRate = 60000) // Every minute
    public void monitorSystemHealth() {
        try {
            // Monitor thread count
            int activeThreads = Thread.activeCount();
            if (activeThreads > 200) {
                log.warn("High thread count detected: {} active threads", activeThreads);
            }

            // Monitor garbage collection
            long totalGCTime = ManagementFactory.getGarbageCollectorMXBeans().stream()
                    .mapToLong(gc -> gc.getCollectionTime())
                    .sum();
            
            // This is a simple check - in production you'd want more sophisticated GC monitoring
            if (totalGCTime > 10000) { // More than 10 seconds total GC time
                log.warn("High GC time detected: {} ms total collection time", totalGCTime);
            }

        } catch (Exception e) {
            log.error("Error monitoring system health", e);
        }
    }

    private void generatePerformanceAlerts() {
        // In a production system, this would:
        // 1. Check if any metrics exceed thresholds
        // 2. Send alerts via email, Slack, or other notification systems
        // 3. Create tickets in monitoring systems
        // 4. Log critical performance issues
        
        log.info("Performance alert check completed");
    }
}