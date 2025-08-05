package com.hanihome.hanihome_au_api.domain.entity;

import com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_activities", schema = "transaction")
@EntityListeners(AuditingEntityListener.class)
public class TransactionActivity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private TransactionActivityType activityType;
    
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON formatted additional data
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    protected TransactionActivity() {}
    
    public TransactionActivity(Transaction transaction, TransactionActivityType activityType, 
                             String description, Long userId) {
        this.transaction = transaction;
        this.activityType = activityType;
        this.description = description;
        this.userId = userId;
    }
    
    public TransactionActivity(Transaction transaction, TransactionActivityType activityType, 
                             String description, Long userId, String metadata) {
        this(transaction, activityType, description, userId);
        this.metadata = metadata;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public Transaction getTransaction() { return transaction; }
    public TransactionActivityType getActivityType() { return activityType; }
    public String getDescription() { return description; }
    public Long getUserId() { return userId; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}