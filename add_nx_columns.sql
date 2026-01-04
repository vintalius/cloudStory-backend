-- ========================================
-- Add Missing NX Columns to accounts Table
-- ========================================
-- Run this SQL to add nxCredit, nxPrepaid, and maplePoint columns
-- These are needed so the game client can see the voting rewards

-- Add nxCredit column (for Cash Shop NX)
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS nxCredit INT DEFAULT 0 COMMENT 'NX Credit for Cash Shop';

-- Add nxPrepaid column (for Prepaid NX)
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS nxPrepaid INT DEFAULT 0 COMMENT 'Prepaid NX';

-- Add maplePoint column (for Maple Points)
ALTER TABLE accounts ADD COLUMN IF NOT EXISTS maplePoint INT DEFAULT 0 COMMENT 'Maple Points';

-- Verify the columns were added
SELECT 
    COLUMN_NAME, 
    COLUMN_TYPE, 
    COLUMN_DEFAULT, 
    COLUMN_COMMENT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'accounts' 
AND COLUMN_NAME IN ('paypalNX', 'nxCredit', 'nxPrepaid', 'maplePoint', 'mPoints', 'vPoints')
ORDER BY ORDINAL_POSITION;

-- Test query: Check your account's NX values
-- SELECT name, paypalNX, nxCredit, nxPrepaid, maplePoint, mPoints, vPoints FROM accounts WHERE name = 'YourUsername';
