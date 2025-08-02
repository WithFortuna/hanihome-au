package com.hanihome.hanihome_au_api.domain.shared.event;

public interface DomainEventPublisher {
    
    void publish(Object domainEvent);
    
    void publishAll(java.util.List<Object> domainEvents);
}