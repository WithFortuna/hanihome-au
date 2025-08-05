package com.hanihome.hanihome_au_api.application.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SSENotificationService {

    private final ObjectMapper objectMapper;
    
    // Store active SSE connections by user ID
    private final Map<Long, SseEmitter> userConnections = new ConcurrentHashMap<>();
    
    // Heartbeat executor to keep connections alive
    private final ScheduledExecutorService heartbeatExecutor = Executors.newScheduledThreadPool(2);
    
    // SSE connection timeout (30 minutes)
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;

    /**
     * Create SSE connection for a user
     */
    public SseEmitter createConnection(Long userId) {
        log.info("Creating SSE connection for user: {}", userId);
        
        // Remove existing connection if any
        removeConnection(userId);
        
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        userConnections.put(userId, emitter);
        
        // Set up connection event handlers
        emitter.onCompletion(() -> {
            log.debug("SSE connection completed for user: {}", userId);
            userConnections.remove(userId);
        });
        
        emitter.onTimeout(() -> {
            log.debug("SSE connection timed out for user: {}", userId);
            userConnections.remove(userId);
        });
        
        emitter.onError((throwable) -> {
            log.warn("SSE connection error for user {}: {}", userId, throwable.getMessage());
            userConnections.remove(userId);
        });
        
        // Send initial connection confirmation
        try {
            NotificationMessage welcome = NotificationMessage.builder()
                    .type(NotificationType.CONNECTION)
                    .title("Connected")
                    .message("Real-time notifications enabled")
                    .timestamp(LocalDateTime.now())
                    .data(Map.of("userId", userId))
                    .build();
            
            emitter.send(SseEmitter.event()
                    .name("connection")
                    .data(objectMapper.writeValueAsString(welcome)));
                    
            log.debug("Sent welcome message to user: {}", userId);
            
        } catch (IOException e) {
            log.error("Failed to send welcome message to user {}: {}", userId, e.getMessage());
            userConnections.remove(userId);
            emitter.completeWithError(e);
        }
        
        return emitter;
    }
    
    /**
     * Send notification to a specific user
     */
    public boolean sendToUser(Long userId, NotificationMessage notification) {
        SseEmitter emitter = userConnections.get(userId);
        
        if (emitter == null) {
            log.debug("No active SSE connection for user: {}", userId);
            return false;
        }
        
        try {
            emitter.send(SseEmitter.event()
                    .name(notification.getType().name().toLowerCase())
                    .data(objectMapper.writeValueAsString(notification)));
            
            log.debug("Sent {} notification to user {}: {}", 
                    notification.getType(), userId, notification.getTitle());
            return true;
            
        } catch (IOException e) {
            log.warn("Failed to send notification to user {}: {}", userId, e.getMessage());
            removeConnection(userId);
            return false;
        }
    }
    
    /**
     * Send viewing-related notification
     */
    public void sendViewingNotification(Long userId, ViewingNotificationType type, 
                                      Map<String, Object> viewingData) {
        NotificationMessage notification = NotificationMessage.builder()
                .type(NotificationType.VIEWING)
                .subType(type.name())
                .title(getViewingNotificationTitle(type))
                .message(getViewingNotificationMessage(type, viewingData))
                .timestamp(LocalDateTime.now())
                .data(viewingData)
                .build();
        
        sendToUser(userId, notification);
    }
    
    /**
     * Broadcast notification to multiple users
     */
    public void broadcast(Iterable<Long> userIds, NotificationMessage notification) {
        int sentCount = 0;
        int totalUsers = 0;
        
        for (Long userId : userIds) {
            totalUsers++;
            if (sendToUser(userId, notification)) {
                sentCount++;
            }
        }
        
        log.info("Broadcast notification sent to {}/{} users: {}", 
                sentCount, totalUsers, notification.getTitle());
    }
    
    /**
     * Remove user connection
     */
    public void removeConnection(Long userId) {
        SseEmitter emitter = userConnections.remove(userId);
        if (emitter != null) {
            try {
                emitter.complete();
                log.debug("Removed SSE connection for user: {}", userId);
            } catch (Exception e) {
                log.debug("Error completing SSE connection for user {}: {}", userId, e.getMessage());
            }
        }
    }
    
    /**
     * Get number of active connections
     */
    public int getActiveConnectionCount() {
        return userConnections.size();
    }
    
    /**
     * Check if user has active connection
     */
    public boolean hasActiveConnection(Long userId) {
        return userConnections.containsKey(userId);
    }
    
    /**
     * Start heartbeat to keep connections alive
     */
    public void startHeartbeat() {
        heartbeatExecutor.scheduleWithFixedDelay(this::sendHeartbeat, 30, 30, TimeUnit.SECONDS);
        log.info("Started SSE heartbeat scheduler");
    }
    
    private void sendHeartbeat() {
        if (userConnections.isEmpty()) {
            return;
        }
        
        NotificationMessage heartbeat = NotificationMessage.builder()
                .type(NotificationType.HEARTBEAT)
                .timestamp(LocalDateTime.now())
                .build();
        
        // Send heartbeat to all connected users
        userConnections.entrySet().removeIf(entry -> {
            try {
                entry.getValue().send(SseEmitter.event()
                        .name("heartbeat")
                        .data(objectMapper.writeValueAsString(heartbeat)));
                return false; // Keep connection
            } catch (IOException e) {
                log.debug("Removing dead connection for user: {}", entry.getKey());
                return true; // Remove dead connection
            }
        });
        
        log.debug("Sent heartbeat to {} active connections", userConnections.size());
    }
    
    private String getViewingNotificationTitle(ViewingNotificationType type) {
        return switch (type) {
            case VIEWING_REQUESTED -> "New Viewing Request";
            case VIEWING_CONFIRMED -> "Viewing Confirmed";
            case VIEWING_CANCELLED -> "Viewing Cancelled";
            case VIEWING_RESCHEDULED -> "Viewing Rescheduled";
            case VIEWING_REMINDER -> "Viewing Reminder";
            case VIEWING_COMPLETED -> "Viewing Completed";
        };
    }
    
    private String getViewingNotificationMessage(ViewingNotificationType type, Map<String, Object> data) {
        String propertyTitle = (String) data.getOrDefault("propertyTitle", "Property");
        String scheduledAt = (String) data.getOrDefault("scheduledAt", "");
        
        return switch (type) {
            case VIEWING_REQUESTED -> "A new viewing has been requested for " + propertyTitle;
            case VIEWING_CONFIRMED -> "Your viewing for " + propertyTitle + " has been confirmed";
            case VIEWING_CANCELLED -> "Your viewing for " + propertyTitle + " has been cancelled";
            case VIEWING_RESCHEDULED -> "Your viewing for " + propertyTitle + " has been rescheduled";
            case VIEWING_REMINDER -> "Reminder: You have a viewing for " + propertyTitle + " scheduled";
            case VIEWING_COMPLETED -> "Your viewing for " + propertyTitle + " has been completed";
        };
    }
    
    // Notification types
    public enum NotificationType {
        CONNECTION, HEARTBEAT, VIEWING, SYSTEM
    }
    
    public enum ViewingNotificationType {
        VIEWING_REQUESTED, VIEWING_CONFIRMED, VIEWING_CANCELLED, 
        VIEWING_RESCHEDULED, VIEWING_REMINDER, VIEWING_COMPLETED
    }
    
    // Notification message structure
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class NotificationMessage {
        private NotificationType type;
        private String subType;
        private String title;
        private String message;
        private LocalDateTime timestamp;
        private Map<String, Object> data;
    }
}