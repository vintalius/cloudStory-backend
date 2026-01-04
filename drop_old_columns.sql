-- Delete all old snake_case columns
ALTER TABLE accounts DROP COLUMN nx_credit;
ALTER TABLE accounts DROP COLUMN nx_prepaid;
ALTER TABLE accounts DROP COLUMN maple_point;
ALTER TABLE accounts DROP COLUMN vote_tier;

-- Now update nxCredit with the value from vote (manually set since we removed the old one)
UPDATE accounts SET nxCredit = 13000 WHERE name = 'pc12';

-- Verify
SELECT name, nxCredit FROM accounts WHERE name = 'pc12';
