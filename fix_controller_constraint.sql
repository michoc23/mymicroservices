-- SQL script to fix the users_role_check constraint
-- This adds the CONTROLLER role to the allowed values

-- Drop the old constraint
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

-- Add new constraint with CONTROLLER included
ALTER TABLE users ADD CONSTRAINT users_role_check 
    CHECK (role IN ('PASSENGER', 'DRIVER', 'CONTROLLER', 'ADMIN'));
