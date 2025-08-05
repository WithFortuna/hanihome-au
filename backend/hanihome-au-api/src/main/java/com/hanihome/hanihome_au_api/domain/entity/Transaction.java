package com.hanihome.hanihome_au_api.domain.entity;

import com.hanihome.hanihome_au_api.domain.enums.TransactionStatus;
import com.hanihome.hanihome_au_api.domain.shared.entity.AggregateRoot;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Money;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transactions", schema = "transaction")
@EntityListeners(AuditingEntityListener.class)
public class Transaction extends AggregateRoot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "property_id", nullable = false)
    private Long propertyId;
    
    @Column(name = "tenant_user_id", nullable = false)
    private Long tenantUserId;
    
    @Column(name = "landlord_user_id", nullable = false)
    private Long landlordUserId;
    
    @Column(name = "agent_user_id")
    private Long agentUserId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;
    
    @Column(name = "proposed_rent_amount", precision = 12, scale = 2)
    private BigDecimal proposedRentAmount;
    
    @Column(name = "proposed_bond_amount", precision = 12, scale = 2)
    private BigDecimal proposedBondAmount;
    
    @Column(name = "final_rent_amount", precision = 12, scale = 2)
    private BigDecimal finalRentAmount;
    
    @Column(name = "final_bond_amount", precision = 12, scale = 2)
    private BigDecimal finalBondAmount;
    
    @Column(name = "proposed_lease_start_date")
    private LocalDateTime proposedLeaseStartDate;
    
    @Column(name = "proposed_lease_end_date")
    private LocalDateTime proposedLeaseEndDate;
    
    @Column(name = "final_lease_start_date")
    private LocalDateTime finalLeaseStartDate;
    
    @Column(name = "final_lease_end_date")
    private LocalDateTime finalLeaseEndDate;
    
    @Column(name = "contract_document_url")
    private String contractDocumentUrl;
    
    @Column(name = "docusign_envelope_id")
    private String docusignEnvelopeId;
    
    @Column(name = "tenant_signed_at")
    private LocalDateTime tenantSignedAt;
    
    @Column(name = "landlord_signed_at")
    private LocalDateTime landlordSignedAt;
    
    @Column(name = "contract_completed_at")
    private LocalDateTime contractCompletedAt;
    
    @Column(name = "version", nullable = false)
    @Version
    private Long version = 0L;
    
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TransactionActivity> activities = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    // Constructors
    protected Transaction() {}
    
    public Transaction(Long propertyId, Long tenantUserId, Long landlordUserId, 
                      BigDecimal proposedRentAmount, BigDecimal proposedBondAmount,
                      LocalDateTime proposedLeaseStartDate, LocalDateTime proposedLeaseEndDate,
                      Long createdBy) {
        this.propertyId = propertyId;
        this.tenantUserId = tenantUserId;
        this.landlordUserId = landlordUserId;
        this.status = TransactionStatus.PROPOSED;
        this.proposedRentAmount = proposedRentAmount;
        this.proposedBondAmount = proposedBondAmount;
        this.proposedLeaseStartDate = proposedLeaseStartDate;
        this.proposedLeaseEndDate = proposedLeaseEndDate;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
        this.version = 0L;
    }
    
    // Business methods
    public void updateStatus(TransactionStatus newStatus, Long updatedBy) {
        if (this.status == newStatus) {
            return;
        }
        
        validateStatusTransition(newStatus);
        
        TransactionStatus oldStatus = this.status;
        this.status = newStatus;
        this.updatedBy = updatedBy;
        
        // Add activity log
        addActivity(com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType.STATUS_CHANGED, 
                   String.format("Status changed from %s to %s", oldStatus, newStatus), updatedBy);
    }
    
    public void makeCounterOffer(BigDecimal newRentAmount, BigDecimal newBondAmount,
                                LocalDateTime newLeaseStart, LocalDateTime newLeaseEnd, Long userId) {
        this.proposedRentAmount = newRentAmount;
        this.proposedBondAmount = newBondAmount;
        this.proposedLeaseStartDate = newLeaseStart;
        this.proposedLeaseEndDate = newLeaseEnd;
        this.updatedBy = userId;
        
        addActivity(com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType.COUNTER_OFFER_MADE, 
                   String.format("Counter offer: Rent $%s, Bond $%s", newRentAmount, newBondAmount), userId);
    }
    
    public void acceptOffer(Long userId) {
        this.finalRentAmount = this.proposedRentAmount;
        this.finalBondAmount = this.proposedBondAmount;
        this.finalLeaseStartDate = this.proposedLeaseStartDate;
        this.finalLeaseEndDate = this.proposedLeaseEndDate;
        this.status = TransactionStatus.APPROVED;
        this.updatedBy = userId;
        
        addActivity(com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType.OFFER_ACCEPTED, 
                   "Offer accepted", userId);
    }
    
    public void setContractDetails(String contractDocumentUrl, String docusignEnvelopeId, Long userId) {
        this.contractDocumentUrl = contractDocumentUrl;
        this.docusignEnvelopeId = docusignEnvelopeId;
        this.status = TransactionStatus.CONTRACT_PENDING;
        this.updatedBy = userId;
        
        addActivity(com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType.CONTRACT_REQUESTED, 
                   "Contract documents prepared", userId);
    }
    
    public void signContract(Long userId, boolean isTenant) {
        if (isTenant) {
            this.tenantSignedAt = LocalDateTime.now();
            addActivity(com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType.CONTRACT_SIGNED, 
                       "Contract signed by tenant", userId);
        } else {
            this.landlordSignedAt = LocalDateTime.now();
            addActivity(com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType.CONTRACT_SIGNED, 
                       "Contract signed by landlord", userId);
        }
        
        // Check if both parties have signed
        if (this.tenantSignedAt != null && this.landlordSignedAt != null) {
            this.contractCompletedAt = LocalDateTime.now();
            this.status = TransactionStatus.COMPLETED;
            addActivity(com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType.CONTRACT_COMPLETED, 
                       "Contract fully executed", userId);
        }
        
        this.updatedBy = userId;
    }
    
    public void cancel(String reason, Long userId) {
        this.status = TransactionStatus.CANCELLED;
        this.updatedBy = userId;
        
        addActivity(com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType.TRANSACTION_CANCELLED, 
                   reason != null ? reason : "Transaction cancelled", userId);
    }
    
    private void addActivity(com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType type, 
                           String description, Long userId) {
        TransactionActivity activity = new TransactionActivity(this, type, description, userId);
        this.activities.add(activity);
    }
    
    private void validateStatusTransition(TransactionStatus newStatus) {
        // Add validation logic for allowed status transitions
        switch (this.status) {
            case PROPOSED:
                if (newStatus != TransactionStatus.NEGOTIATING && 
                    newStatus != TransactionStatus.APPROVED && 
                    newStatus != TransactionStatus.CANCELLED) {
                    throw new IllegalStateException("Invalid status transition from PROPOSED to " + newStatus);
                }
                break;
            case NEGOTIATING:
                if (newStatus != TransactionStatus.APPROVED && 
                    newStatus != TransactionStatus.CANCELLED) {
                    throw new IllegalStateException("Invalid status transition from NEGOTIATING to " + newStatus);
                }
                break;
            case APPROVED:
                if (newStatus != TransactionStatus.CONTRACT_PENDING && 
                    newStatus != TransactionStatus.CANCELLED) {
                    throw new IllegalStateException("Invalid status transition from APPROVED to " + newStatus);
                }
                break;
            case CONTRACT_PENDING:
                if (newStatus != TransactionStatus.COMPLETED && 
                    newStatus != TransactionStatus.CANCELLED) {
                    throw new IllegalStateException("Invalid status transition from CONTRACT_PENDING to " + newStatus);
                }
                break;
            case COMPLETED:
            case CANCELLED:
                throw new IllegalStateException("Cannot change status from " + this.status);
        }
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public Long getPropertyId() { return propertyId; }
    public Long getTenantUserId() { return tenantUserId; }
    public Long getLandlordUserId() { return landlordUserId; }
    public Long getAgentUserId() { return agentUserId; }
    public void setAgentUserId(Long agentUserId) { this.agentUserId = agentUserId; }
    public TransactionStatus getStatus() { return status; }
    public BigDecimal getProposedRentAmount() { return proposedRentAmount; }
    public BigDecimal getProposedBondAmount() { return proposedBondAmount; }
    public BigDecimal getFinalRentAmount() { return finalRentAmount; }
    public BigDecimal getFinalBondAmount() { return finalBondAmount; }
    public LocalDateTime getProposedLeaseStartDate() { return proposedLeaseStartDate; }
    public LocalDateTime getProposedLeaseEndDate() { return proposedLeaseEndDate; }
    public LocalDateTime getFinalLeaseStartDate() { return finalLeaseStartDate; }
    public LocalDateTime getFinalLeaseEndDate() { return finalLeaseEndDate; }
    public String getContractDocumentUrl() { return contractDocumentUrl; }
    public String getDocusignEnvelopeId() { return docusignEnvelopeId; }
    public LocalDateTime getTenantSignedAt() { return tenantSignedAt; }
    public LocalDateTime getLandlordSignedAt() { return landlordSignedAt; }
    public LocalDateTime getContractCompletedAt() { return contractCompletedAt; }
    public Long getVersion() { return version; }
    public List<TransactionActivity> getActivities() { return new ArrayList<>(activities); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getCreatedBy() { return createdBy; }
    public Long getUpdatedBy() { return updatedBy; }
}