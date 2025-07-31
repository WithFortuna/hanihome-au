-- Add profile-related fields to users table
ALTER TABLE users 
ADD COLUMN address VARCHAR(500),
ADD COLUMN password VARCHAR(255),
ADD COLUMN is_enabled BOOLEAN NOT NULL DEFAULT true,
ADD COLUMN two_factor_enabled BOOLEAN NOT NULL DEFAULT false,
ADD COLUMN login_attempts INTEGER NOT NULL DEFAULT 0,
ADD COLUMN locked_until TIMESTAMP;

-- Create index for performance on commonly queried fields
CREATE INDEX idx_users_enabled ON users(is_enabled);
CREATE INDEX idx_users_locked_until ON users(locked_until);
CREATE INDEX idx_users_login_attempts ON users(login_attempts);

-- Add comment for documentation
COMMENT ON COLUMN users.address IS 'User address for profile information';
COMMENT ON COLUMN users.password IS 'Encrypted password for users with password login';
COMMENT ON COLUMN users.is_enabled IS 'Account enabled status';
COMMENT ON COLUMN users.two_factor_enabled IS 'Two-factor authentication enabled status';
COMMENT ON COLUMN users.login_attempts IS 'Failed login attempt counter';
COMMENT ON COLUMN users.locked_until IS 'Account lock expiration timestamp';