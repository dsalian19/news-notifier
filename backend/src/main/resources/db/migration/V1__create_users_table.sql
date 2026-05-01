CREATE TABLE users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    phone_number  VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    notify_email  BOOLEAN      NOT NULL DEFAULT false,
    notify_sms    BOOLEAN      NOT NULL DEFAULT false,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);
