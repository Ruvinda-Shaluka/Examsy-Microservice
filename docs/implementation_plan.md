# Examsy — Microservices Architecture Migration Plan

## Overview

**Examsy** is a full-stack online examination platform built with a React (Vite + TailwindCSS) frontend and a monolithic Spring Boot 4 / Java 21 backend backed by a single MySQL database. The goal is to decompose the monolith into a production-grade microservices system — retaining all existing features while adding independent scalability, fault isolation, clear ownership boundaries, and modern DevOps practices.

---

## Current State Analysis

### What Exists Today (Monolith)

| Layer | Technology | Notes |
|---|---|---|
| Frontend | React 19 + Vite + TailwindCSS | Axios with JWT interceptor, role-based routing (Admin / Teacher / Student) |
| Backend | Spring Boot 4, Java 21, Single WAR | Single app, single DB, 13 controllers |
| Database | MySQL (single schema `examsy_db`) | JPA/Hibernate, `ddl-auto=update` |
| Auth | Spring Security + JWT + Google OAuth2 | Issued in `AuthController`, validated via filter globally |
| AI Grading | Groq Cloud API | Called from `GroqGradingService`, `GroqMockExamService` |
| Email | SMTP via Gmail (JavaMailSender) | Used in notifications, password reset |
| File Storage | Cloudinary (direct from frontend) | PDF / image upload URLs stored in DB |
| Rate Limiting | Bucket4j (in-memory, per-instance) | `RateLimitFilter` |
| OCR | Tess4j | Used for answer-script PDF analysis |
| PDF Gen | Apache PDFBox | Result reports |

### Domain Boundary Identification (from Controllers + Entities)

```
Auth          → UserAccount, Role, OAuth2 (Google)
User Profiles → Admin, Teacher, Student (each with profile-specific fields)
Classes       → Course, ClassEnrollment, ClassJoinRequest, ClassAnnouncement
Exams         → Exam, Question, QuestionOption, ExamSubmission, SubmissionAnswer
Grading       → SmartGrading (Groq AI), ManualGrading, ProctoringLog
Notifications → Notification (push), Email (SMTP)
Reports       → Report (Admin moderation)
Mock Exams    → MockExam, MockQuestion (AI-generated)
Analytics     → (aggregation over Exams, Submissions, Enrollments)
```

---

## Proposed Microservices Architecture

### Service Decomposition

```
┌─────────────────────────────────────────────────────────────┐
│                     FRONTEND (React/Vite)                   │
│              Single SPA — unchanged externally              │
└─────────────────────┬───────────────────────────────────────┘
                      │ HTTPS
                      ▼
┌─────────────────────────────────────────────────────────────┐
│                    API GATEWAY                              │
│         (Spring Cloud Gateway / Kong)                       │
│  • JWT Validation  • Rate Limiting  • Request Routing       │
│  • CORS            • Load Balancing                         │
└──────┬──────────────┬────────────┬──────────────────────────┘
       │              │            │
  ┌────▼───┐   ┌──────▼──┐  ┌─────▼──────┐  ┌──────────────┐
  │  AUTH  │   │  USER   │  │  CLASS     │  │  EXAM        │
  │SERVICE │   │PROFILE  │  │  SERVICE   │  │  SERVICE     │
  │:8081   │   │SERVICE  │  │  :8083     │  │  :8084       │
  │        │   │:8082    │  │            │  │              │
  └────────┘   └─────────┘  └────────────┘  └──────────────┘
  
  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐
  │  GRADING &   │  │NOTIFICATION  │  │  ADMIN &             │
  │  AI SERVICE  │  │  SERVICE     │  │  REPORT SERVICE      │
  │  :8085       │  │  :8086       │  │  :8087               │
  └──────────────┘  └──────────────┘  └──────────────────────┘
  
  ┌──────────────────────────────────────────────────────────┐
  │              ANALYTICS SERVICE  :8088                    │
  └──────────────────────────────────────────────────────────┘
```

---

## Service-by-Service Breakdown

### 1. 🔐 Auth Service (`examsy-auth-service`) — Port 8081

**Responsibilities:** JWT issuance, Google OAuth2, password reset, token refresh

**Owns:**
- `UserAccount` entity + `Role` enum
- Password reset tokens (in-memory / Redis)
- OAuth2 state management

**Database:** `examsy_auth_db` (MySQL)

**Key Endpoints (kept same path for zero frontend change):**
```
POST /api/v1/auth/sign-in
POST /api/v1/auth/signup/student
POST /api/v1/auth/signup/teacher
POST /api/v1/auth/forgot-password
POST /api/v1/auth/verify-code
POST /api/v1/auth/reset-password
GET  /oauth2/authorization/google
```

