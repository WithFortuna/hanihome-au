package com.hanihome.hanihome_au_api.presentation.web.notification;

import com.hanihome.hanihome_au_api.application.notification.service.FCMNotificationService;
import com.hanihome.hanihome_au_api.application.notification.service.FCMTokenService;
import com.hanihome.hanihome_au_api.domain.entity.FCMToken;
import com.hanihome.hanihome_au_api.dto.response.ApiResponse;
import com.hanihome.hanihome_au_api.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/v1/fcm")
@RequiredArgsConstructor
@Tag(name = "FCM", description = "Firebase Cloud Messaging API")
public class FCMController {

    private final FCMTokenService fcmTokenService;
    private final FCMNotificationService fcmNotificationService;

    @PostMapping("/tokens")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "FCM 토큰 등록", description = "사용자의 FCM 토큰을 등록하거나 업데이트합니다")
    public ResponseEntity<ApiResponse<FCMTokenResponse>> registerToken(
            @Valid @RequestBody RegisterFCMTokenRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        FCMToken fcmToken = fcmTokenService.registerOrUpdateToken(
                userPrincipal.getId(),
                request.token(),
                request.deviceId(),
                request.deviceType(),
                request.appVersion()
        );

        FCMTokenResponse response = new FCMTokenResponse(
                fcmToken.getId(),
                fcmToken.getDeviceId(),
                fcmToken.getDeviceType(),
                fcmToken.getActive(),
                fcmToken.getCreatedAt()
        );

        return ResponseEntity.ok(ApiResponse.success(request.token() != null ? "FCM 토큰이 성공적으로 등록되었습니다" : "FCM 토큰이 성공적으로 업데이트되었습니다", response));
    }

    @GetMapping("/tokens")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "사용자 FCM 토큰 목록 조회", description = "현재 사용자의 활성 FCM 토큰 목록을 조회합니다")
    public ResponseEntity<ApiResponse<List<String>>> getUserTokens(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<String> tokens = fcmTokenService.getActiveTokensByUserId(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("활성 FCM 토큰 목록을 조회했습니다", tokens));
    }

    @DeleteMapping("/tokens")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "FCM 토큰 비활성화", description = "특정 FCM 토큰을 비활성화합니다")
    public ResponseEntity<ApiResponse<Void>> deactivateToken(
            @RequestParam String token,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        fcmTokenService.deactivateToken(token);
        return ResponseEntity.ok(ApiResponse.success("FCM 토큰이 비활성화되었습니다", null));
    }

    @DeleteMapping("/tokens/all")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "모든 FCM 토큰 비활성화", description = "현재 사용자의 모든 FCM 토큰을 비활성화합니다")
    public ResponseEntity<ApiResponse<Void>> deactivateAllTokens(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        fcmTokenService.deactivateAllUserTokens(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("모든 FCM 토큰이 비활성화되었습니다", null));
    }

    @PostMapping("/test-notification")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "테스트 알림 전송", description = "현재 사용자에게 테스트 푸시 알림을 전송합니다")
    public ResponseEntity<ApiResponse<Void>> sendTestNotification(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        List<String> tokens = fcmTokenService.getActiveTokensByUserId(userPrincipal.getId());
        
        if (tokens.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("등록된 FCM 토큰이 없습니다", null));
        }

        tokens.forEach(token -> {
            Map<String, String> data = Map.of(
                    "type", "test",
                    "userId", userPrincipal.getId().toString()
            );
            
            fcmNotificationService.sendNotification(
                    token,
                    "테스트 알림",
                    "HaniHome 푸시 알림이 정상적으로 작동합니다!",
                    data
            ).whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("Failed to send test notification: {}", throwable.getMessage());
                } else {
                    log.info("Test notification sent successfully: {}", result);
                }
            });
        });

        return ResponseEntity.ok(ApiResponse.success("테스트 알림을 전송했습니다", null));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "FCM 토큰 통계", description = "FCM 토큰 관련 통계를 조회합니다 (관리자 전용)")
    public ResponseEntity<ApiResponse<FCMTokenService.FCMTokenStats>> getTokenStats() {
        FCMTokenService.FCMTokenStats stats = fcmTokenService.getTokenStats();
        return ResponseEntity.ok(ApiResponse.success("FCM 토큰 통계를 조회했습니다", stats));
    }

    // DTOs
    public record RegisterFCMTokenRequest(
            @NotBlank(message = "FCM 토큰은 필수입니다")
            String token,
            
            String deviceId,
            String deviceType,
            String appVersion
    ) {}

    public record FCMTokenResponse(
            Long id,
            String deviceId,
            String deviceType,
            Boolean active,
            java.time.LocalDateTime createdAt
    ) {}
}