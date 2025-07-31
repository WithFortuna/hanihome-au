package com.hanihome.api.service;

import com.hanihome.api.dto.SessionInfo;
import com.hanihome.api.entity.User;
import com.hanihome.api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionManagementService {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> redisObjectTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    private static final String SESSION_PREFIX = "session:";
    private static final String USER_SESSIONS_PREFIX = "user_sessions:";
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String ACTIVE_USERS_PREFIX = "active_users";
    private static final String BLACKLIST_PREFIX = "blacklist:";

    public SessionInfo createSession(User user, String deviceInfo, String ipAddress) {
        String sessionId = UUID.randomUUID().toString();
        
        SessionInfo sessionInfo = SessionInfo.builder()
                .sessionId(sessionId)
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .createdAt(LocalDateTime.now())
                .lastAccessedAt(LocalDateTime.now())
                .active(true)
                .build();

        // Generate tokens
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRole().name(), user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail(), user.getId());

        sessionInfo.setAccessToken(accessToken);
        sessionInfo.setRefreshToken(refreshToken);

        // Store session information
        storeSession(sessionInfo);
        
        // Add to user's active sessions
        addToUserSessions(user.getId(), sessionId);
        
        // Update active users set
        redisTemplate.opsForSet().add(ACTIVE_USERS_PREFIX, user.getId().toString());
        
        log.info("Created new session {} for user {} from IP {}", sessionId, user.getEmail(), ipAddress);
        
        return sessionInfo;
    }

    public SessionInfo refreshSession(String refreshToken) {
        try {
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new RuntimeException("Invalid refresh token");
            }

            String email = jwtTokenProvider.getUserEmailFromToken(refreshToken);
            Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

            if (!jwtTokenProvider.validateRefreshToken(refreshToken, userId)) {
                throw new RuntimeException("Refresh token not found or expired");
            }

            // Find session by refresh token
            SessionInfo sessionInfo = findSessionByRefreshToken(refreshToken, userId);
            if (sessionInfo == null) {
                throw new RuntimeException("Session not found");
            }

            // Generate new access token
            String newAccessToken = jwtTokenProvider.createAccessToken(email, sessionInfo.getRole(), userId);
            
            // Update session
            sessionInfo.setAccessToken(newAccessToken);
            sessionInfo.setLastAccessedAt(LocalDateTime.now());
            
            storeSession(sessionInfo);
            
            log.info("Refreshed session {} for user {}", sessionInfo.getSessionId(), email);
            
            return sessionInfo;
            
        } catch (Exception e) {
            log.error("Failed to refresh session", e);
            throw new RuntimeException("Token refresh failed: " + e.getMessage());
        }
    }

    public void updateSessionActivity(String sessionId, String ipAddress) {
        SessionInfo sessionInfo = getSession(sessionId);
        if (sessionInfo != null && sessionInfo.isActive()) {
            sessionInfo.setLastAccessedAt(LocalDateTime.now());
            sessionInfo.setIpAddress(ipAddress);
            storeSession(sessionInfo);
        }
    }

    public void invalidateSession(String sessionId) {
        SessionInfo sessionInfo = getSession(sessionId);
        if (sessionInfo != null) {
            // Blacklist current access token
            if (sessionInfo.getAccessToken() != null) {
                jwtTokenProvider.blacklistToken(sessionInfo.getAccessToken());
            }
            
            // Revoke refresh token
            jwtTokenProvider.revokeRefreshToken(sessionInfo.getUserId());
            
            // Mark session as inactive
            sessionInfo.setActive(false);
            storeSession(sessionInfo);
            
            // Remove from user sessions
            removeFromUserSessions(sessionInfo.getUserId(), sessionId);
            
            log.info("Invalidated session {} for user {}", sessionId, sessionInfo.getEmail());
        }
    }

    public void invalidateAllUserSessions(Long userId, String currentSessionId) {
        Set<String> sessionIds = getUserActiveSessions(userId);
        
        for (String sessionId : sessionIds) {
            if (!sessionId.equals(currentSessionId)) {
                invalidateSession(sessionId);
            }
        }
        
        // Revoke all user tokens except current
        jwtTokenProvider.revokeAllUserTokens(userId);
        
        log.info("Invalidated all sessions for user {} except current session {}", userId, currentSessionId);
    }

    public List<SessionInfo> getUserSessions(Long userId) {
        Set<String> sessionIds = getUserActiveSessions(userId);
        List<SessionInfo> sessions = new ArrayList<>();
        
        for (String sessionId : sessionIds) {
            SessionInfo session = getSession(sessionId);
            if (session != null && session.isActive()) {
                sessions.add(session);
            }
        }
        
        // Sort by last accessed time (most recent first)
        sessions.sort((a, b) -> b.getLastAccessedAt().compareTo(a.getLastAccessedAt()));
        
        return sessions;
    }

    public void cleanupExpiredSessions() {
        log.info("Starting cleanup of expired sessions");
        
        Set<String> activeUserIds = redisTemplate.opsForSet().members(ACTIVE_USERS_PREFIX);
        int cleanedSessions = 0;
        
        if (activeUserIds != null) {
            for (String userIdStr : activeUserIds) {
                Long userId = Long.parseLong(userIdStr);
                Set<String> sessionIds = getUserActiveSessions(userId);
                
                for (String sessionId : sessionIds) {
                    SessionInfo session = getSession(sessionId);
                    if (session == null || isSessionExpired(session)) {
                        invalidateSession(sessionId);
                        cleanedSessions++;
                    }
                }
                
                // Remove user from active set if no active sessions
                if (getUserActiveSessions(userId).isEmpty()) {
                    redisTemplate.opsForSet().remove(ACTIVE_USERS_PREFIX, userIdStr);
                }
            }
        }
        
        log.info("Cleanup completed. Removed {} expired sessions", cleanedSessions);
    }

    public Map<String, Object> getSessionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        Set<String> activeUserIds = redisTemplate.opsForSet().members(ACTIVE_USERS_PREFIX);
        int totalSessions = 0;
        int activeSessions = 0;
        
        if (activeUserIds != null) {
            for (String userIdStr : activeUserIds) {
                Long userId = Long.parseLong(userIdStr);
                Set<String> sessionIds = getUserActiveSessions(userId);
                totalSessions += sessionIds.size();
                
                for (String sessionId : sessionIds) {
                    SessionInfo session = getSession(sessionId);
                    if (session != null && session.isActive() && !isSessionExpired(session)) {
                        activeSessions++;
                    }
                }
            }
        }
        
        stats.put("activeUsers", activeUserIds != null ? activeUserIds.size() : 0);
        stats.put("totalSessions", totalSessions);
        stats.put("activeSessions", activeSessions);
        stats.put("timestamp", LocalDateTime.now());
        
        return stats;
    }

    private void storeSession(SessionInfo sessionInfo) {
        String key = SESSION_PREFIX + sessionInfo.getSessionId();
        redisObjectTemplate.opsForValue().set(key, sessionInfo, Duration.ofDays(7)); // 7 days TTL
    }

    private SessionInfo getSession(String sessionId) {
        String key = SESSION_PREFIX + sessionId;
        return (SessionInfo) redisObjectTemplate.opsForValue().get(key);
    }

    private void addToUserSessions(Long userId, String sessionId) {
        String key = USER_SESSIONS_PREFIX + userId;
        redisTemplate.opsForSet().add(key, sessionId);
        redisTemplate.expire(key, Duration.ofDays(7));
    }

    private void removeFromUserSessions(Long userId, String sessionId) {
        String key = USER_SESSIONS_PREFIX + userId;
        redisTemplate.opsForSet().remove(key, sessionId);
    }

    private Set<String> getUserActiveSessions(Long userId) {
        String key = USER_SESSIONS_PREFIX + userId;
        Set<String> sessions = redisTemplate.opsForSet().members(key);
        return sessions != null ? sessions : new HashSet<>();
    }

    private SessionInfo findSessionByRefreshToken(String refreshToken, Long userId) {
        Set<String> sessionIds = getUserActiveSessions(userId);
        
        for (String sessionId : sessionIds) {
            SessionInfo session = getSession(sessionId);
            if (session != null && refreshToken.equals(session.getRefreshToken())) {
                return session;
            }
        }
        
        return null;
    }

    private boolean isSessionExpired(SessionInfo session) {
        if (session == null || !session.isActive()) {
            return true;
        }
        
        // Check if session is inactive for more than 7 days
        LocalDateTime expiredTime = session.getLastAccessedAt().plusDays(7);
        return LocalDateTime.now().isAfter(expiredTime);
    }
}