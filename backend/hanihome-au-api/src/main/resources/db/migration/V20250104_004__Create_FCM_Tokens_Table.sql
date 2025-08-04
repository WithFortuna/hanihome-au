-- Create FCM tokens table for push notification management
CREATE TABLE fcm_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(500) NOT NULL,
    device_id VARCHAR(100),
    device_type VARCHAR(20),
    app_version VARCHAR(20),
    active BOOLEAN NOT NULL DEFAULT true,
    last_used TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_fcm_token_user_id ON fcm_tokens(user_id);
CREATE INDEX idx_fcm_token_device_id ON fcm_tokens(device_id);
CREATE INDEX idx_fcm_token_active ON fcm_tokens(active);
CREATE INDEX idx_fcm_token_created_at ON fcm_tokens(created_at);
CREATE INDEX idx_fcm_token_last_used ON fcm_tokens(last_used);

-- Add foreign key constraint to users table
ALTER TABLE fcm_tokens ADD CONSTRAINT fk_fcm_tokens_user_id 
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

-- Add unique constraint for user_id + device_id combination
CREATE UNIQUE INDEX idx_fcm_tokens_user_device ON fcm_tokens(user_id, device_id) 
    WHERE device_id IS NOT NULL;

-- Add trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_fcm_tokens_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_fcm_tokens_updated_at
    BEFORE UPDATE ON fcm_tokens
    FOR EACH ROW
    EXECUTE FUNCTION update_fcm_tokens_updated_at();