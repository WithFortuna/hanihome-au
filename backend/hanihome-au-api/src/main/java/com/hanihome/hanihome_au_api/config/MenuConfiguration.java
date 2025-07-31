package com.hanihome.hanihome_au_api.config;

import com.hanihome.hanihome_au_api.domain.enums.Permission;
import com.hanihome.hanihome_au_api.domain.enums.UserRole;
import com.hanihome.hanihome_au_api.dto.response.MenuItemDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MenuConfiguration {

    public List<MenuItemDto> getMenuForRole(UserRole role) {
        return getAllMenuItems().stream()
                .filter(menuItem -> canAccessMenu(role, menuItem))
                .collect(Collectors.toList());
    }

    public List<String> getAvailableFeaturesForRole(UserRole role) {
        return getAllFeatures().stream()
                .filter(feature -> canAccessFeature(role, feature))
                .collect(Collectors.toList());
    }

    private List<MenuItemDto> getAllMenuItems() {
        return List.of(
            // Dashboard menus
            MenuItemDto.builder()
                .id("dashboard")
                .title("Dashboard")
                .path("/dashboard")
                .icon("dashboard")
                .requiredPermissions(Set.of())
                .requiredRoles(Set.of(UserRole.TENANT, UserRole.LANDLORD, UserRole.AGENT, UserRole.ADMIN))
                .build(),

            // Property management menus
            MenuItemDto.builder()
                .id("properties")
                .title("Properties")
                .path("/properties")
                .icon("home")
                .requiredPermissions(Set.of(Permission.PROPERTY_READ))
                .requiredRoles(Set.of(UserRole.TENANT, UserRole.LANDLORD, UserRole.AGENT, UserRole.ADMIN))
                .children(List.of(
                    MenuItemDto.builder()
                        .id("property-search")
                        .title("Search Properties")
                        .path("/properties/search")
                        .icon("search")
                        .requiredPermissions(Set.of(Permission.PROPERTY_READ))
                        .requiredRoles(Set.of(UserRole.TENANT, UserRole.LANDLORD, UserRole.AGENT, UserRole.ADMIN))
                        .build(),
                    MenuItemDto.builder()
                        .id("my-properties")
                        .title("My Properties")
                        .path("/properties/my-properties")
                        .icon("my-home")
                        .requiredPermissions(Set.of(Permission.PROPERTY_CREATE))
                        .requiredRoles(Set.of(UserRole.LANDLORD, UserRole.AGENT, UserRole.ADMIN))
                        .build(),
                    MenuItemDto.builder()
                        .id("add-property")
                        .title("Add Property")
                        .path("/properties/add")
                        .icon("add-home")
                        .requiredPermissions(Set.of(Permission.PROPERTY_CREATE))
                        .requiredRoles(Set.of(UserRole.LANDLORD, UserRole.AGENT, UserRole.ADMIN))
                        .build()
                ))
                .build(),

            // Application management menus
            MenuItemDto.builder()
                .id("applications")
                .title("Applications")
                .path("/applications")
                .icon("application")
                .requiredPermissions(Set.of(Permission.APPLICATION_READ))
                .requiredRoles(Set.of(UserRole.TENANT, UserRole.LANDLORD, UserRole.AGENT, UserRole.ADMIN))
                .children(List.of(
                    MenuItemDto.builder()
                        .id("my-applications")
                        .title("My Applications")
                        .path("/applications/my-applications")
                        .icon("my-applications")
                        .requiredPermissions(Set.of(Permission.APPLICATION_READ))
                        .requiredRoles(Set.of(UserRole.TENANT))
                        .build(),
                    MenuItemDto.builder()
                        .id("incoming-applications")
                        .title("Incoming Applications")
                        .path("/applications/incoming")
                        .icon("inbox")
                        .requiredPermissions(Set.of(Permission.APPLICATION_APPROVE))
                        .requiredRoles(Set.of(UserRole.LANDLORD, UserRole.AGENT, UserRole.ADMIN))
                        .build()
                ))
                .build(),

            // Payment management menus
            MenuItemDto.builder()
                .id("payments")
                .title("Payments")
                .path("/payments")
                .icon("payment")
                .requiredPermissions(Set.of(Permission.PAYMENT_READ))
                .requiredRoles(Set.of(UserRole.TENANT, UserRole.LANDLORD, UserRole.AGENT, UserRole.ADMIN))
                .children(List.of(
                    MenuItemDto.builder()
                        .id("payment-history")
                        .title("Payment History")
                        .path("/payments/history")
                        .icon("history")
                        .requiredPermissions(Set.of(Permission.PAYMENT_READ))
                        .requiredRoles(Set.of(UserRole.TENANT, UserRole.LANDLORD, UserRole.AGENT, UserRole.ADMIN))
                        .build(),
                    MenuItemDto.builder()
                        .id("income-reports")
                        .title("Income Reports")
                        .path("/payments/income-reports")
                        .icon("chart")
                        .requiredPermissions(Set.of(Permission.LANDLORD_INCOME))
                        .requiredRoles(Set.of(UserRole.LANDLORD, UserRole.AGENT, UserRole.ADMIN))
                        .build()
                ))
                .build(),

            // User management menus
            MenuItemDto.builder()
                .id("users")
                .title("User Management")
                .path("/users")
                .icon("users")
                .requiredPermissions(Set.of(Permission.ADMIN_USERS))
                .requiredRoles(Set.of(UserRole.ADMIN))
                .children(List.of(
                    MenuItemDto.builder()
                        .id("all-users")
                        .title("All Users")
                        .path("/users/all")
                        .icon("users-list")
                        .requiredPermissions(Set.of(Permission.ADMIN_USERS))
                        .requiredRoles(Set.of(UserRole.ADMIN))
                        .build(),
                    MenuItemDto.builder()
                        .id("user-roles")
                        .title("Role Management")
                        .path("/users/roles")
                        .icon("roles")
                        .requiredPermissions(Set.of(Permission.USER_ROLE_CHANGE))
                        .requiredRoles(Set.of(UserRole.ADMIN))
                        .build()
                ))
                .build(),

            // Agent-specific menus
            MenuItemDto.builder()
                .id("agent-dashboard")
                .title("Agent Dashboard")
                .path("/agent")
                .icon("agent")
                .requiredPermissions(Set.of(Permission.AGENT_CLIENTS))
                .requiredRoles(Set.of(UserRole.AGENT, UserRole.ADMIN))
                .children(List.of(
                    MenuItemDto.builder()
                        .id("my-clients")
                        .title("My Clients")
                        .path("/agent/clients")
                        .icon("clients")
                        .requiredPermissions(Set.of(Permission.AGENT_CLIENTS))
                        .requiredRoles(Set.of(UserRole.AGENT, UserRole.ADMIN))
                        .build(),
                    MenuItemDto.builder()
                        .id("commission-reports")
                        .title("Commission Reports")
                        .path("/agent/commission")
                        .icon("commission")
                        .requiredPermissions(Set.of(Permission.AGENT_COMMISSION))
                        .requiredRoles(Set.of(UserRole.AGENT, UserRole.ADMIN))
                        .build()
                ))
                .build(),

            // Admin-specific menus
            MenuItemDto.builder()
                .id("admin")
                .title("Administration")
                .path("/admin")
                .icon("admin")
                .requiredPermissions(Set.of(Permission.ADMIN_DASHBOARD))
                .requiredRoles(Set.of(UserRole.ADMIN))
                .children(List.of(
                    MenuItemDto.builder()
                        .id("system-settings")
                        .title("System Settings")
                        .path("/admin/settings")
                        .icon("settings")
                        .requiredPermissions(Set.of(Permission.ADMIN_SETTINGS))
                        .requiredRoles(Set.of(UserRole.ADMIN))
                        .build(),
                    MenuItemDto.builder()
                        .id("system-reports")
                        .title("System Reports")
                        .path("/admin/reports")
                        .icon("reports")
                        .requiredPermissions(Set.of(Permission.ADMIN_REPORTS))
                        .requiredRoles(Set.of(UserRole.ADMIN))
                        .build(),
                    MenuItemDto.builder()
                        .id("system-health")
                        .title("System Health")
                        .path("/admin/health")
                        .icon("health")
                        .requiredPermissions(Set.of(Permission.SYSTEM_HEALTH))
                        .requiredRoles(Set.of(UserRole.ADMIN))
                        .build()
                ))
                .build(),

            // Profile menu (available to all authenticated users)
            MenuItemDto.builder()
                .id("profile")
                .title("Profile")
                .path("/profile")
                .icon("profile")
                .requiredPermissions(Set.of(Permission.USER_READ))
                .requiredRoles(Set.of(UserRole.TENANT, UserRole.LANDLORD, UserRole.AGENT, UserRole.ADMIN))
                .build()
        );
    }

    private List<String> getAllFeatures() {
        return List.of(
            // Property features
            "property.create",
            "property.edit",
            "property.delete",
            "property.approve",
            "property.view.all",
            "property.view.own",
            
            // Application features
            "application.create",
            "application.view.own",
            "application.view.all",
            "application.approve",
            "application.reject",
            
            // Payment features
            "payment.create",
            "payment.view",
            "payment.refund",
            "payment.reports",
            
            // User features
            "user.view.profile",
            "user.edit.profile",
            "user.view.all",
            "user.manage.roles",
            "user.delete",
            
            // Review features
            "review.create",
            "review.edit.own",
            "review.moderate",
            "review.delete",
            
            // Agent features
            "agent.clients.manage",
            "agent.commission.view",
            "agent.commission.settings",
            
            // Landlord features
            "landlord.tenants.manage",
            "landlord.income.reports",
            
            // Admin features
            "admin.dashboard",
            "admin.settings",
            "admin.reports",
            "admin.system.health",
            "admin.logs",
            
            // Export features
            "export.properties",
            "export.applications",
            "export.payments",
            "export.reports"
        );
    }

    private boolean canAccessMenu(UserRole role, MenuItemDto menuItem) {
        // Check if user's role is in the required roles
        if (!menuItem.getRequiredRoles().isEmpty() && !menuItem.getRequiredRoles().contains(role)) {
            return false;
        }

        // Check if user has all required permissions
        if (!menuItem.getRequiredPermissions().isEmpty()) {
            return menuItem.getRequiredPermissions().stream()
                    .allMatch(role::hasPermission);
        }

        return true;
    }

    private boolean canAccessFeature(UserRole role, String feature) {
        return switch (feature) {
            // Property features
            case "property.create" -> role.hasPermission(Permission.PROPERTY_CREATE);
            case "property.edit" -> role.hasPermission(Permission.PROPERTY_UPDATE);
            case "property.delete" -> role.hasPermission(Permission.PROPERTY_DELETE);
            case "property.approve" -> role.hasPermission(Permission.PROPERTY_APPROVE);
            case "property.view.all" -> role.hasPermission(Permission.PROPERTY_READ);
            case "property.view.own" -> role.hasPermission(Permission.PROPERTY_READ);
            
            // Application features
            case "application.create" -> role.hasPermission(Permission.APPLICATION_CREATE);
            case "application.view.own" -> role.hasPermission(Permission.APPLICATION_READ);
            case "application.view.all" -> role.hasPermission(Permission.APPLICATION_READ) && 
                                         (role == UserRole.AGENT || role == UserRole.ADMIN);
            case "application.approve" -> role.hasPermission(Permission.APPLICATION_APPROVE);
            case "application.reject" -> role.hasPermission(Permission.APPLICATION_APPROVE);
            
            // Payment features
            case "payment.create" -> role.hasPermission(Permission.PAYMENT_CREATE);
            case "payment.view" -> role.hasPermission(Permission.PAYMENT_READ);
            case "payment.refund" -> role.hasPermission(Permission.PAYMENT_REFUND);
            case "payment.reports" -> role.hasPermission(Permission.LANDLORD_INCOME) || 
                                    role.hasPermission(Permission.AGENT_COMMISSION);
            
            // User features
            case "user.view.profile" -> role.hasPermission(Permission.USER_READ);
            case "user.edit.profile" -> role.hasPermission(Permission.USER_UPDATE);
            case "user.view.all" -> role.hasPermission(Permission.ADMIN_USERS);
            case "user.manage.roles" -> role.hasPermission(Permission.USER_ROLE_CHANGE);
            case "user.delete" -> role.hasPermission(Permission.USER_DELETE);
            
            // Review features
            case "review.create" -> role.hasPermission(Permission.REVIEW_CREATE);
            case "review.edit.own" -> role.hasPermission(Permission.REVIEW_UPDATE);
            case "review.moderate" -> role.hasPermission(Permission.REVIEW_MODERATE);
            case "review.delete" -> role.hasPermission(Permission.REVIEW_DELETE);
            
            // Agent features
            case "agent.clients.manage" -> role.hasPermission(Permission.AGENT_CLIENTS);
            case "agent.commission.view" -> role.hasPermission(Permission.AGENT_COMMISSION);
            case "agent.commission.settings" -> role.hasPermission(Permission.AGENT_COMMISSION);
            
            // Landlord features
            case "landlord.tenants.manage" -> role.hasPermission(Permission.LANDLORD_TENANTS);
            case "landlord.income.reports" -> role.hasPermission(Permission.LANDLORD_INCOME);
            
            // Admin features
            case "admin.dashboard" -> role.hasPermission(Permission.ADMIN_DASHBOARD);
            case "admin.settings" -> role.hasPermission(Permission.ADMIN_SETTINGS);
            case "admin.reports" -> role.hasPermission(Permission.ADMIN_REPORTS);
            case "admin.system.health" -> role.hasPermission(Permission.SYSTEM_HEALTH);
            case "admin.logs" -> role.hasPermission(Permission.SYSTEM_LOGS);
            
            // Export features (various permission requirements)
            case "export.properties" -> role.hasPermission(Permission.PROPERTY_READ);
            case "export.applications" -> role.hasPermission(Permission.APPLICATION_READ);
            case "export.payments" -> role.hasPermission(Permission.PAYMENT_READ);
            case "export.reports" -> role.hasPermission(Permission.ADMIN_REPORTS);
            
            default -> false;
        };
    }
}