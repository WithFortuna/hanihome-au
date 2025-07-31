package com.hanihome.hanihome_au_api.domain.entity;

import com.hanihome.hanihome_au_api.domain.enums.PrivacyLevel;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_privacy_settings")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPrivacySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "privacy_settings_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "name_privacy", nullable = false)
    @Builder.Default
    private PrivacyLevel namePrivacy = PrivacyLevel.MEMBERS_ONLY;

    @Enumerated(EnumType.STRING)
    @Column(name = "phone_privacy", nullable = false)
    @Builder.Default
    private PrivacyLevel phonePrivacy = PrivacyLevel.PRIVATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_privacy", nullable = false)
    @Builder.Default
    private PrivacyLevel emailPrivacy = PrivacyLevel.PRIVATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_privacy", nullable = false)
    @Builder.Default
    private PrivacyLevel addressPrivacy = PrivacyLevel.PRIVATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "bio_privacy", nullable = false)
    @Builder.Default
    private PrivacyLevel bioPrivacy = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_image_privacy", nullable = false)
    @Builder.Default
    private PrivacyLevel profileImagePrivacy = PrivacyLevel.PUBLIC;

    @Column(name = "gdpr_consent_given", nullable = false)
    @Builder.Default
    private Boolean gdprConsentGiven = false;

    @Column(name = "gdpr_consent_at")
    private LocalDateTime gdprConsentAt;

    @Column(name = "marketing_consent", nullable = false)
    @Builder.Default
    private Boolean marketingConsent = false;

    @Column(name = "data_processing_consent", nullable = false)
    @Builder.Default
    private Boolean dataProcessingConsent = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods
    public void giveGdprConsent() {
        this.gdprConsentGiven = true;
        this.gdprConsentAt = LocalDateTime.now();
    }

    public void revokeGdprConsent() {
        this.gdprConsentGiven = false;
        this.gdprConsentAt = null;
    }

    public boolean isFieldVisible(String fieldName, boolean isOwner, boolean isAuthenticated) {
        if (isOwner) return true;

        PrivacyLevel privacy = getPrivacyForField(fieldName);
        if (privacy == null) return false;

        return switch (privacy) {
            case PUBLIC -> true;
            case MEMBERS_ONLY -> isAuthenticated;
            case PRIVATE -> false;
        };
    }

    private PrivacyLevel getPrivacyForField(String fieldName) {
        return switch (fieldName.toLowerCase()) {
            case "name" -> namePrivacy;
            case "phone" -> phonePrivacy;
            case "email" -> emailPrivacy;
            case "address" -> addressPrivacy;
            case "bio" -> bioPrivacy;
            case "profileimage", "profile_image" -> profileImagePrivacy;
            default -> null;
        };
    }

    public void updatePrivacySettings(PrivacyLevel namePrivacy, PrivacyLevel phonePrivacy, 
                                    PrivacyLevel emailPrivacy, PrivacyLevel addressPrivacy, 
                                    PrivacyLevel bioPrivacy, PrivacyLevel profileImagePrivacy) {
        if (namePrivacy != null) this.namePrivacy = namePrivacy;
        if (phonePrivacy != null) this.phonePrivacy = phonePrivacy;
        if (emailPrivacy != null) this.emailPrivacy = emailPrivacy;
        if (addressPrivacy != null) this.addressPrivacy = addressPrivacy;
        if (bioPrivacy != null) this.bioPrivacy = bioPrivacy;
        if (profileImagePrivacy != null) this.profileImagePrivacy = profileImagePrivacy;
    }
}