package com.hanihome.hanihome_au_api.infrastructure.event;

import com.hanihome.hanihome_au_api.domain.shared.event.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {
    
    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(Object domainEvent) {
        applicationEventPublisher.publishEvent(domainEvent);
    }

    @Override
    public void publishAll(List<Object> domainEvents) {
        domainEvents.forEach(this::publish);
    }
}