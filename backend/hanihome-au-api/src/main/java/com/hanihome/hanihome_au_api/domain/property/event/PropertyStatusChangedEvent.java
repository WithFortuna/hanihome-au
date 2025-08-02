package com.hanihome.hanihome_au_api.domain.property.event;

import com.hanihome.hanihome_au_api.domain.property.valueobject.PropertyId;
import com.hanihome.hanihome_au_api.domain.property.valueobject.PropertyStatus;

import java.time.LocalDateTime;

public class PropertyStatusChangedEvent {
    private final PropertyId propertyId;
    private final PropertyStatus oldStatus;
    private final PropertyStatus newStatus;
    private final LocalDateTime occurredAt;

    public PropertyStatusChangedEvent(PropertyId propertyId, PropertyStatus oldStatus, PropertyStatus newStatus) {
        this.propertyId = propertyId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.occurredAt = LocalDateTime.now();
    }

    public PropertyId getPropertyId() { return propertyId; }
    public PropertyStatus getOldStatus() { return oldStatus; }
    public PropertyStatus getNewStatus() { return newStatus; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
}