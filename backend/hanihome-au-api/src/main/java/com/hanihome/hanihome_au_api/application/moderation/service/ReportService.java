package com.hanihome.hanihome_au_api.application.moderation.service;

import com.hanihome.hanihome_au_api.domain.entity.Report;
import com.hanihome.hanihome_au_api.domain.entity.ReportAction;
import com.hanihome.hanihome_au_api.domain.enums.ReportStatus;
import com.hanihome.hanihome_au_api.domain.enums.ReportType;
import com.hanihome.hanihome_au_api.repository.ReportActionRepository;
import com.hanihome.hanihome_au_api.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportActionRepository reportActionRepository;
    private final AutoModerationService autoModerationService;

    @Transactional
    public Report createReport(Long reporterId, ReportType reportType, String description, 
                              String targetType, Long targetId, Integer priority, String evidenceUrls) {
        log.info("Creating new report - Reporter: {}, Type: {}, Target: {}:{}", 
                 reporterId, reportType, targetType, targetId);

        // Check for duplicate reports
        List<ReportStatus> excludeStatuses = Arrays.asList(ReportStatus.DISMISSED, ReportStatus.DUPLICATE);
        Optional<Report> existingReport = reportRepository.findExistingReport(reporterId, targetType, targetId, excludeStatuses);
        
        if (existingReport.isPresent()) {
            log.info("Duplicate report detected for Reporter: {}, Target: {}:{}", reporterId, targetType, targetId);
            return existingReport.get();
        }

        Report report = Report.builder()
                .reporterId(reporterId)
                .reportType(reportType)
                .description(description)
                .targetType(targetType)
                .targetId(targetId)
                .priority(priority)
                .evidenceUrls(evidenceUrls)
                .build();

        Report savedReport = reportRepository.save(report);

        // Create initial action record
        ReportAction initialAction = ReportAction.builder()
                .report(savedReport)
                .actionType(ReportAction.ActionType.CREATED)
                .description("Report created")
                .performedBy(reporterId)
                .build();
        
        reportActionRepository.save(initialAction);

        // Trigger auto-moderation if applicable
        autoModerationService.processReportForAutoModeration(savedReport);

        log.info("Report created successfully with ID: {}", savedReport.getId());
        return savedReport;
    }

    public Page<Report> getReports(ReportType reportType, ReportStatus status, Long assignedAdminId, 
                                  String targetType, LocalDateTime startDate, LocalDateTime endDate, 
                                  Pageable pageable) {
        log.info("Fetching reports with filters - Type: {}, Status: {}, Admin: {}", reportType, status, assignedAdminId);
        
        return reportRepository.searchReports(reportType, status, assignedAdminId, targetType, 
                                            startDate, endDate, pageable);
    }

    public Optional<Report> getReportById(Long reportId) {
        return reportRepository.findById(reportId);
    }

    public List<Report> getReportsByReporter(Long reporterId) {
        return reportRepository.findByReporterIdOrderByCreatedAtDesc(reporterId);
    }

    public List<Report> getReportsByTarget(String targetType, Long targetId) {
        return reportRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId);
    }

    public List<Report> getReportsAssignedToAdmin(Long adminId) {
        return reportRepository.findByAssignedAdminIdOrderByCreatedAtDesc(adminId);
    }

    public Page<Report> getUnassignedReports(Pageable pageable) {
        return reportRepository.findUnassignedReports(ReportStatus.PENDING, pageable);
    }

    public List<Report> getHighPriorityReports() {
        List<ReportStatus> excludeStatuses = Arrays.asList(
            ReportStatus.RESOLVED, ReportStatus.DISMISSED, ReportStatus.DUPLICATE
        );
        return reportRepository.findHighPriorityReports(3, excludeStatuses);
    }

    public List<Report> getOverdueReports() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(72);
        List<ReportStatus> completedStatuses = Arrays.asList(
            ReportStatus.RESOLVED, ReportStatus.DISMISSED, 
            ReportStatus.CONTENT_REMOVED, ReportStatus.USER_SUSPENDED, 
            ReportStatus.ACCOUNT_BANNED
        );
        return reportRepository.findOverdueReports(threshold, completedStatuses);
    }

    @Transactional
    public void assignReportToAdmin(Long reportId, Long adminId) {
        log.info("Assigning report {} to admin {}", reportId, adminId);
        
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        
        report.assignToAdmin(adminId);
        reportRepository.save(report);
        
        log.info("Report {} assigned to admin {} successfully", reportId, adminId);
    }

    @Transactional
    public void updateReportStatus(Long reportId, ReportStatus newStatus, Long adminId) {
        log.info("Updating report {} status to {} by admin {}", reportId, newStatus, adminId);
        
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        
        report.updateStatus(newStatus);
        reportRepository.save(report);
        
        log.info("Report {} status updated to {} successfully", reportId, newStatus);
    }

    @Transactional
    public void addAdminNotes(Long reportId, String notes, Long adminId) {
        log.info("Adding admin notes to report {} by admin {}", reportId, adminId);
        
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        
        report.addAdminNotes(notes, adminId);
        reportRepository.save(report);
        
        log.info("Admin notes added to report {} successfully", reportId);
    }

    @Transactional
    public void resolveReport(Long reportId, String resolution, Long adminId) {
        log.info("Resolving report {} by admin {}", reportId, adminId);
        
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        
        report.resolve(resolution, adminId);
        reportRepository.save(report);
        
        log.info("Report {} resolved successfully", reportId);
    }

    @Transactional
    public void dismissReport(Long reportId, String reason, Long adminId) {
        log.info("Dismissing report {} by admin {}", reportId, adminId);
        
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        
        report.dismiss(reason, adminId);
        reportRepository.save(report);
        
        log.info("Report {} dismissed successfully", reportId);
    }

    @Transactional
    public void escalateReport(Long reportId, Long adminId, String reason) {
        log.info("Escalating report {} by admin {}", reportId, adminId);
        
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        
        report.escalate(adminId, reason);
        reportRepository.save(report);
        
        log.info("Report {} escalated successfully", reportId);
    }

    public List<ReportAction> getReportActions(Long reportId) {
        return reportActionRepository.findByReportIdOrderByCreatedAtDesc(reportId);
    }

    public Long getReportCount(String targetType, Long targetId) {
        List<ReportStatus> excludeStatuses = Arrays.asList(ReportStatus.DISMISSED, ReportStatus.DUPLICATE);
        return reportRepository.countReportsForTarget(targetType, targetId, excludeStatuses);
    }

    public boolean hasUserReportedTarget(Long reporterId, String targetType, Long targetId) {
        List<ReportStatus> excludeStatuses = Arrays.asList(ReportStatus.DISMISSED, ReportStatus.DUPLICATE);
        return reportRepository.findExistingReport(reporterId, targetType, targetId, excludeStatuses).isPresent();
    }

    // Statistics methods
    public Long getReportCountByStatus(ReportStatus status) {
        return reportRepository.countByStatus(status);
    }

    public Long getReportCountByType(ReportType type) {
        return reportRepository.countByReportType(type);
    }

    public Long getReportCountInPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return reportRepository.countByCreatedAtBetween(startDate, endDate);
    }

    public List<Object[]> getReportTypeStatistics(LocalDateTime startDate) {
        return reportRepository.getReportTypeStatistics(startDate);
    }

    public List<Object[]> getReportStatusStatistics(LocalDateTime startDate) {
        return reportRepository.getReportStatusStatistics(startDate);
    }

    public List<Object[]> getDailyReportCounts(LocalDateTime startDate) {
        return reportRepository.getDailyReportCounts(startDate);
    }

    public Double getAverageResolutionTimeInHours(LocalDateTime startDate) {
        return reportRepository.getAverageResolutionTimeInHours(startDate);
    }

    public Long getActiveReportsCountByAdmin(Long adminId) {
        List<ReportStatus> excludeStatuses = Arrays.asList(
            ReportStatus.RESOLVED, ReportStatus.DISMISSED, 
            ReportStatus.CONTENT_REMOVED, ReportStatus.USER_SUSPENDED, 
            ReportStatus.ACCOUNT_BANNED
        );
        return reportRepository.countActiveReportsByAdmin(adminId, excludeStatuses);
    }
}