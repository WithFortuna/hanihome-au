-- V7__Add_geo_spatial_indexes.sql
-- Add indexes for geographic coordinates and location-based searches

-- Create indexes for latitude and longitude columns for faster geographic searches
CREATE INDEX IF NOT EXISTS idx_properties_latitude ON properties (latitude);
CREATE INDEX IF NOT EXISTS idx_properties_longitude ON properties (longitude);

-- Create composite index for coordinate pair searches
CREATE INDEX IF NOT EXISTS idx_properties_coordinates ON properties (latitude, longitude);

-- PostgreSQL spatial functionality using PostGIS would require:
-- CREATE EXTENSION IF NOT EXISTS postgis;
-- For now, we'll use coordinate-based indexing without PostGIS
-- Future enhancement: Add PostGIS support for advanced spatial queries

-- Create indexes for common search combinations with coordinates
CREATE INDEX IF NOT EXISTS idx_properties_type_coordinates ON properties (property_type, latitude, longitude);
CREATE INDEX IF NOT EXISTS idx_properties_status_coordinates ON properties (status, latitude, longitude);
CREATE INDEX IF NOT EXISTS idx_properties_rent_coordinates ON properties (monthly_rent, latitude, longitude);

-- Add indexes for address-based searches
CREATE INDEX IF NOT EXISTS idx_properties_city_district ON properties (city, district);
CREATE INDEX IF NOT EXISTS idx_properties_zipcode ON properties (zip_code);

-- Create index for full-text search on address fields (if needed)
-- ALTER TABLE properties ADD FULLTEXT(address, city, district);

-- Insert some sample data with coordinates for testing (Melbourne area)
-- These are sample properties around Melbourne CBD
INSERT INTO properties (
    title, description, address, detail_address, zip_code, city, district,
    property_type, rental_type, deposit, monthly_rent, maintenance_fee,
    area, rooms, bathrooms, floor, total_floors, available_date,
    status, landlord_id, latitude, longitude,
    parking_available, pet_allowed, furnished, short_term_available,
    created_date, modified_date
) VALUES 
-- Melbourne CBD
('Modern CBD Apartment', 'Luxurious 2-bedroom apartment in the heart of Melbourne CBD', 
 '123 Collins Street, Melbourne VIC 3000', 'Apartment 1501', '3000', 'Melbourne', 'Melbourne', 
 'APARTMENT', 'MONTHLY', 5200, 650, 50, 85.5, 2, 2, 15, 20, '2024-02-01',
 'ACTIVE', 1, -37.8136, 144.9631, true, false, true, false, NOW(), NOW()),

-- South Yarra
('Elegant South Yarra Townhouse', 'Beautiful 3-bedroom townhouse near Toorak Road', 
 '456 Toorak Road, South Yarra VIC 3141', '', '3141', 'South Yarra', 'Stonnington', 
 'HOUSE', 'MONTHLY', 7800, 780, 0, 145.0, 3, 2, 0, 2, '2024-02-15',
 'ACTIVE', 1, -37.8398, 144.9889, true, true, false, false, NOW(), NOW()),

-- Carlton
('Charming Carlton Terrace', 'Historic 2-bedroom terrace house near Melbourne Uni', 
 '789 Lygon Street, Carlton VIC 3053', '', '3053', 'Carlton', 'Melbourne', 
 'HOUSE', 'MONTHLY', 6240, 520, 30, 120.0, 2, 1, 0, 2, '2024-03-01',
 'ACTIVE', 1, -37.7983, 144.9648, false, true, false, true, NOW(), NOW()),

-- Fitzroy
('Trendy Fitzroy Loft', 'Industrial-style 1-bedroom loft in trendy Fitzroy', 
 '321 Brunswick Street, Fitzroy VIC 3065', 'Loft 3', '3065', 'Fitzroy', 'Yarra', 
 'STUDIO', 'MONTHLY', 2400, 480, 20, 55.0, 1, 1, 2, 3, '2024-02-10',
 'ACTIVE', 1, -37.7964, 144.9787, false, false, true, true, NOW(), NOW()),

-- Richmond
('Richmond Warehouse Conversion', 'Spacious 2-bedroom converted warehouse', 
 '654 Swan Street, Richmond VIC 3121', '', '3121', 'Richmond', 'Yarra', 
 'APARTMENT', 'MONTHLY', 5200, 650, 40, 95.0, 2, 2, 1, 3, '2024-02-20',
 'ACTIVE', 1, -37.8197, 144.9969, true, true, false, false, NOW(), NOW()),

-- St Kilda
('Beachside St Kilda Unit', '1-bedroom unit walking distance to St Kilda Beach', 
 '987 Acland Street, St Kilda VIC 3182', 'Unit 8', '3182', 'St Kilda', 'Port Phillip', 
 'APARTMENT', 'MONTHLY', 3120, 520, 35, 60.0, 1, 1, 3, 4, '2024-02-25',
 'ACTIVE', 1, -37.8677, 144.9786, false, false, true, true, NOW(), NOW()),

-- Prahran
('Prahran Family Home', 'Spacious 4-bedroom family home with garden', 
 '147 Chapel Street, Prahran VIC 3181', '', '3181', 'Prahran', 'Stonnington', 
 'HOUSE', 'MONTHLY', 9360, 780, 0, 180.0, 4, 2, 0, 2, '2024-03-10',
 'ACTIVE', 1, -37.8516, 144.9911, true, true, false, false, NOW(), NOW()),

-- Collingwood
('Collingwood Artist Studio', 'Creative space with living area for artists', 
 '258 Smith Street, Collingwood VIC 3066', '', '3066', 'Collingwood', 'Yarra', 
 'STUDIO', 'MONTHLY', 2880, 360, 25, 40.0, 1, 1, 1, 2, '2024-02-28',
 'ACTIVE', 1, -37.7999, 144.9888, false, false, false, true, NOW(), NOW());

-- Add some comments for documentation
COMMENT ON INDEX idx_properties_coordinates IS 'Composite index for latitude,longitude searches';
COMMENT ON INDEX idx_properties_type_coordinates IS 'Index for property type filtered geographic searches';
COMMENT ON INDEX idx_properties_status_coordinates IS 'Index for status filtered geographic searches';