-- lottery.status: used in WHERE status = 'ACTIVE' on every eligibility check.
-- No existing constraint covers this column.
CREATE INDEX idx_lottery_status ON lottery (status);

