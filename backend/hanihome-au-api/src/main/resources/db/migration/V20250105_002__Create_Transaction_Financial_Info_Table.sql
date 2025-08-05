-- V20250105_002__Create_Transaction_Financial_Info_Table.sql
-- Create transaction_financial_info table for secure financial data management

-- Create transaction_financial_info table
CREATE TABLE transaction.transaction_financial_info (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL UNIQUE,
    
    -- Rental information
    weekly_rent_amount DECIMAL(12,2) NOT NULL,
    monthly_rent_amount DECIMAL(12,2),
    payment_frequency VARCHAR(20) NOT NULL DEFAULT 'WEEKLY',
    
    -- Bond/Security deposit information
    bond_amount DECIMAL(12,2) NOT NULL,
    bond_weeks_equivalent DECIMAL(4,2),
    
    -- Additional costs
    utilities_included BOOLEAN NOT NULL DEFAULT FALSE,
    pet_bond_amount DECIMAL(12,2),
    key_money_amount DECIMAL(12,2),
    application_fee DECIMAL(12,2),
    
    -- Payment information
    bond_payment_status VARCHAR(20) DEFAULT 'PENDING',
    first_rent_payment_status VARCHAR(20) DEFAULT 'PENDING',
    bond_paid_at TIMESTAMP,
    first_rent_paid_at TIMESTAMP,
    
    -- Tax information
    gst_applicable BOOLEAN NOT NULL DEFAULT FALSE,
    gst_amount DECIMAL(12,2),
    total_amount_including_gst DECIMAL(12,2),
    
    -- Bank account information (encrypted)
    landlord_bank_account_encrypted TEXT,
    tenant_bank_account_encrypted TEXT,
    
    -- Payment references
    bond_payment_reference VARCHAR(255),
    rent_payment_reference VARCHAR(255),
    
    -- Calculation metadata
    calculation_notes TEXT,
    validation_errors TEXT,
    
    -- Audit fields
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by BIGINT,
    updated_by BIGINT,
    
    -- Constraints
    CONSTRAINT fk_transaction_financial_info_transaction FOREIGN KEY (transaction_id) 
        REFERENCES transaction.transactions(id) ON DELETE CASCADE,
    CONSTRAINT fk_transaction_financial_info_created_by FOREIGN KEY (created_by) 
        REFERENCES auth.users(id),
    CONSTRAINT fk_transaction_financial_info_updated_by FOREIGN KEY (updated_by) 
        REFERENCES auth.users(id),
    
    -- Check constraints for data integrity
    CONSTRAINT chk_payment_frequency CHECK (payment_frequency IN ('WEEKLY', 'FORTNIGHTLY', 'MONTHLY')),
    CONSTRAINT chk_bond_payment_status CHECK (bond_payment_status IN ('PENDING', 'PAID', 'OVERDUE', 'CANCELLED', 'REFUNDED')),
    CONSTRAINT chk_first_rent_payment_status CHECK (first_rent_payment_status IN ('PENDING', 'PAID', 'OVERDUE', 'CANCELLED', 'REFUNDED')),
    
    -- Financial amount constraints
    CONSTRAINT chk_weekly_rent_positive CHECK (weekly_rent_amount > 0),
    CONSTRAINT chk_bond_amount_non_negative CHECK (bond_amount >= 0),
    CONSTRAINT chk_pet_bond_non_negative CHECK (pet_bond_amount IS NULL OR pet_bond_amount >= 0),
    CONSTRAINT chk_key_money_non_negative CHECK (key_money_amount IS NULL OR key_money_amount >= 0),
    CONSTRAINT chk_application_fee_non_negative CHECK (application_fee IS NULL OR application_fee >= 0),
    CONSTRAINT chk_gst_amount_non_negative CHECK (gst_amount IS NULL OR gst_amount >= 0),
    
    -- Bond weeks equivalent should be reasonable (0-10 weeks)
    CONSTRAINT chk_bond_weeks_reasonable CHECK (bond_weeks_equivalent IS NULL OR 
        (bond_weeks_equivalent >= 0 AND bond_weeks_equivalent <= 10)),
    
    -- If GST is applicable, GST amount should be present
    CONSTRAINT chk_gst_consistency CHECK (
        (gst_applicable = FALSE) OR 
        (gst_applicable = TRUE AND gst_amount IS NOT NULL AND total_amount_including_gst IS NOT NULL)
    ),
    
    -- Payment status consistency
    CONSTRAINT chk_payment_date_consistency CHECK (
        (bond_payment_status != 'PAID' OR bond_paid_at IS NOT NULL) AND
        (first_rent_payment_status != 'PAID' OR first_rent_paid_at IS NOT NULL)
    )
);

