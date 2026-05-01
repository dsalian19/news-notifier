# Project Progress

> Updated at the start and end of each session. Read this first for context before beginning any work.

---

## What Has Been Built

### 1. Monorepo Skeleton
- `/backend` — Java + Spring Boot backend package structure (`model/`, `repository/`, `service/`, `controller/`, `config/`, `security/`, `cron/`, `dto/`)
- `/frontend` — React + TypeScript frontend package structure (`pages/`, `components/`, `hooks/`, `api/`, `context/`, `types/`)
- Root `.gitignore` covering Node, Java/Maven, IDE files, and all `.env` variants

### 2. Backend Build System (`backend/pom.xml`)
- Spring Boot 3.3.5 parent (Java 17)
- Dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `postgresql` driver, `flyway-core`, `flyway-database-postgresql`
- Flyway Maven plugin (10.20.1) configured for standalone migration runs

### 3. PostgreSQL Database — Local + Render
- Local database: `news_notifier` (owner: `deepaksalian`, host: `localhost:5432`)
- Render database: `news_notifier` on `dpg-d7qib9egvqtc73cgtnhg-a.oregon-postgres.render.com` (user: `news_notifier_user`)
- `backend/.env` — local credentials (gitignored, never committed)
- `backend/.env.example` — safe template committed to repo
- `backend/src/main/resources/application.properties` — connects via `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars

### 4. Flyway Migrations (applied to local DB)
All 5 migrations have been run against the local `news_notifier` database:

| Version | Description | Status |
|---------|-------------|--------|
| V1 | Create `users` table | Applied |
| V2 | Create `categories` table | Applied |
| V3 | Create `user_categories` join table | Applied |
| V4 | Create `category_digests` table | Applied |
| V5 | Seed 8 v1 categories | Applied |

**Note:** Migrations have NOT yet been run against the Render (production) database. They will run automatically when the backend is deployed to Render — Flyway executes on Spring Boot startup using the configured env vars.

### 5. Category Seed Data
The following categories are seeded in the local database:

| Name | Guardian API Key |
|------|-----------------|
| World | world |
| US News | us-news |
| Politics | politics |
| Business | business |
| Technology | technology |
| Science | science |
| Sport | sport |
| Culture | culture |

---

## What Needs To Be Done

### Immediate Next Steps
- [ ] **Deploy backend to Render** — set `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars in Render dashboard; Flyway will automatically apply V1–V5 on first startup
- [ ] **Create Spring Boot main application class** — `backend/src/main/java/com/newsnotifier/NewsNotifierApplication.java`
- [ ] **Implement JPA entity classes** — `User`, `Category`, `UserCategory`, `CategoryDigest` in `backend/.../model/`
- [ ] **Implement Spring Data repositories** — one interface per entity in `backend/.../repository/`

### Auth Layer
- [ ] Add Spring Security + JWT dependencies to `pom.xml`
- [ ] Implement JWT token generation and validation (`security/`)
- [ ] Implement `POST /api/auth/register` and `POST /api/auth/login` endpoints
- [ ] Implement authentication filter and security config (`config/`)

### User Features
- [ ] `POST /api/users/categories` — subscribe to categories
- [ ] `DELETE /api/users/categories/{id}` — unsubscribe
- [ ] `GET /api/users/categories` — list subscriptions
- [ ] `PATCH /api/users/preferences` — update notify_email / notify_sms

### Digest / Portal Features
- [ ] `GET /api/digests` — list digests for the user's categories (filterable by date, category)
- [ ] `GET /api/digests/{id}` — full digest detail with article URLs
- [ ] `GET /api/categories` — list all available categories

### Cron Job
- [ ] Guardian API client (fetch articles per category)
- [ ] OpenAI integration (generate short + long summaries)
- [ ] `CategoryDigest` writer service
- [ ] Email notification via Resend
- [ ] SMS notification via Twilio
- [ ] Spring `@Scheduled` cron trigger (daily)

### Frontend
- [ ] Initialize React + TypeScript app (Vite or Create React App)
- [ ] Auth pages: Landing, Login, Signup (2-step)
- [ ] Portal page: digest list with date/category filter
- [ ] Digest detail view with article links
- [ ] Settings page: notification preferences + category management

### Deployment
- [ ] Configure Render service for backend (set production env vars in Render dashboard)
- [ ] Configure Vercel for frontend
- [ ] Wire up CI/CD (GitHub → Render auto-deploy on push to `main`)

---

## Environment Setup (for new sessions)

```bash
# Load local env vars before running any Maven commands
source backend/.env

# Run backend locally
mvn -f backend/pom.xml spring-boot:run

# Run migrations (local)
mvn -f backend/pom.xml flyway:migrate \
  -Dflyway.url=$DATABASE_URL \
  -Dflyway.user=$DB_USERNAME \
  -Dflyway.password=$DB_PASSWORD
```

Production credentials are in the Render dashboard only — never in any file.
