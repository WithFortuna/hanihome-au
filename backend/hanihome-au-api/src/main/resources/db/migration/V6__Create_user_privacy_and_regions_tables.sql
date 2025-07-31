-- Create user privacy settings table
CREATE TABLE user_privacy_settings (
    privacy_settings_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(user_id) ON DELETE CASCADE,
    name_privacy VARCHAR(20) NOT NULL DEFAULT 'MEMBERS_ONLY',
    phone_privacy VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    email_privacy VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    address_privacy VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    bio_privacy VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    profile_image_privacy VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    gdpr_consent_given BOOLEAN NOT NULL DEFAULT false,
    gdpr_consent_at TIMESTAMP,
    marketing_consent BOOLEAN NOT NULL DEFAULT false,
    data_processing_consent BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create user preferred regions table
CREATE TABLE user_preferred_regions (
    preferred_region_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    region_name VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    country VARCHAR(50) NOT NULL DEFAULT 'Australia',
    priority INTEGER NOT NULL DEFAULT 1,
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    radius_km INTEGER DEFAULT 10,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, region_name)
);

-- Create indexes for performance
CREATE INDEX idx_user_privacy_settings_user_id ON user_privacy_settings(user_id);
CREATE INDEX idx_user_preferred_regions_user_id ON user_preferred_regions(user_id);
CREATE INDEX idx_user_preferred_regions_active ON user_preferred_regions(is_active);
CREATE INDEX idx_user_preferred_regions_priority ON user_preferred_regions(priority);
CREATE INDEX idx_user_preferred_regions_location ON user_preferred_regions(latitude, longitude);

-- Add constraints for enum values
ALTER TABLE user_privacy_settings ADD CONSTRAINT chk_name_privacy 
    CHECK (name_privacy IN ('PUBLIC', 'MEMBERS_ONLY', 'PRIVATE'));

ALTER TABLE user_privacy_settings ADD CONSTRAINT chk_phone_privacy 
    CHECK (phone_privacy IN ('PUBLIC', 'MEMBERS_ONLY', 'PRIVATE'));

ALTER TABLE user_privacy_settings ADD CONSTRAINT chk_email_privacy 
    CHECK (email_privacy IN ('PUBLIC', 'MEMBERS_ONLY', 'PRIVATE'));

ALTER TABLE user_privacy_settings ADD CONSTRAINT chk_address_privacy 
    CHECK (address_privacy IN ('PUBLIC', 'MEMBERS_ONLY', 'PRIVATE'));

ALTER TABLE user_privacy_settings ADD CONSTRAINT chk_bio_privacy 
    CHECK (bio_privacy IN ('PUBLIC', 'MEMBERS_ONLY', 'PRIVATE'));

ALTER TABLE user_privacy_settings ADD CONSTRAINT chk_profile_image_privacy 
    CHECK (profile_image_privacy IN ('PUBLIC', 'MEMBERS_ONLY', 'PRIVATE'));

-- Validate coordinates are within valid ranges
ALTER TABLE user_preferred_regions ADD CONSTRAINT chk_latitude 
    CHECK (latitude IS NULL OR (latitude >= -90 AND latitude <= 90));

ALTER TABLE user_preferred_regions ADD CONSTRAINT chk_longitude 
    CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180));

ALTER TABLE user_preferred_regions ADD CONSTRAINT chk_radius_positive 
    CHECK (radius_km IS NULL OR radius_km > 0);

ALTER TABLE user_preferred_regions ADD CONSTRAINT chk_priority_positive 
    CHECK (priority > 0);

-- Create trigger to automatically update updated_at column
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_user_privacy_settings_updated_at BEFORE UPDATE
    ON user_privacy_settings FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add comments for documentation
COMMENT ON TABLE user_privacy_settings IS 'Privacy settings for user profile information';
COMMENT ON COLUMN user_privacy_settings.privacy_settings_id IS 'Primary key for privacy settings';
COMMENT ON COLUMN user_privacy_settings.user_id IS 'Foreign key to users table';
COMMENT ON COLUMN user_privacy_settings.name_privacy IS 'Privacy level for user name: PUBLIC, MEMBERS_ONLY, PRIVATE';
COMMENT ON COLUMN user_privacy_settings.phone_privacy IS 'Privacy level for phone number';
COMMENT ON COLUMN user_privacy_settings.email_privacy IS 'Privacy level for email address';
COMMENT ON COLUMN user_privacy_settings.address_privacy IS 'Privacy level for address information';
COMMENT ON COLUMN user_privacy_settings.bio_privacy IS 'Privacy level for user bio/description';
COMMENT ON COLUMN user_privacy_settings.profile_image_privacy IS 'Privacy level for profile image';
COMMENT ON COLUMN user_privacy_settings.gdpr_consent_given IS 'Whether user has given GDPR consent';
COMMENT ON COLUMN user_privacy_settings.gdpr_consent_at IS 'Timestamp when GDPR consent was given';
COMMENT ON COLUMN user_privacy_settings.marketing_consent IS 'Whether user consented to marketing communications';
COMMENT ON COLUMN user_privacy_settings.data_processing_consent IS 'Whether user consented to data processing';

COMMENT ON TABLE user_preferred_regions IS 'User preferred regions for property searches';
COMMENT ON COLUMN user_preferred_regions.preferred_region_id IS 'Primary key for preferred regions';
COMMENT ON COLUMN user_preferred_regions.user_id IS 'Foreign key to users table';
COMMENT ON COLUMN user_preferred_regions.region_name IS 'Name of the preferred region/suburb';
COMMENT ON COLUMN user_preferred_regions.state IS 'Australian state (NSW, VIC, QLD, etc.)';
COMMENT ON COLUMN user_preferred_regions.country IS 'Country name, default Australia';
COMMENT ON COLUMN user_preferred_regions.priority IS 'Priority level (1=highest priority)';
COMMENT ON COLUMN user_preferred_regions.latitude IS 'Latitude coordinate for region center';
COMMENT ON COLUMN user_preferred_regions.longitude IS 'Longitude coordinate for region center';
COMMENT ON COLUMN user_preferred_regions.radius_km IS 'Search radius in kilometers from coordinates';
COMMENT ON COLUMN user_preferred_regions.is_active IS 'Whether this preferred region is currently active';

-- Initialize privacy settings for existing users
INSERT INTO user_privacy_settings (user_id)
SELECT user_id FROM users 
WHERE NOT EXISTS (
    SELECT 1 FROM user_privacy_settings ups 
    WHERE ups.user_id = users.user_id
);