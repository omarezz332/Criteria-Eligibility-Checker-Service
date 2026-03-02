ALTER TABLE lottery_criteria
    ADD CONSTRAINT uq_lottery_criteria_type UNIQUE (lottery_id, criteria_type);
