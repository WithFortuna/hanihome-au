package com.hanihome.hanihome_au_api.controller;

import com.hanihome.hanihome_au_api.config.MenuConfiguration;
import com.hanihome.hanihome_au_api.domain.entity.User;
import com.hanihome.hanihome_au_api.domain.enums.UserRole;
import com.hanihome.hanihome_au_api.dto.response.ApiResponse;
import com.hanihome.hanihome_au_api.dto.response.MenuItemDto;
import com.hanihome.hanihome_au_api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuConfiguration menuConfiguration;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuItemDto>>> getMenuForCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.ok(ApiResponse.error("User not authenticated"));
            }

            Long userId = Long.parseLong(authentication.getName());
            Optional<User> userOpt = userService.findById(userId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("User not found"));
            }
            
            User user = userOpt.get();
            UserRole role = user.getRole();

            log.info("Fetching menu for user: {} with role: {}", userId, role);

            List<MenuItemDto> menu = menuConfiguration.getMenuForRole(role);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Menu retrieved successfully",
                menu
            ));
        } catch (Exception e) {
            log.error("Error fetching menu: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to fetch menu"));
        }
    }

    @GetMapping("/features")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableFeaturesForCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.ok(ApiResponse.error("User not authenticated"));
            }

            Long userId = Long.parseLong(authentication.getName());
            Optional<User> userOpt = userService.findById(userId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("User not found"));
            }
            
            User user = userOpt.get();
            UserRole role = user.getRole();

            log.info("Fetching available features for user: {} with role: {}", userId, role);

            List<String> features = menuConfiguration.getAvailableFeaturesForRole(role);
            
            return ResponseEntity.ok(ApiResponse.success(
                "Available features retrieved successfully",
                features
            ));
        } catch (Exception e) {
            log.error("Error fetching available features: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to fetch available features"));
        }
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMenuAndFeaturesForRole(@PathVariable String role) {
        try {
            UserRole userRole = UserRole.fromString(role);
            
            log.info("Fetching menu and features for role: {}", userRole);

            List<MenuItemDto> menu = menuConfiguration.getMenuForRole(userRole);
            List<String> features = menuConfiguration.getAvailableFeaturesForRole(userRole);
            
            Map<String, Object> result = Map.of(
                "role", userRole,
                "menu", menu,
                "features", features
            );
            
            return ResponseEntity.ok(ApiResponse.success(
                "Menu and features retrieved successfully for role: " + userRole,
                result
            ));
        } catch (IllegalArgumentException e) {
            log.error("Invalid role provided: {}", role);
            return ResponseEntity.ok(ApiResponse.error("Invalid role: " + role));
        } catch (Exception e) {
            log.error("Error fetching menu and features for role {}: {}", role, e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to fetch menu and features"));
        }
    }

    @GetMapping("/user-permissions")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUserPermissions() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.ok(ApiResponse.error("User not authenticated"));
            }

            Long userId = Long.parseLong(authentication.getName());
            Optional<User> userOpt = userService.findById(userId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("User not found"));
            }
            
            User user = userOpt.get();
            UserRole role = user.getRole();

            log.info("Fetching permissions for user: {} with role: {}", userId, role);

            Map<String, Object> permissions = Map.of(
                "userId", userId,
                "role", role.name(),
                "roleName", role.getDisplayName(),
                "authority", role.getAuthority(),
                "permissions", role.getPermissions().stream()
                    .map(permission -> Map.of(
                        "permission", permission.getPermission(),
                        "description", permission.getDescription()
                    ))
                    .toList(),
                "authorities", role.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .toList()
            );
            
            return ResponseEntity.ok(ApiResponse.success(
                "User permissions retrieved successfully",
                permissions
            ));
        } catch (Exception e) {
            log.error("Error fetching user permissions: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to fetch user permissions"));
        }
    }

    @GetMapping("/check-feature/{feature}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkFeatureAccess(@PathVariable String feature) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.ok(ApiResponse.error("User not authenticated"));
            }

            Long userId = Long.parseLong(authentication.getName());
            Optional<User> userOpt = userService.findById(userId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("User not found"));
            }
            
            User user = userOpt.get();
            UserRole role = user.getRole();

            log.info("Checking feature access for user: {} with role: {} for feature: {}", userId, role, feature);

            List<String> availableFeatures = menuConfiguration.getAvailableFeaturesForRole(role);
            boolean hasAccess = availableFeatures.contains(feature);
            
            Map<String, Object> result = Map.of(
                "userId", userId,
                "role", role.name(),
                "feature", feature,
                "hasAccess", hasAccess
            );
            
            return ResponseEntity.ok(ApiResponse.success(
                "Feature access checked successfully",
                result
            ));
        } catch (Exception e) {
            log.error("Error checking feature access: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to check feature access"));
        }
    }

    @PostMapping("/check-features")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkMultipleFeatureAccess(
            @RequestBody List<String> features) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.ok(ApiResponse.error("User not authenticated"));
            }

            Long userId = Long.parseLong(authentication.getName());
            Optional<User> userOpt = userService.findById(userId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("User not found"));
            }
            
            User user = userOpt.get();
            UserRole role = user.getRole();

            log.info("Checking multiple feature access for user: {} with role: {} for features: {}", 
                    userId, role, features);

            List<String> availableFeatures = menuConfiguration.getAvailableFeaturesForRole(role);
            
            Map<String, Boolean> featureAccess = features.stream()
                .collect(java.util.stream.Collectors.toMap(
                    feature -> feature,
                    availableFeatures::contains
                ));
            
            Map<String, Object> result = Map.of(
                "userId", userId,
                "role", role.name(),
                "featureAccess", featureAccess
            );
            
            return ResponseEntity.ok(ApiResponse.success(
                "Multiple feature access checked successfully",
                result
            ));
        } catch (Exception e) {
            log.error("Error checking multiple feature access: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Failed to check multiple feature access"));
        }
    }
}