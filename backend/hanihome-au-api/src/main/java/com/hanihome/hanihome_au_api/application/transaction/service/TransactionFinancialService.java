package com.hanihome.hanihome_au_api.application.transaction.service;

import com.hanihome.hanihome_au_api.domain.entity.Transaction;
import com.hanihome.hanihome_au_api.domain.entity.TransactionFinancialInfo;
import com.hanihome.hanihome_au_api.domain.enums.PaymentFrequency;
import com.hanihome.hanihome_au_api.domain.enums.PaymentStatus;
import com.hanihome.hanihome_au_api.repository.TransactionFinancialInfoRepository;
import com.hanihome.hanihome_au_api.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TransactionFinancialService {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionFinancialService.class);
    private static final String ENCRYPTION_ALGORITHM = "AES";
    
    @Value("${app.financial.encryption.key:defaultEncryptionKey1234567890}")
    private String encryptionKey;
    
    private final TransactionFinancialInfoRepository financialInfoRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionHistoryService historyService;
    
    public TransactionFinancialService(TransactionFinancialInfoRepository financialInfoRepository,
                                     TransactionRepository transactionRepository,
                                     TransactionHistoryService historyService) {
        this.financialInfoRepository = financialInfoRepository;
        this.transactionRepository = transactionRepository;
        this.historyService = historyService;
    }
    
    /**
     * Create financial information for a transaction
     */
    public TransactionFinancialInfo createFinancialInfo(Long transactionId, 
                                                       BigDecimal weeklyRentAmount,
                                                       BigDecimal bondAmount,
                                                       PaymentFrequency paymentFrequency,
                                                       Boolean utilitiesIncluded,
                                                       Long createdBy) {
        logger.info("Creating financial info for transaction {}", transactionId);
        
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isEmpty()) {
            throw new IllegalArgumentException("Transaction not found: " + transactionId);
        }
        
        // Check if financial info already exists
        Optional<TransactionFinancialInfo> existingInfo = financialInfoRepository.findByTransactionId(transactionId);
        if (existingInfo.isPresent()) {
            throw new IllegalStateException("Financial information already exists for transaction: " + transactionId);
        }
        
        Transaction transaction = transactionOpt.get();
        TransactionFinancialInfo financialInfo = new TransactionFinancialInfo(
            transaction, weeklyRentAmount, bondAmount, paymentFrequency, utilitiesIncluded, createdBy
        );
        
        // Validate the financial information
        String validationResult = financialInfo.validateFinancialInfo();
        if (!validationResult.isEmpty()) {
            logger.warn("Financial info validation warnings for transaction {}: {}", transactionId, validationResult);
        }
        
        TransactionFinancialInfo savedInfo = financialInfoRepository.save(financialInfo);
        
        // Log activity
        historyService.logActivity(transactionId, 
            com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType.PAYMENT_RECEIVED,
            String.format("Financial information created: Weekly rent $%s, Bond $%s", 
                         weeklyRentAmount, bondAmount),
            createdBy, null);
        
        logger.info("Financial info created for transaction {}", transactionId);
        return savedInfo;
    }
    
    /**
     * Update rental amount
     */
    public TransactionFinancialInfo updateRentAmount(Long transactionId, BigDecimal newWeeklyRentAmount, 
                                                   String reason, Long updatedBy) {
        logger.info("Updating rent amount for transaction {} to ${}", transactionId, newWeeklyRentAmount);
        
        TransactionFinancialInfo financialInfo = getFinancialInfoByTransactionId(transactionId);
        BigDecimal oldAmount = financialInfo.getWeeklyRentAmount();
        
        financialInfo.updateRentAmount(newWeeklyRentAmount, updatedBy);
        TransactionFinancialInfo savedInfo = financialInfoRepository.save(financialInfo);
        
        // Log activity
        String description = String.format("Rent amount updated from $%s to $%s", oldAmount, newWeeklyRentAmount);
        if (reason != null && !reason.trim().isEmpty()) {
            description += ": " + reason;
        }
        
        historyService.logActivity(transactionId,
            com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType.PAYMENT_RECEIVED,
            description, updatedBy, null);
        
        logger.info("Rent amount updated for transaction {}", transactionId);
        return savedInfo;
    }
    
    /**
     * Update bond amount
     */
    public TransactionFinancialInfo updateBondAmount(Long transactionId, BigDecimal newBondAmount, 
                                                   String reason, Long updatedBy) {
        logger.info("Updating bond amount for transaction {} to ${}", transactionId, newBondAmount);
        
        TransactionFinancialInfo financialInfo = getFinancialInfoByTransactionId(transactionId);
        BigDecimal oldAmount = financialInfo.getBondAmount();
        
        financialInfo.updateBondAmount(newBondAmount, updatedBy);
        TransactionFinancialInfo savedInfo = financialInfoRepository.save(financialInfo);
        
        // Log activity
        String description = String.format("Bond amount updated from $%s to $%s", oldAmount, newBondAmount);
        if (reason != null && !reason.trim().isEmpty()) {
            description += ": " + reason;
        }
        
        historyService.logActivity(transactionId,
            com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType.PAYMENT_RECEIVED,
            description, updatedBy, null);
        
        logger.info("Bond amount updated for transaction {}", transactionId);
        return savedInfo;
    }
    
    /**
     * Mark bond as paid
     */
    public TransactionFinancialInfo markBondAsPaid(Long transactionId, String paymentReference, Long updatedBy) {
        logger.info("Marking bond as paid for transaction {}", transactionId);
        
        TransactionFinancialInfo financialInfo = getFinancialInfoByTransactionId(transactionId);
        financialInfo.markBondPaid(paymentReference, updatedBy);
        
        TransactionFinancialInfo savedInfo = financialInfoRepository.save(financialInfo);
        
        // Log activity
        historyService.logActivity(transactionId,
            com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType.PAYMENT_RECEIVED,
            String.format("Bond payment received: $%s (Reference: %s)", 
                         financialInfo.getBondAmount(), paymentReference),
            updatedBy, null);
        
        logger.info("Bond marked as paid for transaction {}", transactionId);
        return savedInfo;
    }
    
    /**
     * Mark first rent as paid
     */
    public TransactionFinancialInfo markFirstRentAsPaid(Long transactionId, String paymentReference, Long updatedBy) {
        logger.info("Marking first rent as paid for transaction {}", transactionId);
        
        TransactionFinancialInfo financialInfo = getFinancialInfoByTransactionId(transactionId);
        financialInfo.markFirstRentPaid(paymentReference, updatedBy);
        
        TransactionFinancialInfo savedInfo = financialInfoRepository.save(financialInfo);
        
        // Log activity
        historyService.logActivity(transactionId,
            com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType.PAYMENT_RECEIVED,
            String.format("First rent payment received: $%s (Reference: %s)", 
                         financialInfo.getWeeklyRentAmount(), paymentReference),
            updatedBy, null);
        
        logger.info("First rent marked as paid for transaction {}", transactionId);
        return savedInfo;
    }
    
    /**
     * Add additional costs (pet bond, key money, application fee)
     */
    public TransactionFinancialInfo addAdditionalCosts(Long transactionId, BigDecimal petBondAmount,
                                                     BigDecimal keyMoneyAmount, BigDecimal applicationFee,
                                                     Long updatedBy) {
        logger.info("Adding additional costs to transaction {}", transactionId);
        
        TransactionFinancialInfo financialInfo = getFinancialInfoByTransactionId(transactionId);
        financialInfo.addAdditionalCosts(petBondAmount, keyMoneyAmount, applicationFee, updatedBy);
        
        TransactionFinancialInfo savedInfo = financialInfoRepository.save(financialInfo);
        
        // Log activity
        StringBuilder description = new StringBuilder("Additional costs added:");
        if (petBondAmount != null && petBondAmount.compareTo(BigDecimal.ZERO) > 0) {
            description.append(String.format(" Pet bond $%s", petBondAmount));
        }
        if (keyMoneyAmount != null && keyMoneyAmount.compareTo(BigDecimal.ZERO) > 0) {
            description.append(String.format(" Key money $%s", keyMoneyAmount));
        }
        if (applicationFee != null && applicationFee.compareTo(BigDecimal.ZERO) > 0) {
            description.append(String.format(" Application fee $%s", applicationFee));
        }
        
        historyService.logActivity(transactionId,
            com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType.PAYMENT_RECEIVED,
            description.toString(), updatedBy, null);
        
        logger.info("Additional costs added to transaction {}", transactionId);
        return savedInfo;
    }
    
    /**
     * Enable GST calculation
     */
    public TransactionFinancialInfo enableGst(Long transactionId, BigDecimal gstRate, Long updatedBy) {
        logger.info("Enabling GST for transaction {} at rate {}", transactionId, gstRate);
        
        TransactionFinancialInfo financialInfo = getFinancialInfoByTransactionId(transactionId);
        financialInfo.enableGst(gstRate, updatedBy);
        
        TransactionFinancialInfo savedInfo = financialInfoRepository.save(financialInfo);
        
        // Log activity
        historyService.logActivity(transactionId,
            com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType.PAYMENT_RECEIVED,
            String.format("GST enabled at %.1f%% rate. GST amount: $%s", 
                         gstRate.multiply(BigDecimal.valueOf(100)), financialInfo.getGstAmount()),
            updatedBy, null);
        
        logger.info("GST enabled for transaction {}", transactionId);
        return savedInfo;
    }
    
    /**
     * Store encrypted bank account details
     */
    public void storeBankAccountDetails(Long transactionId, String landlordBankAccount, 
                                      String tenantBankAccount, Long updatedBy) {
        logger.info("Storing bank account details for transaction {}", transactionId);
        
        TransactionFinancialInfo financialInfo = getFinancialInfoByTransactionId(transactionId);
        
        try {
            if (landlordBankAccount != null) {
                String encryptedLandlordAccount = encryptSensitiveData(landlordBankAccount);
                financialInfo.setLandlordBankAccountEncrypted(encryptedLandlordAccount);
            }
            
            if (tenantBankAccount != null) {
                String encryptedTenantAccount = encryptSensitiveData(tenantBankAccount);
                financialInfo.setTenantBankAccountEncrypted(encryptedTenantAccount);
            }
            
            financialInfoRepository.save(financialInfo);
            
            // Log activity (without sensitive details)
            historyService.logActivity(transactionId,
                com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType.DOCUMENT_UPLOADED,
                "Bank account details securely stored", updatedBy, null);
            
            logger.info("Bank account details stored for transaction {}", transactionId);
            
        } catch (Exception e) {
            logger.error("Failed to encrypt bank account details for transaction {}", transactionId, e);
            throw new RuntimeException("Failed to securely store bank account details", e);
        }
    }
    
    /**
     * Get decrypted bank account details (for authorized users only)
     */
    public BankAccountDetails getDecryptedBankAccountDetails(Long transactionId, Long requestingUserId) {
        logger.info("Retrieving bank account details for transaction {} by user {}", transactionId, requestingUserId);
        
        TransactionFinancialInfo financialInfo = getFinancialInfoByTransactionId(transactionId);
        
        // Check authorization (user must be involved in the transaction)
        Transaction transaction = financialInfo.getTransaction();
        if (!isUserAuthorizedForFinancialData(transaction, requestingUserId)) {
            throw new SecurityException("User not authorized to access financial data for this transaction");
        }
        
        try {
            String landlordAccount = null;
            String tenantAccount = null;
            
            if (financialInfo.getLandlordBankAccountEncrypted() != null) {
                landlordAccount = decryptSensitiveData(financialInfo.getLandlordBankAccountEncrypted());
            }
            
            if (financialInfo.getTenantBankAccountEncrypted() != null) {
                tenantAccount = decryptSensitiveData(financialInfo.getTenantBankAccountEncrypted());
            }
            
            // Log access
            historyService.logActivity(transactionId,
                com.hanihome.hanihome_au_api.domain.enums.TransactionActivityType.DOCUMENT_UPLOADED,
                "Bank account details accessed", requestingUserId, null);
            
            return new BankAccountDetails(landlordAccount, tenantAccount);
            
        } catch (Exception e) {
            logger.error("Failed to decrypt bank account details for transaction {}", transactionId, e);
            throw new RuntimeException("Failed to retrieve bank account details", e);
        }
    }
    
    /**
     * Get financial information by transaction ID
     */
    @Transactional(readOnly = true)
    public TransactionFinancialInfo getFinancialInfoByTransactionId(Long transactionId) {
        return financialInfoRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new IllegalArgumentException("Financial information not found for transaction: " + transactionId));
    }
    
    /**
     * Get financial summary statistics
     */
    @Transactional(readOnly = true)
    public FinancialSummary getFinancialSummary() {
        BigDecimal averageWeeklyRent = financialInfoRepository.findAverageWeeklyRent();
        BigDecimal averageBondAmount = financialInfoRepository.findAverageBondAmount();
        BigDecimal totalBondPaid = financialInfoRepository.getTotalBondAmountPaid();
        BigDecimal totalWeeklyRentActive = financialInfoRepository.getTotalWeeklyRentForPaidTransactions();
        
        long pendingBondPayments = financialInfoRepository.countByBondPaymentStatus(PaymentStatus.PENDING);
        long pendingRentPayments = financialInfoRepository.countByFirstRentPaymentStatus(PaymentStatus.PENDING);
        
        return new FinancialSummary(averageWeeklyRent, averageBondAmount, totalBondPaid, 
                                  totalWeeklyRentActive, pendingBondPayments, pendingRentPayments);
    }
    
    /**
     * Get transactions with incomplete payments
     */
    @Transactional(readOnly = true)
    public List<TransactionFinancialInfo> getIncompletePayments() {
        return financialInfoRepository.findIncompletePayments();
    }
    
    /**
     * Validate all financial information
     */
    public void validateAllFinancialInfo() {
        logger.info("Validating all financial information");
        
        List<TransactionFinancialInfo> allFinancialInfo = financialInfoRepository.findAll();
        int validatedCount = 0;
        int errorsFound = 0;
        
        for (TransactionFinancialInfo info : allFinancialInfo) {
            String validationResult = info.validateFinancialInfo();
            if (!validationResult.isEmpty()) {
                errorsFound++;
                logger.warn("Validation errors for transaction {}: {}", 
                           info.getTransaction().getId(), validationResult);
            }
            financialInfoRepository.save(info);
            validatedCount++;
        }
        
        logger.info("Validated {} financial records, found {} with errors", validatedCount, errorsFound);
    }
    
    // Helper methods
    private boolean isUserAuthorizedForFinancialData(Transaction transaction, Long userId) {
        return userId.equals(transaction.getTenantUserId()) || 
               userId.equals(transaction.getLandlordUserId()) || 
               (transaction.getAgentUserId() != null && userId.equals(transaction.getAgentUserId()));
    }
    
    private String encryptSensitiveData(String data) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(), ENCRYPTION_ALGORITHM);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedData);
    }
    
    private String decryptSensitiveData(String encryptedData) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(encryptionKey.getBytes(), ENCRYPTION_ALGORITHM);
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
        byte[] decryptedData = cipher.doFinal(decodedData);
        return new String(decryptedData);
    }
    
    // DTOs
    public static class BankAccountDetails {
        private final String landlordBankAccount;
        private final String tenantBankAccount;
        
        public BankAccountDetails(String landlordBankAccount, String tenantBankAccount) {
            this.landlordBankAccount = landlordBankAccount;
            this.tenantBankAccount = tenantBankAccount;
        }
        
        public String getLandlordBankAccount() { return landlordBankAccount; }
        public String getTenantBankAccount() { return tenantBankAccount; }
    }
    
    public static class FinancialSummary {
        private final BigDecimal averageWeeklyRent;
        private final BigDecimal averageBondAmount;
        private final BigDecimal totalBondPaid;
        private final BigDecimal totalWeeklyRentActive;
        private final long pendingBondPayments;
        private final long pendingRentPayments;
        
        public FinancialSummary(BigDecimal averageWeeklyRent, BigDecimal averageBondAmount,
                              BigDecimal totalBondPaid, BigDecimal totalWeeklyRentActive,
                              long pendingBondPayments, long pendingRentPayments) {
            this.averageWeeklyRent = averageWeeklyRent;
            this.averageBondAmount = averageBondAmount;
            this.totalBondPaid = totalBondPaid;
            this.totalWeeklyRentActive = totalWeeklyRentActive;
            this.pendingBondPayments = pendingBondPayments;
            this.pendingRentPayments = pendingRentPayments;
        }
        
        // Getters
        public BigDecimal getAverageWeeklyRent() { return averageWeeklyRent; }
        public BigDecimal getAverageBondAmount() { return averageBondAmount; }
        public BigDecimal getTotalBondPaid() { return totalBondPaid; }
        public BigDecimal getTotalWeeklyRentActive() { return totalWeeklyRentActive; }
        public long getPendingBondPayments() { return pendingBondPayments; }
        public long getPendingRentPayments() { return pendingRentPayments; }
    }
}