**Emits events (Kafka/RabbitMQ):**
- `user.registered.student` → User Profile Service creates student profile
- `user.registered.teacher` → User Profile Service creates teacher profile

---

### 2. 👤 User Profile Service (`examsy-profile-service`) — Port 8082

**Responsibilities:** Profile CRUD for Admin, Teacher, Student

**Owns:**
- `Admin`, `Teacher`, `Student` entities (without auth credentials)

**Database:** `examsy_profile_db` (MySQL)

**Key Endpoints:**
```
GET  /api/v1/students/me
PUT  /api/v1/students/me
GET  /api/v1/teachers/me
PUT  /api/v1/teachers/me
GET  /api/v1/admins/me
PUT  /api/v1/admins/me
```

**Listens to events:**
- `user.registered.student` → auto-creates Student record

---

### 3. 🏫 Class Service (`examsy-class-service`) — Port 8083

**Responsibilities:** Course lifecycle, enrollment, announcements, join requests, class codes

**Owns:**
- `Course`, `ClassEnrollment`, `ClassJoinRequest`, `ClassAnnouncement`

**Database:** `examsy_class_db` (MySQL)

**Key Endpoints:**
```
GET|POST        /api/v1/teacher/dashboard/classes
DELETE          /api/v1/teacher/dashboard/classes/{classId}
GET             /api/v1/teacher/classes/{classId}/stream
POST|PUT|DELETE /api/v1/teacher/classes/{classId}/announcements
POST            /api/v1/teacher/classes/{classId}/invite
GET|POST        /api/v1/teacher/classes/{classId}/requests
POST            /api/v1/teacher/classes/requests/{id}/approve|reject
DELETE          /api/v1/teacher/classes/{classId}/students/{studentId}
POST            /api/v1/teacher/dashboard/rotate-codes
GET             /api/v1/student/dashboard/classes
POST            /api/v1/student/dashboard/classes/join
DELETE          /api/v1/student/dashboard/classes/{courseId}/unenroll
POST            /api/v1/student/dashboard/classes/report
```

---

### 4. 📝 Exam Service (`examsy-exam-service`) — Port 8084

**Responsibilities:** Exam publishing, scheduling, student-facing exam interface, submission handling, proctoring logs, mock exams

**Owns:**
- `Exam`, `Question`, `QuestionOption`, `ExamSubmission`, `SubmissionAnswer`, `ProctoringLog`, `MockExam`, `MockQuestion`

**Database:** `examsy_exam_db` (MySQL)

**Key Endpoints:**
```
POST   /api/v1/teacher/exams/publish
GET    /api/v1/teacher/exams/class/{classId}
DELETE /api/v1/teacher/exams/{examId}
PUT    /api/v1/teacher/exams/{examId}/timing
GET    /api/v1/teacher/exams/ongoing
GET    /api/v1/teacher/exams/{examId}/monitor
POST   /api/v1/teacher/exams/{examId}/broadcast
POST   /api/v1/teacher/exams/{examId}/warn/{studentId}
GET    /api/v1/student/exams/{examId}
POST   /api/v1/student/exams/{examId}/submit
GET    /api/v1/student/exams/vault/{classId}
GET    /api/v1/student/dashboard/calendar/exams
POST   /api/v1/student/exams/{examId}/log-event
GET    /api/v1/student/exams/analytics
POST   /api/v1/mock-exams/generate
```

**Emits events:**
- `exam.submitted` → Grading Service
- `exam.upcoming.48h` → Notification Service

---

### 5. 🤖 Grading & AI Service (`examsy-grading-service`) — Port 8085

**Responsibilities:** Groq AI grading, manual grade approval, OCR, mock exam generation, PDF report generation

**Owns:**
- Grading workflow state (may store result in Exam DB via event or direct DB share)

**Database:** `examsy_grading_db` (MySQL) for pending grading queue

**External Integrations:** Groq Cloud API, Tess4j OCR, Apache PDFBox

**Key Endpoints:**
```
POST /api/v1/teacher/exams/{examId}/grade/{submissionId}/auto
POST /api/v1/teacher/exams/{examId}/grade/{submissionId}/approve
GET  /api/v1/teacher/exams/pending-gradings
GET  /api/v1/teacher/exams/{examId}/analytics
```

**Listens to events:**
- `exam.submitted` → triggers AI grading pipeline

---

### 6. 🔔 Notification Service (`examsy-notification-service`) — Port 8086

