package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.FCMToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {

    /**
     * 사용자의 활성 FCM 토큰 목록 조회
     */
    List<FCMToken> findByUserIdAndActiveTrue(Long userId);

    /**
     * 특정 토큰으로 FCM 토큰 엔티티 조회
     */
    Optional<FCMToken> findByToken(String token);

    /**
     * 사용자ID와 디바이스ID로 FCM 토큰 조회
     */
    Optional<FCMToken> findByUserIdAndDeviceId(Long userId, String deviceId);

    /**
     * 사용자의 모든 FCM 토큰 조회 (활성/비활성 포함)
     */
    List<FCMToken> findByUserId(Long userId);

    /**
     * 특정 기간 이후 사용되지 않은 토큰 조회
     */
    @Query("SELECT f FROM FCMToken f WHERE f.active = true AND (f.lastUsed IS NULL OR f.lastUsed < :cutoffDate)")
    List<FCMToken> findInactiveTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 오래된 토큰들 비활성화
     */
    @Modifying
    @Query("UPDATE FCMToken f SET f.active = false WHERE f.lastUsed < :cutoffDate OR (f.lastUsed IS NULL AND f.createdAt < :cutoffDate)")
    int deactivateOldTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 특정 토큰 비활성화
     */
    @Modifying
    @Query("UPDATE FCMToken f SET f.active = false WHERE f.token = :token")
    int deactivateToken(@Param("token") String token);

    /**
     * 사용자의 모든 토큰 비활성화
     */
    @Modifying
    @Query("UPDATE FCMToken f SET f.active = false WHERE f.userId = :userId")
    int deactivateAllUserTokens(@Param("userId") Long userId);

    /**
     * 활성 토큰 개수 조회
     */
    @Query("SELECT COUNT(f) FROM FCMToken f WHERE f.active = true")
    long countActiveTokens();

    /**
     * 사용자별 활성 토큰 개수 조회
     */
    @Query("SELECT COUNT(f) FROM FCMToken f WHERE f.userId = :userId AND f.active = true")
    long countActiveTokensByUserId(@Param("userId") Long userId);
}