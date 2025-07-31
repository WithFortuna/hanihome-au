package com.hanihome.api.service;

import com.hanihome.api.dto.PasswordChangeDto;
import com.hanihome.api.dto.UserProfileDto;
import com.hanihome.hanihome_au_api.domain.entity.User;
import com.hanihome.hanihome_au_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityAuditService securityAuditService;
    private final FileStorageService fileStorageService;

    @Cacheable(value = "userProfile", key = "#userId")
    public UserProfileDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
        
        log.info("Retrieved profile for user: {}", user.getEmail());
        securityAuditService.logUserAction(userId, "PROFILE_VIEW", "사용자가 자신의 프로필을 조회했습니다", null);
        
        return UserProfileDto.fromEntity(user);
    }

    @Cacheable(value = "publicProfile", key = "#userId")
    public UserProfileDto getPublicProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
        
        if (!user.isEnabled()) {
            throw new RuntimeException("비활성화된 사용자입니다");
        }
        
        return UserProfileDto.toPublicProfile(user);
    }

    @CachePut(value = "userProfile", key = "#userId")
    @CacheEvict(value = "publicProfile", key = "#userId")
    public UserProfileDto updateProfile(Long userId, UserProfileDto profileDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
        
        // Store original values for audit
        String originalName = user.getName();
        String originalPhone = user.getPhone();
        String originalAddress = user.getAddress();
        
        // Update user entity
        profileDto.updateEntity(user);
        
        User updatedUser = userRepository.save(user);
        
        // Log the profile update
        String changes = buildChangeDescription(originalName, originalPhone, originalAddress, profileDto);
        securityAuditService.logUserAction(userId, "PROFILE_UPDATE", "사용자가 프로필을 수정했습니다", changes);
        
        log.info("Updated profile for user: {}", user.getEmail());
        
        return UserProfileDto.fromEntity(updatedUser);
    }

    public void changePassword(Long userId, PasswordChangeDto passwordChangeDto) {
        if (!passwordChangeDto.isPasswordMatching()) {
            throw new RuntimeException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
        
        // Verify current password
        if (!passwordEncoder.matches(passwordChangeDto.getCurrentPassword(), user.getPassword())) {
            securityAuditService.logSecurityEvent(userId, "PASSWORD_CHANGE_FAILED", 
                "잘못된 현재 비밀번호로 비밀번호 변경 시도", null);
            throw new RuntimeException("현재 비밀번호가 올바르지 않습니다");
        }
        
        // Check if new password is same as current
        if (passwordEncoder.matches(passwordChangeDto.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("새 비밀번호는 현재 비밀번호와 달라야 합니다");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        
        // Log successful password change
        securityAuditService.logSecurityEvent(userId, "PASSWORD_CHANGED", 
            "사용자가 비밀번호를 성공적으로 변경했습니다", null);
        
        log.info("Password changed successfully for user: {}", user.getEmail());
    }

    public String uploadProfileImage(Long userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("업로드할 파일이 없습니다");
        }
        
        // Validate file type and size
        if (!isValidImageFile(file)) {
            throw new RuntimeException("유효하지 않은 이미지 파일입니다");
        }
        
        if (file.getSize() > 5 * 1024 * 1024) { // 5MB limit
            throw new RuntimeException("파일 크기는 5MB를 초과할 수 없습니다");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
        
        try {
            // Delete old profile image if exists
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                fileStorageService.deleteFile(user.getProfileImageUrl());
            }
            
            // Upload new image
            String imageUrl = fileStorageService.storeFile(file, "profiles");
            
            // Update user profile
            user.setProfileImageUrl(imageUrl);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            securityAuditService.logUserAction(userId, "PROFILE_IMAGE_UPLOAD", 
                "사용자가 프로필 이미지를 업로드했습니다", imageUrl);
            
            log.info("Profile image uploaded for user: {}", user.getEmail());
            
            return imageUrl;
            
        } catch (Exception e) {
            log.error("Failed to upload profile image for user: {}", user.getEmail(), e);
            throw new RuntimeException("프로필 이미지 업로드에 실패했습니다: " + e.getMessage());
        }
    }

    public void deleteProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
        
        if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
            try {
                fileStorageService.deleteFile(user.getProfileImageUrl());
                
                user.setProfileImageUrl(null);
                user.setUpdatedAt(LocalDateTime.now());
                userRepository.save(user);
                
                securityAuditService.logUserAction(userId, "PROFILE_IMAGE_DELETE", 
                    "사용자가 프로필 이미지를 삭제했습니다", null);
                
                log.info("Profile image deleted for user: {}", user.getEmail());
                
            } catch (Exception e) {
                log.error("Failed to delete profile image for user: {}", user.getEmail(), e);
                throw new RuntimeException("프로필 이미지 삭제에 실패했습니다");
            }
        }
    }

    public void enableTwoFactor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
        
        user.setTwoFactorEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        securityAuditService.logSecurityEvent(userId, "TWO_FACTOR_ENABLED", 
            "사용자가 2단계 인증을 활성화했습니다", null);
        
        log.info("Two-factor authentication enabled for user: {}", user.getEmail());
    }

    public void disableTwoFactor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
        
        user.setTwoFactorEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        securityAuditService.logSecurityEvent(userId, "TWO_FACTOR_DISABLED", 
            "사용자가 2단계 인증을 비활성화했습니다", null);
        
        log.info("Two-factor authentication disabled for user: {}", user.getEmail());
    }

    public void requestAccountDeletion(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
        
        // Mark account for deletion (soft delete)
        user.setEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        
        securityAuditService.logSecurityEvent(userId, "ACCOUNT_DELETION_REQUESTED", 
            "사용자가 계정 삭제를 요청했습니다", null);
        
        log.info("Account deletion requested for user: {}", user.getEmail());
        
        // TODO: Schedule actual data deletion after grace period
        // TODO: Send confirmation email
    }

    private boolean isValidImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/gif") ||
            contentType.equals("image/webp")
        );
    }

    private String buildChangeDescription(String originalName, String originalPhone, 
                                        String originalAddress, UserProfileDto newProfile) {
        StringBuilder changes = new StringBuilder();
        
        if (!originalName.equals(newProfile.getName())) {
            changes.append("이름: ").append(originalName).append(" → ").append(newProfile.getName()).append("; ");
        }
        
        if (!Objects.equals(originalPhone, newProfile.getPhone())) {
            changes.append("전화번호: ").append(originalPhone).append(" → ").append(newProfile.getPhone()).append("; ");
        }
        
        if (!Objects.equals(originalAddress, newProfile.getAddress())) {
            changes.append("주소: ").append(originalAddress).append(" → ").append(newProfile.getAddress()).append("; ");
        }
        
        return changes.toString();
    }
}