# News Summarizer — Planning Document

## Summary
A news digest app that pulls articles from The Guardian API, generates AI summaries,
and delivers them to users via email/SMS. Users can also log into a web portal to
view full narrative digests and browse their history.

## Why
- Get familiar with Claude Code
- Build a portfolio project
- Learn full stack development from 0 → 1
- Get familiar with building across the full stack

---

## User Stories
- Users can sign up, log in, and log out
- Users can select categories they are interested in during signup
- Users can update their category preferences at any time
- Users can set their notification preference (email, SMS, both, or none) during signup
- Users can update notification preference later
- Users receive a daily notification via their chosen method containing a one-line
  summary per selected category, or "No updates today" for categories with no
  new articles
- Users can log into the portal and view the full narrative digest for each of their
  selected categories for any past day
- Users can filter their portal digest history by date, category, and keyword
- Users can click through to the original Guardian article from any digest entry
- Users can delete their account (low priority)

---

## Wireframe

### Pages
1. **Landing page** — App name, short description, Login and Signup buttons
2. **Signup** — Two steps: (1) email, phone, password (2) select categories and
   notification preference
3. **Login** — Email and password
4. **Portal** — Main app page. Top bar with Settings and Logout. Filter bar with
   date picker, category dropdown, keyword search. Content area with digest cards
   ordered by date descending. Each card shows category, date, long summary,
   and source article links
5. **Settings** — Manage category preferences and notification preferences

### Design Direction
Clean and professional. Light background, dark text, one accent color,
generous whitespace. Reference: The Guardian's own website aesthetic.
Desktop-first, mobile secondary.

---

## Data Models

### User
| Field         | Type      | Notes                  |
|---------------|-----------|------------------------|
| id            | UUID      | primary key            |
| email         | VARCHAR   | unique, required       |
| phone_number  | VARCHAR   | required               |
| password_hash | VARCHAR   |                        |
| notify_email  | BOOLEAN   | default false          |
| notify_sms    | BOOLEAN   | default false          |
| created_at    | TIMESTAMP |                        |
| updated_at    | TIMESTAMP |                        |

### Category
| Field        | Type    | Notes                                        |
|--------------|---------|----------------------------------------------|
| id           | UUID    | primary key                                  |
| name         | VARCHAR | display name e.g. "Sports"                   |
| guardian_key | VARCHAR | API key used in Guardian calls e.g. "sport"  |

Static seeded table. Populated once, never modified. Categories are hardcoded
based on Guardian API sections, filtered by personal preference.

#### Categories to support in V1 (guardian_key):
- world
- us-news
- politics
- business
- technology
- science
- sport
- culture

### UserCategory (join table)
| Field       | Type      | Notes              |
|-------------|-----------|--------------------|
| id          | UUID      | primary key        |
| user_id     | UUID      | FK → User          |
| category_id | UUID      | FK → Category      |
| created_at  | TIMESTAMP |                    |

Unique constraint on (user_id, category_id).
Deleting a row = unsubscribing from a category.
Portal shows all CategoryDigests for a user's currently active categories
(Philosophy B — current interest model, not subscription log).

### CategoryDigest
| Field         | Type      | Notes                                   |
|---------------|-----------|-----------------------------------------|
| id            | UUID      | primary key                             |
| category_id   | UUID      | FK → Category                           |
| digest_date   | DATE      |                                         |
| short_summary | TEXT      | one-liner for notification              |
| long_summary  | TEXT      | narrative paragraph for portal          |
| article_urls  | JSONB     | array of Guardian article URLs          |
| created_at    | TIMESTAMP |                                         |

Unique constraint on (category_id, digest_date).
One row per category per day, shared across all users.
If no articles exist for a category that day, short_summary and long_summary
are both set to "No updates today" and article_urls is an empty array.

---

## Components / Architecture

- **Frontend** — Browser-based UI. Handles all pages and user interaction.
  Talks to the backend exclusively through REST API calls. Never talks directly
  to any external service.
- **Backend** — Handles all business logic, auth, data access, and external
  API calls.
- **Database** — Only the backend reads and writes to it.
- **Guardian API** — External service. Only the cron job calls this.
- **AI API** — External service. Only the cron job calls this.
- **Cron Job** — Scheduled task that runs inside the backend once per day.
  Not a separate service.
- **Notification Services** — External services for email and SMS.
  Only the cron job calls these.

