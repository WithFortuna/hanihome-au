package com.hanihome.hanihome_au_api.application.transaction.service;

import com.hanihome.hanihome_au_api.domain.transaction.event.TransactionCompletedEvent;
import com.hanihome.hanihome_au_api.domain.property.repository.PropertyRepository;
import com.hanihome.hanihome_au_api.domain.property.entity.Property;
import com.hanihome.hanihome_au_api.domain.property.valueobject.PropertyId;
import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import com.hanihome.hanihome_au_api.application.notification.service.SSENotificationService;
import com.hanihome.hanihome_au_api.application.property.service.SearchCacheEvictionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event handler for transaction completion events
 * Handles automatic property status updates and related business processes
 */
@Service
@Transactional
public class TransactionCompletionEventHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionCompletionEventHandler.class);
    
    private final PropertyRepository propertyRepository;
    private final SSENotificationService notificationService;
    private final SearchCacheEvictionService cacheEvictionService;
    
    public TransactionCompletionEventHandler(
            PropertyRepository propertyRepository,
            SSENotificationService notificationService,
            SearchCacheEvictionService cacheEvictionService) {
        this.propertyRepository = propertyRepository;
        this.notificationService = notificationService;
        this.cacheEvictionService = cacheEvictionService;
    }
    
    /**
     * Handles transaction completion events by updating related property status
     * and sending notifications
     */
    @EventListener
    @Async
    public void handleTransactionCompleted(TransactionCompletedEvent event) {
        try {
            logger.info("Processing TransactionCompletedEvent for transaction {} and property {}", 
                       event.getTransactionId(), event.getPropertyId());
            
            // Update property status to RENTED/COMPLETED
            updatePropertyStatus(event);
            
            // Clear search cache to ensure updated status is reflected in search results
            cacheEvictionService.evictPropertyRelatedCache();
            
            // Send notifications to relevant parties
            sendCompletionNotifications(event);
            
            // Update related properties in same building (if applicable)
            updateRelatedProperties(event);
            
            logger.info("Successfully processed TransactionCompletedEvent for transaction {}", 
                       event.getTransactionId());
            
        } catch (Exception e) {
            logger.error("Failed to process TransactionCompletedEvent for transaction {}: {}", 
                        event.getTransactionId(), e.getMessage(), e);
            
            // Implement compensation logic or retry mechanism here
            handleEventProcessingFailure(event, e);
        }
    }
    
    /**
     * Updates the property status when transaction is completed
     */
    private void updatePropertyStatus(TransactionCompletedEvent event) {
        PropertyId propertyId = new PropertyId(event.getPropertyId());
        
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new IllegalStateException(
                    "Property not found with ID: " + event.getPropertyId()));
        
        // Check if property can transition to completed status
        if (property.getStatus().canTransitionTo(PropertyStatus.COMPLETED)) {
            property.changeStatus(PropertyStatus.COMPLETED);
            propertyRepository.save(property);
            
            logger.info("Property {} status updated to COMPLETED after transaction completion", 
                       event.getPropertyId());
        } else {
            // Fallback to RENTED status if COMPLETED is not available
            if (property.getStatus().canTransitionTo(PropertyStatus.RENTED)) {
                property.markAsRented();
                propertyRepository.save(property);
                
                logger.info("Property {} status updated to RENTED after transaction completion", 
                           event.getPropertyId());
            } else {
                logger.warn("Cannot update property {} status from {} after transaction completion", 
                           event.getPropertyId(), property.getStatus());
            }
        }
    }
    
    /**
     * Sends completion notifications to relevant parties
     */
    private void sendCompletionNotifications(TransactionCompletedEvent event) {
        try {
            // Notify landlord
            notificationService.sendNotification(
                event.getLandlordUserId(),
                "Transaction Completed",
                "Your property rental transaction has been successfully completed.",
                "transaction_completed",
                event.getTransactionId().toString()
            );
            
            // Notify tenant
            notificationService.sendNotification(
                event.getTenantUserId(),
                "Transaction Completed", 
                "Your rental agreement has been successfully completed.",
                "transaction_completed",
                event.getTransactionId().toString()
            );
            
            // Notify agent if involved
            if (event.getAgentUserId() != null) {
                notificationService.sendNotification(
                    event.getAgentUserId(),
                    "Transaction Completed",
                    "The rental transaction you managed has been successfully completed.",
                    "transaction_completed",
                    event.getTransactionId().toString()
                );
            }
            
            logger.debug("Sent completion notifications for transaction {}", event.getTransactionId());
            
        } catch (Exception e) {
            logger.warn("Failed to send completion notifications for transaction {}: {}", 
                       event.getTransactionId(), e.getMessage());
            // Continue processing even if notifications fail
        }
    }
    
    /**
     * Updates related properties in the same building or complex
     */
    private void updateRelatedProperties(TransactionCompletedEvent event) {
        try {
            // Find related properties by the same owner and similar address
            // This is a placeholder for more complex business logic
            logger.debug("Checking for related properties to update for transaction {}", 
                        event.getTransactionId());
            
            // Implementation could include:
            // - Finding other units in the same building
            // - Updating availability status if all units are rented
            // - Adjusting pricing based on market conditions
            // - Updating search index rankings
            
        } catch (Exception e) {
            logger.warn("Failed to update related properties for transaction {}: {}", 
                       event.getTransactionId(), e.getMessage());
            // Continue processing even if related property updates fail
        }
    }
    
    /**
     * Handles failures in event processing
     */
    private void handleEventProcessingFailure(TransactionCompletedEvent event, Exception error) {
        try {
            // Log the failure for monitoring and alerting
            logger.error("TransactionCompletedEvent processing failed - Transaction: {}, Property: {}, Error: {}", 
                        event.getTransactionId(), event.getPropertyId(), error.getMessage());
            
            // Could implement:
            // - Dead letter queue for failed events
            // - Retry mechanism with exponential backoff  
            // - Administrative notifications for manual intervention
            // - Rollback compensation if needed
            
            // For now, just ensure the error is logged for monitoring
            
        } catch (Exception compensationError) {
            logger.error("Failed to handle event processing failure: {}", compensationError.getMessage());
        }
    }
}