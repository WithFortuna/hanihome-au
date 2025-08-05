-- V20250105_001__Create_Transaction_Tables.sql
-- Create transaction and transaction_activities tables

-- Create transactions table
CREATE TABLE transaction.transactions (
    id BIGSERIAL PRIMARY KEY,
    property_id BIGINT NOT NULL,
    tenant_user_id BIGINT NOT NULL,
    landlord_user_id BIGINT NOT NULL,
    agent_user_id BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'PROPOSED',
    proposed_rent_amount DECIMAL(12,2),
    proposed_bond_amount DECIMAL(12,2),
    final_rent_amount DECIMAL(12,2),
    final_bond_amount DECIMAL(12,2),
    proposed_lease_start_date TIMESTAMP,
    proposed_lease_end_date TIMESTAMP,
    final_lease_start_date TIMESTAMP,
    final_lease_end_date TIMESTAMP,
    contract_document_url VARCHAR(500),
    docusign_envelope_id VARCHAR(255),
    tenant_signed_at TIMESTAMP,
    landlord_signed_at TIMESTAMP,
    contract_completed_at TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    created_by BIGINT,
    updated_by BIGINT,
    
    CONSTRAINT fk_transactions_property FOREIGN KEY (property_id) REFERENCES property.properties(id),
    CONSTRAINT fk_transactions_tenant FOREIGN KEY (tenant_user_id) REFERENCES auth.users(id),
    CONSTRAINT fk_transactions_landlord FOREIGN KEY (landlord_user_id) REFERENCES auth.users(id),
    CONSTRAINT fk_transactions_agent FOREIGN KEY (agent_user_id) REFERENCES auth.users(id),
    CONSTRAINT fk_transactions_created_by FOREIGN KEY (created_by) REFERENCES auth.users(id),
    CONSTRAINT fk_transactions_updated_by FOREIGN KEY (updated_by) REFERENCES auth.users(id),
    
    CONSTRAINT chk_transactions_status CHECK (status IN ('PROPOSED', 'NEGOTIATING', 'APPROVED', 'CONTRACT_PENDING', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_transactions_amounts_positive CHECK (
        (proposed_rent_amount IS NULL OR proposed_rent_amount >= 0) AND
        (proposed_bond_amount IS NULL OR proposed_bond_amount >= 0) AND
        (final_rent_amount IS NULL OR final_rent_amount >= 0) AND
        (final_bond_amount IS NULL OR final_bond_amount >= 0)
    ),
    CONSTRAINT chk_transactions_lease_dates CHECK (
        (proposed_lease_start_date IS NULL OR proposed_lease_end_date IS NULL OR proposed_lease_start_date < proposed_lease_end_date) AND
        (final_lease_start_date IS NULL OR final_lease_end_date IS NULL OR final_lease_start_date < final_lease_end_date)
    )
);

-- Create transaction_activities table
CREATE TABLE transaction.transaction_activities (
    id BIGSERIAL PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    activity_type VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    user_id BIGINT NOT NULL,
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_transaction_activities_transaction FOREIGN KEY (transaction_id) REFERENCES transaction.transactions(id) ON DELETE CASCADE,
    CONSTRAINT fk_transaction_activities_user FOREIGN KEY (user_id) REFERENCES auth.users(id),
    
    CONSTRAINT chk_activity_type CHECK (activity_type IN (
        'TRANSACTION_CREATED', 'OFFER_MADE', 'COUNTER_OFFER_MADE', 'OFFER_ACCEPTED', 'OFFER_REJECTED',
        'CONTRACT_REQUESTED', 'CONTRACT_SIGNED', 'CONTRACT_COMPLETED', 'TRANSACTION_CANCELLED',
        'PROPERTY_STATUS_UPDATED', 'PAYMENT_RECEIVED', 'VIEWING_SCHEDULED', 'VIEWING_COMPLETED',
        'MESSAGE_SENT', 'DOCUMENT_UPLOADED', 'STATUS_CHANGED'
    ))
);

-- Create indexes for performance
CREATE INDEX idx_transactions_property_id ON transaction.transactions(property_id);
CREATE INDEX idx_transactions_tenant_user_id ON transaction.transactions(tenant_user_id);
CREATE INDEX idx_transactions_landlord_user_id ON transaction.transactions(landlord_user_id);
CREATE INDEX idx_transactions_agent_user_id ON transaction.transactions(agent_user_id);
CREATE INDEX idx_transactions_status ON transaction.transactions(status);
CREATE INDEX idx_transactions_created_at ON transaction.transactions(created_at);
CREATE INDEX idx_transactions_updated_at ON transaction.transactions(updated_at);
CREATE INDEX idx_transactions_status_created_at ON transaction.transactions(status, created_at);

-- Composite index for finding user's transactions
CREATE INDEX idx_transactions_user_composite ON transaction.transactions(tenant_user_id, landlord_user_id, agent_user_id, created_at DESC);

-- Index for active transactions query
CREATE INDEX idx_transactions_active ON transaction.transactions(status, created_at) WHERE status NOT IN ('COMPLETED', 'CANCELLED');

-- Index for pending signature transactions
CREATE INDEX idx_transactions_pending_signature ON transaction.transactions(status, tenant_signed_at, landlord_signed_at) 
WHERE status = 'CONTRACT_PENDING';

-- Indexes for transaction_activities
CREATE INDEX idx_transaction_activities_transaction_id ON transaction.transaction_activities(transaction_id);
CREATE INDEX idx_transaction_activities_user_id ON transaction.transaction_activities(user_id);
CREATE INDEX idx_transaction_activities_activity_type ON transaction.transaction_activities(activity_type);
CREATE INDEX idx_transaction_activities_created_at ON transaction.transaction_activities(created_at);
CREATE INDEX idx_transaction_activities_transaction_created_at ON transaction.transaction_activities(transaction_id, created_at);

-- Index for timeline activities
CREATE INDEX idx_transaction_activities_timeline ON transaction.transaction_activities(transaction_id, activity_type, created_at)
WHERE activity_type IN ('TRANSACTION_CREATED', 'OFFER_MADE', 'COUNTER_OFFER_MADE', 'OFFER_ACCEPTED', 
                       'CONTRACT_REQUESTED', 'CONTRACT_SIGNED', 'CONTRACT_COMPLETED', 'TRANSACTION_CANCELLED', 'STATUS_CHANGED');

-- Index for searching activities by description
CREATE INDEX idx_transaction_activities_description_search ON transaction.transaction_activities USING gin(to_tsvector('english', description));

-- Add comments for documentation
COMMENT ON TABLE transaction.transactions IS 'Main transaction records tracking property rental deals';
COMMENT ON TABLE transaction.transaction_activities IS 'Activity log for all transaction-related events';

COMMENT ON COLUMN transaction.transactions.status IS 'Current status of the transaction';
COMMENT ON COLUMN transaction.transactions.version IS 'Optimistic locking version field';
COMMENT ON COLUMN transaction.transactions.proposed_rent_amount IS 'Initially proposed rental amount per week/month';
COMMENT ON COLUMN transaction.transactions.proposed_bond_amount IS 'Initially proposed bond/security deposit amount';
COMMENT ON COLUMN transaction.transactions.final_rent_amount IS 'Final agreed rental amount';
COMMENT ON COLUMN transaction.transactions.final_bond_amount IS 'Final agreed bond amount';
COMMENT ON COLUMN transaction.transactions.docusign_envelope_id IS 'DocuSign envelope ID for contract signing';

COMMENT ON COLUMN transaction.transaction_activities.activity_type IS 'Type of activity that occurred';
COMMENT ON COLUMN transaction.transaction_activities.metadata IS 'JSON formatted additional data for the activity';

-- Create trigger to automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION transaction.update_transactions_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_transactions_updated_at
    BEFORE UPDATE ON transaction.transactions
    FOR EACH ROW
    EXECUTE FUNCTION transaction.update_transactions_updated_at();

-- Grant permissions to hanihome_user
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE transaction.transactions TO hanihome_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLE transaction.transaction_activities TO hanihome_user;
GRANT USAGE, SELECT ON SEQUENCE transaction.transactions_id_seq TO hanihome_user;
GRANT USAGE, SELECT ON SEQUENCE transaction.transaction_activities_id_seq TO hanihome_user;