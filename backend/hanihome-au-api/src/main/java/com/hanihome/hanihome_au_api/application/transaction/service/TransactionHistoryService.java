package com.hanihome.hanihome_au_api.application.transaction.service;

import com.hanihome.hanihome_au_api.domain.entity.Transaction;
import com.hanihome.hanihome_au_api.domain.entity.TransactionActivity;
import com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType;
import com.hanihome.hanihome_au_api.domain.enums.TransactionStatus;
import com.hanihome.hanihome_au_api.repository.TransactionActivityRepository;
import com.hanihome.hanihome_au_api.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TransactionHistoryService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionHistoryService.class);
    
    private final TransactionRepository transactionRepository;
    private final TransactionActivityRepository activityRepository;
    
    public TransactionHistoryService(TransactionRepository transactionRepository,
                                   TransactionActivityRepository activityRepository) {
        this.transactionRepository = transactionRepository;
        this.activityRepository = activityRepository;
    }
    
    /**
     * Create a new transaction and log the initial activity
     */
    public Transaction createTransaction(Long propertyId, Long tenantUserId, Long landlordUserId,
                                       java.math.BigDecimal proposedRentAmount, 
                                       java.math.BigDecimal proposedBondAmount,
                                       LocalDateTime proposedLeaseStartDate, 
                                       LocalDateTime proposedLeaseEndDate,
                                       Long createdBy) {
        logger.info("Creating new transaction for property {} between tenant {} and landlord {}", 
                   propertyId, tenantUserId, landlordUserId);
        
        Transaction transaction = new Transaction(propertyId, tenantUserId, landlordUserId,
                                                proposedRentAmount, proposedBondAmount,
                                                proposedLeaseStartDate, proposedLeaseEndDate,
                                                createdBy);
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Log initial activity
        logActivity(savedTransaction.getId(), TransactionActivityType.TRANSACTION_CREATED,
                   String.format("Transaction created for property %d with rent $%s and bond $%s", 
                                propertyId, proposedRentAmount, proposedBondAmount),
                   createdBy, null);
        
        logger.info("Transaction {} created successfully", savedTransaction.getId());
        return savedTransaction;
    }
    
    /**
     * Log transaction activity
     */
    public void logActivity(Long transactionId, TransactionActivityType activityType, 
                          String description, Long userId, String metadata) {
        logger.debug("Logging activity {} for transaction {}", activityType, transactionId);
        
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            logger.error("Transaction {} not found when logging activity", transactionId);
            throw new IllegalArgumentException("Transaction not found: " + transactionId);
        }
        
        Transaction transaction = transactionOpt.get();
        TransactionActivity activity = new TransactionActivity(transaction, activityType, 
                                                             description, userId, metadata);
        
        activityRepository.save(activity);
        logger.debug("Activity logged successfully for transaction {}", transactionId);
    }
    
    /**
     * Get transaction with full activity history
     */
    @Transactional(readOnly = true)
    public Optional<Transaction> getTransactionWithHistory(Long transactionId) {
        return transactionRepository.findByIdWithActivities(transactionId);
    }
    
    /**
     * Get transaction activity timeline
     */
    @Transactional(readOnly = true)
    public List<TransactionActivity> getTransactionTimeline(Long transactionId) {
        return activityRepository.findTimelineActivities(transactionId);
    }
    
    /**
     * Get all activities for a transaction with pagination
     */
    @Transactional(readOnly = true)
    public Page<TransactionActivity> getTransactionActivities(Long transactionId, Pageable pageable) {
        return activityRepository.findByTransactionIdOrderByCreatedAtDesc(transactionId, pageable);
    }
    
    /**
     * Get recent activities for a user across all their transactions
     */
    @Transactional(readOnly = true)
    public List<TransactionActivity> getRecentActivitiesForUser(Long userId, int days) {
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        return activityRepository.findRecentActivitiesByUser(userId, sinceDate);
    }
    
    /**
     * Get audit trail for a transaction (key activities only)
     */
    @Transactional(readOnly = true)
    public List<TransactionActivity> getAuditTrail(Long transactionId) {
        List<TransactionActivityType> auditTypes = Arrays.asList(
            TransactionActivityType.TRANSACTION_CREATED,
            TransactionActivityType.OFFER_MADE,
            TransactionActivityType.OFFER_ACCEPTED,
            TransactionActivityType.OFFER_REJECTED,
            TransactionActivityType.CONTRACT_REQUESTED,
            TransactionActivityType.CONTRACT_SIGNED,
            TransactionActivityType.CONTRACT_COMPLETED,
            TransactionActivityType.TRANSACTION_CANCELLED,
            TransactionActivityType.STATUS_CHANGED
        );
        
        return activityRepository.findAuditTrail(transactionId, auditTypes);
    }
    
    /**
     * Search activities by description
     */
    @Transactional(readOnly = true)
    public List<TransactionActivity> searchActivities(String searchTerm) {
        return activityRepository.searchByDescription(searchTerm);
    }
    
    /**
     * Get transaction statistics
     */
    @Transactional(readOnly = true)
    public TransactionStatistics getTransactionStatistics() {
        long totalTransactions = transactionRepository.count();
        long proposedCount = transactionRepository.countByStatus(TransactionStatus.PROPOSED);
        long negotiatingCount = transactionRepository.countByStatus(TransactionStatus.NEGOTIATING);
        long approvedCount = transactionRepository.countByStatus(TransactionStatus.APPROVED);
        long contractPendingCount = transactionRepository.countByStatus(TransactionStatus.CONTRACT_PENDING);
        long completedCount = transactionRepository.countByStatus(TransactionStatus.COMPLETED);
        long cancelledCount = transactionRepository.countByStatus(TransactionStatus.CANCELLED);
        
        return new TransactionStatistics(totalTransactions, proposedCount, negotiatingCount,
                                       approvedCount, contractPendingCount, completedCount, cancelledCount);
    }
    
    /**
     * Get transactions requiring attention (pending signatures, expired proposals, etc.)
     */
    @Transactional(readOnly = true)
    public List<Transaction> getTransactionsRequringAttention() {
        List<Transaction> pendingSignatures = transactionRepository.findTransactionsPendingSignature();
        
        // Add expired transactions (proposals older than 30 days)
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<Transaction> expiredTransactions = transactionRepository.findExpiredTransactions(cutoffDate);
        
        pendingSignatures.addAll(expiredTransactions);
        return pendingSignatures;
    }
    
    /**
     * Get recent transactions for dashboard
     */
    @Transactional(readOnly = true)
    public List<Transaction> getRecentTransactions(int days) {
        LocalDateTime sinceDate = LocalDateTime.now().minusDays(days);
        return transactionRepository.findRecentTransactions(sinceDate);
    }
    
    /**
     * Update transaction status and log activity
     */
    public void updateTransactionStatus(Long transactionId, TransactionStatus newStatus, 
                                      String reason, Long userId) {
        logger.info("Updating transaction {} status to {}", transactionId, newStatus);
        
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            throw new IllegalArgumentException("Transaction not found: " + transactionId);
        }
        
        Transaction transaction = transactionOpt.get();
        TransactionStatus oldStatus = transaction.getStatus();
        
        transaction.updateStatus(newStatus, userId);
        transactionRepository.save(transaction);
        
        // Log the status change activity with reason
        String description = reason != null ? 
            String.format("Status changed from %s to %s: %s", oldStatus, newStatus, reason) :
            String.format("Status changed from %s to %s", oldStatus, newStatus);
            
        logActivity(transactionId, TransactionActivityType.STATUS_CHANGED, description, userId, null);
        
        logger.info("Transaction {} status updated to {}", transactionId, newStatus);
    }
    
    /**
     * Archive old completed transactions and their activities
     */
    @Transactional
    public void archiveOldTransactions(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        
        List<Transaction> oldCompletedTransactions = transactionRepository.findByDateRange(
            LocalDateTime.now().minusYears(10), cutoffDate
        ).stream()
        .filter(t -> t.getStatus() == TransactionStatus.COMPLETED || t.getStatus() == TransactionStatus.CANCELLED)
        .toList();
        
        logger.info("Archiving {} old transactions", oldCompletedTransactions.size());
        
        // In a real implementation, you might move these to an archive table
        // For now, we'll just log the archival
        for (Transaction transaction : oldCompletedTransactions) {
            logActivity(transaction.getId(), TransactionActivityType.STATUS_CHANGED,
                       "Transaction archived due to retention policy", null, 
                       String.format("{\"archived_at\":\"%s\",\"retention_days\":%d}", 
                                   LocalDateTime.now(), retentionDays));
        }
    }
    
    // Inner class for statistics DTO
    public static class TransactionStatistics {
        private final long totalTransactions;
        private final long proposedCount;
        private final long negotiatingCount;
        private final long approvedCount;
        private final long contractPendingCount;
        private final long completedCount;
        private final long cancelledCount;
        
        public TransactionStatistics(long totalTransactions, long proposedCount, long negotiatingCount,
                                   long approvedCount, long contractPendingCount, long completedCount, 
                                   long cancelledCount) {
            this.totalTransactions = totalTransactions;
            this.proposedCount = proposedCount;
            this.negotiatingCount = negotiatingCount;
            this.approvedCount = approvedCount;
            this.contractPendingCount = contractPendingCount;
            this.completedCount = completedCount;
            this.cancelledCount = cancelledCount;
        }
        
        // Getters
        public long getTotalTransactions() { return totalTransactions; }
        public long getProposedCount() { return proposedCount; }
        public long getNegotiatingCount() { return negotiatingCount; }
        public long getApprovedCount() { return approvedCount; }
        public long getContractPendingCount() { return contractPendingCount; }
        public long getCompletedCount() { return completedCount; }
        public long getCancelledCount() { return cancelledCount; }
        public long getActiveCount() { 
            return proposedCount + negotiatingCount + approvedCount + contractPendingCount; 
        }
    }
}