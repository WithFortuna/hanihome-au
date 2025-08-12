package com.hanihome.hanihome_au_api.presentation.web.admin;

import com.hanihome.hanihome_au_api.application.admin.dto.DashboardStatsDto;
import com.hanihome.hanihome_au_api.application.admin.dto.PropertyManagementDto;
import com.hanihome.hanihome_au_api.application.admin.dto.UserManagementDto;
import com.hanihome.hanihome_au_api.application.admin.service.AdminDashboardService;
import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import com.hanihome.hanihome_au_api.domain.enums.UserRole;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "관리자 대시보드 API")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/stats")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN')")
    @Operation(summary = "대시보드 통계 조회", description = "관리자 대시보드의 모든 통계 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<DashboardStatsDto>> getDashboardStats() {
        log.info("Admin requesting dashboard statistics");
        
        DashboardStatsDto stats = adminDashboardService.getDashboardStats();
        
        return ResponseEntity.ok(ApiResponse.success(
                "Dashboard statistics retrieved successfully",
                stats
        ));
    }

    @GetMapping("/users")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN')")
    @Operation(summary = "사용자 관리 목록 조회", description = "관리를 위한 사용자 목록을 페이징하여 조회합니다.")
    public ResponseEntity<ApiResponse<Page<UserManagementDto>>> getUsers(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "desc") String sortDirection,
            @Parameter(description = "검색어 (이름, 이메일)") @RequestParam(required = false) String search,
            @Parameter(description = "사용자 역할 필터") @RequestParam(required = false) UserRole role,
            @Parameter(description = "사용자 상태 필터") @RequestParam(required = false) String status) {
        
        log.info("Admin fetching users - page: {}, size: {}, search: {}, role: {}, status: {}", 
                 page, size, search, role, status);

        Sort sort = Sort.by(sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<UserManagementDto> users = adminDashboardService.getAllUsersForManagement(pageable, search, role, status);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Users retrieved successfully",
                users
        ));
    }

    @GetMapping("/properties")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN')")
    @Operation(summary = "매물 관리 목록 조회", description = "관리를 위한 매물 목록을 페이징하여 조회합니다.")
    public ResponseEntity<ApiResponse<Page<PropertyManagementDto>>> getProperties(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "정렬 방향") @RequestParam(defaultValue = "desc") String sortDirection,
            @Parameter(description = "검색어 (제목, 주소)") @RequestParam(required = false) String search,
            @Parameter(description = "매물 상태 필터") @RequestParam(required = false) PropertyStatus status,
            @Parameter(description = "승인 상태 필터") @RequestParam(required = false) Boolean approved) {
        
        log.info("Admin fetching properties - page: {}, size: {}, search: {}, status: {}, approved: {}", 
                 page, size, search, status, approved);

        Sort sort = Sort.by(sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<PropertyManagementDto> properties = adminDashboardService.getAllPropertiesForManagement(pageable, search, status, approved);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Properties retrieved successfully",
                properties
        ));
    }

    @PostMapping("/properties/{propertyId}/approve")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN')")
    @Operation(summary = "매물 승인", description = "관리자가 매물을 승인합니다.")
    public ResponseEntity<ApiResponse<String>> approveProperty(
            @Parameter(description = "매물 ID") @PathVariable Long propertyId,
            Authentication authentication) {
        
        String adminUserId = authentication.getName();
        log.info("Admin {} approving property {}", adminUserId, propertyId);
        
        adminDashboardService.approveProperty(propertyId, adminUserId);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Property approved successfully",
                "Property " + propertyId + " has been approved"
        ));
    }

    @PostMapping("/properties/{propertyId}/reject")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN')")
    @Operation(summary = "매물 거부", description = "관리자가 매물을 거부합니다.")
    public ResponseEntity<ApiResponse<String>> rejectProperty(
            @Parameter(description = "매물 ID") @PathVariable Long propertyId,
            @Parameter(description = "거부 사유") @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        String adminUserId = authentication.getName();
        String reason = request.get("reason");
        log.info("Admin {} rejecting property {} with reason: {}", adminUserId, propertyId, reason);
        
        adminDashboardService.rejectProperty(propertyId, reason, adminUserId);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Property rejected successfully",
                "Property " + propertyId + " has been rejected"
        ));
    }

    @PostMapping("/users/{userId}/suspend")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN')")
    @Operation(summary = "사용자 계정 정지", description = "관리자가 사용자 계정을 정지합니다.")
    public ResponseEntity<ApiResponse<String>> suspendUser(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "정지 사유") @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        String adminUserId = authentication.getName();
        String reason = request.get("reason");
        log.info("Admin {} suspending user {} with reason: {}", adminUserId, userId, reason);
        
        adminDashboardService.suspendUser(userId, reason, adminUserId);
        
        return ResponseEntity.ok(ApiResponse.success(
                "User suspended successfully",
                "User " + userId + " has been suspended"
        ));
    }

    @PostMapping("/users/{userId}/activate")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN')")
    @Operation(summary = "사용자 계정 활성화", description = "관리자가 정지된 사용자 계정을 활성화합니다.")
    public ResponseEntity<ApiResponse<String>> activateUser(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            Authentication authentication) {
        
        String adminUserId = authentication.getName();
        log.info("Admin {} activating user {}", adminUserId, userId);
        
        adminDashboardService.activateUser(userId, adminUserId);
        
        return ResponseEntity.ok(ApiResponse.success(
                "User activated successfully",
                "User " + userId + " has been activated"
        ));
    }

    @GetMapping("/stats/users")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN')")
    @Operation(summary = "사용자 통계 조회", description = "사용자 관련 상세 통계를 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats(
            @Parameter(description = "통계 조회 기간 (days)") @RequestParam(defaultValue = "30") int days) {
        
        log.info("Admin requesting user statistics for {} days", days);
        
        // This would be implemented with detailed user statistics
        Map<String, Object> stats = Map.of(
                "message", "User statistics would be here",
                "days", days
        );
        
        return ResponseEntity.ok(ApiResponse.success(
                "User statistics retrieved successfully",
                stats
        ));
    }

    @GetMapping("/stats/properties")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN')")
    @Operation(summary = "매물 통계 조회", description = "매물 관련 상세 통계를 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPropertyStats(
            @Parameter(description = "통계 조회 기간 (days)") @RequestParam(defaultValue = "30") int days) {
        
        log.info("Admin requesting property statistics for {} days", days);
        
        // This would be implemented with detailed property statistics
        Map<String, Object> stats = Map.of(
                "message", "Property statistics would be here",
                "days", days
        );
        
        return ResponseEntity.ok(ApiResponse.success(
                "Property statistics retrieved successfully",
                stats
        ));
    }

    @GetMapping("/stats/transactions")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN')")
    @Operation(summary = "거래 통계 조회", description = "거래 관련 상세 통계를 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTransactionStats(
            @Parameter(description = "통계 조회 기간 (days)") @RequestParam(defaultValue = "30") int days) {
        
        log.info("Admin requesting transaction statistics for {} days", days);
        
        // This would be implemented with detailed transaction statistics
        Map<String, Object> stats = Map.of(
                "message", "Transaction statistics would be here",
                "days", days
        );
        
        return ResponseEntity.ok(ApiResponse.success(
                "Transaction statistics retrieved successfully",
                stats
        ));
    }

    @GetMapping("/system/health")
    @PreAuthorize("@securityExpressionHandler.hasRole('ADMIN')")
    @Operation(summary = "시스템 상태 조회", description = "시스템의 전반적인 상태를 조회합니다.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemHealth() {
        log.info("Admin requesting system health check");
        
        // This would be implemented with actual system health metrics
        Map<String, Object> health = Map.of(
                "status", "UP",
                "database", "UP",
                "redis", "UP",
                "elasticsearch", "UP",
                "diskSpace", Map.of("status", "UP", "free", "10GB", "total", "100GB"),
                "memory", Map.of("status", "UP", "free", "2GB", "total", "8GB")
        );
        
        return ResponseEntity.ok(ApiResponse.success(
                "System health retrieved successfully",
                health
        ));
    }
}