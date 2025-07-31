-- V1__Initial_Schema.sql
-- HaniHome AU Initial Database Schema

-- Enable necessary extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create schemas for different modules
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS property;
CREATE SCHEMA IF NOT EXISTS transaction;
CREATE SCHEMA IF NOT EXISTS review;

-- Database info table for tracking initialization and migrations
CREATE TABLE IF NOT EXISTS public.database_info (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    version VARCHAR(50) NOT NULL,
    initialized_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- Insert initial database info
INSERT INTO public.database_info (name, version, initialized_at) 
VALUES ('hanihome_au', '1.0.0', NOW())
ON CONFLICT (name) DO UPDATE SET
    version = EXCLUDED.version,
    updated_at = NOW();

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_database_info_name ON public.database_info(name);

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