-- V8__Add_adminNotes_to_users.sql
ALTER TABLE properties
ADD COLUMN admin_notes TEXT;
ALTER TABLE properties
    ADD COLUMN agent_id INT;
