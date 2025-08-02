package com.hanihome.hanihome_au_api.domain.shared.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for aggregate roots in DDD
 * Manages domain events and ensures consistency
 */
public abstract class AggregateRoot<ID> {
    
    private final List<Object> domainEvents = new ArrayList<>();

    /**
     * Add a domain event to be published
     */
    protected void addDomainEvent(Object event) {
        domainEvents.add(event);
    }

    /**
     * Get all domain events
     */
    public List<Object> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    /**
     * Clear all domain events
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }

    /**
     * Check if there are any domain events
     */
    public boolean hasDomainEvents() {
        return !domainEvents.isEmpty();
    }

    /**
     * Get the aggregate root ID
     */
    public abstract ID getId();
}