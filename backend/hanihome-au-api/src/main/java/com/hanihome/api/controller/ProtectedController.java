package com.hanihome.api.controller;

import com.hanihome.api.dto.ApiResponse;
import com.hanihome.api.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/protected")
@Tag(name = "Protected Endpoints", description = "Role-based protected API endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProtectedController {

    @Operation(summary = "Get user profile", description = "Any authenticated user can access")
    @GetMapping("/profile")
    @PreAuthorize("hasRole('TENANT') or hasRole('LANDLORD') or hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserProfile(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("email", user.getEmail());
        profile.put("name", user.getName());
        profile.put("role", user.getRole());
        profile.put("enabled", user.isEnabled());
        profile.put("lastAccess", LocalDateTime.now());
        
        log.info("User {} accessed profile", user.getEmail());
        
        return ResponseEntity.ok(ApiResponse.success("프로필 정보를 성공적으로 조회했습니다.", profile));
    }

    @Operation(summary = "Tenant only endpoint", description = "Only users with TENANT role can access")
    @GetMapping("/tenant-only")
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<ApiResponse<String>> getTenantOnlyData(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        log.info("Tenant {} accessed tenant-only endpoint", user.getEmail());
        
        return ResponseEntity.ok(ApiResponse.success(
            "테넌트 전용 데이터에 접근했습니다.", 
            "이 데이터는 TENANT 역할을 가진 사용자만 볼 수 있습니다."
        ));
    }

    @Operation(summary = "Landlord only endpoint", description = "Only users with LANDLORD role can access")
    @GetMapping("/landlord-only")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<ApiResponse<String>> getLandlordOnlyData(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        log.info("Landlord {} accessed landlord-only endpoint", user.getEmail());
        
        return ResponseEntity.ok(ApiResponse.success(
            "집주인 전용 데이터에 접근했습니다.", 
            "이 데이터는 LANDLORD 역할을 가진 사용자만 볼 수 있습니다."
        ));
    }

    @Operation(summary = "Agent or Admin endpoint", description = "Users with AGENT or ADMIN role can access")
    @GetMapping("/agent-admin")
    @PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> getAgentAdminData(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        log.info("User {} with role {} accessed agent-admin endpoint", user.getEmail(), user.getRole());
        
        return ResponseEntity.ok(ApiResponse.success(
            "에이전트/관리자 전용 데이터에 접근했습니다.", 
            "이 데이터는 AGENT 또는 ADMIN 역할을 가진 사용자만 볼 수 있습니다."
        ));
    }

    @Operation(summary = "Admin only endpoint", description = "Only users with ADMIN role can access")
    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminOnlyData(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        Map<String, Object> adminData = new HashMap<>();
        adminData.put("systemInfo", "관리자 시스템 정보");
        adminData.put("userCount", 150);
        adminData.put("propertyCount", 1250);
        adminData.put("systemHealth", "정상");
        adminData.put("lastSystemUpdate", LocalDateTime.now());
        
        log.info("Admin {} accessed admin-only endpoint", user.getEmail());
        
        return ResponseEntity.ok(ApiResponse.success(
            "관리자 전용 데이터에 접근했습니다.", 
            adminData
        ));
    }

    @Operation(summary = "Create protected resource", description = "Create a resource that requires authentication")
    @PostMapping("/resource")
    @PreAuthorize("hasRole('LANDLORD') or hasRole('AGENT') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createProtectedResource(
            @RequestBody Map<String, Object> resourceData,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        Map<String, Object> createdResource = new HashMap<>();
        createdResource.put("id", System.currentTimeMillis());
        createdResource.put("data", resourceData);
        createdResource.put("createdBy", user.getEmail());
        createdResource.put("createdByRole", user.getRole());
        createdResource.put("createdAt", LocalDateTime.now());
        
        log.info("User {} with role {} created protected resource", user.getEmail(), user.getRole());
        
        return ResponseEntity.ok(ApiResponse.success(
            "보호된 리소스가 성공적으로 생성되었습니다.", 
            createdResource
        ));
    }

    @Operation(summary = "Update protected resource", description = "Update requires specific permissions")
    @PutMapping("/resource/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('LANDLORD') and #id < 1000) or (hasRole('AGENT') and #id < 500)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProtectedResource(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updateData,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        Map<String, Object> updatedResource = new HashMap<>();
        updatedResource.put("id", id);
        updatedResource.put("data", updateData);
        updatedResource.put("updatedBy", user.getEmail());
        updatedResource.put("updatedByRole", user.getRole());
        updatedResource.put("updatedAt", LocalDateTime.now());
        
        log.info("User {} with role {} updated protected resource {}", user.getEmail(), user.getRole(), id);
        
        return ResponseEntity.ok(ApiResponse.success(
            "보호된 리소스가 성공적으로 업데이트되었습니다.", 
            updatedResource
        ));
    }

    @Operation(summary = "Delete protected resource", description = "Delete requires ADMIN role")
    @DeleteMapping("/resource/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteProtectedResource(
            @PathVariable Long id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        
        log.info("Admin {} deleted protected resource {}", user.getEmail(), id);
        
        return ResponseEntity.ok(ApiResponse.success(
            "보호된 리소스가 성공적으로 삭제되었습니다.", 
            "Resource ID: " + id
        ));
    }
}