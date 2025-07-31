package com.hanihome.api.controller;

import com.hanihome.api.annotation.RateLimit;
import com.hanihome.api.dto.PasswordChangeDto;
import com.hanihome.api.dto.UserProfileDto;
import com.hanihome.api.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileDto> getCurrentUserProfile() {
        Long userId = getCurrentUserId();
        UserProfileDto profile = userProfileService.getUserProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileDto> getPublicProfile(@PathVariable Long userId) {
        UserProfileDto profile = userProfileService.getPublicProfile(userId);
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserProfileDto> updateProfile(@Valid @RequestBody UserProfileDto profileDto) {
        Long userId = getCurrentUserId();
        UserProfileDto updatedProfile = userProfileService.updateProfile(userId, profileDto);
        return ResponseEntity.ok(updatedProfile);
    }

    @PostMapping("/password/change")
    @PreAuthorize("hasRole('USER')")
    @RateLimit(value = 3, windowSeconds = 300, scope = RateLimit.RateLimitScope.USER, 
               message = "비밀번호 변경 시도가 너무 많습니다. 5분 후 다시 시도해주세요.")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody PasswordChangeDto passwordChangeDto) {
        try {
            Long userId = getCurrentUserId();
            userProfileService.changePassword(userId, passwordChangeDto);
            return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다"));
        } catch (RuntimeException e) {
            log.error("Password change failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    @RateLimit(value = 5, windowSeconds = 3600, scope = RateLimit.RateLimitScope.USER,
               message = "프로필 이미지 업로드 한도를 초과했습니다. 1시간 후 다시 시도해주세요.")
    public ResponseEntity<Map<String, String>> uploadProfileImage(@RequestParam("file") MultipartFile file) {
        try {
            Long userId = getCurrentUserId();
            String imageUrl = userProfileService.uploadProfileImage(userId, file);
            return ResponseEntity.ok(Map.of("imageUrl", imageUrl, "message", "프로필 이미지가 성공적으로 업로드되었습니다"));
        } catch (RuntimeException e) {
            log.error("Profile image upload failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/image")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> deleteProfileImage() {
        try {
            Long userId = getCurrentUserId();
            userProfileService.deleteProfileImage(userId);
            return ResponseEntity.ok(Map.of("message", "프로필 이미지가 성공적으로 삭제되었습니다"));
        } catch (RuntimeException e) {
            log.error("Profile image deletion failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/two-factor/enable")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> enableTwoFactor() {
        try {
            Long userId = getCurrentUserId();
            userProfileService.enableTwoFactor(userId);
            return ResponseEntity.ok(Map.of("message", "2단계 인증이 활성화되었습니다"));
        } catch (RuntimeException e) {
            log.error("Two-factor authentication enablement failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/two-factor/disable")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> disableTwoFactor() {
        try {
            Long userId = getCurrentUserId();
            userProfileService.disableTwoFactor(userId);
            return ResponseEntity.ok(Map.of("message", "2단계 인증이 비활성화되었습니다"));
        } catch (RuntimeException e) {
            log.error("Two-factor authentication disablement failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/delete-account")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> requestAccountDeletion() {
        try {
            Long userId = getCurrentUserId();
            userProfileService.requestAccountDeletion(userId);
            return ResponseEntity.ok(Map.of("message", "계정 삭제 요청이 처리되었습니다"));
        } catch (RuntimeException e) {
            log.error("Account deletion request failed", e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        log.error("Runtime exception in UserProfileController", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "서버 오류가 발생했습니다: " + e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Unexpected exception in UserProfileController", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "예상치 못한 오류가 발생했습니다"));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("인증되지 않은 사용자입니다");
        }
        
        // Extract user ID from authentication principal
        String userIdStr = authentication.getName();
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("유효하지 않은 사용자 ID입니다");
        }
    }
}