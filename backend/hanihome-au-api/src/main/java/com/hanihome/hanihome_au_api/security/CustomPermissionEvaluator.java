package com.hanihome.hanihome_au_api.security;

import com.hanihome.hanihome_au_api.domain.user.entity.User;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserRole;
import com.hanihome.hanihome_au_api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final UserService userService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        try {
            Long userId = Long.parseLong(authentication.getName());
            Optional<User> userOpt = userService.findById(userId);
            
            if (userOpt.isEmpty()) {
                return false;
            }
            
            User user = userOpt.get();
            
            if (permission instanceof String permissionString) {
                return user.hasPermission(permissionString);
            }
            
            return false;
        } catch (Exception e) {
            log.error("Error evaluating permission: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        try {
            Long userId = Long.parseLong(authentication.getName());
            Optional<User> userOpt = userService.findById(userId);
            
            if (userOpt.isEmpty()) {
                return false;
            }
            
            User user = userOpt.get();
            
            // Handle specific resource-based permissions
            return switch (targetType.toLowerCase()) {
                case "property" -> evaluatePropertyPermission(user, targetId, permission);
                case "user" -> evaluateUserPermission(user, targetId, permission);
                case "application" -> evaluateApplicationPermission(user, targetId, permission);
                case "review" -> evaluateReviewPermission(user, targetId, permission);
                case "payment" -> evaluatePaymentPermission(user, targetId, permission);
                default -> hasPermission(authentication, null, permission);
            };
        } catch (Exception e) {
            log.error("Error evaluating resource permission: {}", e.getMessage());
            return false;
        }
    }

    private boolean evaluatePropertyPermission(User user, Serializable propertyId, Object permission) {
        if (permission instanceof String permissionString) {
            return user.canManageProperty() || 
                   (permissionString.contains("READ") && user.getRole() == UserRole.TENANT);
        }
        return false;
    }

    private boolean evaluateUserPermission(User user, Serializable targetUserId, Object permission) {
        UserRole role = user.getRole();
        
        // Admin has full user management access
        if (role == UserRole.ADMIN) {
            return true;
        }

        // Users can read/update their own information
        Long currentUserId = user.getId().getValue();
        if (currentUserId.equals(targetUserId)) {
            return permission instanceof String permString && 
                   (permString.contains("READ") || permString.contains("UPDATE"));
        }

        // Agent can read user info for their clients
        if (role == UserRole.AGENT && permission instanceof String permString && permString.contains("READ")) {
            return true;
        }

        return false;
    }

    private boolean evaluateApplicationPermission(User user, Serializable applicationId, Object permission) {
        UserRole role = user.getRole();
        return role == UserRole.ADMIN || role == UserRole.AGENT || role == UserRole.LANDLORD || 
               (role == UserRole.TENANT && permission instanceof String permString && permString.contains("READ"));
    }

    private boolean evaluateReviewPermission(User user, Serializable reviewId, Object permission) {
        UserRole role = user.getRole();
        return role == UserRole.ADMIN || role == UserRole.AGENT || role == UserRole.LANDLORD || role == UserRole.TENANT;
    }

    private boolean evaluatePaymentPermission(User user, Serializable paymentId, Object permission) {
        UserRole role = user.getRole();
        return role == UserRole.ADMIN || role == UserRole.AGENT || 
               (role == UserRole.LANDLORD && permission instanceof String permString && 
                (permString.contains("READ") || permString.contains("REFUND"))) ||
               (role == UserRole.TENANT && permission instanceof String permString && 
                (permString.contains("CREATE") || permString.contains("READ")));
    }
}