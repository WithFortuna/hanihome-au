-- Integration Test Database Initialization Script
-- This script is executed when the PostgreSQL testcontainer starts

-- Create test database if not exists
-- (This is mainly for documentation as the container already creates the database)

-- Set timezone for consistent testing
SET timezone = 'UTC';

-- Create extensions that might be needed for the application
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enable PostGIS extension for geospatial data (if needed in future)
-- CREATE EXTENSION IF NOT EXISTS postgis;

-- Grant necessary permissions to test user
-- (The container user already has full permissions, but this is for clarity)
GRANT ALL PRIVILEGES ON SCHEMA public TO testuser;