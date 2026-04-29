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

## Conventions
- Always use environment variables for API keys and secrets
- Never hardcode credentials anywhere
- Ask before adding new dependencies