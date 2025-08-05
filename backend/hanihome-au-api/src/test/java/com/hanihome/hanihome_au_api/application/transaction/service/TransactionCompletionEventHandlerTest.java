package com.hanihome.hanihome_au_api.application.transaction.service;

import com.hanihome.hanihome_au_api.domain.transaction.event.TransactionCompletedEvent;
import com.hanihome.hanihome_au_api.domain.property.repository.PropertyRepository;
import com.hanihome.hanihome_au_api.domain.property.entity.Property;
import com.hanihome.hanihome_au_api.domain.property.valueobject.PropertyId;
import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import com.hanihome.hanihome_au_api.application.notification.service.SSENotificationService;
import com.hanihome.hanihome_au_api.application.property.service.SearchCacheEvictionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionCompletionEventHandlerTest {

    @Mock
    private PropertyRepository propertyRepository;
    
    @Mock
    private SSENotificationService notificationService;
    
    @Mock
    private SearchCacheEvictionService cacheEvictionService;
    
    @Mock
    private Property property;
    
    private TransactionCompletionEventHandler eventHandler;
    
    @BeforeEach
    void setUp() {
        eventHandler = new TransactionCompletionEventHandler(
            propertyRepository, 
            notificationService,
            cacheEvictionService
        );
    }
    
    @Test
    void handleTransactionCompleted_ShouldUpdatePropertyStatusToCompleted_WhenTransitionIsAllowed() {
        // Given
        Long transactionId = 1L;
        Long propertyId = 2L;
        Long tenantId = 3L;
        Long landlordId = 4L;
        Long agentId = 5L;
        LocalDateTime completedAt = LocalDateTime.now();
        
        TransactionCompletedEvent event = new TransactionCompletedEvent(
            transactionId, propertyId, tenantId, landlordId, agentId, completedAt
        );
        
        PropertyId expectedPropertyId = new PropertyId(propertyId);
        
        // Mock property repository
        when(propertyRepository.findById(expectedPropertyId))
            .thenReturn(Optional.of(property));
        
        // Mock property status transitions
        when(property.getStatus()).thenReturn(PropertyStatus.ACTIVE);
        when(property.getStatus().canTransitionTo(PropertyStatus.COMPLETED))
            .thenReturn(true);
        
        // When
        eventHandler.handleTransactionCompleted(event);
        
        // Then
        verify(propertyRepository).findById(expectedPropertyId);
        verify(property).changeStatus(PropertyStatus.COMPLETED);
        verify(propertyRepository).save(property);
        verify(cacheEvictionService).evictPropertyRelatedCache();
        
        // Verify notifications are sent
        verify(notificationService).sendNotification(
            eq(landlordId), 
            eq("Transaction Completed"),
            anyString(),
            eq("transaction_completed"),
            eq(transactionId.toString())
        );
        
        verify(notificationService).sendNotification(
            eq(tenantId), 
            eq("Transaction Completed"),
            anyString(),
            eq("transaction_completed"),
            eq(transactionId.toString())
        );
        
        verify(notificationService).sendNotification(
            eq(agentId), 
            eq("Transaction Completed"),
            anyString(),
            eq("transaction_completed"),
            eq(transactionId.toString())
        );
    }
    
    @Test
    void handleTransactionCompleted_ShouldUpdatePropertyStatusToRented_WhenCompletedTransitionNotAllowed() {
        // Given
        Long transactionId = 1L;
        Long propertyId = 2L;
        Long tenantId = 3L;
        Long landlordId = 4L;
        LocalDateTime completedAt = LocalDateTime.now();
        
        TransactionCompletedEvent event = new TransactionCompletedEvent(
            transactionId, propertyId, tenantId, landlordId, null, completedAt
        );
        
        PropertyId expectedPropertyId = new PropertyId(propertyId);
        
        // Mock property repository
        when(propertyRepository.findById(expectedPropertyId))
            .thenReturn(Optional.of(property));
        
        // Mock property status transitions - COMPLETED not allowed, RENTED allowed
        when(property.getStatus()).thenReturn(PropertyStatus.ACTIVE);
        when(property.getStatus().canTransitionTo(PropertyStatus.COMPLETED))
            .thenReturn(false);
        when(property.getStatus().canTransitionTo(PropertyStatus.RENTED))
            .thenReturn(true);
        
        // When
        eventHandler.handleTransactionCompleted(event);
        
        // Then
        verify(propertyRepository).findById(expectedPropertyId);
        verify(property).markAsRented();
        verify(propertyRepository).save(property);
        verify(cacheEvictionService).evictPropertyRelatedCache();
        
        // Verify notifications are sent (no agent notification)
        verify(notificationService).sendNotification(
            eq(landlordId), 
            eq("Transaction Completed"),
            anyString(),
            eq("transaction_completed"),
            eq(transactionId.toString())
        );
        
        verify(notificationService).sendNotification(
            eq(tenantId), 
            eq("Transaction Completed"),
            anyString(),
            eq("transaction_completed"),
            eq(transactionId.toString())
        );
        
        // Verify no agent notification (agentId is null)
        verify(notificationService, times(2)).sendNotification(anyLong(), anyString(), anyString(), anyString(), anyString());
    }
    
    @Test
    void handleTransactionCompleted_ShouldLogWarning_WhenNoStatusTransitionAllowed() {
        // Given
        Long transactionId = 1L;
        Long propertyId = 2L;
        Long tenantId = 3L;
        Long landlordId = 4L;
        LocalDateTime completedAt = LocalDateTime.now();
        
        TransactionCompletedEvent event = new TransactionCompletedEvent(
            transactionId, propertyId, tenantId, landlordId, null, completedAt
        );
        
        PropertyId expectedPropertyId = new PropertyId(propertyId);
        
        // Mock property repository
        when(propertyRepository.findById(expectedPropertyId))
            .thenReturn(Optional.of(property));
        
        // Mock property status transitions - neither COMPLETED nor RENTED allowed
        when(property.getStatus()).thenReturn(PropertyStatus.COMPLETED);
        when(property.getStatus().canTransitionTo(PropertyStatus.COMPLETED))
            .thenReturn(false);
        when(property.getStatus().canTransitionTo(PropertyStatus.RENTED))
            .thenReturn(false);
        
        // When
        eventHandler.handleTransactionCompleted(event);
        
        // Then
        verify(propertyRepository).findById(expectedPropertyId);
        verify(property, never()).changeStatus(any());
        verify(property, never()).markAsRented();
        verify(propertyRepository, never()).save(property);
        
        // Cache should still be evicted and notifications should still be sent
        verify(cacheEvictionService).evictPropertyRelatedCache();
        verify(notificationService, times(2)).sendNotification(anyLong(), anyString(), anyString(), anyString(), anyString());
    }
    
    @Test
    void handleTransactionCompleted_ShouldThrowException_WhenPropertyNotFound() {
        // Given
        Long transactionId = 1L;
        Long propertyId = 2L;
        Long tenantId = 3L;
        Long landlordId = 4L;
        LocalDateTime completedAt = LocalDateTime.now();
        
        TransactionCompletedEvent event = new TransactionCompletedEvent(
            transactionId, propertyId, tenantId, landlordId, null, completedAt
        );
        
        PropertyId expectedPropertyId = new PropertyId(propertyId);
        
        // Mock property repository to return empty
        when(propertyRepository.findById(expectedPropertyId))
            .thenReturn(Optional.empty());
        
        // When/Then - exception should be caught and logged
        eventHandler.handleTransactionCompleted(event);
        
        // Verify repository was called but no property operations occurred
        verify(propertyRepository).findById(expectedPropertyId);
        verify(property, never()).changeStatus(any());
        verify(propertyRepository, never()).save(property);
    }
}