-- Test Data for H2 Database
-- This data is used for unit and integration tests

-- Insert test users
INSERT INTO users (id, email, name, oauth_provider, oauth_id, user_role, phone_number, preferred_language, is_active, created_at, updated_at) VALUES
(1, 'tenant1@test.com', 'John Smith', 'GOOGLE', 'google123', 'TENANT', '+61412345678', 'en', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'landlord1@test.com', 'Sarah Johnson', 'GOOGLE', 'google456', 'LANDLORD', '+61423456789', 'en', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'agent1@test.com', 'Michael Brown', 'GOOGLE', 'google789', 'AGENT', '+61434567890', 'en', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'admin1@test.com', 'Admin User', 'GOOGLE', 'google101', 'ADMIN', '+61445678901', 'en', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'tenant2@test.com', '김민수', 'GOOGLE', 'google112', 'TENANT', '+61456789012', 'ko', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'landlord2@test.com', 'Emma Wilson', 'APPLE', 'apple123', 'LANDLORD', '+61467890123', 'en', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'inactive@test.com', 'Inactive User', 'GOOGLE', 'google999', 'TENANT', '+61478901234', 'en', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert test properties
INSERT INTO properties (id, title, description, property_type, rental_type, status, price, currency, deposit, bedrooms, bathrooms, parking_spaces, floor_area, 
                       address_line1, suburb, state, postcode, country, latitude, longitude, available_from, pet_friendly, furnished, 
                       air_conditioning, heating, internet, parking, owner_id, created_at, updated_at) VALUES
(1, 'Modern 2BR Apartment in CBD', 'Beautiful modern apartment with city views', 'APARTMENT', 'LONG_TERM', 'ACTIVE', 2500.00, 'AUD', 5000.00, 2, 2, 1, 85.0,
 '123 Collins Street', 'Melbourne', 'VIC', '3000', 'Australia', -37.8136, 144.9631, '2024-02-01', FALSE, TRUE, TRUE, TRUE, TRUE, TRUE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(2, 'Spacious 3BR House with Garden', 'Family home with large backyard', 'HOUSE', 'LONG_TERM', 'ACTIVE', 3200.00, 'AUD', 6400.00, 3, 2, 2, 150.0,
 '456 Smith Street', 'Richmond', 'VIC', '3121', 'Australia', -37.8197, 144.9834, '2024-03-01', TRUE, FALSE, TRUE, TRUE, TRUE, TRUE, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(3, 'Studio Apartment Near University', 'Perfect for students', 'STUDIO', 'SHORT_TERM', 'ACTIVE', 1200.00, 'AUD', 2400.00, 0, 1, 0, 35.0,
 '789 Swanston Street', 'Carlton', 'VIC', '3053', 'Australia', -37.7963, 144.9664, '2024-01-15', FALSE, TRUE, FALSE, TRUE, TRUE, FALSE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(4, 'Luxury Penthouse with Harbor Views', 'Premium property with stunning views', 'PENTHOUSE', 'LONG_TERM', 'INACTIVE', 8500.00, 'AUD', 17000.00, 4, 3, 2, 250.0,
 '101 Circular Quay', 'Sydney', 'NSW', '2000', 'Australia', -33.8608, 151.2108, '2024-04-01', FALSE, TRUE, TRUE, TRUE, TRUE, TRUE, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

(5, 'Cozy 1BR Unit Close to Beach', 'Walking distance to Bondi Beach', 'UNIT', 'SHORT_TERM', 'ACTIVE', 1800.00, 'AUD', 3600.00, 1, 1, 0, 55.0,
 '234 Campbell Parade', 'Bondi Beach', 'NSW', '2026', 'Australia', -33.8915, 151.2767, '2024-02-15', TRUE, TRUE, TRUE, FALSE, TRUE, FALSE, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert property images
INSERT INTO property_images (property_id, image_url, image_order, is_primary, alt_text, created_at) VALUES
(1, 'https://test-bucket.s3.amazonaws.com/property1_main.jpg', 0, TRUE, 'Main view of modern apartment', CURRENT_TIMESTAMP),
(1, 'https://test-bucket.s3.amazonaws.com/property1_kitchen.jpg', 1, FALSE, 'Modern kitchen', CURRENT_TIMESTAMP),
(1, 'https://test-bucket.s3.amazonaws.com/property1_bedroom.jpg', 2, FALSE, 'Master bedroom', CURRENT_TIMESTAMP),
(2, 'https://test-bucket.s3.amazonaws.com/property2_exterior.jpg', 0, TRUE, 'House exterior with garden', CURRENT_TIMESTAMP),
(2, 'https://test-bucket.s3.amazonaws.com/property2_living.jpg', 1, FALSE, 'Spacious living room', CURRENT_TIMESTAMP),
(3, 'https://test-bucket.s3.amazonaws.com/property3_studio.jpg', 0, TRUE, 'Studio apartment layout', CURRENT_TIMESTAMP),
(4, 'https://test-bucket.s3.amazonaws.com/property4_penthouse.jpg', 0, TRUE, 'Luxury penthouse with harbor views', CURRENT_TIMESTAMP),
(5, 'https://test-bucket.s3.amazonaws.com/property5_beachview.jpg', 0, TRUE, 'Beach view from apartment', CURRENT_TIMESTAMP);

-- Insert property status history
INSERT INTO property_status_history (property_id, previous_status, new_status, changed_by, reason, created_at) VALUES
(1, NULL, 'ACTIVE', 2, 'Initial property listing', DATEADD('DAY', -30, CURRENT_TIMESTAMP)),
(2, NULL, 'ACTIVE', 6, 'Initial property listing', DATEADD('DAY', -25, CURRENT_TIMESTAMP)),
(3, NULL, 'ACTIVE', 2, 'Initial property listing', DATEADD('DAY', -20, CURRENT_TIMESTAMP)),
(4, 'ACTIVE', 'INACTIVE', 4, 'Property maintenance required', DATEADD('DAY', -10, CURRENT_TIMESTAMP)),
(5, NULL, 'ACTIVE', 2, 'Initial property listing', DATEADD('DAY', -15, CURRENT_TIMESTAMP));

-- Insert property favorites
INSERT INTO property_favorites (user_id, property_id, created_at) VALUES
(1, 1, CURRENT_TIMESTAMP),
(1, 2, CURRENT_TIMESTAMP),
(1, 5, CURRENT_TIMESTAMP),
(5, 1, CURRENT_TIMESTAMP),
(5, 3, CURRENT_TIMESTAMP);

-- Insert search history
INSERT INTO search_history (user_id, search_query, search_filters, result_count, search_duration_ms, created_at) VALUES
(1, 'apartment melbourne', '{"minPrice":2000,"maxPrice":3000,"bedrooms":2}', 15, 245, DATEADD('HOUR', -2, CURRENT_TIMESTAMP)),
(1, 'house richmond', '{"propertyType":"HOUSE","petFriendly":true}', 8, 189, DATEADD('HOUR', -1, CURRENT_TIMESTAMP)),
(5, '스튜디오 칼튼', '{"propertyType":"STUDIO","maxPrice":1500}', 5, 156, DATEADD('MINUTE', -30, CURRENT_TIMESTAMP));

-- Insert FCM tokens
INSERT INTO fcm_tokens (user_id, token, device_type, device_id, is_active, created_at, updated_at) VALUES
(1, 'fcm_token_user1_device1', 'ANDROID', 'android_device_123', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 'fcm_token_user1_device2', 'WEB', 'web_session_456', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'fcm_token_user2_device1', 'IOS', 'ios_device_789', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'fcm_token_user5_device1', 'ANDROID', 'android_device_321', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert viewings
INSERT INTO viewings (property_id, tenant_id, landlord_id, viewing_date, status, tenant_notes, created_at, updated_at) VALUES
(1, 1, 2, DATEADD('DAY', 2, CURRENT_TIMESTAMP), 'CONFIRMED', 'Looking forward to the viewing', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 5, 6, DATEADD('DAY', 3, CURRENT_TIMESTAMP), 'PENDING', '주말 시간이 가능합니다', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 1, 2, DATEADD('DAY', -1, CURRENT_TIMESTAMP), 'COMPLETED', 'Very interested in this property', DATEADD('DAY', -2, CURRENT_TIMESTAMP), DATEADD('DAY', -1, CURRENT_TIMESTAMP)),
(5, 5, 2, DATEADD('DAY', 1, CURRENT_TIMESTAMP), 'CONFIRMED', 'Can we meet at 2 PM?', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert transactions
INSERT INTO transactions (property_id, tenant_id, landlord_id, status, transaction_type, lease_start_date, lease_end_date, created_at, updated_at) VALUES
(3, 1, 2, 'COMPLETED', 'RENTAL', '2024-01-15', '2024-07-15', DATEADD('DAY', -5, CURRENT_TIMESTAMP), DATEADD('DAY', -2, CURRENT_TIMESTAMP),
(1, 5, 2, 'IN_PROGRESS', 'RENTAL', '2024-02-01', '2025-01-31', DATEADD('DAY', -3, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP);

-- Insert transaction activities
INSERT INTO transaction_activities (transaction_id, activity_type, description, performed_by, created_at, metadata) VALUES
(1, 'APPLICATION_SUBMITTED', 'Tenant submitted rental application', 1, DATEADD('DAY', -5, CURRENT_TIMESTAMP), '{"applicationId":"app_001"}'),
(1, 'APPLICATION_APPROVED', 'Landlord approved the application', 2, DATEADD('DAY', -4, CURRENT_TIMESTAMP), '{"approvalDate":"2024-01-10"}'),
(1, 'CONTRACT_SIGNED', 'Both parties signed the lease contract', 1, DATEADD('DAY', -2, CURRENT_TIMESTAMP), '{"contractId":"contract_001"}'),
(2, 'APPLICATION_SUBMITTED', 'Tenant submitted rental application', 5, DATEADD('DAY', -3, CURRENT_TIMESTAMP), '{"applicationId":"app_002"}'),
(2, 'DOCUMENTS_REQUESTED', 'Additional documents requested', 2, DATEADD('DAY', -1, CURRENT_TIMESTAMP), '{"documentsRequired":["income_proof","references"]}');

-- Insert transaction financial info
INSERT INTO transaction_financial_info (transaction_id, rent_amount, bond_amount, payment_frequency, payment_due_date, payment_method, payment_status, created_at, updated_at) VALUES
(1, 1200.00, 2400.00, 'MONTHLY', 15, 'BANK_TRANSFER', 'PAID', DATEADD('DAY', -5, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP),
(2, 2500.00, 5000.00, 'MONTHLY', 1, 'DIRECT_DEBIT', 'PENDING', DATEADD('DAY', -3, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP);

-- Insert reports
INSERT INTO reports (reporter_id, reported_property_id, report_type, status, description, created_at, updated_at) VALUES
(1, 4, 'SPAM', 'PENDING', 'This property listing appears to be fake', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 2, 'MISLEADING', 'REVIEWED', 'Property description does not match the actual condition', DATEADD('DAY', -1, CURRENT_TIMESTAMP), CURRENT_TIMESTAMP);

-- Insert report actions
INSERT INTO report_actions (report_id, action_type, action_description, performed_by, created_at) VALUES
(2, 'INVESTIGATED', 'Admin investigated the reported property', 4, CURRENT_TIMESTAMP),
(2, 'RESOLVED', 'Property owner updated the description', 4, CURRENT_TIMESTAMP);