**Responsibilities:** In-app notifications, SMTP email dispatch, 48-hour exam reminders, grading alerts, warning messages

**Owns:**
- `Notification` entity

**Database:** `examsy_notification_db` (MySQL)

**External Integrations:** Gmail SMTP (JavaMailSender)

**Key Endpoints:**
```
GET  /api/v1/notifications/me
POST /api/v1/teacher/exams/trigger-reminders
```

**Listens to events:**
- `exam.upcoming.48h` → sends reminder emails
- `grade.released` → notifies student
- `student.warned` → sends warning

---

### 7. 🛡️ Admin & Report Service (`examsy-admin-service`) — Port 8087

**Responsibilities:** Admin moderation — reviewing reports, terminating classes/teachers, issuing warnings

**Owns:**
- `Report` entity

**Database:** `examsy_admin_db` (MySQL)

**Key Endpoints:**
```
GET    /api/v1/admin/reports
DELETE /api/v1/admin/reports/{reportId}/terminate-class
DELETE /api/v1/admin/reports/{reportId}/terminate-teacher
PUT    /api/v1/admin/reports/{reportId}/dismiss
POST   /api/v1/admin/reports/{reportId}/warn-teacher
POST   /api/v1/admin/reports/{reportId}/reply-student
GET    /api/v1/admin/dashboard/metrics
```

---

### 8. 📊 Analytics Service (`examsy-analytics-service`) — Port 8088

**Responsibilities:** Aggregated GPA, score progression, class performance charts, pass rates — read-only, computed views

**Database:** Read replica or denormalized `examsy_analytics_db`

**Key Endpoints:**
```
GET /api/v1/student/exams/analytics
GET /api/v1/teacher/exams/{examId}/analytics
GET /api/v1/admin/dashboard/metrics
```

**Listens to events:**
- `exam.submitted`, `grade.released` → updates materialized analytics

---

## Infrastructure & Cross-Cutting Concerns

### API Gateway

**Technology choice: Spring Cloud Gateway** (fits Spring ecosystem, supports JWT validation at edge)

- Single entry point: `https://api.examsy.lk` → routes to services by path prefix
- JWT validation at gateway level (verifies signature using shared secret or Auth Service public key)
- Rate limiting: move Bucket4j from per-service to gateway level (with Redis backing for distributed limiting)
- CORS: centralized at gateway, remove per-service CORS config

### Service Discovery & Load Balancing

**Technology: Netflix Eureka** (Spring Cloud Netflix)

- Each service registers on startup
- Gateway discovers services dynamically
- Add `spring-cloud-starter-netflix-eureka-client` to each service

### Asynchronous Messaging

**Technology: Apache Kafka** (preferred) or RabbitMQ

| Topic / Queue | Producer | Consumer |
|---|---|---|
| `examsy.user.registered` | Auth Service | Profile Service |
| `examsy.exam.submitted` | Exam Service | Grading Service, Notification Service |
| `examsy.grade.released` | Grading Service | Notification Service, Analytics Service |
| `examsy.exam.upcoming` | Exam Service (scheduled) | Notification Service |
| `examsy.student.warned` | Exam Service | Notification Service |

### Centralized Configuration

**Technology: Spring Cloud Config Server**

- Single `examsy-config-server` service reads config from a **private Git repo** (`examsy-config-repo`)
- Each service fetches its config on startup (`bootstrap.yml` → config server URL)
- Secrets (DB passwords, API keys, JWT secret) stored in **HashiCorp Vault** or env variables injected via CI/CD — **NOT in code**

### Distributed Tracing & Observability

| Concern | Tool |
|---|---|
| Distributed tracing | Micrometer + Zipkin (or Jaeger) |
| Metrics | Spring Actuator + Prometheus |
| Dashboards | Grafana |
| Centralized logging | ELK Stack (Elasticsearch + Logstash + Kibana) or Loki |
| Correlation IDs | Spring Cloud Sleuth (auto-injects trace-id into logs) |

### Database Strategy — One DB per Service

| Service | Database Name | Engine |
|---|---|---|
| Auth Service | `examsy_auth_db` | MySQL |
| Profile Service | `examsy_profile_db` | MySQL |
| Class Service | `examsy_class_db` | MySQL |
| Exam Service | `examsy_exam_db` | MySQL |
| Grading Service | `examsy_grading_db` | MySQL |
| Notification Service | `examsy_notification_db` | MySQL |
| Admin Service | `examsy_admin_db` | MySQL |
| Analytics Service | `examsy_analytics_db` | MySQL (read replica) |
| Config Server Cache | `examsy_redis` | Redis |

