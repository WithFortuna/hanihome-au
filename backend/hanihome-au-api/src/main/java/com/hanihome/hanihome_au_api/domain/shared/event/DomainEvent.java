package com.hanihome.hanihome_au_api.domain.shared.event;

import java.time.LocalDateTime;

/**
 * Marker interface for domain events
 * All domain events should implement this interface
 */
public interface DomainEvent {
    
    /**
     * Returns the timestamp when the event occurred
     */
    LocalDateTime occurredAt();
}