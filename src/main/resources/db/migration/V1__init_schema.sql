CREATE TABLE applicant_profile
(
    id              UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL,
    age             INT          NOT NULL,
    gender          VARCHAR(10)  NOT NULL,
    country         VARCHAR(50)  NOT NULL,
    nationality     VARCHAR(50)  NOT NULL,
    marital_status  VARCHAR(20)  NOT NULL,
    has_disability  BOOLEAN      NOT NULL DEFAULT FALSE,
    disability_name VARCHAR(100),
    education_level VARCHAR(20)  NOT NULL,
    career_years    INT          NOT NULL DEFAULT 0,
    created_date    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE lottery
(
    id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name         VARCHAR(200) NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_date TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE lottery_criteria
(
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lottery_id     UUID         NOT NULL REFERENCES lottery (id),
    criteria_type  VARCHAR(50)  NOT NULL,
    criteria_value VARCHAR(100) NOT NULL
);

CREATE TABLE application_preference
(
    id                   UUID PRIMARY KEY       DEFAULT gen_random_uuid(),
    applicant_id         UUID          NOT NULL REFERENCES applicant_profile (id),
    lottery_id           UUID          NOT NULL REFERENCES lottery (id),
    lottery_rank_mark    DOUBLE PRECISION NOT NULL,
    preference_order_num INT           NOT NULL,
    created_date         TIMESTAMP     NOT NULL DEFAULT now(),
    version              INT           NOT NULL DEFAULT 0, -- optimistic locking
    UNIQUE (applicant_id, lottery_id),
    UNIQUE (applicant_id, preference_order_num)
);
