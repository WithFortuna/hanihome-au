package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.TransactionActivity;
import com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionActivityRepository extends JpaRepository<TransactionActivity, Long> {
    
    // Find activities by transaction ID
    List<TransactionActivity> findByTransactionIdOrderByCreatedAtDesc(Long transactionId);
    
    // Find activities by transaction ID with pagination
    Page<TransactionActivity> findByTransactionIdOrderByCreatedAtDesc(Long transactionId, Pageable pageable);
    
    // Find activities by user
    List<TransactionActivity> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Find activities by activity type
    List<TransactionActivity> findByActivityTypeOrderByCreatedAtDesc(TransactionActivityType activityType);
    
    // Find activities by transaction and activity type
    List<TransactionActivity> findByTransactionIdAndActivityTypeOrderByCreatedAtDesc(
            Long transactionId, TransactionActivityType activityType);
    
    // Find recent activities for a user's transactions
    @Query("SELECT ta FROM TransactionActivity ta JOIN ta.transaction t WHERE " +
           "(t.tenantUserId = :userId OR t.landlordUserId = :userId OR t.agentUserId = :userId) " +
           "AND ta.createdAt >= :sinceDate ORDER BY ta.createdAt DESC")
    List<TransactionActivity> findRecentActivitiesByUser(@Param("userId") Long userId, 
                                                        @Param("sinceDate") LocalDateTime sinceDate);
    
    // Find activities by date range
    @Query("SELECT ta FROM TransactionActivity ta WHERE ta.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY ta.createdAt DESC")
    List<TransactionActivity> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    // Search activities by description
    @Query("SELECT ta FROM TransactionActivity ta WHERE LOWER(ta.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY ta.createdAt DESC")
    List<TransactionActivity> searchByDescription(@Param("searchTerm") String searchTerm);
    
    // Find activities for audit trail
    @Query("SELECT ta FROM TransactionActivity ta WHERE ta.transactionId = :transactionId " +
           "AND ta.activityType IN :activityTypes ORDER BY ta.createdAt ASC")
    List<TransactionActivity> findAuditTrail(@Param("transactionId") Long transactionId, 
                                           @Param("activityTypes") List<TransactionActivityType> activityTypes);
    
    // Count activities by type for transaction
    @Query("SELECT COUNT(ta) FROM TransactionActivity ta WHERE ta.transactionId = :transactionId " +
           "AND ta.activityType = :activityType")
    Long countByTransactionIdAndActivityType(@Param("transactionId") Long transactionId, 
                                           @Param("activityType") TransactionActivityType activityType);
    
    // Find latest activity for transaction
    @Query("SELECT ta FROM TransactionActivity ta WHERE ta.transactionId = :transactionId " +
           "ORDER BY ta.createdAt DESC LIMIT 1")
    TransactionActivity findLatestActivityByTransactionId(@Param("transactionId") Long transactionId);
    
    // Find activities for timeline view
    @Query("SELECT ta FROM TransactionActivity ta WHERE ta.transactionId = :transactionId " +
           "AND ta.activityType IN ('TRANSACTION_CREATED', 'OFFER_MADE', 'COUNTER_OFFER_MADE', " +
           "'OFFER_ACCEPTED', 'CONTRACT_REQUESTED', 'CONTRACT_SIGNED', 'CONTRACT_COMPLETED', " +
           "'TRANSACTION_CANCELLED', 'STATUS_CHANGED') ORDER BY ta.createdAt ASC")
    List<TransactionActivity> findTimelineActivities(@Param("transactionId") Long transactionId);
}