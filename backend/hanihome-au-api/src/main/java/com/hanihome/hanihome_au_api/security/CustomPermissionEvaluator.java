package com.hanihome.hanihome_au_api.security;

import com.hanihome.hanihome_au_api.domain.entity.User;
import com.hanihome.hanihome_au_api.domain.enums.Permission;
import com.hanihome.hanihome_au_api.domain.enums.UserRole;
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
                Permission requiredPermission = Permission.fromString(permissionString);
                return user.getRole().hasPermission(requiredPermission);
            }
            
            if (permission instanceof Permission requiredPermission) {
                return user.getRole().hasPermission(requiredPermission);
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
        UserRole role = user.getRole();
        Permission requiredPermission = parsePermission(permission);
        
        if (requiredPermission == null) {
            return false;
        }

        // Admin and Agent have full access
        if (role == UserRole.ADMIN || role == UserRole.AGENT) {
            return role.hasPermission(requiredPermission);
        }

        // Landlord can manage their own properties
        if (role == UserRole.LANDLORD) {
            // TODO: Check if user owns this property
            return role.hasPermission(requiredPermission);
        }

        // Tenant can view properties and create applications
        if (role == UserRole.TENANT) {
            return requiredPermission == Permission.PROPERTY_READ || 
                   requiredPermission == Permission.APPLICATION_CREATE;
        }

        return false;
    }

    private boolean evaluateUserPermission(User user, Serializable targetUserId, Object permission) {
        UserRole role = user.getRole();
        Permission requiredPermission = parsePermission(permission);
        
        if (requiredPermission == null) {
            return false;
        }

        // Admin has full user management access
        if (role == UserRole.ADMIN) {
            return role.hasPermission(requiredPermission);
        }

        // Users can read/update their own information
        Long currentUserId = user.getId();
        if (currentUserId.equals(targetUserId)) {
            return requiredPermission == Permission.USER_READ || 
                   requiredPermission == Permission.USER_UPDATE;
        }

        // Agent can read user info for their clients
        if (role == UserRole.AGENT && requiredPermission == Permission.USER_READ) {
            // TODO: Check if target user is agent's client
            return true;
        }

        return false;
    }

    private boolean evaluateApplicationPermission(User user, Serializable applicationId, Object permission) {
        UserRole role = user.getRole();
        Permission requiredPermission = parsePermission(permission);
        
        if (requiredPermission == null) {
            return false;
        }

        // Admin and Agent have full access
        if (role == UserRole.ADMIN || role == UserRole.AGENT) {
            return role.hasPermission(requiredPermission);
        }

        // Landlord can view and approve applications for their properties
        if (role == UserRole.LANDLORD) {
            // TODO: Check if application is for landlord's property
            return requiredPermission == Permission.APPLICATION_READ || 
                   requiredPermission == Permission.APPLICATION_APPROVE;
        }

        // Tenant can manage their own applications
        if (role == UserRole.TENANT) {
            // TODO: Check if user owns this application
            return requiredPermission == Permission.APPLICATION_READ || 
                   requiredPermission == Permission.APPLICATION_UPDATE;
        }

        return false;
    }

    private boolean evaluateReviewPermission(User user, Serializable reviewId, Object permission) {
        UserRole role = user.getRole();
        Permission requiredPermission = parsePermission(permission);
        
        if (requiredPermission == null) {
            return false;
        }

        // Admin has full access
        if (role == UserRole.ADMIN) {
            return role.hasPermission(requiredPermission);
        }

        // Agent and Landlord can moderate reviews
        if ((role == UserRole.AGENT || role == UserRole.LANDLORD) && 
            requiredPermission == Permission.REVIEW_MODERATE) {
            return true;
        }

        // Users can manage their own reviews
        // TODO: Check if user owns this review
        if (requiredPermission == Permission.REVIEW_UPDATE || requiredPermission == Permission.REVIEW_DELETE) {
            return role.hasPermission(Permission.REVIEW_UPDATE);
        }

        return role.hasPermission(requiredPermission);
    }

    private boolean evaluatePaymentPermission(User user, Serializable paymentId, Object permission) {
        UserRole role = user.getRole();
        Permission requiredPermission = parsePermission(permission);
        
        if (requiredPermission == null) {
            return false;
        }

        // Admin and Agent have full payment access
        if (role == UserRole.ADMIN || role == UserRole.AGENT) {
            return role.hasPermission(requiredPermission);
        }

        // Landlord can view payments and process refunds for their properties
        if (role == UserRole.LANDLORD) {
            return requiredPermission == Permission.PAYMENT_READ || 
                   requiredPermission == Permission.PAYMENT_REFUND;
        }

        // Tenant can create payments and view their own payment history
        if (role == UserRole.TENANT) {
            return requiredPermission == Permission.PAYMENT_CREATE || 
                   requiredPermission == Permission.PAYMENT_READ;
        }

        return false;
    }

    private Permission parsePermission(Object permission) {
        try {
            if (permission instanceof String permissionString) {
                return Permission.fromString(permissionString);
            }
            if (permission instanceof Permission requiredPermission) {
                return requiredPermission;
            }
            return null;
        } catch (Exception e) {
            log.error("Error parsing permission: {}", e.getMessage());
            return null;
        }
    }
}