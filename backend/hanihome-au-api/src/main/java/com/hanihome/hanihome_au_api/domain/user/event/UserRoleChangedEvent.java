package com.hanihome.hanihome_au_api.domain.user.event;

import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserRole;

import java.time.LocalDateTime;

public class UserRoleChangedEvent {
    private final UserId userId;
    private final UserRole oldRole;
    private final UserRole newRole;
    private final LocalDateTime occurredAt;

    public UserRoleChangedEvent(UserId userId, UserRole oldRole, UserRole newRole) {
        this.userId = userId;
        this.oldRole = oldRole;
        this.newRole = newRole;
        this.occurredAt = LocalDateTime.now();
    }

    public UserId getUserId() { return userId; }
    public UserRole getOldRole() { return oldRole; }
    public UserRole getNewRole() { return newRole; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}