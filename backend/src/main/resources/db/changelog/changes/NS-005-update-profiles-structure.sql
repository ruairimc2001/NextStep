-- liquibase formatted sql

-- changeset ruairi:NS-005-update-profiles-structure
-- Convert skills_text and interests_text from TEXT to TEXT[] arrays
ALTER TABLE profiles RENAME COLUMN skills_text TO skills;
ALTER TABLE profiles RENAME COLUMN interests_text TO interests;

-- Convert TEXT columns to TEXT[] arrays, splitting existing comma-separated data
ALTER TABLE profiles ALTER COLUMN skills TYPE text[] USING string_to_array(skills, ',');
ALTER TABLE profiles ALTER COLUMN interests TYPE text[] USING string_to_array(interests, ',');

-- Add first_name and surname columns
ALTER TABLE profiles ADD COLUMN first_name VARCHAR(100);
ALTER TABLE profiles ADD COLUMN surname VARCHAR(100);

-- Update existing profiles with dummy names
UPDATE profiles SET first_name = 'John', surname = 'Doe' WHERE user_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11';
UPDATE profiles SET first_name = 'Jane', surname = 'Smith' WHERE user_id = 'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22';
UPDATE profiles SET first_name = 'Test', surname = 'User' WHERE user_id = 'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33';

