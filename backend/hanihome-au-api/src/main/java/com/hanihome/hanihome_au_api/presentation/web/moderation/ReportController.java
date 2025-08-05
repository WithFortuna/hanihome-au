package com.hanihome.hanihome_au_api.presentation.web.moderation;

import com.hanihome.hanihome_au_api.application.moderation.service.ReportService;
import com.hanihome.hanihome_au_api.domain.entity.Report;
import com.hanihome.hanihome_au_api.domain.entity.ReportAction;
import com.hanihome.hanihome_au_api.domain.enums.ReportStatus;
import com.hanihome.hanihome_au_api.domain.enums.ReportType;
import com.hanihome.hanihome_au_api.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@Tag(name = "Report Management", description = "신고 및 신고 처리 관리 API")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "신고 생성", description = "새로운 신고를 생성합니다.")
    public ResponseEntity<ApiResponse<Report>> createReport(
            @RequestBody CreateReportRequest request,
            Authentication authentication) {
        
        Long reporterId = getCurrentUserId(authentication);
        log.info("Creating report - Reporter: {}, Type: {}, Target: {}:{}", 
                 reporterId, request.getReportType(), request.getTargetType(), request.getTargetId());

        Report report = reportService.createReport(
                reporterId,
                request.getReportType(),
                request.getDescription(),
                request.getTargetType(),
                request.getTargetId(),
                request.getPriority(),
                request.getEvidenceUrls()
        );

        return ResponseEntity.ok(ApiResponse.success(
                "Report created successfully",
                report
        ));
    }

    @GetMapping
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN') or @securityExpressionHandler.hasRole('MODERATOR')")
    @Operation(summary = "신고 목록 조회", description = "신고 목록을 필터링하여 조회합니다.")
    public ResponseEntity<ApiResponse<Page<Report>>> getReports(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "desc") String sortDirection,
            @Parameter(description = "신고 유형") @RequestParam(required = false) ReportType reportType,
            @Parameter(description = "신고 상태") @RequestParam(required = false) ReportStatus status,
            @Parameter(description = "담당 관리자 ID") @RequestParam(required = false) Long assignedAdminId,
            @Parameter(description = "신고 대상 타입") @RequestParam(required = false) String targetType,
            @Parameter(description = "시작 날짜") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "종료 날짜") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        log.info("Fetching reports with filters - Type: {}, Status: {}, Admin: {}", reportType, status, assignedAdminId);

        Sort sort = Sort.by(sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Report> reports = reportService.getReports(reportType, status, assignedAdminId, targetType, startDate, endDate, pageable);

        return ResponseEntity.ok(ApiResponse.success(
                "Reports retrieved successfully",
                reports
        ));
    }

    @GetMapping("/{reportId}")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN') or @securityExpressionHandler.hasRole('MODERATOR') or @securityExpressionHandler.canAccessReport(#reportId)")
    @Operation(summary = "신고 상세 조회", description = "특정 신고의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<Report>> getReport(@PathVariable Long reportId) {
        log.info("Fetching report details for ID: {}", reportId);

        Optional<Report> report = reportService.getReportById(reportId);
        if (report.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(ApiResponse.success(
                "Report retrieved successfully",
                report.get()
        ));
    }

    @GetMapping("/{reportId}/actions")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN') or @securityExpressionHandler.hasRole('MODERATOR')")
    @Operation(summary = "신고 처리 이력 조회", description = "특정 신고의 처리 이력을 조회합니다.")
    public ResponseEntity<ApiResponse<List<ReportAction>>> getReportActions(@PathVariable Long reportId) {
        log.info("Fetching report actions for report ID: {}", reportId);

        List<ReportAction> actions = reportService.getReportActions(reportId);

        return ResponseEntity.ok(ApiResponse.success(
                "Report actions retrieved successfully",
                actions
        ));
    }

    @PostMapping("/{reportId}/assign")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN')")
    @Operation(summary = "신고 담당자 배정", description = "신고를 특정 관리자에게 배정합니다.")
    public ResponseEntity<ApiResponse<String>> assignReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, Long> request,
            Authentication authentication) {

        Long adminId = request.get("adminId");
        log.info("Assigning report {} to admin {} by {}", reportId, adminId, authentication.getName());

        reportService.assignReportToAdmin(reportId, adminId);

        return ResponseEntity.ok(ApiResponse.success(
                "Report assigned successfully",
                "Report " + reportId + " assigned to admin " + adminId
        ));
    }

    @PutMapping("/{reportId}/status")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN') or @securityExpressionHandler.hasRole('MODERATOR')")
    @Operation(summary = "신고 상태 변경", description = "신고의 상태를 변경합니다.")
    public ResponseEntity<ApiResponse<String>> updateReportStatus(
            @PathVariable Long reportId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        ReportStatus newStatus = ReportStatus.valueOf(request.get("status"));
        Long adminId = getCurrentUserId(authentication);
        log.info("Updating report {} status to {} by admin {}", reportId, newStatus, adminId);

        reportService.updateReportStatus(reportId, newStatus, adminId);

        return ResponseEntity.ok(ApiResponse.success(
                "Report status updated successfully",
                "Report " + reportId + " status updated to " + newStatus
        ));
    }

    @PostMapping("/{reportId}/notes")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN') or @securityExpressionHandler.hasRole('MODERATOR')")
    @Operation(summary = "관리자 메모 추가", description = "신고에 관리자 메모를 추가합니다.")
    public ResponseEntity<ApiResponse<String>> addAdminNotes(
            @PathVariable Long reportId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String notes = request.get("notes");
        Long adminId = getCurrentUserId(authentication);
        log.info("Adding admin notes to report {} by admin {}", reportId, adminId);

        reportService.addAdminNotes(reportId, notes, adminId);

        return ResponseEntity.ok(ApiResponse.success(
                "Admin notes added successfully",
                "Notes added to report " + reportId
        ));
    }

    @PostMapping("/{reportId}/resolve")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN') or @securityExpressionHandler.hasRole('MODERATOR')")
    @Operation(summary = "신고 해결", description = "신고를 해결 처리합니다.")
    public ResponseEntity<ApiResponse<String>> resolveReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String resolution = request.get("resolution");
        Long adminId = getCurrentUserId(authentication);
        log.info("Resolving report {} by admin {}", reportId, adminId);

        reportService.resolveReport(reportId, resolution, adminId);

        return ResponseEntity.ok(ApiResponse.success(
                "Report resolved successfully",
                "Report " + reportId + " has been resolved"
        ));
    }

    @PostMapping("/{reportId}/dismiss")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN') or @securityExpressionHandler.hasRole('MODERATOR')")
    @Operation(summary = "신고 기각", description = "신고를 기각 처리합니다.")
    public ResponseEntity<ApiResponse<String>> dismissReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String reason = request.get("reason");
        Long adminId = getCurrentUserId(authentication);
        log.info("Dismissing report {} by admin {}", reportId, adminId);

        reportService.dismissReport(reportId, reason, adminId);

        return ResponseEntity.ok(ApiResponse.success(
                "Report dismissed successfully",
                "Report " + reportId + " has been dismissed"
        ));
    }

    @PostMapping("/{reportId}/escalate")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN') or @securityExpressionHandler.hasRole('MODERATOR')")
    @Operation(summary = "신고 상급자 전달", description = "복잡한 신고를 상급자에게 전달합니다.")
    public ResponseEntity<ApiResponse<String>> escalateReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String reason = request.get("reason");
        Long adminId = getCurrentUserId(authentication);
        log.info("Escalating report {} by admin {}", reportId, adminId);

        reportService.escalateReport(reportId, adminId, reason);

        return ResponseEntity.ok(ApiResponse.success(
                "Report escalated successfully",
                "Report " + reportId + " has been escalated"
        ));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "내가 신고한 목록", description = "현재 사용자가 신고한 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<Report>>> getMyReports(Authentication authentication) {
        Long reporterId = getCurrentUserId(authentication);
        log.info("Fetching reports by user {}", reporterId);

        List<Report> reports = reportService.getReportsByReporter(reporterId);

        return ResponseEntity.ok(ApiResponse.success(
                "User reports retrieved successfully",
                reports
        ));
    }

    @GetMapping("/target/{targetType}/{targetId}")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN') or @securityExpressionHandler.hasRole('MODERATOR')")
    @Operation(summary = "특정 대상에 대한 신고 조회", description = "특정 대상(사용자, 매물 등)에 대한 모든 신고를 조회합니다.")
    public ResponseEntity<ApiResponse<List<Report>>> getReportsByTarget(
            @PathVariable String targetType,
            @PathVariable Long targetId) {
        
        log.info("Fetching reports for target {}:{}", targetType, targetId);

        List<Report> reports = reportService.getReportsByTarget(targetType, targetId);

        return ResponseEntity.ok(ApiResponse.success(
                "Target reports retrieved successfully",
                reports
        ));
    }

    @GetMapping("/assigned")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN') or @securityExpressionHandler.hasRole('MODERATOR')")
    @Operation(summary = "담당 신고 목록", description = "현재 관리자에게 배정된 신고 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<Report>>> getAssignedReports(Authentication authentication) {
        Long adminId = getCurrentUserId(authentication);
        log.info("Fetching reports assigned to admin {}", adminId);

        List<Report> reports = reportService.getReportsAssignedToAdmin(adminId);

        return ResponseEntity.ok(ApiResponse.success(
                "Assigned reports retrieved successfully",
                reports
        ));
    }

    @GetMapping("/unassigned")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN')")
    @Operation(summary = "미배정 신고 목록", description = "아직 담당자가 배정되지 않은 신고 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<Report>>> getUnassignedReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching unassigned reports");

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Report> reports = reportService.getUnassignedReports(pageable);

        return ResponseEntity.ok(ApiResponse.success(
                "Unassigned reports retrieved successfully",
                reports
        ));
    }

    @GetMapping("/high-priority")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN') or @securityExpressionHandler.hasRole('MODERATOR')")
    @Operation(summary = "고우선순위 신고 목록", description = "고우선순위 신고 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<Report>>> getHighPriorityReports() {
        log.info("Fetching high priority reports");

        List<Report> reports = reportService.getHighPriorityReports();

        return ResponseEntity.ok(ApiResponse.success(
                "High priority reports retrieved successfully",
                reports
        ));
    }

    @GetMapping("/overdue")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN')")
    @Operation(summary = "기한 초과 신고 목록", description = "처리 기한이 초과된 신고 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<Report>>> getOverdueReports() {
        log.info("Fetching overdue reports");

        List<Report> reports = reportService.getOverdueReports();

        return ResponseEntity.ok(ApiResponse.success(
                "Overdue reports retrieved successfully",
                reports
        ));
    }

    @GetMapping("/stats")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN')")
    @Operation(summary = "신고 통계", description = "신고 관련 통계를 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReportStatistics(
            @RequestParam(defaultValue = "30") int days) {
        
        log.info("Fetching report statistics for {} days", days);

        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        
        Map<String, Object> stats = Map.of(
                "totalReports", reportService.getReportCountInPeriod(startDate, LocalDateTime.now()),
                "typeStatistics", reportService.getReportTypeStatistics(startDate),
                "statusStatistics", reportService.getReportStatusStatistics(startDate),
                "dailyCounts", reportService.getDailyReportCounts(startDate),
                "averageResolutionTime", reportService.getAverageResolutionTimeInHours(startDate)
        );

        return ResponseEntity.ok(ApiResponse.success(
                "Report statistics retrieved successfully",
                stats
        ));
    }

    private Long getCurrentUserId(Authentication authentication) {
        // 실제 구현에서는 SecurityContext에서 사용자 ID를 추출
        // 현재는 플레이스홀더 반환
        return 1L;
    }

    // Request DTOs
    public static class CreateReportRequest {
        private ReportType reportType;
        private String description;
        private String targetType;
        private Long targetId;
        private Integer priority;
        private String evidenceUrls;

        // Getters and setters
        public ReportType getReportType() { return reportType; }
        public void setReportType(ReportType reportType) { this.reportType = reportType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getTargetType() { return targetType; }
        public void setTargetType(String targetType) { this.targetType = targetType; }
        public Long getTargetId() { return targetId; }
        public void setTargetId(Long targetId) { this.targetId = targetId; }
        public Integer getPriority() { return priority; }
        public void setPriority(Integer priority) { this.priority = priority; }
        public String getEvidenceUrls() { return evidenceUrls; }
        public void setEvidenceUrls(String evidenceUrls) { this.evidenceUrls = evidenceUrls; }
    }
}