**Migration approach:**
1. Extract tables from `examsy_db` into per-service schemas
2. Use Flyway (replace Hibernate `ddl-auto=update` — too risky in production) for versioned schema migrations in each service
3. During transition: keep shared DB temporarily with clear schema prefixes, then cut over

### Version Control Strategy

**Repository structure: Mono-repo (recommended for a small team)**

```
examsy/
├── services/
│   ├── examsy-auth-service/        (Spring Boot Maven project)
│   ├── examsy-profile-service/
│   ├── examsy-class-service/
│   ├── examsy-exam-service/
│   ├── examsy-grading-service/
│   ├── examsy-notification-service/
│   ├── examsy-admin-service/
│   ├── examsy-analytics-service/
│   ├── examsy-api-gateway/
│   └── examsy-config-server/
├── frontend/                       (Current Examsy-Frontend — unchanged)
├── infra/
│   ├── docker/                     (Per-service Dockerfiles)
│   ├── k8s/                        (Kubernetes manifests, Helm charts)
│   ├── terraform/                  (Cloud infra as code)
│   └── docker-compose.yml          (Local dev full-stack startup)
├── config-repo/                    (Private: Spring Cloud Config files)
└── .github/
    └── workflows/
        ├── ci-auth-service.yml
        ├── ci-exam-service.yml
        └── ...                     (One CI pipeline per service)
```

**Branching strategy: GitFlow adapted**
```
main          ← production-ready, protected, requires PR + 1 review
develop       ← integration branch
feature/*     ← feature branches per service (e.g. feature/auth-refresh-token)
release/x.y.z ← release candidate branch
hotfix/*      ← critical production fixes
```

**Tags:** `v1.0.0-auth`, `v1.0.0-exam` — semantic versioning per service

### Containerization

Every service gets a `Dockerfile`:

```dockerfile
# Multi-stage build for Spring Boot service
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -q
COPY src ./src
RUN ./mvnw package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Docker Compose for local development:**

```yaml
# docker-compose.yml (simplified excerpt)
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
  redis:
    image: redis:7-alpine
  kafka:
    image: confluentinc/cp-kafka:7.6.0
  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.0
  config-server:
    build: ./services/examsy-config-server
    depends_on: [mysql]
  eureka-server:
    build: ./services/examsy-eureka-server
  api-gateway:
    build: ./services/examsy-api-gateway
    ports: ["8080:8080"]
    depends_on: [eureka-server, config-server]
  auth-service:
    build: ./services/examsy-auth-service
    depends_on: [mysql, kafka, config-server, eureka-server]
  # ... all other services
  frontend:
    build: ./frontend
    ports: ["5173:80"]
