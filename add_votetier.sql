ALTER TABLE accounts ADD COLUMN voteTier VARCHAR(20) DEFAULT 'NONE';
CREATE INDEX idx_vote_tier ON accounts(voteTier);
SELECT 'voteTier column added successfully!' as Status;
