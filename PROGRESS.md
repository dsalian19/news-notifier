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
- Dependencies: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`, `postgresql` driver, `flyway-core`, `flyway-database-postgresql`, `lombok`, `jjwt-api/impl/jackson` (0.12.6)
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

### 5. Backend Java Layer
- `NewsNotifierApplication.java` — Spring Boot entry point (`@SpringBootApplication`)
- JPA entities in `backend/.../model/`:
  - `User` — maps `users` table; `@CreationTimestamp` / `@UpdateTimestamp` for audit fields
  - `Category` — maps `categories` table (read-only seeded data)
  - `UserCategory` — maps `user_categories` as a first-class entity (own `id` + `createdAt`)
  - `CategoryDigest` — maps `category_digests`; `article_urls` JSONB via `StringListConverter`
- `StringListConverter` in `config/` — JPA `AttributeConverter<List<String>, String>` using Jackson
- Spring Data repositories in `backend/.../repository/`:
  - `UserRepository` — `findByEmail`
  - `CategoryRepository` — `findByGuardianKey`
  - `UserCategoryRepository` — `findByUser`, `findByUserAndCategory`
  - `CategoryDigestRepository` — `findByCategoryAndDigestDate`, `findByCategoryIn`
- All entities use Lombok (`@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor`) and `@UuidGenerator` for UUID primary keys
- Verified: app boots cleanly, Flyway validates all 5 migrations, Hibernate validates all 4 entities against the schema

### 6. Category Seed Data
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

### 7. Auth Layer
- `JwtUtil` in `security/` — generates and validates JWT tokens (HMAC-SHA256, 1-day expiry); reads secret and expiry from env vars
- `UserDetailsServiceImpl` in `security/` — loads user by email via `UserRepository` for Spring Security
- `JwtAuthFilter` in `security/` — `OncePerRequestFilter`; reads `Authorization: Bearer` header, validates token, sets security context
- `SecurityConfig` in `config/` — stateless session, CSRF disabled, `/auth/**` and `/error` open, all other routes require a valid JWT; returns 401 (not 403) for unauthenticated requests; `BCryptPasswordEncoder` bean
- `AuthController` in `controller/` — `POST /auth/register` (201), `POST /auth/login` (200)
- `UserService` in `service/` — `register()` BCrypt-hashes password, rejects duplicate email with 409; `login()` verifies password; both failure cases return identical 401 to prevent email enumeration
- DTOs in `dto/`: `RegisterRequest` (email, password, phoneNumber), `LoginRequest` (email, password), `AuthResponse` (token, id, email, notifyEmail, notifySms)
- Unit tests: `JwtUtilTest` (3 tests), `UserServiceTest` (5 tests)

### 8. User & Category API Layer
All 5 endpoints are protected (JWT required). Controllers are thin — they extract the current user's email via `principal.getName()` and delegate to a service.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/categories` | List all 8 seeded categories |
| GET | `/user/me` | Current user profile (id, email, phoneNumber, notifyEmail, notifySms) |
| GET | `/user/categories` | User's currently subscribed categories |
| PUT | `/user/categories` | Replace all subscriptions (full desired state sent as `{ categoryIds: [...] }`) |
| PUT | `/user/preferences` | Update notification preferences (`{ notifyEmail, notifySms }`) |

**New files:**
- `controller/CategoryController.java` — handles `GET /categories`
- `controller/UserController.java` — handles all `/user/**` routes
- `service/CategoryService.java` — category listing, subscription listing, bulk replace logic
- `dto/CategoryResponse.java` — `record(UUID id, String name)`
- `dto/UserProfileResponse.java` — `record(UUID id, String email, String phoneNumber, boolean notifyEmail, boolean notifySms)`
- `dto/UpdateCategoriesRequest.java` — `record(List<UUID> categoryIds)`
- `dto/UpdatePreferencesRequest.java` — `record(boolean notifyEmail, boolean notifySms)`

**Modified files:**
- `service/UserService.java` — added `getProfile()` and `updatePreferences()`

**Unit tests:** `CategoryServiceTest` (4 tests covering `replaceSubscriptions` happy path, invalid category ID, user not found, and empty list)

### 9. Digest API Layer
Two authenticated endpoints for the portal.

| Method | Path | Description |
|--------|------|-------------|
| GET | `/digests` | List digests for subscribed categories; optional `?categoryId=`, `?date=` (ISO), `?keyword=` filters; ordered by date desc |
| GET | `/digests/{id}` | Full digest detail; 404 if not found or user not subscribed to that category |

**New files:**
- `dto/DigestResponse.java` — single record used by both endpoints: `id`, `categoryId`, `categoryName`, `digestDate`, `shortSummary`, `longSummary`, `articleUrls`
- `service/DigestService.java` — `getDigests()` (with all filter combos) and `getDigestById()`; empty subscription list short-circuits before hitting the DB
- `controller/DigestController.java` — thin controller; `@DateTimeFormat(iso = ISO.DATE)` handles `LocalDate` query param parsing

**Modified files:**
- `repository/CategoryDigestRepository.java` — added `findForUserCategories` `@Query` JPQL method; single method covers all 4 filter combinations via nullable params

**Unit tests:** `DigestServiceTest` (9 tests covering all filter combos, empty subscriptions, detail happy path, detail not-found, detail not-subscribed)

### 10. Backend Deployment
- Deployed to Render as a Docker web service at `https://news-notifier-backend.onrender.com`
- Auto-deploys on every push to `main` (Render watches the repo)
- Flyway ran V1–V5 migrations against the Render Postgres instance on first boot
- Free tier — instance spins down after ~15 min inactivity; first request after sleep takes ~30s to wake up
- UptimeRobot configured to ping the backend every 5 minutes, keeping the instance awake so the cron job fires reliably at 1am UTC

### 11. Guardian API Client (Cron Job — Component 1)
- `client/GuardianClient.java` — fetches previous day's articles for a given `guardian_key` using Spring `RestClient` (no new dependency)
- `dto/GuardianArticle.java` — record with `title`, `webUrl`, `bodyText`
- Fetches up to 30 articles per category with full `bodyText` via `show-fields=bodyText`
- Private inner records handle Guardian API JSON deserialization (`GuardianApiResponse`, `GuardianResponseBody`, `GuardianResult`, `GuardianFields`)
- On any failure: logs the error and returns empty list so the cron job can continue with other categories
- New env var: `GUARDIAN_API_KEY` — added to `application.properties` and `.env.example`; must be set in Render dashboard for production

---

## What Needs To Be Done

### Cron Job (build in order)

**Component 2: OpenAI integration**
- [ ] New `client/OpenAiClient.java` — sends up to 30 articles (full bodyText) for a category, returns `{ shortSummary, longSummary }` as structured JSON in a single call
- [ ] Model: `gpt-4o-mini`
- [ ] New env var: `OPENAI_API_KEY`
- [ ] Requires adding OpenAI dependency to `pom.xml` (confirm before adding)

**Component 3: Notification service**
- [ ] New `service/NotificationService.java` — given a user + list of `(categoryName, shortSummary)` pairs, sends a combined email via Resend and/or combined SMS via Twilio based on user's `notifyEmail`/`notifySms` flags
- [ ] Email: one message per user, all subscribed categories' short summaries + link to portal
- [ ] SMS: same short summaries combined into one message; Twilio handles multi-part splitting automatically
- [ ] Categories with no articles: include "No updates today" in notification
- [ ] New env vars: `RESEND_API_KEY`, `TWILIO_ACCOUNT_SID`, `TWILIO_AUTH_TOKEN`, `TWILIO_FROM_NUMBER`
- [ ] Requires adding Resend and Twilio dependencies to `pom.xml` (confirm before adding)

**Component 4: Cron orchestrator**
- [ ] New `cron/DigestCronJob.java` — `@Scheduled` at `0 0 1 * * *` (1am UTC): fetch articles → generate summaries → write `CategoryDigest` rows → notify all users
- [ ] Uses previous day's date for all Guardian API calls
- [ ] On failure for any category: log, skip, continue with remaining categories
- [ ] If 0 articles returned for a category: write digest with `shortSummary = longSummary = "No updates today"` and empty `articleUrls`
- [ ] Add `@EnableScheduling` to `NewsNotifierApplication.java`

### Frontend
- [ ] Initialize React + TypeScript app (Vite)
- [ ] Auth pages: Landing, Login, Signup (2-step)
- [ ] Portal page: digest list with date/category/keyword filter
- [ ] Settings page: notification preferences + category management

### Deployment
- [x] Backend deployed to Render (auto-deploys on push to `main`)
- [ ] Add cron job env vars to Render dashboard (`GUARDIAN_API_KEY`, `OPENAI_API_KEY`, `RESEND_API_KEY`, `TWILIO_*`)
- [ ] Deploy frontend to Vercel

---

## Environment Setup (for new sessions)

```bash
# Load and export local env vars before running any Maven commands
# set -a is required so vars are inherited by the Maven subprocess
set -a && source backend/.env && set +a

# Run backend locally
mvn -f backend/pom.xml spring-boot:run

# Run migrations (local)
mvn -f backend/pom.xml flyway:migrate \
  -Dflyway.url=$DATABASE_URL \
  -Dflyway.user=$DB_USERNAME \
  -Dflyway.password=$DB_PASSWORD
```

Production credentials are in the Render dashboard only — never in any file.
