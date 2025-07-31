package com.hanihome.hanihome_au_api.dto.response;

import com.hanihome.hanihome_au_api.domain.enums.Permission;
import com.hanihome.hanihome_au_api.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemDto {
    
    private String id;
    private String title;
    private String path;
    private String icon;
    private Set<Permission> requiredPermissions;
    private Set<UserRole> requiredRoles;
    private List<MenuItemDto> children;
    private boolean visible;
    private boolean enabled;
    private String description;
    private Integer order;

    public static MenuItemDto createSimpleMenuItem(String id, String title, String path, String icon) {
        return MenuItemDto.builder()
                .id(id)
                .title(title)
                .path(path)
                .icon(icon)
                .requiredPermissions(Set.of())
                .requiredRoles(Set.of())
                .visible(true)
                .enabled(true)
                .build();
    }

    public static MenuItemDto createMenuItemWithPermission(String id, String title, String path, 
                                                          String icon, Permission permission, UserRole... roles) {
        return MenuItemDto.builder()
                .id(id)
                .title(title)
                .path(path)
                .icon(icon)
                .requiredPermissions(Set.of(permission))
                .requiredRoles(Set.of(roles))
                .visible(true)
                .enabled(true)
                .build();
    }

    public static MenuItemDto createMenuItemWithRoles(String id, String title, String path, 
                                                      String icon, UserRole... roles) {
        return MenuItemDto.builder()
                .id(id)
                .title(title)
                .path(path)
                .icon(icon)
                .requiredPermissions(Set.of())
                .requiredRoles(Set.of(roles))
                .visible(true)
                .enabled(true)
                .build();
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public boolean isAccessibleByRole(UserRole role) {
        if (requiredRoles.isEmpty() && requiredPermissions.isEmpty()) {
            return true;
        }

        // Check if user's role is in the required roles
        if (!requiredRoles.isEmpty() && !requiredRoles.contains(role)) {
            return false;
        }

        // Check if user has all required permissions
        if (!requiredPermissions.isEmpty()) {
            return requiredPermissions.stream().allMatch(role::hasPermission);
        }

        return true;
    }

    public MenuItemDto filterForRole(UserRole role) {
        if (!isAccessibleByRole(role)) {
            return null;
        }

        MenuItemDto filtered = MenuItemDto.builder()
                .id(this.id)
                .title(this.title)
                .path(this.path)
                .icon(this.icon)
                .requiredPermissions(this.requiredPermissions)
                .requiredRoles(this.requiredRoles)
                .visible(this.visible)
                .enabled(this.enabled)
                .description(this.description)
                .order(this.order)
                .build();

        if (hasChildren()) {
            List<MenuItemDto> filteredChildren = children.stream()
                    .map(child -> child.filterForRole(role))
                    .filter(child -> child != null)
                    .toList();
            
            if (!filteredChildren.isEmpty()) {
                filtered.setChildren(filteredChildren);
            }
        }

        return filtered;
    }
}