```

### CI/CD Pipeline

**Platform: GitHub Actions** (already using GitHub repos)

**Per-service pipeline (`ci-auth-service.yml` example):**

```
Trigger: push to develop or main (paths: services/examsy-auth-service/**)

Jobs:
  1. lint-and-test
     - mvn test (unit + integration tests with Testcontainers)
     - SonarQube quality gate (optional)
  
  2. build-and-push (only on main)
     - docker build
     - push to GitHub Container Registry (ghcr.io)
     - tag: ghcr.io/ruvinda-shaluka/examsy-auth-service:v${{ version }}
  
  3. deploy (only on main, after build passes)
     - kubectl apply -f infra/k8s/auth-service/
     OR
     - docker-compose up -d auth-service (for simpler deploy)
```

**Secrets management in GitHub Actions:**
- GitHub repository secrets for `MYSQL_PASSWORD`, `JWT_SECRET`, `GROQ_API_KEY`, etc.
- Never hardcoded (current `application.properties` has exposed credentials — must be fixed immediately)

---

## Security Hardening Plan

> [!CAUTION]
> The current `application.properties` contains plaintext DB password, JWT secret, Gmail App Password, Google OAuth2 client secret, and Groq API keys. These **must be rotated immediately** and never committed to VCS again.

### Token Strategy in Microservices

- Auth Service issues **short-lived JWTs** (15 minutes) + **refresh tokens** (stored in Redis, 7 days)
- API Gateway validates JWT signature using Auth Service's **public key** (asymmetric RS256) — no inter-service call needed for validation
- Services trust JWT claims (userId, role) passed via request headers from gateway

### Inter-Service Communication Security

- **Internal calls** (service-to-service): use **mTLS** or a shared internal JWT signed with a service account secret
- No service is directly exposed to the internet — only the API Gateway port is public

---

## Migration Phases

### Phase 1 — Foundation (Week 1–2)
- [ ] Create mono-repo structure with proper `.gitignore` and branch protection
- [ ] Rotate and externalize ALL credentials (environment variables / `.env.local` files for dev)
- [ ] Set up `docker-compose.yml` with MySQL (multi-database), Redis, Kafka, Zookeeper
- [ ] Bootstrap `examsy-config-server` and `examsy-eureka-server`
- [ ] Bootstrap `examsy-api-gateway` with basic routing (pass-through first)
- [ ] Set up Flyway in the existing backend, create initial migration scripts from existing schema

### Phase 2 — Auth Service Extraction (Week 3)
- [ ] Create `examsy-auth-service` Spring Boot project
- [ ] Migrate `UserAccount`, `Role` entities + all `AuthController` endpoints
- [ ] Implement Kafka producer for `user.registered` events
- [ ] Switch JWT to RS256 (asymmetric keys), configure gateway to validate with public key
- [ ] Set up GitHub Actions CI pipeline for Auth Service
- [ ] Test: sign-in, sign-up, OAuth2 flow, password reset

### Phase 3 — Profile + Class Services (Week 4–5)
- [ ] Create `examsy-profile-service` — migrate Student, Teacher, Admin profile endpoints
- [ ] Consume `user.registered` from Kafka to auto-create profiles
- [ ] Create `examsy-class-service` — migrate Course, Enrollment, Announcement logic
- [ ] Update API Gateway routes for `/students/*`, `/teachers/*`, `/admins/*`, `/teacher/dashboard/*`, `/student/dashboard/*`

### Phase 4 — Exam Service (Week 6–7)
- [ ] Create `examsy-exam-service` — largest extraction
- [ ] Migrate Exam, Question, Submission, Proctoring logic
- [ ] Implement Kafka producers: `exam.submitted`, `exam.upcoming`
- [ ] Keep Mock Exam generation in this service (or split to grading service)

### Phase 5 — Grading & AI + Notifications (Week 8–9)
- [ ] Create `examsy-grading-service` with Groq integration, OCR, PDFBox
- [ ] Create `examsy-notification-service` with JavaMailSender + in-app notifications
- [ ] Wire up Kafka consumers for grading and notification triggers

### Phase 6 — Admin, Analytics & Observability (Week 10–11)
- [ ] Create `examsy-admin-service`
- [ ] Create `examsy-analytics-service` (event-driven materialized views)
- [ ] Set up Prometheus + Grafana + Zipkin dashboards
- [ ] Configure centralized logging (ELK or Loki)

### Phase 7 — Production Hardening (Week 12)
- [ ] Kubernetes manifests for all services (or Docker Swarm if simpler)
- [ ] Configure Horizontal Pod Autoscaler (HPA) for Exam + Grading services
- [ ] Full end-to-end integration test suite
- [ ] Load test with k6 or JMeter
- [ ] Security audit (OWASP ZAP scan on gateway)
- [ ] Final documentation update

---

## Open Questions

> [!IMPORTANT]
> Please review these before execution begins:

1. **Repository structure**: Mono-repo (recommended above for team size) vs. poly-repo (separate GitHub repo per service)? Mono-repo is simpler to manage for a solo/small team.

2. **Message broker**: Kafka (more robust, better for analytics/event sourcing) vs. RabbitMQ (simpler setup)? Given analytics requirements, Kafka is recommended.

3. **Deployment target**: Local Docker Compose only? Or cloud (AWS / GCP / Railway / Render)? This affects which managed services to use.

4. **Frontend change**: The frontend currently points to a single `VITE_API_BASE_URL=http://localhost:8080`. With microservices, this stays **the same** (gateway port) so **no frontend code changes are required** — confirm this is acceptable.

5. **Credential rotation**: You need to rotate all exposed secrets (DB password, JWT secret, Gmail App Password, OAuth2 client secret, all 3 Groq API keys) immediately. Do you want to handle this as the very first task?

6. **Flyway vs Hibernate DDL**: Recommend replacing `ddl-auto=update` with Flyway for versioned, safe migrations. Agreed?

---

## Verification Plan

### Automated Tests
- Each service: unit tests (JUnit 5 + Mockito) + integration tests (Testcontainers with real MySQL)
- Gateway routing tests
- Contract tests (Spring Cloud Contract) between services
- End-to-end: Playwright or Cypress against `docker-compose` environment

### Manual Verification
- Full user journey: Register → Login → Create Class → Publish Exam → Student submits → AI grades → Notification sent
- Admin moderation flow
- Google OAuth2 flow
- Password reset flow
- Real-time exam monitoring (proctoring)
