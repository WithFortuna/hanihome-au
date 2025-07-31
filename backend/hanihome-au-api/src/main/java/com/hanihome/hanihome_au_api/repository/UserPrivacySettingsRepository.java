package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.UserPrivacySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPrivacySettingsRepository extends JpaRepository<UserPrivacySettings, Long> {

    /**
     * Find privacy settings by user ID
     */
    @Query("SELECT ups FROM UserPrivacySettings ups WHERE ups.user.id = :userId")
    Optional<UserPrivacySettings> findByUserId(@Param("userId") Long userId);

    /**
     * Delete privacy settings by user ID
     */
    @Query("DELETE FROM UserPrivacySettings ups WHERE ups.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    /**
     * Check if user exists with privacy settings
     */
    boolean existsByUserId(Long userId);

    /**
     * Find users who have given GDPR consent
     */
    @Query("SELECT ups FROM UserPrivacySettings ups WHERE ups.gdprConsentGiven = true")
    java.util.List<UserPrivacySettings> findUsersWithGdprConsent();

    /**
     * Find users who have given marketing consent
     */
    @Query("SELECT ups FROM UserPrivacySettings ups WHERE ups.marketingConsent = true")
    java.util.List<UserPrivacySettings> findUsersWithMarketingConsent();
}