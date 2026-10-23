# Placement Management Portal

Integrated full-stack portal for academic operations, recruitment workflows, interviews, offers, internships, dashboards, and role-based administration.

## Overview

This repository contains:

- **Backend:** Spring Boot + PostgreSQL API (`/backend`)
- **Frontend:** React + TypeScript + Vite web app (`/frontend`)

The system is designed around role-based access control for users such as super admin, TPO/admin teams, faculty/HOD, and students.

## Key Features

- Authentication, refresh-token flow, password reset, and role/permission checks
- Student academic registry and import/export workflows
- Company, placement drive, policy, eligibility, and application flows
- Interview rounds, scheduling, evaluations, question bank, and feedback
- Offer lifecycle, internship tracking, and PPO workflows
- Attendance, timetable, CRT, events, and notification center
- Role-specific dashboards and placement analytics
- Public portal pages and public overview API

## Technology Stack

### Backend (`backend/pom.xml`)

| Component | Version / Tool |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.16 |
| Database | PostgreSQL |
| Migrations | Flyway |
| API Docs | springdoc OpenAPI (`/swagger-ui.html`) |
| Security/JWT | Spring Security, JJWT 0.12.6 |
| Build Tool | Maven |

### Frontend (`frontend/package.json`)

| Component | Version / Tool |
|---|---|
| React | 18.3.1 |
| TypeScript | ~5.6.3 |
| Vite | ^6.4.5 |
| Routing | react-router-dom 6.28.1 |
| Styling | Tailwind CSS 3.4.17 |
| HTTP Client | Axios |

## Repository Structure

```text
Placement-Management/
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/edu/jecrc/portal/modules/
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-dev.yml
│       │       ├── application-prod.yml
│       │       └── db/migration/
│       └── test/
└── frontend/
    ├── package.json
    ├── vercel.json
    └── src/
```

## Local Setup

### 1) Prerequisites

- Java 21
- Maven 3.9+
- Node.js 18+ and npm
- PostgreSQL (dev defaults expect local DB)

### 2) Backend

```bash
cd backend
mvn spring-boot:run
```

Notes:

- Default active profile is `dev` (`backend/src/main/resources/application.yml`).
- Dev datasource defaults:
  - `jdbc:postgresql://localhost:5436/jecrc_portal`
  - username/password: `postgres` / `postgres`
- Override values using environment variables (see root `.env.example`).

### 3) Frontend

```bash
cd frontend
npm ci
npm run dev
```

The frontend uses same-origin `/api/*` calls. In deployment, Vercel rewrite rules are configured in `frontend/vercel.json`.

## Demo Credentials (Local/Dev Only)

These are **demo-only** credentials intended for local evaluation.

- `BootstrapAdminRunner` creates/updates the super admin account.
- In `dev`, demo user seeding is enabled by default (`app.bootstrap.seed-demo-users: true`).

### Default Local Dev Behavior

| Account Type | Email | Default Password Source |
|---|---|---|
| Super Admin | `admin@jecrcuniversity.edu.in` | `APP_ADMIN_PASSWORD` (fallback: `Admin@JECRC2026!`) |
| Demo users (TPO/HOD/Faculty/Student/etc.) | seeded by bootstrap | `APP_DEMO_PASSWORD` in dev (default `Password@123`), otherwise falls back to admin password |

Demo account emails are defined in:

- `backend/src/main/java/edu/jecrc/portal/config/BootstrapAdminRunner.java`

Do **not** use these defaults in production.

## API & Module Overview

Base API path: `/api/v1`

| Area | Example Paths |
|---|---|
| Auth | `/auth/login`, `/auth/refresh`, `/auth/logout`, `/auth/forgot-password`, `/auth/reset-password` |
| Public | `/public/overview` |
| Users/Admin | `/users/me`, `/admin/users` |
| Academic Registry | `/students`, `/departments`, `/programs`, `/academic-batches`, `/students/import/*` |
| Placement | `/companies`, `/drives`, `/job-profiles`, `/placement-policies`, `/applications` |
| Interview | `/rounds`, `/appointments`, `/interview-questions`, `/interview-feedback` |
| Offers/Internships/PPO | `/offers`, `/internships`, `/ppo-reviews` |
| Academic Ops | `/classrooms`, `/faculty`, `/timetables`, `/attendance`, `/crt`, `/events`, `/notifications` |
| Dashboards/Analytics | `/dashboards/*`, `/analytics/placements` |
| Audit | `/audit-logs` |

For endpoint-level details, run backend and open:

- `/swagger-ui.html`
- `/v3/api-docs`

## Testing

### Backend Tests

```bash
cd backend
mvn test
```

Integration tests are present under `backend/src/test/java/edu/jecrc/portal`.

### Frontend Verification

No dedicated frontend test script is defined in `frontend/package.json`.
Use build/type-check as a validation step:

```bash
cd frontend
npm run build
```

## Deployment Notes

- **Backend containerization:** see `backend/Dockerfile` (multi-stage build, Java 21 runtime, `SPRING_PROFILES_ACTIVE=prod`).
- **Frontend hosting:** `frontend/vercel.json` rewrites `/api/:path*` to a backend URL.
  - Update this destination to your own backend before production deployment.
- **Environment variables:** use root `.env.example` as the baseline template.

## Security Notes

- Never commit real secrets; use environment variables.
- Replace all default/demo passwords before any non-local deployment.
- Keep `APP_SEED_DEMO_USERS=false` in production.
