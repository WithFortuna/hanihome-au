-- Create users table
CREATE TABLE IF NOT EXISTS users (
    user_id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    profile_image_url VARCHAR(500),
    phone VARCHAR(20),
    bio VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'TENANT',
    oauth_provider VARCHAR(20) NOT NULL,
    oauth_provider_id VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_oauth_provider ON users(oauth_provider, oauth_provider_id);
CREATE INDEX idx_users_is_active ON users(is_active);
CREATE INDEX idx_users_created_at ON users(created_at);

-- Add constraints for enum values
ALTER TABLE users ADD CONSTRAINT chk_user_role 
    CHECK (role IN ('TENANT', 'LANDLORD', 'AGENT', 'ADMIN'));

ALTER TABLE users ADD CONSTRAINT chk_oauth_provider 
    CHECK (oauth_provider IN ('GOOGLE', 'KAKAO', 'APPLE'));

-- Ensure unique combination of oauth provider and provider id
CREATE UNIQUE INDEX idx_users_oauth_unique ON users(oauth_provider, oauth_provider_id);

-- Add comments for documentation
COMMENT ON TABLE users IS 'User accounts with OAuth authentication';
COMMENT ON COLUMN users.user_id IS 'Primary key for user identification';
COMMENT ON COLUMN users.email IS 'User email address, must be unique';
COMMENT ON COLUMN users.name IS 'Display name of the user';
COMMENT ON COLUMN users.role IS 'User role: TENANT, LANDLORD, AGENT, or ADMIN';
COMMENT ON COLUMN users.oauth_provider IS 'OAuth provider: GOOGLE, KAKAO, or APPLE';
COMMENT ON COLUMN users.oauth_provider_id IS 'Unique identifier from OAuth provider';
COMMENT ON COLUMN users.is_active IS 'Whether the user account is active';
COMMENT ON COLUMN users.is_email_verified IS 'Whether the email address has been verified';
COMMENT ON COLUMN users.last_login_at IS 'Timestamp of last successful login';
COMMENT ON COLUMN users.created_at IS 'Timestamp when the user was created';
COMMENT ON COLUMN users.updated_at IS 'Timestamp when the user was last updated';