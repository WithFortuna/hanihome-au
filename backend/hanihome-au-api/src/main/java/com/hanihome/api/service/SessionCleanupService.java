package com.hanihome.api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionCleanupService {

    private final SessionManagementService sessionManagementService;

    /**
     * Clean up expired sessions every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour = 3,600,000 milliseconds
    public void cleanupExpiredSessions() {
        try {
            log.info("Starting scheduled session cleanup");
            sessionManagementService.cleanupExpiredSessions();
            log.info("Scheduled session cleanup completed successfully");
        } catch (Exception e) {
            log.error("Error during scheduled session cleanup", e);
        }
    }

    /**
     * Log session statistics every 30 minutes for monitoring
     */
    @Scheduled(fixedRate = 1800000) // 30 minutes = 1,800,000 milliseconds
    public void logSessionStatistics() {
        try {
            var stats = sessionManagementService.getSessionStatistics();
            log.info("Session Statistics: Active Users: {}, Total Sessions: {}, Active Sessions: {}", 
                stats.get("activeUsers"), stats.get("totalSessions"), stats.get("activeSessions"));
        } catch (Exception e) {
            log.error("Error logging session statistics", e);
        }
    }
}