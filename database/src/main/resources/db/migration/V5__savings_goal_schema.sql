CREATE TABLE savings_goal
(
    id UUID PRIMARY KEY,

    user_id UUID NOT NULL,

    currency_id SMALLINT NOT NULL,

    title VARCHAR(150) NOT NULL,

    description TEXT,

    target_amount NUMERIC(19,4) NOT NULL,

    current_amount NUMERIC(19,4) NOT NULL DEFAULT 0,

    target_date TIMESTAMPTZ,

    completed_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL,

    updated_at TIMESTAMPTZ NOT NULL,

    version INTEGER NOT NULL,

    CONSTRAINT fk_savings_goal_user
        FOREIGN KEY (user_id)
            REFERENCES app_user(id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT fk_savings_goal_currency
        FOREIGN KEY (currency_id)
            REFERENCES currency(id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT,

    CONSTRAINT chk_savings_goal_target_amount
        CHECK (target_amount > 0),

    CONSTRAINT chk_savings_goal_current_amount
        CHECK (current_amount >= 0)
);

CREATE INDEX idx_savings_goal_user
    ON savings_goal(user_id);

CREATE INDEX idx_savings_goal_currency
    ON savings_goal(currency_id);

CREATE INDEX idx_savings_goal_target_date
    ON savings_goal(target_date);

COMMENT ON TABLE savings_goal IS
'Represents a savings goal.';

COMMENT ON COLUMN savings_goal.target_date IS
'Desired completion date for the savings goal.';
