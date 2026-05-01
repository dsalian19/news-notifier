CREATE TABLE categories (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(100) NOT NULL,
    guardian_key VARCHAR(100) NOT NULL UNIQUE
);
