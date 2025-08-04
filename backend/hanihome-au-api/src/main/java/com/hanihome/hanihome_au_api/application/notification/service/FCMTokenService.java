package com.hanihome.hanihome_au_api.application.notification.service;

import com.hanihome.hanihome_au_api.domain.entity.FCMToken;
import com.hanihome.hanihome_au_api.repository.FCMTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FCMTokenService {

    private final FCMTokenRepository fcmTokenRepository;
    private static final int MAX_TOKENS_PER_USER = 5; // 사용자당 최대 토큰 수

    /**
     * FCM 토큰 등록 또는 업데이트
     */
    public FCMToken registerOrUpdateToken(Long userId, String token, String deviceId, String deviceType, String appVersion) {
        // 기존 토큰 확인
        Optional<FCMToken> existingToken = fcmTokenRepository.findByUserIdAndDeviceId(userId, deviceId);
        
        if (existingToken.isPresent()) {
            // 기존 토큰 업데이트
            FCMToken fcmToken = existingToken.get();
            fcmToken.updateToken(token);
            fcmToken.activate();
            
            log.info("Updated FCM token for user: {}, device: {}", userId, deviceId);
            return fcmTokenRepository.save(fcmToken);
        } else {
            // 새 토큰 생성
            FCMToken newToken = FCMToken.builder()
                    .userId(userId)
                    .token(token)
                    .deviceId(deviceId)
                    .deviceType(deviceType)
                    .appVersion(appVersion)
                    .build();
            
            // 사용자당 토큰 수 제한 확인
            cleanupExcessTokens(userId);
            
            log.info("Registered new FCM token for user: {}, device: {}", userId, deviceId);
            return fcmTokenRepository.save(newToken);
        }
    }

    /**
     * 사용자의 활성 FCM 토큰 목록 조회
     */
    @Transactional(readOnly = true)
    public List<String> getActiveTokensByUserId(Long userId) {
        return fcmTokenRepository.findByUserIdAndActiveTrue(userId)
                .stream()
                .map(FCMToken::getToken)
                .toList();
    }

    /**
     * 토큰 사용 시간 업데이트
     */
    public void updateTokenUsage(String token) {
        fcmTokenRepository.findByToken(token)
                .ifPresent(fcmToken -> {
                    fcmToken.updateLastUsed();
                    fcmTokenRepository.save(fcmToken);
                });
    }

    /**
     * 토큰 비활성화
     */
    public void deactivateToken(String token) {
        fcmTokenRepository.deactivateToken(token);
        log.info("Deactivated FCM token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
    }

    /**
     * 사용자의 모든 토큰 비활성화
     */
    public void deactivateAllUserTokens(Long userId) {
        int deactivatedCount = fcmTokenRepository.deactivateAllUserTokens(userId);
        log.info("Deactivated {} FCM tokens for user: {}", deactivatedCount, userId);
    }

    /**
     * 사용자당 토큰 수 제한을 위한 정리 작업
     */
    private void cleanupExcessTokens(Long userId) {
        List<FCMToken> userTokens = fcmTokenRepository.findByUserId(userId);
        
        if (userTokens.size() >= MAX_TOKENS_PER_USER) {
            // 가장 오래된 토큰부터 삭제
            userTokens.stream()
                    .sorted((t1, t2) -> {
                        LocalDateTime time1 = t1.getLastUsed() != null ? t1.getLastUsed() : t1.getCreatedAt();
                        LocalDateTime time2 = t2.getLastUsed() != null ? t2.getLastUsed() : t2.getCreatedAt();
                        return time1.compareTo(time2);
                    })
                    .limit(userTokens.size() - MAX_TOKENS_PER_USER + 1)
                    .forEach(token -> {
                        fcmTokenRepository.delete(token);
                        log.info("Deleted excess FCM token for user: {}", userId);
                    });
        }
    }

    /**
     * 주기적으로 오래된 토큰 정리 (30일 이상 미사용)
     */
    @Scheduled(cron = "0 0 2 * * ?") // 매일 새벽 2시
    public void cleanupOldTokens() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        int deactivatedCount = fcmTokenRepository.deactivateOldTokens(cutoffDate);
        
        if (deactivatedCount > 0) {
            log.info("Deactivated {} old FCM tokens", deactivatedCount);
        }
    }

    /**
     * FCM 토큰 통계 조회
     */
    @Transactional(readOnly = true)
    public FCMTokenStats getTokenStats() {
        long totalActiveTokens = fcmTokenRepository.countActiveTokens();
        return new FCMTokenStats(totalActiveTokens);
    }

    public record FCMTokenStats(long totalActiveTokens) {}
}