package com.hanihome.hanihome_au_api.security;

import com.hanihome.hanihome_au_api.domain.entity.User;
import com.hanihome.hanihome_au_api.domain.enums.Permission;
import com.hanihome.hanihome_au_api.domain.enums.UserRole;
import com.hanihome.hanihome_au_api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component("securityExpressionHandler")
@RequiredArgsConstructor
public class SecurityExpressionHandler {

    private final UserService userService;

    public boolean hasPermission(String permission) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            Long userId = Long.parseLong(authentication.getName());
            Optional<User> userOpt = userService.findById(userId);
            
            if (userOpt.isEmpty()) {
                return false;
            }
            
            User user = userOpt.get();
            Permission requiredPermission = Permission.fromString(permission);
            
            return user.getRole().hasPermission(requiredPermission);
        } catch (Exception e) {
            log.error("Error checking permission {}: {}", permission, e.getMessage());
            return false;
        }
    }

    public boolean hasRole(String role) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            Long userId = Long.parseLong(authentication.getName());
            Optional<User> userOpt = userService.findById(userId);
            
            if (userOpt.isEmpty()) {
                return false;
            }
            
            User user = userOpt.get();
            
            return user.getRole().name().equals(role) || 
                   user.getRole().getAuthority().equals("ROLE_" + role);
        } catch (Exception e) {
            log.error("Error checking role {}: {}", role, e.getMessage());
            return false;
        }
    }

    public boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOwner(Long resourceOwnerId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            Long userId = Long.parseLong(authentication.getName());
            return userId.equals(resourceOwnerId);
        } catch (Exception e) {
            log.error("Error checking ownership: {}", e.getMessage());
            return false;
        }
    }

    public boolean isOwnerOrHasPermission(Long resourceOwnerId, String permission) {
        return isOwner(resourceOwnerId) || hasPermission(permission);
    }

    public boolean isOwnerOrHasRole(Long resourceOwnerId, String role) {
        return isOwner(resourceOwnerId) || hasRole(role);
    }

    public boolean canManageProperty(Long propertyId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            Long userId = Long.parseLong(authentication.getName());
            Optional<User> userOpt = userService.findById(userId);
            
            if (userOpt.isEmpty()) {
                return false;
            }
            
            User user = userOpt.get();
            UserRole role = user.getRole();

            // Admin and Agent can manage all properties
            if (role == UserRole.ADMIN || role == UserRole.AGENT) {
                return true;
            }

            // Landlord can manage their own properties
            if (role == UserRole.LANDLORD) {
                // TODO: Check if user owns this property
                return true; // Placeholder - should check property ownership
            }

            return false;
        } catch (Exception e) {
            log.error("Error checking property management permission: {}", e.getMessage());
            return false;
        }
    }

    public boolean canViewProperty(Long propertyId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            Long userId = Long.parseLong(authentication.getName());
            Optional<User> userOpt = userService.findById(userId);
            
            if (userOpt.isEmpty()) {
                return false;
            }
            
            User user = userOpt.get();
            
            // All authenticated users can view properties (basic read access)
            return user.getRole().hasPermission(Permission.PROPERTY_READ);
        } catch (Exception e) {
            log.error("Error checking property view permission: {}", e.getMessage());
            return false;
        }
    }

    public boolean canManageUser(Long targetUserId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return false;
            }

            Long userId = Long.parseLong(authentication.getName());
            Optional<User> userOpt = userService.findById(userId);
            
            if (userOpt.isEmpty()) {
                return false;
            }
            
            User user = userOpt.get();
            UserRole role = user.getRole();

            // Admin can manage all users
            if (role == UserRole.ADMIN) {
                return true;
            }

            // Users can manage themselves
            if (userId.equals(targetUserId)) {
                return true;
            }

            // Agent can manage their clients (placeholder logic)
            if (role == UserRole.AGENT) {
                // TODO: Check if target user is agent's client
                return false; // Placeholder
            }

            return false;
        } catch (Exception e) {
            log.error("Error checking user management permission: {}", e.getMessage());
            return false;
        }
    }

    public boolean canAccessAdminFeatures() {
        return hasRole("ADMIN");
    }

    public boolean canAccessAgentFeatures() {
        return hasAnyRole("AGENT", "ADMIN");
    }

    public boolean canAccessLandlordFeatures() {
        return hasAnyRole("LANDLORD", "AGENT", "ADMIN");
    }

    public boolean canAccessTenantFeatures() {
        return hasAnyRole("TENANT", "LANDLORD", "AGENT", "ADMIN");
    }

    public Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }
            return Long.parseLong(authentication.getName());
        } catch (Exception e) {
            log.error("Error getting current user ID: {}", e.getMessage());
            return null;
        }
    }

    public UserRole getCurrentUserRole() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            Long userId = Long.parseLong(authentication.getName());
            Optional<User> userOpt = userService.findById(userId);
            
            if (userOpt.isEmpty()) {
                return null;
            }
            
            User user = userOpt.get();
            return user.getRole();
        } catch (Exception e) {
            log.error("Error getting current user role: {}", e.getMessage());
            return null;
        }
    }
}