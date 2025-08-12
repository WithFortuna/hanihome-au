package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.TransactionFinancialInfo;
import com.hanihome.hanihome_au_api.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionFinancialInfoRepository extends JpaRepository<TransactionFinancialInfo, Long> {
    
    // Find financial info by transaction ID
    Optional<TransactionFinancialInfo> findByTransactionId(Long transactionId);
    
    // Find by payment status
    List<TransactionFinancialInfo> findByBondPaymentStatus(PaymentStatus bondPaymentStatus);
    List<TransactionFinancialInfo> findByFirstRentPaymentStatus(PaymentStatus firstRentPaymentStatus);
    
    // Find incomplete payments
    @Query("SELECT tfi FROM TransactionFinancialInfo tfi WHERE " +
           "tfi.bondPaymentStatus != 'PAID' OR tfi.firstRentPaymentStatus != 'PAID'")
    List<TransactionFinancialInfo> findIncompletePayments();
    
    // Find by rent amount range
    @Query("SELECT tfi FROM TransactionFinancialInfo tfi WHERE " +
           "tfi.weeklyRentAmount BETWEEN :minAmount AND :maxAmount")
    List<TransactionFinancialInfo> findByWeeklyRentAmountRange(
            @Param("minAmount") BigDecimal minAmount, 
            @Param("maxAmount") BigDecimal maxAmount);
    
    // Find by bond amount range
    @Query("SELECT tfi FROM TransactionFinancialInfo tfi WHERE " +
           "tfi.bondAmount BETWEEN :minAmount AND :maxAmount")
    List<TransactionFinancialInfo> findByBondAmountRange(
            @Param("minAmount") BigDecimal minAmount, 
            @Param("maxAmount") BigDecimal maxAmount);
    
    // Find transactions with GST
    List<TransactionFinancialInfo> findByGstApplicableTrue();
    
    // Find transactions with utilities included
    List<TransactionFinancialInfo> findByUtilitiesIncludedTrue();
    
    // Find transactions with pet bond
    @Query("SELECT tfi FROM TransactionFinancialInfo tfi WHERE tfi.petBondAmount IS NOT NULL AND tfi.petBondAmount > 0")
    List<TransactionFinancialInfo> findTransactionsWithPetBond();
    
    // Find transactions with validation errors
    @Query("SELECT tfi FROM TransactionFinancialInfo tfi WHERE tfi.validationErrors IS NOT NULL")
    List<TransactionFinancialInfo> findTransactionsWithValidationErrors();
    
    // Calculate average rent amounts
    @Query("SELECT AVG(tfi.weeklyRentAmount) FROM TransactionFinancialInfo tfi WHERE tfi.weeklyRentAmount IS NOT NULL")
    BigDecimal findAverageWeeklyRent();
    
    @Query("SELECT AVG(tfi.bondAmount) FROM TransactionFinancialInfo tfi WHERE tfi.bondAmount IS NOT NULL")
    BigDecimal findAverageBondAmount();
    
    // Find high-value transactions
    @Query("SELECT tfi FROM TransactionFinancialInfo tfi WHERE " +
           "tfi.weeklyRentAmount > :threshold OR tfi.bondAmount > :bondThreshold")
    List<TransactionFinancialInfo> findHighValueTransactions(
            @Param("threshold") BigDecimal threshold, 
            @Param("bondThreshold") BigDecimal bondThreshold);
    
    // Count by payment status
    @Query("SELECT COUNT(tfi) FROM TransactionFinancialInfo tfi WHERE tfi.bondPaymentStatus = :status")
    Long countByBondPaymentStatus(@Param("status") PaymentStatus status);
    
    @Query("SELECT COUNT(tfi) FROM TransactionFinancialInfo tfi WHERE tfi.firstRentPaymentStatus = :status")
    Long countByFirstRentPaymentStatus(@Param("status") PaymentStatus status);
    
    // Financial summary queries
    @Query("SELECT SUM(tfi.weeklyRentAmount) FROM TransactionFinancialInfo tfi WHERE tfi.bondPaymentStatus = 'PAID'")
    BigDecimal getTotalWeeklyRentForPaidTransactions();
    
    @Query("SELECT SUM(tfi.bondAmount) FROM TransactionFinancialInfo tfi WHERE tfi.bondPaymentStatus = 'PAID'")
    BigDecimal getTotalBondAmountPaid();
    
    // Find by transaction user involvement
    @Query("SELECT tfi FROM TransactionFinancialInfo tfi JOIN tfi.transaction t WHERE " +
           "t.tenantUserId = :userId OR t.landlordUserId = :userId OR t.agentUserId = :userId")
    List<TransactionFinancialInfo> findByUserInvolvement(@Param("userId") Long userId);
    
    // Find transactions needing financial verification
    @Query("SELECT tfi FROM TransactionFinancialInfo tfi WHERE " +
           "(tfi.landlordBankAccountEncrypted IS NULL OR tfi.tenantBankAccountEncrypted IS NULL) " +
           "AND (tfi.bondPaymentStatus = 'PENDING' OR tfi.firstRentPaymentStatus = 'PENDING')")
    List<TransactionFinancialInfo> findTransactionsNeedingBankDetails();
}