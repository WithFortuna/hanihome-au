package com.hanihome.hanihome_au_api.application.admin.dto;

import com.hanihome.hanihome_au_api.domain.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserManagementDto {
    private Long userId;
    private String email;
    private String name;
    private String phoneNumber;
    private UserRole role;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
    private Long totalProperties;
    private Long totalTransactions;
    private Long totalViewings;
    private String profileImageUrl;
    private List<String> permissions;
    private Boolean isEmailVerified;
    private Boolean isPhoneVerified;
    private String suspensionReason;
    private LocalDateTime suspensionDate;
}