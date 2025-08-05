package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.Transaction;
import com.hanihome.hanihome_au_api.domain.enums.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Find transactions by property
    List<Transaction> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);
    
    // Find transactions by user (tenant, landlord, or agent)
    @Query("SELECT t FROM Transaction t WHERE t.tenantUserId = :userId OR t.landlordUserId = :userId OR t.agentUserId = :userId ORDER BY t.createdAt DESC")
    List<Transaction> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    // Find transactions by user with pagination
    @Query("SELECT t FROM Transaction t WHERE t.tenantUserId = :userId OR t.landlordUserId = :userId OR t.agentUserId = :userId")
    Page<Transaction> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Find transactions by tenant
    List<Transaction> findByTenantUserIdOrderByCreatedAtDesc(Long tenantUserId);
    
    // Find transactions by landlord
    List<Transaction> findByLandlordUserIdOrderByCreatedAtDesc(Long landlordUserId);
    
    // Find transactions by agent
    List<Transaction> findByAgentUserIdOrderByCreatedAtDesc(Long agentUserId);
    
    // Find transactions by status
    List<Transaction> findByStatusOrderByCreatedAtDesc(TransactionStatus status);
    
    // Find active transactions (not completed or cancelled)
    @Query("SELECT t FROM Transaction t WHERE t.status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY t.createdAt DESC")
    List<Transaction> findActiveTransactions();
    
    // Find transactions requiring attention (pending signatures, etc.)
    @Query("SELECT t FROM Transaction t WHERE t.status = 'CONTRACT_PENDING' AND " +
           "(t.tenantSignedAt IS NULL OR t.landlordSignedAt IS NULL) ORDER BY t.createdAt DESC")
    List<Transaction> findTransactionsPendingSignature();
    
    // Find expired transactions (old proposals that haven't progressed)
    @Query("SELECT t FROM Transaction t WHERE t.status IN ('PROPOSED', 'NEGOTIATING') AND " +
           "t.createdAt < :cutoffDate ORDER BY t.createdAt DESC")
    List<Transaction> findExpiredTransactions(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Find transactions by date range
    @Query("SELECT t FROM Transaction t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
    
    // Check if property has active transactions
    @Query("SELECT COUNT(t) > 0 FROM Transaction t WHERE t.propertyId = :propertyId AND " +
           "t.status NOT IN ('COMPLETED', 'CANCELLED')")
    boolean hasActiveTransactions(@Param("propertyId") Long propertyId);
    
    // Find transaction with activities (with join fetch)
    @Query("SELECT t FROM Transaction t LEFT JOIN FETCH t.activities WHERE t.id = :id")
    Optional<Transaction> findByIdWithActivities(@Param("id") Long id);
    
    // Count transactions by status
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status")
    Long countByStatus(@Param("status") TransactionStatus status);
    
    // Find recent transactions for dashboard
    @Query("SELECT t FROM Transaction t WHERE t.createdAt >= :sinceDate ORDER BY t.createdAt DESC")
    List<Transaction> findRecentTransactions(@Param("sinceDate") LocalDateTime sinceDate);
}