-- V4: Create properties and related tables
-- Author: System
-- Description: Create properties, property_options, property_images tables with proper indexing

-- Create properties table
CREATE TABLE properties (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    address VARCHAR(500) NOT NULL,
    detail_address VARCHAR(100),
    zip_code VARCHAR(10),
    city VARCHAR(50),
    district VARCHAR(50),
    property_type VARCHAR(50) NOT NULL,
    rental_type VARCHAR(50) NOT NULL,
    deposit DECIMAL(12,0),
    monthly_rent DECIMAL(10,0),
    maintenance_fee DECIMAL(12,0),
    area DECIMAL(8,2),
    rooms INT,
    bathrooms INT,
    floor INT,
    total_floors INT,
    available_date DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING_APPROVAL',
    landlord_id BIGINT NOT NULL,
    agent_id BIGINT,
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    parking_available BOOLEAN,
    pet_allowed BOOLEAN,
    furnished BOOLEAN,
    short_term_available BOOLEAN,
    admin_notes TEXT,
    approved_at TIMESTAMP,
    approved_by BIGINT,
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Create property_options table (for ElementCollection)
CREATE TABLE property_options (
    property_id BIGINT NOT NULL,
    option_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (property_id, option_name),
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE
);

-- Create property_images table (redesigned as separate entity)
CREATE TABLE property_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    property_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    thumbnail_url VARCHAR(500),
    image_order INT NOT NULL DEFAULT 0,
    description VARCHAR(200),
    is_main BOOLEAN NOT NULL DEFAULT FALSE,
    file_size BIGINT,
    content_type VARCHAR(50),
    original_file_name VARCHAR(100),
    created_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE
);

-- Create indexes for properties table
CREATE INDEX idx_property_type ON properties(property_type);
CREATE INDEX idx_rental_type ON properties(rental_type);
CREATE INDEX idx_status ON properties(status);
CREATE INDEX idx_monthly_rent ON properties(monthly_rent);
CREATE INDEX idx_deposit ON properties(deposit);
CREATE INDEX idx_address ON properties(address);
CREATE INDEX idx_available_date ON properties(available_date);
CREATE INDEX idx_created_date ON properties(created_date);
CREATE INDEX idx_landlord_id ON properties(landlord_id);
CREATE INDEX idx_location ON properties(latitude, longitude);
CREATE INDEX idx_area ON properties(area);
CREATE INDEX idx_price_range ON properties(deposit, monthly_rent);

-- Create indexes for property_images table
CREATE INDEX idx_property_id ON property_images(property_id);
CREATE INDEX idx_image_order ON property_images(image_order);
CREATE INDEX idx_is_main ON property_images(is_main);

-- Add constraints
ALTER TABLE properties ADD CONSTRAINT chk_property_type 
    CHECK (property_type IN ('APARTMENT', 'VILLA', 'STUDIO', 'TWO_ROOM', 'THREE_ROOM', 'OFFICETEL', 'HOUSE'));

ALTER TABLE properties ADD CONSTRAINT chk_rental_type 
    CHECK (rental_type IN ('MONTHLY', 'JEONSE', 'SALE'));

ALTER TABLE properties ADD CONSTRAINT chk_status 
    CHECK (status IN ('ACTIVE', 'INACTIVE', 'PENDING_APPROVAL', 'REJECTED', 'COMPLETED', 'SUSPENDED'));

ALTER TABLE properties ADD CONSTRAINT chk_positive_values 
    CHECK (
        (deposit IS NULL OR deposit >= 0) AND 
        (monthly_rent IS NULL OR monthly_rent >= 0) AND 
        (maintenance_fee IS NULL OR maintenance_fee >= 0) AND
        (area IS NULL OR area > 0) AND
        (rooms IS NULL OR rooms >= 0) AND
        (bathrooms IS NULL OR bathrooms >= 0)
    );

-- Ensure only one main image per property
CREATE UNIQUE INDEX idx_unique_main_image ON property_images(property_id, is_main) 
    WHERE is_main = TRUE;