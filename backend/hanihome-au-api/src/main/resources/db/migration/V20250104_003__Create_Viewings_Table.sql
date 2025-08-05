-- Viewings table for property viewing appointments
-- Created: 2025-01-04
-- Purpose: Store property viewing appointments and their status

CREATE TABLE IF NOT EXISTS viewings (
    id BIGSERIAL PRIMARY KEY,
    property_id BIGINT NOT NULL,
    tenant_user_id BIGINT NOT NULL,
    landlord_user_id BIGINT NOT NULL,
    agent_user_id BIGINT NULL,
    
    scheduled_at TIMESTAMP NOT NULL,
    duration_minutes INTEGER NOT NULL DEFAULT 60,
    status VARCHAR(20) NOT NULL DEFAULT 'REQUESTED',
    
    tenant_notes TEXT,
    landlord_notes TEXT,
    agent_notes TEXT,
    
    contact_phone VARCHAR(20),
    contact_email VARCHAR(255),
    
    confirmed_at TIMESTAMP NULL,
    cancelled_at TIMESTAMP NULL,
    cancellation_reason VARCHAR(500),
    cancelled_by_user_id BIGINT,
    
    completed_at TIMESTAMP NULL,
    feedback_rating INTEGER CHECK (feedback_rating >= 1 AND feedback_rating <= 5),
    feedback_comment TEXT,
    
    rescheduled_from_viewing_id BIGINT,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    
    -- Foreign key constraints (assuming property and user tables exist)
    CONSTRAINT fk_viewings_property FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    CONSTRAINT fk_viewings_rescheduled_from FOREIGN KEY (rescheduled_from_viewing_id) REFERENCES viewings(id) ON DELETE SET NULL,
    
    -- Check constraints
    CONSTRAINT chk_viewings_status CHECK (status IN ('REQUESTED', 'CONFIRMED', 'CANCELLED', 'COMPLETED', 'NO_SHOW')),
    CONSTRAINT chk_viewings_duration CHECK (duration_minutes > 0 AND duration_minutes <= 480), -- Max 8 hours
    CONSTRAINT chk_viewings_scheduled_future CHECK (scheduled_at > created_at),
    CONSTRAINT chk_viewings_feedback_complete CHECK (
        (feedback_rating IS NULL AND feedback_comment IS NULL) OR 
        (status = 'COMPLETED' AND feedback_rating IS NOT NULL)
    )
);

-- Indexes for performance optimization
CREATE INDEX idx_viewings_property_id ON viewings(property_id);
CREATE INDEX idx_viewings_tenant_user_id ON viewings(tenant_user_id);
CREATE INDEX idx_viewings_landlord_user_id ON viewings(landlord_user_id);
CREATE INDEX idx_viewings_agent_user_id ON viewings(agent_user_id) WHERE agent_user_id IS NOT NULL;
CREATE INDEX idx_viewings_status ON viewings(status);
CREATE INDEX idx_viewings_scheduled_at ON viewings(scheduled_at);
CREATE INDEX idx_viewings_created_at ON viewings(created_at);

-- Composite indexes for common queries
CREATE INDEX idx_viewings_property_status_scheduled ON viewings(property_id, status, scheduled_at);
CREATE INDEX idx_viewings_tenant_status_scheduled ON viewings(tenant_user_id, status, scheduled_at DESC);
CREATE INDEX idx_viewings_landlord_status_scheduled ON viewings(landlord_user_id, status, scheduled_at DESC);

-- Index for conflict detection queries
CREATE INDEX idx_viewings_conflict_detection ON viewings(property_id, scheduled_at, duration_minutes) 
WHERE status IN ('REQUESTED', 'CONFIRMED');

-- Index for finding upcoming confirmed viewings (for reminders)
CREATE INDEX idx_viewings_upcoming_confirmed ON viewings(status, scheduled_at) 
WHERE status = 'CONFIRMED';

-- Index for finding overdue viewings
CREATE INDEX idx_viewings_overdue ON viewings(status, scheduled_at, duration_minutes) 
WHERE status = 'CONFIRMED';

-- Add table and column comments
COMMENT ON TABLE viewings IS 'Property viewing appointments between tenants and landlords/agents';
COMMENT ON COLUMN viewings.property_id IS 'ID of the property being viewed';
COMMENT ON COLUMN viewings.tenant_user_id IS 'ID of the user requesting the viewing';
COMMENT ON COLUMN viewings.landlord_user_id IS 'ID of the property owner';
COMMENT ON COLUMN viewings.agent_user_id IS 'ID of the real estate agent (optional)';
COMMENT ON COLUMN viewings.scheduled_at IS 'Start time of the viewing appointment';
COMMENT ON COLUMN viewings.duration_minutes IS 'Duration of the viewing in minutes';
COMMENT ON COLUMN viewings.status IS 'Current status of the viewing appointment';
COMMENT ON COLUMN viewings.feedback_rating IS 'Rating given by tenant after viewing (1-5)';
COMMENT ON COLUMN viewings.rescheduled_from_viewing_id IS 'Original viewing ID if this is a rescheduled appointment';

-- Trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_viewings_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_viewings_updated_at
    BEFORE UPDATE ON viewings
    FOR EACH ROW
    EXECUTE FUNCTION update_viewings_updated_at();