-- Create indexes for performance
CREATE INDEX idx_transaction_financial_info_transaction_id ON transaction.transaction_financial_info(transaction_id);
CREATE INDEX idx_transaction_financial_info_weekly_rent ON transaction.transaction_financial_info(weekly_rent_amount);
CREATE INDEX idx_transaction_financial_info_bond_amount ON transaction.transaction_financial_info(bond_amount);
CREATE INDEX idx_transaction_financial_info_payment_frequency ON transaction.transaction_financial_info(payment_frequency);
CREATE INDEX idx_transaction_financial_info_bond_payment_status ON transaction.transaction_financial_info(bond_payment_status);
CREATE INDEX idx_transaction_financial_info_rent_payment_status ON transaction.transaction_financial_info(first_rent_payment_status);
CREATE INDEX idx_transaction_financial_info_created_at ON transaction.transaction_financial_info(created_at);
CREATE INDEX idx_transaction_financial_info_gst_applicable ON transaction.transaction_financial_info(gst_applicable);
CREATE INDEX idx_transaction_financial_info_utilities_included ON transaction.transaction_financial_info(utilities_included);

-- Composite indexes for common queries
CREATE INDEX idx_transaction_financial_info_payment_status_composite 
ON transaction.transaction_financial_info(bond_payment_status, first_rent_payment_status);

CREATE INDEX idx_transaction_financial_info_rent_range 
ON transaction.transaction_financial_info(weekly_rent_amount, bond_amount);

-- Index for incomplete payments
CREATE INDEX idx_transaction_financial_info_incomplete_payments 
ON transaction.transaction_financial_info(bond_payment_status, first_rent_payment_status) 
WHERE bond_payment_status != 'PAID' OR first_rent_payment_status != 'PAID';

-- Index for transactions with validation errors
CREATE INDEX idx_transaction_financial_info_validation_errors 
ON transaction.transaction_financial_info(id) 
WHERE validation_errors IS NOT NULL;

-- Index for transactions with additional costs
CREATE INDEX idx_transaction_financial_info_additional_costs 
ON transaction.transaction_financial_info(pet_bond_amount, key_money_amount, application_fee)
WHERE pet_bond_amount IS NOT NULL OR key_money_amount IS NOT NULL OR application_fee IS NOT NULL;

-- Add comments for documentation
COMMENT ON TABLE transaction.transaction_financial_info IS 'Financial information for property rental transactions including rent, bond, and payment details';

COMMENT ON COLUMN transaction.transaction_financial_info.weekly_rent_amount IS 'Weekly rental amount in AUD';
COMMENT ON COLUMN transaction.transaction_financial_info.monthly_rent_amount IS 'Calculated monthly rental amount (weekly * 52 / 12)';
COMMENT ON COLUMN transaction.transaction_financial_info.bond_amount IS 'Security deposit/bond amount in AUD';
COMMENT ON COLUMN transaction.transaction_financial_info.bond_weeks_equivalent IS 'Number of weeks of rent the bond represents';
COMMENT ON COLUMN transaction.transaction_financial_info.utilities_included IS 'Whether utilities are included in rent';
COMMENT ON COLUMN transaction.transaction_financial_info.pet_bond_amount IS 'Additional bond for pets';
COMMENT ON COLUMN transaction.transaction_financial_info.key_money_amount IS 'Key money or upfront payment';
COMMENT ON COLUMN transaction.transaction_financial_info.gst_applicable IS 'Whether GST applies to this transaction';
COMMENT ON COLUMN transaction.transaction_financial_info.landlord_bank_account_encrypted IS 'Encrypted landlord bank account details';
COMMENT ON COLUMN transaction.transaction_financial_info.tenant_bank_account_encrypted IS 'Encrypted tenant bank account details';
COMMENT ON COLUMN transaction.transaction_financial_info.validation_errors IS 'Validation warnings or errors for this financial record';

