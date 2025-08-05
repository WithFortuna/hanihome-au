package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.Report;
import com.hanihome.hanihome_au_api.domain.enums.ReportStatus;
import com.hanihome.hanihome_au_api.domain.enums.ReportType;
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
public interface ReportRepository extends JpaRepository<Report, Long> {

    // Basic find methods
    List<Report> findByReporterIdOrderByCreatedAtDesc(Long reporterId);
    
    List<Report> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, Long targetId);
    
    Page<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);
    
    Page<Report> findByReportTypeOrderByCreatedAtDesc(ReportType reportType, Pageable pageable);
    
    List<Report> findByAssignedAdminIdOrderByCreatedAtDesc(Long adminId);

    // Advanced queries
    @Query("SELECT r FROM Report r WHERE r.status IN :statuses ORDER BY r.priority DESC, r.createdAt ASC")
    Page<Report> findByStatusInOrderByPriorityDescCreatedAtAsc(@Param("statuses") List<ReportStatus> statuses, Pageable pageable);

    @Query("SELECT r FROM Report r WHERE r.assignedAdminId IS NULL AND r.status = :status ORDER BY r.priority DESC, r.createdAt ASC")
    Page<Report> findUnassignedReports(@Param("status") ReportStatus status, Pageable pageable);

    @Query("SELECT r FROM Report r WHERE r.priority >= :priority AND r.status NOT IN :excludeStatuses ORDER BY r.createdAt ASC")
    List<Report> findHighPriorityReports(@Param("priority") Integer priority, @Param("excludeStatuses") List<ReportStatus> excludeStatuses);

    @Query("SELECT r FROM Report r WHERE r.createdAt < :threshold AND r.status NOT IN :completedStatuses")
    List<Report> findOverdueReports(@Param("threshold") LocalDateTime threshold, @Param("completedStatuses") List<ReportStatus> completedStatuses);

    @Query("SELECT r FROM Report r WHERE r.reporterId = :reporterId AND r.targetType = :targetType AND r.targetId = :targetId AND r.status NOT IN :excludeStatuses")
    Optional<Report> findExistingReport(@Param("reporterId") Long reporterId, 
                                       @Param("targetType") String targetType, 
                                       @Param("targetId") Long targetId, 
                                       @Param("excludeStatuses") List<ReportStatus> excludeStatuses);

    // Statistics queries
    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = :status")
    Long countByStatus(@Param("status") ReportStatus status);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.reportType = :type")
    Long countByReportType(@Param("type") ReportType type);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.createdAt >= :startDate AND r.createdAt < :endDate")
    Long countByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(r) FROM Report r WHERE r.assignedAdminId = :adminId AND r.status NOT IN :excludeStatuses")
    Long countActiveReportsByAdmin(@Param("adminId") Long adminId, @Param("excludeStatuses") List<ReportStatus> excludeStatuses);

    @Query("SELECT r.reportType, COUNT(r) FROM Report r WHERE r.createdAt >= :startDate GROUP BY r.reportType")
    List<Object[]> getReportTypeStatistics(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT r.status, COUNT(r) FROM Report r WHERE r.createdAt >= :startDate GROUP BY r.status")
    List<Object[]> getReportStatusStatistics(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT DATE(r.createdAt), COUNT(r) FROM Report r WHERE r.createdAt >= :startDate GROUP BY DATE(r.createdAt) ORDER BY DATE(r.createdAt)")
    List<Object[]> getDailyReportCounts(@Param("startDate") LocalDateTime startDate);

    // Duplicate detection
    @Query("SELECT COUNT(r) FROM Report r WHERE r.targetType = :targetType AND r.targetId = :targetId AND r.status NOT IN :excludeStatuses")
    Long countReportsForTarget(@Param("targetType") String targetType, 
                              @Param("targetId") Long targetId, 
                              @Param("excludeStatuses") List<ReportStatus> excludeStatuses);

    // Auto-moderation queries
    @Query("SELECT r FROM Report r WHERE r.autoModerated = false AND r.reportType IN :autoModeratedTypes AND r.status = :status")
    List<Report> findCandidatesForAutoModeration(@Param("autoModeratedTypes") List<ReportType> autoModeratedTypes, 
                                                 @Param("status") ReportStatus status);

    // Admin dashboard queries
    @Query("SELECT COUNT(r) FROM Report r WHERE r.assignedAdminId = :adminId AND r.status IN :activeStatuses")
    Long countAssignedActiveReports(@Param("adminId") Long adminId, @Param("activeStatuses") List<ReportStatus> activeStatuses);

    @Query("SELECT AVG(EXTRACT(EPOCH FROM (r.resolvedAt - r.createdAt))/3600) FROM Report r WHERE r.resolvedAt IS NOT NULL AND r.createdAt >= :startDate")
    Double getAverageResolutionTimeInHours(@Param("startDate") LocalDateTime startDate);

    // Search functionality
    @Query("SELECT r FROM Report r WHERE " +
           "(:reportType IS NULL OR r.reportType = :reportType) AND " +
           "(:status IS NULL OR r.status = :status) AND " +
           "(:assignedAdminId IS NULL OR r.assignedAdminId = :assignedAdminId) AND " +
           "(:targetType IS NULL OR r.targetType = :targetType) AND " +
           "(:startDate IS NULL OR r.createdAt >= :startDate) AND " +
           "(:endDate IS NULL OR r.createdAt <= :endDate) " +
           "ORDER BY r.priority DESC, r.createdAt DESC")
    Page<Report> searchReports(@Param("reportType") ReportType reportType,
                              @Param("status") ReportStatus status,
                              @Param("assignedAdminId") Long assignedAdminId,
                              @Param("targetType") String targetType,
                              @Param("startDate") LocalDateTime startDate,
                              @Param("endDate") LocalDateTime endDate,
                              Pageable pageable);
}