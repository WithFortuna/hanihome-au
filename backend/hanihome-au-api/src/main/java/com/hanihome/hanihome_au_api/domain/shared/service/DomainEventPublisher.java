package com.hanihome.hanihome_au_api.domain.shared.service;

/**
 * Domain service for publishing domain events
 * Abstracts the event publishing mechanism from domain layer
 */
public interface DomainEventPublisher {
    
    /**
     * Publishes a domain event
     */
    void publish(Object event);
    
    /**
     * Publishes multiple domain events
     */
    default void publishAll(Iterable<Object> events) {
        events.forEach(this::publish);
    }
}