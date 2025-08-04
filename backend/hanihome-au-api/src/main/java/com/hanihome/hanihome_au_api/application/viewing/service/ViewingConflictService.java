package com.hanihome.hanihome_au_api.application.viewing.service;

import com.hanihome.hanihome_au_api.domain.entity.Viewing;
import com.hanihome.hanihome_au_api.domain.enums.ViewingStatus;
import com.hanihome.hanihome_au_api.repository.ViewingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.OptimisticLockException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViewingConflictService {

    private final ViewingRepository viewingRepository;
    
    // Property-level locks for concurrent booking prevention
    private final ConcurrentHashMap<Long, ReentrantLock> propertyLocks = new ConcurrentHashMap<>();
    
    /**
     * Check for time conflicts before creating a viewing
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public ConflictCheckResult checkTimeConflicts(Long propertyId, LocalDateTime startTime, 
                                                 LocalDateTime endTime, Long excludeViewingId) {
        log.debug("Checking conflicts for property {} from {} to {}", propertyId, startTime, endTime);
        
        List<Viewing> conflictingViewings = viewingRepository.findConflictingViewings(
                propertyId, startTime, endTime, excludeViewingId != null ? excludeViewingId : 0L);
        
        if (conflictingViewings.isEmpty()) {
            return ConflictCheckResult.noConflict();
        }
        
        // Categorize conflicts by status
        List<Viewing> activeConflicts = conflictingViewings.stream()
                .filter(v -> v.getStatus() == ViewingStatus.CONFIRMED || v.getStatus() == ViewingStatus.REQUESTED)
                .toList();
        
        if (activeConflicts.isEmpty()) {
            return ConflictCheckResult.noConflict();
        }
        
        return ConflictCheckResult.hasConflict(activeConflicts);
    }
    
    /**
     * Create viewing with conflict prevention using property-level locking
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Retryable(value = {OptimisticLockException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public Viewing createViewingWithConflictPrevention(Viewing viewing) {
        Long propertyId = viewing.getPropertyId();
        
        // Get or create property-specific lock
        ReentrantLock propertyLock = propertyLocks.computeIfAbsent(propertyId, k -> new ReentrantLock());
        
        propertyLock.lock();
        try {
            log.debug("Acquired lock for property {} to create viewing", propertyId);
            
            // Double-check for conflicts after acquiring lock
            LocalDateTime endTime = viewing.getScheduledAt().plusMinutes(viewing.getDurationMinutes());
            ConflictCheckResult conflictResult = checkTimeConflicts(
                    propertyId, viewing.getScheduledAt(), endTime, null);
            
            if (conflictResult.hasConflict()) {
                throw new ViewingConflictException("Time slot is no longer available", 
                        conflictResult.getConflictingViewings());
            }
            
            // Save the viewing
            Viewing savedViewing = viewingRepository.save(viewing);
            log.info("Successfully created viewing {} for property {} at {}", 
                    savedViewing.getId(), propertyId, viewing.getScheduledAt());
            
            return savedViewing;
            
        } finally {
            propertyLock.unlock();
            log.debug("Released lock for property {}", propertyId);
        }
    }
    
    /**
     * Update viewing with conflict prevention
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Retryable(value = {OptimisticLockException.class}, maxAttempts = 3, backoff = @Backoff(delay = 100))
    public Viewing updateViewingWithConflictPrevention(Viewing viewing, LocalDateTime newStartTime, 
                                                      Integer newDurationMinutes) {
        Long propertyId = viewing.getPropertyId();
        ReentrantLock propertyLock = propertyLocks.computeIfAbsent(propertyId, k -> new ReentrantLock());
        
        propertyLock.lock();
        try {
            log.debug("Acquired lock for property {} to update viewing {}", propertyId, viewing.getId());
            
            // Check for conflicts with the new time
            LocalDateTime newEndTime = newStartTime.plusMinutes(newDurationMinutes);
            ConflictCheckResult conflictResult = checkTimeConflicts(
                    propertyId, newStartTime, newEndTime, viewing.getId());
            
            if (conflictResult.hasConflict()) {
                throw new ViewingConflictException("New time slot conflicts with existing viewings", 
                        conflictResult.getConflictingViewings());
            }
            
            // Update the viewing
            viewing.reschedule(newStartTime);
            if (newDurationMinutes != null) {
                viewing.setDurationMinutes(newDurationMinutes);
            }
            
            Viewing savedViewing = viewingRepository.save(viewing);
            log.info("Successfully updated viewing {} for property {} to new time {}", 
                    viewing.getId(), propertyId, newStartTime);
            
            return savedViewing;
            
        } finally {
            propertyLock.unlock();
            log.debug("Released lock for property {}", propertyId);
        }
    }
    
    /**
     * Batch conflict check for multiple time slots
     */
    @Transactional(readOnly = true)
    public List<TimeSlotAvailability> checkMultipleTimeSlots(Long propertyId, 
                                                           List<TimeSlotRequest> timeSlots) {
        log.debug("Checking availability for {} time slots on property {}", timeSlots.size(), propertyId);
        
        return timeSlots.stream()
                .map(slot -> {
                    LocalDateTime endTime = slot.startTime().plusMinutes(slot.durationMinutes());
                    ConflictCheckResult result = checkTimeConflicts(propertyId, slot.startTime(), endTime, null);
                    
                    return new TimeSlotAvailability(
                            slot.startTime(),
                            slot.durationMinutes(),
                            !result.hasConflict(),
                            result.getConflictingViewings()
                    );
                })
                .toList();
    }
    
    /**
     * Clean up inactive property locks periodically
     */
    public void cleanupPropertyLocks() {
        propertyLocks.entrySet().removeIf(entry -> {
            ReentrantLock lock = entry.getValue();
            if (!lock.isLocked() && lock.getQueueLength() == 0) {
                return true; // Remove unused locks
            }
            return false;
        });
        log.debug("Cleaned up property locks. Remaining locks: {}", propertyLocks.size());
    }
    
    /**
     * Result of conflict checking
     */
    public static class ConflictCheckResult {
        private final boolean hasConflict;
        private final List<Viewing> conflictingViewings;
        
        private ConflictCheckResult(boolean hasConflict, List<Viewing> conflictingViewings) {
            this.hasConflict = hasConflict;
            this.conflictingViewings = conflictingViewings;
        }
        
        public static ConflictCheckResult noConflict() {
            return new ConflictCheckResult(false, List.of());
        }
        
        public static ConflictCheckResult hasConflict(List<Viewing> conflicts) {
            return new ConflictCheckResult(true, conflicts);
        }
        
        public boolean hasConflict() {
            return hasConflict;
        }
        
        public List<Viewing> getConflictingViewings() {
            return conflictingViewings;
        }
    }
    
    /**
     * Time slot request for batch checking
     */
    public record TimeSlotRequest(LocalDateTime startTime, Integer durationMinutes) {}
    
    /**
     * Time slot availability result
     */
    public record TimeSlotAvailability(
            LocalDateTime startTime,
            Integer durationMinutes,
            boolean available,
            List<Viewing> conflictingViewings
    ) {}
    
    /**
     * Custom exception for viewing conflicts
     */
    public static class ViewingConflictException extends RuntimeException {
        private final List<Viewing> conflictingViewings;
        
        public ViewingConflictException(String message, List<Viewing> conflictingViewings) {
            super(message);
            this.conflictingViewings = conflictingViewings;
        }
        
        public List<Viewing> getConflictingViewings() {
            return conflictingViewings;
        }
    }
}