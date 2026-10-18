CREATE TABLE external_identity
(
    id               UUID PRIMARY KEY,

    user_id          UUID         NOT NULL,

    provider         VARCHAR(30)  NOT NULL,

    provider_subject VARCHAR(255) NOT NULL,

    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_external_identity_user
        FOREIGN KEY (user_id)
            REFERENCES app_user (id)
            ON DELETE CASCADE
            ON UPDATE RESTRICT,

    CONSTRAINT uk_external_identity_provider_subject
        UNIQUE (provider, provider_subject),

    CONSTRAINT uk_external_identity_user_provider
        UNIQUE (user_id, provider),

    CONSTRAINT chk_external_identity_provider
        CHECK (provider IN ('GOOGLE'))
);

COMMENT
ON TABLE planora.external_identity IS
'External authentication identities linked to local users.';

COMMENT
ON COLUMN planora.external_identity.provider_subject IS
'Stable subject identifier provided by the identity provider.';
