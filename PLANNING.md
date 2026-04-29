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