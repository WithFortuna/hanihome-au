package com.hanihome.hanihome_au_api.domain.user.event;

import com.hanihome.hanihome_au_api.domain.user.valueobject.Email;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserRole;

import java.time.LocalDateTime;

public class UserRegisteredEvent {
    private final UserId userId;
    private final Email email;
    private final String name;
    private final UserRole role;
    private final LocalDateTime occurredAt;

    public UserRegisteredEvent(UserId userId, Email email, String name, UserRole role) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.occurredAt = LocalDateTime.now();
    }

    public UserId getUserId() { return userId; }
    public Email getEmail() { return email; }
    public String getName() { return name; }
    public UserRole getRole() { return role; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}