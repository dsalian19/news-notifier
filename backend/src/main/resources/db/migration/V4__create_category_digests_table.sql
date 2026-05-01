CREATE TABLE category_digests (
    id            UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id   UUID      NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    digest_date   DATE      NOT NULL,
    short_summary TEXT      NOT NULL,
    long_summary  TEXT      NOT NULL,
    article_urls  JSONB     NOT NULL DEFAULT '[]',
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_category_digest_date UNIQUE (category_id, digest_date)
);
