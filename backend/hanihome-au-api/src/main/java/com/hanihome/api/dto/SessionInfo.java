package com.hanihome.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfo {
    
    private String sessionId;
    private Long userId;
    private String email;
    private String role;
    private String deviceInfo;
    private String ipAddress;
    private String accessToken;
    private String refreshToken;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAccessedAt;
    
    private boolean active;
    
    // Computed fields for response
    public String getDeviceType() {
        if (deviceInfo == null) return "Unknown";
        
        String device = deviceInfo.toLowerCase();
        if (device.contains("mobile") || device.contains("android") || device.contains("iphone")) {
            return "Mobile";
        } else if (device.contains("tablet") || device.contains("ipad")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }
    
    public String getBrowser() {
        if (deviceInfo == null) return "Unknown";
        
        String device = deviceInfo.toLowerCase();
        if (device.contains("chrome")) return "Chrome";
        if (device.contains("firefox")) return "Firefox";
        if (device.contains("safari")) return "Safari";
        if (device.contains("edge")) return "Edge";
        return "Other";
    }
    
    public boolean isCurrentSession() {
        // This will be set by the controller when comparing with current session
        return false;
    }
    
    public long getMinutesSinceLastAccess() {
        if (lastAccessedAt == null) return 0;
        return java.time.Duration.between(lastAccessedAt, LocalDateTime.now()).toMinutes();
    }
    
    // Security: Don't expose tokens in JSON responses
    public SessionInfo toSafeResponse() {
        return SessionInfo.builder()
                .sessionId(this.sessionId)
                .userId(this.userId)
                .email(this.email)
                .role(this.role)
                .deviceInfo(this.deviceInfo)
                .ipAddress(this.ipAddress)
                .createdAt(this.createdAt)
                .lastAccessedAt(this.lastAccessedAt)
                .active(this.active)
                .build();
    }
}