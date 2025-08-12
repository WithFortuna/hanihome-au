-- Cleanup script for test data
-- This script removes all test data from H2 database tables
-- Used with @Sql annotation for test cleanup

-- Disable foreign key checks temporarily
SET REFERENTIAL_INTEGRITY FALSE;

-- Delete data in reverse dependency order
DELETE FROM report_actions;
DELETE FROM reports;
DELETE FROM transaction_financial_info;
DELETE FROM transaction_activities;
DELETE FROM transactions;
DELETE FROM viewings;
DELETE FROM fcm_tokens;
DELETE FROM search_history;
DELETE FROM property_favorites;
DELETE FROM property_status_history;
DELETE FROM property_images;
DELETE FROM properties;
DELETE FROM users;

-- Reset auto-increment sequences
ALTER TABLE users ALTER COLUMN id RESTART WITH 1;
ALTER TABLE properties ALTER COLUMN id RESTART WITH 1;
ALTER TABLE property_images ALTER COLUMN id RESTART WITH 1;
ALTER TABLE property_status_history ALTER COLUMN id RESTART WITH 1;
ALTER TABLE property_favorites ALTER COLUMN id RESTART WITH 1;
ALTER TABLE search_history ALTER COLUMN id RESTART WITH 1;
ALTER TABLE fcm_tokens ALTER COLUMN id RESTART WITH 1;
ALTER TABLE viewings ALTER COLUMN id RESTART WITH 1;
ALTER TABLE transactions ALTER COLUMN id RESTART WITH 1;
ALTER TABLE transaction_activities ALTER COLUMN id RESTART WITH 1;
ALTER TABLE transaction_financial_info ALTER COLUMN id RESTART WITH 1;
ALTER TABLE reports ALTER COLUMN id RESTART WITH 1;
ALTER TABLE report_actions ALTER COLUMN id RESTART WITH 1;

-- Re-enable foreign key checks
SET REFERENTIAL_INTEGRITY TRUE;