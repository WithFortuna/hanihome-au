package com.hanihome.hanihome_au_api.application.viewing.service;

import com.hanihome.hanihome_au_api.domain.entity.Viewing;
import com.hanihome.hanihome_au_api.domain.enums.ViewingStatus;
import com.hanihome.hanihome_au_api.repository.ViewingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ViewingMaintenanceService {

    private final ViewingRepository viewingRepository;
    private final ViewingConflictService conflictService;

    /**
     * Process overdue viewings
     * Runs every 30 minutes to check for viewings that should be marked as completed or no-show
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // 30 minutes
    public void processOverdueViewings() {
        log.info("Starting processing of overdue viewings");
        
        try {
            LocalDateTime now = LocalDateTime.now();
            List<Viewing> overdueViewings = viewingRepository.findOverdueViewings(now);
            
            log.info("Found {} overdue viewings to process", overdueViewings.size());
            
            for (Viewing viewing : overdueViewings) {
                processOverdueViewing(viewing);
            }
            
            log.info("Completed processing of overdue viewings");
            
        } catch (Exception e) {
            log.error("Error processing overdue viewings", e);
        }
    }

    /**
     * Clean up property locks
     * Runs every hour to clean up unused locks
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // 1 hour
    public void cleanupPropertyLocks() {
        log.debug("Starting property lock cleanup");
        
        try {
            conflictService.cleanupPropertyLocks();
            log.debug("Completed property lock cleanup");
            
        } catch (Exception e) {
            log.error("Error during property lock cleanup", e);
        }
    }

    /**
     * Send viewing reminders
     * Runs every hour to check for viewings that need reminders
     */
    @Scheduled(fixedRate = 60 * 60 * 1000) // 1 hour
    public void sendViewingReminders() {
        log.debug("Starting viewing reminder check");
        
        try {
            LocalDateTime reminderStart = LocalDateTime.now().plusHours(23); // 23 hours from now
            LocalDateTime reminderEnd = LocalDateTime.now().plusHours(25);   // 25 hours from now
            
            List<Viewing> upcomingViewings = viewingRepository.findUpcomingConfirmedViewings(
                    reminderStart, reminderEnd);
            
            log.info("Found {} viewings that need 24-hour reminders", upcomingViewings.size());
            
            for (Viewing viewing : upcomingViewings) {
                sendViewingReminder(viewing);
            }
            
        } catch (Exception e) {
            log.error("Error sending viewing reminders", e);
        }
    }

    /**
     * Cancel expired requested viewings
     * Runs daily to clean up old viewing requests that were never confirmed
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void cancelExpiredRequests() {
        log.info("Starting cleanup of expired viewing requests");
        
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(7); // 7 days old
            
            List<Viewing> expiredRequests = viewingRepository.findByStatusOrderByScheduledAtAsc(ViewingStatus.REQUESTED)
                    .stream()
                    .filter(v -> v.getCreatedAt().isBefore(cutoffTime))
                    .toList();
            
            log.info("Found {} expired viewing requests to cancel", expiredRequests.size());
            
            for (Viewing viewing : expiredRequests) {
                viewing.cancel(null, "Automatically cancelled - no response from landlord");
                viewingRepository.save(viewing);
                log.debug("Cancelled expired viewing request: {}", viewing.getId());
            }
            
            log.info("Completed cleanup of expired viewing requests");
            
        } catch (Exception e) {
            log.error("Error cancelling expired requests", e);
        }
    }

    private void processOverdueViewing(Viewing viewing) {
        try {
            LocalDateTime scheduledEnd = viewing.getScheduledAt().plusMinutes(viewing.getDurationMinutes());
            LocalDateTime now = LocalDateTime.now();
            
            // If viewing was scheduled to end more than 2 hours ago, mark as no-show
            if (scheduledEnd.plusHours(2).isBefore(now)) {
                viewing.setStatus(ViewingStatus.NO_SHOW);
                viewingRepository.save(viewing);
                log.info("Marked viewing {} as NO_SHOW (scheduled: {}, ended: {})", 
                        viewing.getId(), viewing.getScheduledAt(), scheduledEnd);
                
                // Could trigger notification to landlord about no-show
                notifyNoShow(viewing);
                
            } else {
                // Just mark as completed for now - in reality, we might want landlord confirmation
                viewing.complete();
                viewingRepository.save(viewing);
                log.info("Marked viewing {} as COMPLETED (auto-completion)", viewing.getId());
            }
            
        } catch (Exception e) {
            log.error("Error processing overdue viewing {}: {}", viewing.getId(), e.getMessage());
        }
    }

    private void sendViewingReminder(Viewing viewing) {
        try {
            // This would integrate with notification service
            log.info("Sending 24-hour reminder for viewing {} scheduled at {}", 
                    viewing.getId(), viewing.getScheduledAt());
            
            // TODO: Integrate with notification service to send:
            // - Email reminder to tenant and landlord
            // - Push notification if available
            // - SMS reminder if phone numbers are available
            
        } catch (Exception e) {
            log.error("Error sending reminder for viewing {}: {}", viewing.getId(), e.getMessage());
        }
    }

    private void notifyNoShow(Viewing viewing) {
        try {
            log.info("Sending no-show notification for viewing {} to landlord {}", 
                    viewing.getId(), viewing.getLandlordUserId());
            
            // TODO: Integrate with notification service to inform landlord
            // about tenant no-show
            
        } catch (Exception e) {
            log.error("Error sending no-show notification for viewing {}: {}", 
                    viewing.getId(), e.getMessage());
        }
    }

    /**
     * Get viewing statistics for monitoring
     */
    @Transactional(readOnly = true)
    public ViewingStats getViewingStats() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        
        long totalViewings = viewingRepository.count();
        long monthlyRequests = viewingRepository.countViewingsByTenantInPeriod(null, startOfMonth, now);
        long confirmedViewings = viewingRepository.findByStatusOrderByScheduledAtAsc(ViewingStatus.CONFIRMED).size();
        long completedViewings = viewingRepository.findByStatusOrderByScheduledAtAsc(ViewingStatus.COMPLETED).size();
        long noShowViewings = viewingRepository.findByStatusOrderByScheduledAtAsc(ViewingStatus.NO_SHOW).size();
        
        return ViewingStats.builder()
                .totalViewings(totalViewings)
                .monthlyRequests(monthlyRequests)
                .confirmedViewings(confirmedViewings)
                .completedViewings(completedViewings)
                .noShowViewings(noShowViewings)
                .noShowRate(completedViewings > 0 ? (double) noShowViewings / completedViewings : 0.0)
                .build();
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ViewingStats {
        private long totalViewings;
        private long monthlyRequests;
        private long confirmedViewings;
        private long completedViewings;
        private long noShowViewings;
        private double noShowRate;
    }
}