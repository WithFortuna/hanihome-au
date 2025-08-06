package com.hanihome.hanihome_au_api.domain.entity;

import com.hanihome.hanihome_au_api.domain.enums.PaymentFrequency;
import com.hanihome.hanihome_au_api.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_financial_info", schema = "transaction")
@EntityListeners(AuditingEntityListener.class)
public class TransactionFinancialInfo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false, unique = true)
    private Transaction transaction;
    
    // Rental information
    @Column(name = "weekly_rent_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal weeklyRentAmount;
    
    @Column(name = "monthly_rent_amount", precision = 12, scale = 2)
    private BigDecimal monthlyRentAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_frequency", nullable = false)
    private PaymentFrequency paymentFrequency;
    
    // Bond/Security deposit information
    @Column(name = "bond_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal bondAmount;
    
    @Column(name = "bond_weeks_equivalent", precision = 4, scale = 2)
    private BigDecimal bondWeeksEquivalent; // How many weeks of rent this bond represents
    
    // Additional costs
    @Column(name = "utilities_included", nullable = false)
    private Boolean utilitiesIncluded = false;
    
    @Column(name = "pet_bond_amount", precision = 12, scale = 2)
    private BigDecimal petBondAmount;
    
    @Column(name = "key_money_amount", precision = 12, scale = 2)
    private BigDecimal keyMoneyAmount;
    
    @Column(name = "application_fee", precision = 12, scale = 2)
    private BigDecimal applicationFee;
    
    // Payment information
    @Enumerated(EnumType.STRING)
    @Column(name = "bond_payment_status")
    private PaymentStatus bondPaymentStatus = PaymentStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "first_rent_payment_status")
    private PaymentStatus firstRentPaymentStatus = PaymentStatus.PENDING;
    
    @Column(name = "bond_paid_at")
    private LocalDateTime bondPaidAt;
    
    @Column(name = "first_rent_paid_at")
    private LocalDateTime firstRentPaidAt;
    
    // Tax information
    @Column(name = "gst_applicable", nullable = false)
    private Boolean gstApplicable = false;
    
    @Column(name = "gst_amount", precision = 12, scale = 2)
    private BigDecimal gstAmount;
    
    @Column(name = "total_amount_including_gst", precision = 12, scale = 2)
    private BigDecimal totalAmountIncludingGst;
    
    // Bank account information (encrypted)
    @Column(name = "landlord_bank_account_encrypted", columnDefinition = "TEXT")
    private String landlordBankAccountEncrypted;
    
    @Column(name = "tenant_bank_account_encrypted", columnDefinition = "TEXT")
    private String tenantBankAccountEncrypted;
    
    // Payment references
    @Column(name = "bond_payment_reference")
    private String bondPaymentReference;
    
    @Column(name = "rent_payment_reference")
    private String rentPaymentReference;
    
    // Calculation metadata
    @Column(name = "calculation_notes", columnDefinition = "TEXT")
    private String calculationNotes;
    
    @Column(name = "validation_errors", columnDefinition = "TEXT")
    private String validationErrors;
    
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
    protected TransactionFinancialInfo() {}
    
    public TransactionFinancialInfo(Transaction transaction, BigDecimal weeklyRentAmount, 
                                  BigDecimal bondAmount, PaymentFrequency paymentFrequency,
                                  Boolean utilitiesIncluded, Long createdBy) {
        this.transaction = transaction;
        this.weeklyRentAmount = weeklyRentAmount;
        this.bondAmount = bondAmount;
        this.paymentFrequency = paymentFrequency;
        this.utilitiesIncluded = utilitiesIncluded != null ? utilitiesIncluded : false;
        this.gstApplicable = false;
        this.createdBy = createdBy;
        this.updatedBy = createdBy;
        
        calculateDerivedAmounts();
    }
    
    // Business methods
    public void updateRentAmount(BigDecimal newWeeklyRentAmount, Long updatedBy) {
        this.weeklyRentAmount = newWeeklyRentAmount;
        this.updatedBy = updatedBy;
        calculateDerivedAmounts();
    }
    
    public void updateBondAmount(BigDecimal newBondAmount, Long updatedBy) {
        this.bondAmount = newBondAmount;
        this.updatedBy = updatedBy;
        calculateDerivedAmounts();
    }
    
    public void markBondPaid(String paymentReference, Long updatedBy) {
        this.bondPaymentStatus = PaymentStatus.PAID;
        this.bondPaidAt = LocalDateTime.now();
        this.bondPaymentReference = paymentReference;
        this.updatedBy = updatedBy;
    }
    
    public void markFirstRentPaid(String paymentReference, Long updatedBy) {
        this.firstRentPaymentStatus = PaymentStatus.PAID;
        this.firstRentPaidAt = LocalDateTime.now();
        this.rentPaymentReference = paymentReference;
        this.updatedBy = updatedBy;
    }
    
    public void enableGst(BigDecimal gstRate, Long updatedBy) {
        this.gstApplicable = true;
        this.updatedBy = updatedBy;
        calculateGst(gstRate);
    }
    
    public void disableGst(Long updatedBy) {
        this.gstApplicable = false;
        this.gstAmount = null;
        this.totalAmountIncludingGst = null;
        this.updatedBy = updatedBy;
    }
    
    public void addAdditionalCosts(BigDecimal petBondAmount, BigDecimal keyMoneyAmount, 
                                 BigDecimal applicationFee, Long updatedBy) {
        this.petBondAmount = petBondAmount;
        this.keyMoneyAmount = keyMoneyAmount;
        this.applicationFee = applicationFee;
        this.updatedBy = updatedBy;
        calculateDerivedAmounts();
    }
    
    public BigDecimal getTotalBondAmount() {
        BigDecimal total = bondAmount != null ? bondAmount : BigDecimal.ZERO;
        if (petBondAmount != null) {
            total = total.add(petBondAmount);
        }
        return total;
    }
    
    public BigDecimal getTotalUpfrontAmount() {
        BigDecimal total = getTotalBondAmount();
        
        // Add first week/month rent
        if (paymentFrequency == PaymentFrequency.WEEKLY && weeklyRentAmount != null) {
            total = total.add(weeklyRentAmount);
        } else if (paymentFrequency == PaymentFrequency.MONTHLY && monthlyRentAmount != null) {
            total = total.add(monthlyRentAmount);
        }
        
        // Add other upfront costs
        if (keyMoneyAmount != null) {
            total = total.add(keyMoneyAmount);
        }
        if (applicationFee != null) {
            total = total.add(applicationFee);
        }
        
        return total;
    }
    
    public boolean isPaymentComplete() {
        return bondPaymentStatus == PaymentStatus.PAID && 
               firstRentPaymentStatus == PaymentStatus.PAID;
    }
    
    public String validateFinancialInfo() {
        StringBuilder errors = new StringBuilder();
        
        if (weeklyRentAmount == null || weeklyRentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.append("Weekly rent amount must be positive. ");
        }
        
        if (bondAmount == null || bondAmount.compareTo(BigDecimal.ZERO) < 0) {
            errors.append("Bond amount cannot be negative. ");
        }
        
        // Bond should typically be 2-6 weeks of rent
        if (weeklyRentAmount != null && bondAmount != null && weeklyRentAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal weeksOfRent = bondAmount.divide(weeklyRentAmount, 2, BigDecimal.ROUND_HALF_UP);
            if (weeksOfRent.compareTo(BigDecimal.valueOf(8)) > 0) {
                errors.append("Bond amount seems unusually high (>8 weeks rent). ");
            }
        }
        
        if (gstApplicable && gstAmount == null) {
            errors.append("GST amount must be calculated when GST is applicable. ");
        }
        
        String validationResult = errors.toString().trim();
        this.validationErrors = validationResult.isEmpty() ? null : validationResult;
        return validationResult;
    }
    
    private void calculateDerivedAmounts() {
        // Calculate monthly rent from weekly rent
        if (weeklyRentAmount != null) {
            this.monthlyRentAmount = weeklyRentAmount.multiply(BigDecimal.valueOf(52))
                                                   .divide(BigDecimal.valueOf(12), 2, BigDecimal.ROUND_HALF_UP);
            
            // Calculate bond weeks equivalent
            if (bondAmount != null && weeklyRentAmount.compareTo(BigDecimal.ZERO) > 0) {
                this.bondWeeksEquivalent = bondAmount.divide(weeklyRentAmount, 2, BigDecimal.ROUND_HALF_UP);
            }
        }
        
        // Recalculate GST if applicable
        if (gstApplicable) {
            calculateGst(BigDecimal.valueOf(0.10)); // 10% GST in Australia
        }
    }
    
    private void calculateGst(BigDecimal gstRate) {
        if (weeklyRentAmount != null) {
            this.gstAmount = weeklyRentAmount.multiply(gstRate);
            this.totalAmountIncludingGst = weeklyRentAmount.add(gstAmount);
        }
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public Transaction getTransaction() { return transaction; }
    public BigDecimal getWeeklyRentAmount() { return weeklyRentAmount; }
    public BigDecimal getMonthlyRentAmount() { return monthlyRentAmount; }
    public PaymentFrequency getPaymentFrequency() { return paymentFrequency; }
    public void setPaymentFrequency(PaymentFrequency paymentFrequency) { 
        this.paymentFrequency = paymentFrequency; 
    }
    public BigDecimal getBondAmount() { return bondAmount; }
    public BigDecimal getBondWeeksEquivalent() { return bondWeeksEquivalent; }
    public Boolean getUtilitiesIncluded() { return utilitiesIncluded; }
    public void setUtilitiesIncluded(Boolean utilitiesIncluded) { 
        this.utilitiesIncluded = utilitiesIncluded; 
    }
    public BigDecimal getPetBondAmount() { return petBondAmount; }
    public BigDecimal getKeyMoneyAmount() { return keyMoneyAmount; }
    public BigDecimal getApplicationFee() { return applicationFee; }
    public PaymentStatus getBondPaymentStatus() { return bondPaymentStatus; }
    public PaymentStatus getFirstRentPaymentStatus() { return firstRentPaymentStatus; }
    public LocalDateTime getBondPaidAt() { return bondPaidAt; }
    public LocalDateTime getFirstRentPaidAt() { return firstRentPaidAt; }
    public Boolean getGstApplicable() { return gstApplicable; }
    public BigDecimal getGstAmount() { return gstAmount; }
    public BigDecimal getTotalAmountIncludingGst() { return totalAmountIncludingGst; }
    public String getLandlordBankAccountEncrypted() { return landlordBankAccountEncrypted; }
    public void setLandlordBankAccountEncrypted(String landlordBankAccountEncrypted) {
        this.landlordBankAccountEncrypted = landlordBankAccountEncrypted;
    }
    public String getTenantBankAccountEncrypted() { return tenantBankAccountEncrypted; }
    public void setTenantBankAccountEncrypted(String tenantBankAccountEncrypted) {
        this.tenantBankAccountEncrypted = tenantBankAccountEncrypted;
    }
    public String getBondPaymentReference() { return bondPaymentReference; }
    public String getRentPaymentReference() { return rentPaymentReference; }
    public String getCalculationNotes() { return calculationNotes; }
    public void setCalculationNotes(String calculationNotes) { 
        this.calculationNotes = calculationNotes; 
    }
    public String getValidationErrors() { return validationErrors; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getCreatedBy() { return createdBy; }
    public Long getUpdatedBy() { return updatedBy; }
}