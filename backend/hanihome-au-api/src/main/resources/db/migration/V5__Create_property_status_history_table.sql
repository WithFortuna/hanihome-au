-- V5: Create property status history table
-- Author: System
-- Description: Create property status history table for tracking all status changes

CREATE TABLE property_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id BIGINT NOT NULL,
    previous_status VARCHAR(50) NOT NULL,
    new_status VARCHAR(50) NOT NULL,
    changed_by BIGINT NOT NULL,
    reason TEXT,
    notes TEXT,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE
);

-- Create indexes for property_status_history table
CREATE INDEX idx_property_status_history_property_id ON property_status_history(property_id);
CREATE INDEX idx_property_status_history_created_date ON property_status_history(created_date);
CREATE INDEX idx_property_status_history_status ON property_status_history(new_status);
CREATE INDEX idx_property_status_history_changed_by ON property_status_history(changed_by);

-- Add constraints for status values
ALTER TABLE property_status_history ADD CONSTRAINT chk_previous_status 
    CHECK (previous_status IN ('ACTIVE', 'INACTIVE', 'PENDING_APPROVAL', 'REJECTED', 'COMPLETED', 'SUSPENDED'));

ALTER TABLE property_status_history ADD CONSTRAINT chk_new_status 
    CHECK (new_status IN ('ACTIVE', 'INACTIVE', 'PENDING_APPROVAL', 'REJECTED', 'COMPLETED', 'SUSPENDED'));