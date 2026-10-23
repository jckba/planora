CREATE TABLE email_verification_challenge
(
    id             UUID PRIMARY KEY,

    user_id        UUID         NOT NULL,

    code_hash      VARCHAR(255) NOT NULL,

    expires_at     TIMESTAMPTZ  NOT NULL,

    attempts       INTEGER      NOT NULL DEFAULT 0,

    max_attempts   INTEGER      NOT NULL,

    consumed_at    TIMESTAMPTZ,

    invalidated_at TIMESTAMPTZ,

    created_at     TIMESTAMPTZ  NOT NULL,

    CONSTRAINT fk_email_verification_user
        FOREIGN KEY (user_id)
            REFERENCES app_user (id)
            ON DELETE CASCADE
            ON UPDATE RESTRICT,

    CONSTRAINT chk_email_verification_attempts
        CHECK (
            attempts >= 0
                AND max_attempts > 0
                AND attempts <= max_attempts
            ),

    CONSTRAINT chk_email_verification_expiration
        CHECK (expires_at > created_at),

    CONSTRAINT chk_email_verification_consumption
        CHECK (consumed_at IS NULL OR consumed_at >= created_at),

    CONSTRAINT chk_email_verification_invalidation
        CHECK (invalidated_at IS NULL OR invalidated_at >= created_at),

    CONSTRAINT chk_email_verification_terminal_state
        CHECK (consumed_at IS NULL OR invalidated_at IS NULL)
);

CREATE INDEX idx_email_verification_user_created
    ON email_verification_challenge (user_id, created_at DESC);

COMMENT ON TABLE email_verification_challenge IS
    'Email verification challenges for local account registration.';
