package com.hanihome.api.dto;

import com.hanihome.api.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    
    private Long id;
    
    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 50, message = "이름은 2-50자 사이여야 합니다")
    private String name;
    
    @Email(message = "유효한 이메일 주소를 입력해주세요")
    @NotBlank(message = "이메일은 필수입니다")
    private String email;
    
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "유효한 전화번호를 입력해주세요")
    private String phone;
    
    @Size(max = 255, message = "주소는 255자를 초과할 수 없습니다")
    private String address;
    
    @Size(max = 500, message = "소개는 500자를 초과할 수 없습니다")
    private String bio;
    
    private String profileImageUrl;
    
    private String role;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    
    private boolean enabled;
    private boolean twoFactorEnabled;
    
    // Security-related fields (read-only)
    private int loginAttempts;
    private LocalDateTime lockedUntil;
    private boolean accountLocked;
    
    public static UserProfileDto fromEntity(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .enabled(user.isEnabled())
                .twoFactorEnabled(user.isTwoFactorEnabled())
                .loginAttempts(user.getLoginAttempts())
                .lockedUntil(user.getLockedUntil())
                .accountLocked(user.isAccountLocked())
                .build();
    }
    
    public void updateEntity(User user) {
        if (name != null) user.setName(name);
        if (phone != null) user.setPhone(phone);
        if (address != null) user.setAddress(address);
        if (bio != null) user.setBio(bio);
        if (profileImageUrl != null) user.setProfileImageUrl(profileImageUrl);
        user.setUpdatedAt(LocalDateTime.now());
    }
    
    // Public profile view (limited data for privacy)
    public static UserProfileDto toPublicProfile(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .name(user.getName())
                .bio(user.getBio())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}