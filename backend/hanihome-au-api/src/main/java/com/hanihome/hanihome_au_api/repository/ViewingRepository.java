package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.Viewing;
import com.hanihome.hanihome_au_api.domain.enums.ViewingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ViewingRepository extends JpaRepository<Viewing, Long> {

    /**
     * Find viewings by tenant user ID
     */
    Page<Viewing> findByTenantUserIdOrderByScheduledAtDesc(Long tenantUserId, Pageable pageable);

    /**
     * Find viewings by landlord user ID
     */
    Page<Viewing> findByLandlordUserIdOrderByScheduledAtDesc(Long landlordUserId, Pageable pageable);

    /**
     * Find viewings by agent user ID
     */
    Page<Viewing> findByAgentUserIdOrderByScheduledAtDesc(Long agentUserId, Pageable pageable);

    /**
     * Find viewings by property ID
     */
    Page<Viewing> findByPropertyIdOrderByScheduledAtDesc(Long propertyId, Pageable pageable);

    /**
     * Find viewings by status
     */
    List<Viewing> findByStatusOrderByScheduledAtAsc(ViewingStatus status);

    /**
     * Find viewings by multiple statuses
     */
    List<Viewing> findByStatusInOrderByScheduledAtAsc(List<ViewingStatus> statuses);

    /**
     * Find active viewings for a property within a time range
     */
    @Query("SELECT v FROM Viewing v WHERE v.propertyId = :propertyId " +
           "AND v.status IN ('REQUESTED', 'CONFIRMED') " +
           "AND v.scheduledAt < :endTime " +
           "AND v.scheduledAt + INTERVAL v.durationMinutes MINUTE > :startTime")
    List<Viewing> findActiveViewingsInTimeRange(@Param("propertyId") Long propertyId,
                                               @Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * Find conflicting viewings for a property at a specific time
     */
    @Query("SELECT v FROM Viewing v WHERE v.propertyId = :propertyId " +
           "AND v.status IN ('REQUESTED', 'CONFIRMED') " +
           "AND v.id != :excludeViewingId " +
           "AND v.scheduledAt < :endTime " +
           "AND v.scheduledAt + INTERVAL v.durationMinutes MINUTE > :startTime")
    List<Viewing> findConflictingViewings(@Param("propertyId") Long propertyId,
                                         @Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime,
                                         @Param("excludeViewingId") Long excludeViewingId);

    /**
     * Find upcoming viewings for reminders
     */
    @Query("SELECT v FROM Viewing v WHERE v.status = 'CONFIRMED' " +
           "AND v.scheduledAt BETWEEN :startTime AND :endTime")
    List<Viewing> findUpcomingConfirmedViewings(@Param("startTime") LocalDateTime startTime,
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * Find overdue viewings that need to be marked as completed or no-show
     */
    @Query("SELECT v FROM Viewing v WHERE v.status = 'CONFIRMED' " +
           "AND v.scheduledAt + INTERVAL v.durationMinutes MINUTE < :currentTime")
    List<Viewing> findOverdueViewings(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Count viewings by tenant in a specific time period
     */
    @Query("SELECT COUNT(v) FROM Viewing v WHERE v.tenantUserId = :tenantUserId " +
           "AND v.createdAt BETWEEN :startDate AND :endDate")
    long countViewingsByTenantInPeriod(@Param("tenantUserId") Long tenantUserId,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Count viewings by landlord in a specific time period
     */
    @Query("SELECT COUNT(v) FROM Viewing v WHERE v.landlordUserId = :landlordUserId " +
           "AND v.createdAt BETWEEN :startDate AND :endDate")
    long countViewingsByLandlordInPeriod(@Param("landlordUserId") Long landlordUserId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Find viewings that can provide feedback (completed viewings without feedback)
     */
    @Query("SELECT v FROM Viewing v WHERE v.tenantUserId = :tenantUserId " +
           "AND v.status = 'COMPLETED' " +
           "AND v.feedbackRating IS NULL " +
           "ORDER BY v.completedAt DESC")
    List<Viewing> findViewingsAwaitingFeedback(@Param("tenantUserId") Long tenantUserId);

    /**
     * Find recent viewings for a property (for showing to potential tenants)
     */
    @Query("SELECT v FROM Viewing v WHERE v.propertyId = :propertyId " +
           "AND v.status = 'COMPLETED' " +
           "AND v.feedbackRating IS NOT NULL " +
           "ORDER BY v.completedAt DESC")
    List<Viewing> findRecentViewingsWithFeedback(@Param("propertyId") Long propertyId, Pageable pageable);

    /**
     * Check if user has already requested viewing for a property
     */
    @Query("SELECT v FROM Viewing v WHERE v.propertyId = :propertyId " +
           "AND v.tenantUserId = :tenantUserId " +
           "AND v.status IN ('REQUESTED', 'CONFIRMED') " +
           "ORDER BY v.scheduledAt DESC")
    List<Viewing> findActiveViewingsByTenantAndProperty(@Param("propertyId") Long propertyId,
                                                       @Param("tenantUserId") Long tenantUserId);

    /**
     * Find viewings by date range for calendar display
     */
    @Query("SELECT v FROM Viewing v WHERE v.landlordUserId = :landlordUserId " +
           "AND v.scheduledAt BETWEEN :startDate AND :endDate " +
           "AND v.status IN ('REQUESTED', 'CONFIRMED') " +
           "ORDER BY v.scheduledAt ASC")
    List<Viewing> findViewingsInDateRange(@Param("landlordUserId") Long landlordUserId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
}