-- Create trigger to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION transaction.update_financial_info_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    
    -- Automatically calculate monthly rent if weekly rent changes
    IF NEW.weekly_rent_amount IS NOT NULL AND NEW.weekly_rent_amount != OLD.weekly_rent_amount THEN
        NEW.monthly_rent_amount = NEW.weekly_rent_amount * 52 / 12;
    END IF;
    
    -- Calculate bond weeks equivalent
    IF NEW.weekly_rent_amount IS NOT NULL AND NEW.bond_amount IS NOT NULL AND NEW.weekly_rent_amount > 0 THEN
        NEW.bond_weeks_equivalent = NEW.bond_amount / NEW.weekly_rent_amount;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_financial_info_updated_at
    BEFORE UPDATE ON transaction.transaction_financial_info
    FOR EACH ROW
    EXECUTE FUNCTION transaction.update_financial_info_updated_at();

-- Create trigger to calculate derived amounts on insert
CREATE OR REPLACE FUNCTION transaction.calculate_financial_info_derived_amounts()
RETURNS TRIGGER AS $$
BEGIN
    -- Calculate monthly rent from weekly rent
    IF NEW.weekly_rent_amount IS NOT NULL THEN
        NEW.monthly_rent_amount = NEW.weekly_rent_amount * 52 / 12;
    END IF;
    
    -- Calculate bond weeks equivalent
    IF NEW.weekly_rent_amount IS NOT NULL AND NEW.bond_amount IS NOT NULL AND NEW.weekly_rent_amount > 0 THEN
        NEW.bond_weeks_equivalent = NEW.bond_amount / NEW.weekly_rent_amount;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_calculate_financial_info_derived_amounts
    BEFORE INSERT ON transaction.transaction_financial_info
    FOR EACH ROW
    EXECUTE FUNCTION transaction.calculate_financial_info_derived_amounts();

-- Grant permissions to hanihome_user
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE transaction.transaction_financial_info TO hanihome_user;
GRANT USAGE, SELECT ON SEQUENCE transaction.transaction_financial_info_id_seq TO hanihome_user;

-- Create a view for financial summaries (excluding sensitive encrypted data)
CREATE VIEW transaction.transaction_financial_summary AS
SELECT 
    tfi.id,
    tfi.transaction_id,
    t.property_id,
    t.status as transaction_status,
    tfi.weekly_rent_amount,
    tfi.monthly_rent_amount,
    tfi.bond_amount,
    tfi.bond_weeks_equivalent,
    tfi.payment_frequency,
    tfi.utilities_included,
    tfi.pet_bond_amount,
    tfi.key_money_amount,
    tfi.application_fee,
    tfi.bond_payment_status,
    tfi.first_rent_payment_status,
    tfi.bond_paid_at,
    tfi.first_rent_paid_at,
    tfi.gst_applicable,
    tfi.gst_amount,
    tfi.total_amount_including_gst,
    (tfi.weekly_rent_amount + COALESCE(tfi.bond_amount, 0) + COALESCE(tfi.pet_bond_amount, 0) + 
     COALESCE(tfi.key_money_amount, 0) + COALESCE(tfi.application_fee, 0)) as total_upfront_amount,
    CASE 
        WHEN tfi.bond_payment_status = 'PAID' AND tfi.first_rent_payment_status = 'PAID' THEN 'COMPLETE'
        WHEN tfi.bond_payment_status = 'PAID' OR tfi.first_rent_payment_status = 'PAID' THEN 'PARTIAL'
        ELSE 'PENDING'
    END as payment_completion_status,
    tfi.validation_errors,
    tfi.created_at,
    tfi.updated_at
FROM transaction.transaction_financial_info tfi
JOIN transaction.transactions t ON tfi.transaction_id = t.id;

-- Grant access to the view
GRANT SELECT ON transaction.transaction_financial_summary TO hanihome_user;

COMMENT ON VIEW transaction.transaction_financial_summary IS 'Summary view of transaction financial information excluding sensitive encrypted data';