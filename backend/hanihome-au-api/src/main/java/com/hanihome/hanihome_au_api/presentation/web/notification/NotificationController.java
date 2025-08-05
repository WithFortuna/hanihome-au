package com.hanihome.hanihome_au_api.presentation.web.notification;

import com.hanihome.hanihome_au_api.application.notification.service.SSENotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Real-time notification APIs")
public class NotificationController {

    private final SSENotificationService sseNotificationService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Connect to notification stream", 
               description = "Establish SSE connection for real-time notifications")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "SSE connection established"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public SseEmitter connectToNotificationStream(Authentication authentication) {
        Long userId = extractUserIdFromAuthentication(authentication);
        log.info("User {} connecting to notification stream", userId);
        
        return sseNotificationService.createConnection(userId);
    }

    @DeleteMapping("/stream")
    @Operation(summary = "Disconnect from notification stream", 
               description = "Close SSE connection")
    public ResponseEntity<Void> disconnectFromNotificationStream(Authentication authentication) {
        Long userId = extractUserIdFromAuthentication(authentication);
        log.info("User {} disconnecting from notification stream", userId);
        
        sseNotificationService.removeConnection(userId);
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status")
    @Operation(summary = "Get notification status", 
               description = "Check if user has active notification connection")
    public ResponseEntity<NotificationStatus> getNotificationStatus(Authentication authentication) {
        Long userId = extractUserIdFromAuthentication(authentication);
        
        boolean hasActiveConnection = sseNotificationService.hasActiveConnection(userId);
        int totalActiveConnections = sseNotificationService.getActiveConnectionCount();
        
        NotificationStatus status = NotificationStatus.builder()
                .userId(userId)
                .connected(hasActiveConnection)
                .totalActiveConnections(totalActiveConnections)
                .build();
        
        return ResponseEntity.ok(status);
    }

    @PostMapping("/test")
    @Operation(summary = "Send test notification", 
               description = "Send a test notification to the user (for development)")
    public ResponseEntity<TestNotificationResponse> sendTestNotification(
            Authentication authentication,
            @RequestBody TestNotificationRequest request) {
        
        Long userId = extractUserIdFromAuthentication(authentication);
        
        SSENotificationService.NotificationMessage testNotification = 
            SSENotificationService.NotificationMessage.builder()
                .type(SSENotificationService.NotificationType.SYSTEM)
                .title(request.getTitle() != null ? request.getTitle() : "Test Notification")
                .message(request.getMessage() != null ? request.getMessage() : "This is a test notification")
                .timestamp(java.time.LocalDateTime.now())
                .data(Map.of("test", true))
                .build();
        
        boolean sent = sseNotificationService.sendToUser(userId, testNotification);
        
        TestNotificationResponse response = TestNotificationResponse.builder()
                .sent(sent)
                .message(sent ? "Test notification sent successfully" : "No active connection found")
                .build();
        
        return ResponseEntity.ok(response);
    }

    private Long extractUserIdFromAuthentication(Authentication authentication) {
        // Simplified - in real implementation, extract from JWT or security context
        return 1L;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NotificationStatus {
        private Long userId;
        private boolean connected;
        private int totalActiveConnections;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TestNotificationRequest {
        private String title;
        private String message;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TestNotificationResponse {
        private boolean sent;
        private String message;
    }
}