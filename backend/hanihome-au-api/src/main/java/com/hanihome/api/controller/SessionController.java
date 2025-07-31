package com.hanihome.api.controller;

import com.hanihome.api.dto.ApiResponse;
import com.hanihome.api.dto.SessionInfo;
import com.hanihome.api.entity.User;
import com.hanihome.api.service.SessionManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/sessions")
@Tag(name = "Session Management", description = "User session management endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class SessionController {

    private final SessionManagementService sessionManagementService;

    @Operation(summary = "Get user's active sessions", description = "Retrieve all active sessions for the current user")
    @GetMapping("/my-sessions")
    @PreAuthorize("hasRole('TENANT') or hasRole('LANDLORD') or hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SessionInfo>>> getMyActiveSessions(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        List<SessionInfo> sessions = sessionManagementService.getUserSessions(user.getId());
        List<SessionInfo> safeSessions = sessions.stream()
                .map(SessionInfo::toSafeResponse)
                .collect(Collectors.toList());
        
        log.info("User {} retrieved {} active sessions", user.getEmail(), safeSessions.size());
        
        return ResponseEntity.ok(ApiResponse.success(
            "활성 세션 목록을 성공적으로 조회했습니다.", 
            safeSessions
        ));
    }

    @Operation(summary = "Refresh access token", description = "Refresh access token using refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Map<String, Object>>> refreshToken(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Refresh token이 필요합니다.")
            );
        }

        try {
            SessionInfo sessionInfo = sessionManagementService.refreshSession(refreshToken);
            
            // Update session activity
            String ipAddress = getClientIpAddress(httpRequest);
            sessionManagementService.updateSessionActivity(sessionInfo.getSessionId(), ipAddress);
            
            Map<String, Object> response = Map.of(
                "accessToken", sessionInfo.getAccessToken(),
                "tokenType", "Bearer",
                "expiresIn", 86400, // 24 hours in seconds
                "sessionId", sessionInfo.getSessionId()
            );
            
            return ResponseEntity.ok(ApiResponse.success(
                "토큰이 성공적으로 갱신되었습니다.", 
                response
            ));
            
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.badRequest().body(
                ApiResponse.error("토큰 갱신에 실패했습니다: " + e.getMessage())
            );
        }
    }

    @Operation(summary = "Invalidate specific session", description = "Invalidate a specific session by session ID")
    @DeleteMapping("/{sessionId}")
    @PreAuthorize("hasRole('TENANT') or hasRole('LANDLORD') or hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> invalidateSession(
            @PathVariable String sessionId,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        
        // Verify the session belongs to the current user
        List<SessionInfo> userSessions = sessionManagementService.getUserSessions(user.getId());
        boolean sessionBelongsToUser = userSessions.stream()
                .anyMatch(session -> session.getSessionId().equals(sessionId));
        
        if (!sessionBelongsToUser) {
            return ResponseEntity.forbidden().body(
                ApiResponse.error("해당 세션에 대한 권한이 없습니다.")
            );
        }
        
        sessionManagementService.invalidateSession(sessionId);
        
        log.info("User {} invalidated session {}", user.getEmail(), sessionId);
        
        return ResponseEntity.ok(ApiResponse.success(
            "세션이 성공적으로 무효화되었습니다.", 
            sessionId
        ));
    }

    @Operation(summary = "Invalidate all other sessions", description = "Invalidate all sessions except the current one")
    @PostMapping("/invalidate-others")
    @PreAuthorize("hasRole('TENANT') or hasRole('LANDLORD') or hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> invalidateOtherSessions(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        User user = (User) authentication.getPrincipal();
        String currentSessionId = request.get("currentSessionId");
        
        if (currentSessionId == null || currentSessionId.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("현재 세션 ID가 필요합니다.")
            );
        }
        
        sessionManagementService.invalidateAllUserSessions(user.getId(), currentSessionId);
        
        log.info("User {} invalidated all other sessions except {}", user.getEmail(), currentSessionId);
        
        return ResponseEntity.ok(ApiResponse.success(
            "다른 모든 세션이 성공적으로 무효화되었습니다.", 
            "현재 세션을 제외한 모든 세션이 종료되었습니다."
        ));
    }

    @Operation(summary = "Get session statistics", description = "Get system-wide session statistics (Admin only)")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSessionStatistics(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        Map<String, Object> stats = sessionManagementService.getSessionStatistics();
        
        log.info("Admin {} retrieved session statistics", user.getEmail());
        
        return ResponseEntity.ok(ApiResponse.success(
            "세션 통계를 성공적으로 조회했습니다.", 
            stats
        ));
    }

    @Operation(summary = "Manual session cleanup", description = "Manually trigger session cleanup (Admin only)")
    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> manualSessionCleanup(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        try {
            sessionManagementService.cleanupExpiredSessions();
            
            log.info("Admin {} triggered manual session cleanup", user.getEmail());
            
            return ResponseEntity.ok(ApiResponse.success(
                "만료된 세션 정리가 성공적으로 완료되었습니다.", 
                "정리 작업이 완료되었습니다."
            ));
            
        } catch (Exception e) {
            log.error("Manual session cleanup failed", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.error("세션 정리 중 오류가 발생했습니다: " + e.getMessage())
            );
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}