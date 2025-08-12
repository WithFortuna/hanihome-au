-- Create composite indexes for property search optimization
-- This migration creates indexes to improve search query performance

-- Composite index for price-based searches
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_properties_price_status 
ON properties (status, monthly_rent, deposit) 
WHERE status = 'ACTIVE';

-- Composite index for location-based searches
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_properties_location_status 
ON properties (status, city, latitude, longitude) 
WHERE status = 'ACTIVE';

-- Composite index for property type and rental type searches
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_properties_types_status 
ON properties (status, property_type, rental_type) 
WHERE status = 'ACTIVE';

-- Composite index for room and bathroom searches
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_properties_rooms_status 
ON properties (status, rooms, bathrooms) 
WHERE status = 'ACTIVE';

-- Composite index for area-based searches
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_properties_area_status 
ON properties (status, area) 
WHERE status = 'ACTIVE';

-- Composite index for amenity-based searches
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_properties_amenities_status 
ON properties (status, parking_available, pet_allowed, furnished, short_term_available) 
WHERE status = 'ACTIVE';

-- Index for full-text search on title and description
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_properties_fulltext_search 
ON properties USING gin(to_tsvector('english', title || ' ' || description))
WHERE status = 'ACTIVE';

-- Index for created date ordering (most common sort)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_properties_created_date_status 
ON properties (status, created_date DESC) 
WHERE status = 'ACTIVE';

-- Index for price ordering
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_properties_price_order_status 
ON properties (status, monthly_rent ASC, id ASC) 
WHERE status = 'ACTIVE';

-- Covering index for common search scenarios
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_properties_search_covering 
ON properties (status, property_type, rental_type, monthly_rent, city) 
INCLUDE (id, title, rooms, bathrooms, area, created_date)
WHERE status = 'ACTIVE';

-- Index for property favorites table
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_property_favorites_user_created 
ON property_favorites (user_id, created_at DESC);

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_property_favorites_property_id 
ON property_favorites (property_id);

-- Index for search history table
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_search_history_user_created 
ON search_history (user_id, created_at DESC);

-- Index for geographic searches (if using PostGIS in the future)
-- This is a placeholder for spatial indexing
-- CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_properties_location_gist 
-- ON properties USING gist(ll_to_earth(latitude, longitude));

-- Update table statistics for better query planning
ANALYZE properties;
ANALYZE property_favorites;
ANALYZE search_history;