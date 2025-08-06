package com.hanihome.hanihome_au_api.infrastructure.config;

import com.hanihome.hanihome_au_api.application.property.service.PropertyApplicationService;
import com.hanihome.hanihome_au_api.application.property.service.SearchCacheEvictionService;
import com.hanihome.hanihome_au_api.application.property.usecase.CreatePropertyUseCase;
import com.hanihome.hanihome_au_api.application.user.usecase.CreateUserUseCase;
import com.hanihome.hanihome_au_api.domain.property.repository.PropertyRepository;
import com.hanihome.hanihome_au_api.domain.property.service.PropertyDomainService;
import com.hanihome.hanihome_au_api.domain.shared.event.DomainEventPublisher;
import com.hanihome.hanihome_au_api.domain.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for DDD components
 * Defines beans for domain services, use cases, and application services
 */
@Configuration
public class DddConfiguration {

    @Bean
    public PropertyDomainService propertyDomainService(PropertyRepository propertyRepository) {
        return new PropertyDomainService(propertyRepository);
    }

    @Bean
    public CreateUserUseCase createUserUseCase(UserRepository userRepository) {
        return new CreateUserUseCase(userRepository);
    }

    @Bean
    public CreatePropertyUseCase createPropertyUseCase(PropertyRepository propertyRepository) {
        return new CreatePropertyUseCase(propertyRepository);
    }

    @Bean
    public PropertyApplicationService propertyApplicationService(
            PropertyRepository propertyRepository,
            PropertyDomainService propertyDomainService,
            CreatePropertyUseCase createPropertyUseCase, 
            DomainEventPublisher domainEventPublisher,
            SearchCacheEvictionService cacheEvictionService) {
        return new PropertyApplicationService(propertyRepository, propertyDomainService, createPropertyUseCase, domainEventPublisher, cacheEvictionService);
    }
}