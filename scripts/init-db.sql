-- HaniHome AU Database Initialization Script
-- This script is executed when the PostgreSQL container starts for the first time

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis" SCHEMA public;

-- Set timezone
SET timezone = 'Australia/Sydney';

-- Create schemas for different modules
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS property;
CREATE SCHEMA IF NOT EXISTS transaction;
CREATE SCHEMA IF NOT EXISTS review;

-- Grant permissions to hanihome_user on all schemas
GRANT USAGE, CREATE ON SCHEMA auth TO hanihome_user;
GRANT USAGE, CREATE ON SCHEMA property TO hanihome_user;
GRANT USAGE, CREATE ON SCHEMA transaction TO hanihome_user;
GRANT USAGE, CREATE ON SCHEMA review TO hanihome_user;
GRANT USAGE, CREATE ON SCHEMA public TO hanihome_user;

-- Grant table permissions
ALTER DEFAULT PRIVILEGES IN SCHEMA auth GRANT ALL ON TABLES TO hanihome_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA property GRANT ALL ON TABLES TO hanihome_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA transaction GRANT ALL ON TABLES TO hanihome_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA review GRANT ALL ON TABLES TO hanihome_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO hanihome_user;

-- Grant sequence permissions
ALTER DEFAULT PRIVILEGES IN SCHEMA auth GRANT ALL ON SEQUENCES TO hanihome_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA property GRANT ALL ON SEQUENCES TO hanihome_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA transaction GRANT ALL ON SEQUENCES TO hanihome_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA review GRANT ALL ON SEQUENCES TO hanihome_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO hanihome_user;

-- Create indexes for commonly queried fields (will be used later)
-- These will be created by JPA/Hibernate, but we can add custom ones here if needed

-- Create database_info table for initialization tracking
CREATE TABLE IF NOT EXISTS public.database_info (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    version VARCHAR(50) NOT NULL,
    initialized_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- Log initialization completion
INSERT INTO public.database_info (name, version, initialized_at) 
VALUES ('hanihome_au', '1.0.0', NOW())
ON CONFLICT (name) DO UPDATE SET
    version = EXCLUDED.version,
    updated_at = NOW();