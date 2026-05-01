# News Summarizer App

## Stack
- Frontend: React + TypeScript, deployed on Vercel
- Backend: Java + Spring Boot, deployed on Render
- Database: PostgreSQL, hosted on Render
- Auth: Spring Security + JWT
- AI: OpenAI API
- Email: Resend
- SMS: Twilio

## Data Models
- User (id, email, phone_number, password_hash, notify_email, notify_sms, created_at, updated_at)
- Category (id, name, guardian_key) — static seeded table
- UserCategory (id, user_id, category_id, created_at) — join table, unique on (user_id, category_id)
- CategoryDigest (id, category_id, digest_date, short_summary, long_summary, article_urls JSONB, created_at) — unique on (category_id, digest_date)

## Architecture
- Frontend talks to backend via REST API only, never directly to external services
- Backend handles all business logic, auth, and external API calls
- Cron job runs inside the backend once per day

## Cron Job Flow
1. Fetch articles from Guardian API per active category
2. Send articles to OpenAI, get back short + long summary per category
3. Write one CategoryDigest row per category
4. Look up all users and their subscriptions
5. Send notifications via Resend/Twilio based on user preferences

## Database Migrations
- Managed by Flyway; migration files live in `backend/src/main/resources/db/migration/`
- Naming convention: `V{n}__{description}.sql` (e.g. `V1__create_users_table.sql`)
- Flyway runs automatically on Spring Boot startup; also runnable standalone via `mvn flyway:migrate`
- Never edit an already-applied migration — add a new versioned file instead

## Local Development
- Environment variables are loaded from `backend/.env` (gitignored — never committed)
- Copy `backend/.env.example` to `backend/.env` and fill in local values to get started
- Source env vars before running Maven: `source backend/.env`
- Run migrations: `mvn -f backend/pom.xml flyway:migrate -Dflyway.url=$DATABASE_URL -Dflyway.user=$DB_USERNAME -Dflyway.password=$DB_PASSWORD`
- Run backend: `mvn -f backend/pom.xml spring-boot:run`
- Production env vars (Render DB credentials, API keys) are set in the Render dashboard only — never in files

## Conventions
- Always use environment variables for API keys and secrets
- Never hardcode credentials anywhere
- Ask before adding new dependencies