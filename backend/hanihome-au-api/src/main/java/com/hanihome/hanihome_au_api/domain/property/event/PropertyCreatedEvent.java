package com.hanihome.hanihome_au_api.domain.property.event;

import com.hanihome.hanihome_au_api.domain.property.valueobject.PropertyId;
import com.hanihome.hanihome_au_api.domain.enums.PropertyType;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;

import java.time.LocalDateTime;

public class PropertyCreatedEvent {
    private final PropertyId propertyId;
    private final UserId ownerId;
    private final String title;
    private final PropertyType type;
    private final LocalDateTime occurredAt;

    public PropertyCreatedEvent(PropertyId propertyId, UserId ownerId, String title, PropertyType type) {
        this.propertyId = propertyId;
        this.ownerId = ownerId;
        this.title = title;
        this.type = type;
        this.occurredAt = LocalDateTime.now();
    }

    public PropertyId getPropertyId() { return propertyId; }
    public UserId getOwnerId() { return ownerId; }
    public String getTitle() { return title; }
    public PropertyType getType() { return type; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}