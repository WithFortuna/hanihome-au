package com.hanihome.hanihome_au_api.domain.transaction.event;

import com.hanihome.hanihome_au_api.domain.shared.event.DomainEvent;

import java.time.LocalDateTime;

/**
 * Domain event triggered when a transaction is completed
 * Used to trigger automatic property status updates and other business processes
 */
public class TransactionCompletedEvent implements DomainEvent {
    
    private final Long transactionId;
    private final Long propertyId;
    private final Long tenantUserId;
    private final Long landlordUserId;
    private final Long agentUserId;
    private final LocalDateTime completedAt;
    private final LocalDateTime occurredAt;
    
    public TransactionCompletedEvent(Long transactionId, Long propertyId, 
                                   Long tenantUserId, Long landlordUserId, 
                                   Long agentUserId, LocalDateTime completedAt) {
        this.transactionId = transactionId;
        this.propertyId = propertyId;
        this.tenantUserId = tenantUserId;
        this.landlordUserId = landlordUserId;
        this.agentUserId = agentUserId;
        this.completedAt = completedAt;
        this.occurredAt = LocalDateTime.now();
    }
    
    @Override
    public LocalDateTime occurredAt() {
        return occurredAt;
    }
    
    // Getters
    public Long getTransactionId() {
        return transactionId;
    }
    
    public Long getPropertyId() {
        return propertyId;
    }
    
    public Long getTenantUserId() {
        return tenantUserId;
    }
    
    public Long getLandlordUserId() {
        return landlordUserId;
    }
    
    public Long getAgentUserId() {
        return agentUserId;
    }
    
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
    
    @Override
    public String toString() {
        return "TransactionCompletedEvent{" +
                "transactionId=" + transactionId +
                ", propertyId=" + propertyId +
                ", tenantUserId=" + tenantUserId +
                ", landlordUserId=" + landlordUserId +
                ", agentUserId=" + agentUserId +
                ", completedAt=" + completedAt +
                ", occurredAt=" + occurredAt +
                '}';
    }
}