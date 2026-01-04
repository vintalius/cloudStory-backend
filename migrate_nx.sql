-- Move data from snake_case to camelCase
UPDATE accounts SET nxCredit = nx_credit WHERE nx_credit > 0 AND nxCredit = 0;
UPDATE accounts SET nxPrepaid = nx_prepaid WHERE nx_prepaid > 0 AND nxPrepaid = 0;
UPDATE accounts SET maplePoint = maple_point WHERE maple_point > 0 AND maplePoint = 0;
UPDATE accounts SET voteTier = vote_tier WHERE vote_tier IS NOT NULL AND voteTier IS NULL;
