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

---

## What Needs To Be Done

### Immediate Next Steps
- [ ] **User & category endpoints** — subscribe/unsubscribe, list subscriptions, update preferences
- [ ] **Deploy backend to Render** — set `DATABASE_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MS` env vars in Render dashboard; Flyway will automatically apply V1–V5 on first startup

### User Features
- [ ] `GET /categories` — list all available categories (authenticated)
- [ ] `GET /user/categories` — list subscribed categories for current user
- [ ] `POST /user/categories/{categoryId}` — subscribe to a category
- [ ] `DELETE /user/categories/{categoryId}` — unsubscribe from a category
- [ ] `GET /user/me` — return current user profile
- [ ] `PUT /user/preferences` — update notify_email, notify_sms, phone_number

### Digest / Portal Features
- [ ] `GET /digests` — list digests for the user's subscribed categories (filterable by `?category=` and `?date=`)
- [ ] `GET /digests/{id}` — full digest detail with long summary and article URLs

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
