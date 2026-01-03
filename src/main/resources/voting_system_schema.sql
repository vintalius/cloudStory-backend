-- ========================================
-- CloudStory Voting System Database Schema
-- ========================================
-- Run this SQL script on your MySQL database to create the votes table
-- and update the accounts table with required columns for NX and vote points

-- ========================================
-- 1. Create votes table
-- ========================================
CREATE TABLE IF NOT EXISTS votes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    vote_site VARCHAR(50) NOT NULL,
    voted_at DATETIME NOT NULL,
    ip_address VARCHAR(45),
    nx_rewarded INT,
    vote_points_rewarded INT,
    
    -- Indexes for better query performance
    INDEX idx_username_site (username, vote_site),
    INDEX idx_voted_at (voted_at),
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================
-- 2. Update accounts table (if columns don't exist)
-- ========================================
-- Note: These columns may already exist in your MapleStory database

-- Add paypalNX column (donated NX / voting NX)
ALTER TABLE accounts ADD COLUMN paypalNX INT DEFAULT 0 COMMENT 'Donated or Vote NX';

-- Add mPoints column (Maple Points)
ALTER TABLE accounts ADD COLUMN mPoints INT DEFAULT 0 COMMENT 'Maple Points';

-- Add vPoints column (Vote Points)
ALTER TABLE accounts ADD COLUMN vPoints INT DEFAULT 0 COMMENT 'Vote Points';

-- Add voteTier column (Voting Tier for reward multipliers)
ALTER TABLE accounts ADD COLUMN voteTier VARCHAR(20) DEFAULT 'NONE' COMMENT 'Current voting tier: NONE, BRONZE, SILVER, GOLD';

-- Create index for voteTier for faster queries
CREATE INDEX idx_vote_tier ON accounts(voteTier);

-- ========================================
-- 3. Verify the changes
-- ========================================
-- Run these queries to check if everything was created correctly:

-- Check votes table structure
DESCRIBE votes;

-- Check accounts table for NX/VP columns
SHOW COLUMNS FROM accounts LIKE '%Points%';
SHOW COLUMNS FROM accounts LIKE 'paypalNX';

-- ========================================
-- 4. Sample queries for testing
-- ========================================

-- Get all votes for a specific user
-- SELECT * FROM votes WHERE username = 'YourUsername' ORDER BY voted_at DESC;

-- Get vote count by site
-- SELECT vote_site, COUNT(*) as vote_count FROM votes GROUP BY vote_site;

-- Get top voters
-- SELECT username, COUNT(*) as total_votes FROM votes GROUP BY username ORDER BY total_votes DESC LIMIT 10;

-- Get user's NX and vote points
-- SELECT name, paypalNX, vPoints, mPoints FROM accounts WHERE name = 'YourUsername';