### Cron Job Flow
1. Fetch today's articles from Guardian API for each active category
2. For each category, send articles to OpenAI and get back short + long summary
   in a single API call returning structured JSON with both fields
3. Write one CategoryDigest row per category to the database
4. Look up all users and their active category subscriptions
5. For each user, send notifications via Resend/Twilio based on their preferences

---

## Tech Stack

| Layer                | Technology              |
|----------------------|-------------------------|
| Frontend             | React + TypeScript      |
| Backend              | Java + Spring Boot      |
| API Layer            | REST                    |
| Database             | PostgreSQL              |
| AI                   | OpenAI API              |
| Email                | Resend                  |
| SMS                  | Twilio                  |
| Auth                 | Spring Security + JWT   |
| Frontend deployment  | Vercel                  |
| Backend deployment   | Render                  |
| Database hosting     | Render                  |

---

## Future of Project
- Hobby / portfolio project. Will not be constantly iterated.
- Under 5 users. No scaling concerns.
- Might have minor additions beyond original scope but core feature set is fixed.

---

## Development Process
1. Project skeleton — folder structure, dev environment, version control
2. Database setup and data model implementation
3. Backend — auth, REST endpoints, cron job
4. Frontend — pages, components, connect to backend
5. Testing and iteration
6. Deployment

---

## Architecture Decisions

### Category subscription endpoint: bulk replace (`PUT /user/categories`) over per-item toggle (POST/DELETE)

**Decision:** A single `PUT /user/categories` endpoint receives the full desired list of category IDs and replaces all subscriptions atomically.

**Rejected alternative:** Individual `POST /user/categories/{id}` (subscribe) and `DELETE /user/categories/{id}` (unsubscribe) endpoints, toggled on each checkbox click.

**Why:** The per-item toggle approach creates a race condition on the frontend. If the user rapidly checks and unchecks a box, multiple requests fire concurrently and can arrive at the server out of order — leaving the database in a state that doesn't match the UI. The fix (debouncing) adds meaningful frontend complexity. Since the Settings page manages subscriptions as a set configured all at once, the bulk replace is a natural fit: the user toggles checkboxes freely with zero API calls, then hits Save to send the final state in a single request. No race conditions, no debouncing, no 409 conflict handling.

---

### Notification preferences: full replace, both fields always required

**Decision:** `PUT /user/preferences` always receives both `notifyEmail` and `notifySms`, even if only one changed.

**Rejected alternative:** A `PATCH` endpoint accepting only the fields that changed.

**Why:** Partial update (PATCH) requires the backend to distinguish "field not sent" from "field explicitly set to false" — non-trivial for booleans. The Settings page always shows both checkboxes and sends both values on submit, so the full-replace pattern fits naturally with zero added complexity.

---

### Service split: `CategoryService` + `UserService` extensions

**Decision:** All category-related logic (listing categories, listing subscriptions, bulk replace) lives in `CategoryService`. User profile and preference logic stays in `UserService`.

**Rejected alternative:** Putting all new logic in `UserService`.

**Why:** `UserService` would grow large and mix concerns. Keeping category logic in its own service makes each class focused on one domain. A third `SubscriptionService` was also considered but rejected as over-engineering for this project's size.

**Cross-service dependency:** `CategoryService` injects `UserRepository` directly rather than calling `UserService`. This avoids a potential circular dependency (if `UserService` ever needs `CategoryService`) and keeps dependencies explicit.

---

### Controller identity extraction: `Principal` parameter

**Decision:** Controller methods accept a `Principal` parameter injected by Spring. The current user's email is extracted via `principal.getName()`.

**Rejected alternatives:**
- `@AuthenticationPrincipal UserDetails userDetails` — also valid but more coupled to Spring Security internals. Since `UserDetails` in this project carries no more information than the email, `Principal` is simpler.
- `SecurityContextHolder.getContext().getAuthentication()` inside services — hides an implicit dependency and makes services harder to unit test (requires mocking the security context).

---

### `guardian_key` excluded from `CategoryResponse`

**Decision:** `CategoryResponse` exposes only `id` and `name`. The `guardian_key` field is never returned by any API endpoint.

**Why:** `guardian_key` is an internal implementation detail used only by the cron job to call the Guardian API. Exposing it in responses leaks an internal concern to the frontend and is unnecessary.