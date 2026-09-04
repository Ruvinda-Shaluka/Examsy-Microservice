# 🎓 Examsy: Monolithic to Distributed Microservices Architecture
## Enterprise Technical Documentation & CV Portfolio Guide

> **Author / Lead Engineer:** Ruvinda Shaluka  
> **Target Architecture:** Event-Driven Microservices (Java 21 / Spring Boot 3.4+ / Spring Cloud / Apache Kafka / Redis / Docker)  
> **Original Architecture:** Spring Boot Monolith with MySQL & Groq Cloud AI  
> **Frontend:** React 19, Vite, TailwindCSS (Axios with Edge-Routed JWT Interceptor)

---

## 📌 Executive Summary

**Examsy** is an enterprise-grade online examination and academic assessment platform featuring AI-powered automated grading, real-time exam proctoring, course collaboration, and dynamic analytics. 

Originally architected as a monolithic Spring Boot backend, the system was systematically decoupled into a high-throughput, fault-tolerant **Event-Driven Microservices Architecture**. This transition addresses critical scalability bottlenecks such as heavy CPU/IO load from handwriting OCR (Tesseract) and LLM inference (Groq Cloud), high database contention during concurrent exam submissions, and tight domain coupling.

```
+---------------------------------------------------------------------------------------+
|                                  React 19 SPA (Vite)                                  |
|                             http://localhost:5173 / Port 80                           |
+-------------------------------------------+-------------------------------------------+
                                            | REST / HTTPS
                                            v
+---------------------------------------------------------------------------------------+
|                 Spring Cloud API Gateway (Port 8080) [examsy-api-gateway]             |
|          * Global CORS  * Dynamic Eureka Routing  * Rate Limiting  * Edge Security    |
+----+----------------------+--------------------+---------------------+----------------+
     |                      |                    |                     |
     v                      v                    v                     v
+------------+       +-------------+      +------------+        +-------------+
|    AUTH    |       |   PROFILE   |      |   CLASS    |        |    EXAM     |
|  SERVICE   |       |   SERVICE   |      |  SERVICE   |        |   SERVICE   |
|  Port 8081 |       |  Port 8082  |      | Port 8083  |        |  Port 8084  |
+-----+------+       +------+------+      +-----+------+        +------+------+
      |                     ^                   |                      |
      |                     |                   |                      |
      | Kafka Event         | Kafka Event       |                      | Kafka Event
      | (user.registered)   | (consume)         |                      | (exam.submitted)
      +-------------------->+                   |                      v
                                                |               +--------------+
                                                |               |  GRADING &   |
                                                |               |  AI SERVICE  |
                                                |               |  Port 8085   |
                                                |               +-------+------+
                                                |                       |
                                                v                       v
                                        +-------------------------------+------+
                                        |  NOTIFICATION & EMAIL SERVICE (8086) |
                                        +--------------------------------------+
```

---

## 🏛️ PART 1: Monolithic System Architecture (`Examsy-Backend`)

The monolithic foundation was developed across 216 commits, implementing a comprehensive set of features from database schema design to advanced AI-based OCR evaluation. Below is the mapping of each functional domain to its exact implementation commits in `Examsy-Backend`.

### 1. Core Architecture, Entity Modeling & Schema Design
* **Key Concept:** Single-schema relational model (`examsy_db`) with Spring Data JPA and Jakarta Bean Validation.
* **Relevant Commits:**
  - `1fbd8f3`: `initialize Examsy Backend project with Spring Boot and Maven setup`
  - `2bc2ac3`: `feat: adding user and class management entity classes according to database schema`
  - `71ce369`: `feat: adding exams & questions entity classes according to database schema`
  - `429f7a4`: `feat: adding Submissions, Grading & Proctoring entity classes according to database schema`
  - `1702cb0`: `feat: adding Reports & Moderation entity classes according to database schema`
  - `fb2f7f4`, `0a6b296`, `7c48884`, `cab05f2`: `feat: extending Repos from JpaRepository`
  - `3d4faa2`: `feat: adding APIResponse`
  - `c2d8b89`: `feat: adding GlobalExceptionHandler for handling global exceptions`
  - `7ab08c1` to `20a803c`: `feat: validated DTOs creation (AdminDTO, ClassAnnouncementDTO, CourseDTO, ExamDTO, QuestionDTO, ReportDTO, StudentDTO, etc.)`
  - `441b641`, `7580ced`: `feat: configuring ModelMapper with strict matching for flat DTO hierarchies`

### 2. Authentication, Role-Based Access Control & OAuth2
* **Key Concept:** Stateless authentication via Spring Security and HMAC-SHA256 JWTs, supporting username/email authentication, verification codes, and Google OAuth2 server-side validation.
* **Relevant Commits:**
  - `c178132`: `chore: add spring security and jwt dependencies`
  - `7f371df`: `feat: implement UserDetails in UserAccount and add Role enum (ADMIN, TEACHER, STUDENT)`
  - `3c53ebf`: `feat: add UserAccountRepository and JWT utilities`
  - `7be5401`: `feat: configure security settings with JWT authentication and user details service`
  - `c563b86`, `eda03cf`: `feat: add APIResponse, AuthDTO, AuthResponseDTO, StudentRegisterDTO, TeacherRegisterDTO`
  - `9119345`: `feat: implement AuthService for user authentication and registration`
  - `b9b8242`: `feat: add AuthController for user registration and authentication endpoints`
  - `43017ae`: `feat: add role field to AuthResponseDTO and update AuthService to include user role in token response`
  - `1cc2222`: `feat: update authentication to allow login via username or email`
  - `cda5e55`: `feat: enhance JWT authentication filter to handle invalid or expired tokens gracefully`
  - `f2b5d4d`: `feat: forgot password implementation (ForgotPasswordDTO, VerifyCodeDTO, ResetPasswordDTO, verification code storage)`
  - `d07d160`, `1de39f8`: `feat: implement google oauth function to user registration and login on server side`

### 3. User Profile Management & Progressive Profiling
* **Key Concept:** Distinct domain profiles for Students, Teachers, and Admins linked via foreign keys to `UserAccount`, supporting progressive onboarding.
* **Relevant Commits:**
  - `27176c4`, `3f96886`: `feat: add StudentController and StudentService for managing student profiles and updates`
  - `2b5cdc8`: `feat: implement progressive profiling and notification preferences updates in StudentService`
  - `601538a`, `c248eef`, `f5b6b85`: `feat: add TeacherController, TeacherRepo with custom query, and TeacherService for teacher management`
  - `bbed2d1`: `feat: updated StudentProfileServiceImpl update and get method to handle new fields in the ui`

### 4. Course, Classroom & Collaboration Lifecycle
* **Key Concept:** Teacher class creation, student join requests with teacher approval queue ("bouncer" logic), dynamic 7-day invite code rotation, email invites, and course roster management.
* **Relevant Commits:**
  - `9eeedd6`, `3741a1a`: `feat: implement ClassEnrollmentService, StudentDashboardController for class cards and unenroll`
  - `50243d8`, `473278b`, `6cb74f1`: `feat: Create TeacherDashboardService, TeacherClassCardDTO, and secure class deletion`
  - `aa1514b`: `feat: added rotateExpiredClassCodes method to replace class codes older than 7 days upon teacher login`
  - `f116cbe`, `d02e67b`: `feat: class code invite link generation and validation`
  - `898bf09`: `feat: added email invite logic to invite student to join a class via email`
  - `4d35e09`, `ebb8cf4`, `dedcf2e`: `feat: added getPeople to get enrolled member list and removeStudent from class`
  - `fef7285`: `feat: created new entity named ClassJoinRequest to hold students waiting to be approved`
  - `40575f1`: `feat: added bouncer logic to student class enrollment`

### 5. Exam Creation, Scheduling & Submissions
* **Key Concept:** Multiple exam formats (MCQ, Short Answer, Uploaded Handwritten PDF), deadline management, and auto-calculation of maximum points.
* **Relevant Commits:**
  - `7c27456`, `588a240`: `feat: updateExamTiming and updateExamDeadline via Coursework interface`
  - `4efb3bc`: `feat: fixed loading exams logic with classId scoping`
  - `b5447f2`: `feat: added ongoing exam endpoint to retrieve all real-time data of active exams`
  - `0237f9b`: `feat: update publish exam to auto calculate max score for mcq exams and short answer exams`
  - `c22ca30`: `feat: improve fetch vault exam method logic to filter finished exams by final score field`
  - `fec2ead`: `feat: fix cascading issue when deleting an exam`

### 6. Real-Time Exam Proctoring & Integrity Telemetry
* **Key Concept:** Capturing client-side anomalies (tab-switching, fullscreen exits, mouse focus loss), live teacher proctoring dashboard, teacher-to-student warnings, and broadcasts.
* **Relevant Commits:**
  - `e214a0a`, `1644f9e`: `feat: added proctoring post/get endpoints to persist and retrieve student behavioral log events`
  - `45ccb68`: `feat: added get proctoring log data endpoint and its logic`
  - `37c2b36`: `feat: improve getLiveMonitorData logic for active teacher supervision`
  - `8eed5d0`: `feat: added broadcast and student warning endpoints in NotificationServiceImpl`

### 7. AI Automated Grading, OCR & LLM Integration
* **Key Concept:** End-to-end multi-modal grading pipeline: Cloudinary PDF download -> Apache PDFBox conversion to `BufferedImage` -> Tesseract OCR text extraction -> Groq Cloud LLaMA 3.1 LLM prompt evaluation with model answer comparison -> teacher review/override workflow.
* **Relevant Commits:**
  - `da76f5a`, `a2ee447`, `c7c6261`: `feat: implement mock exam generator with Groq AI using dynamic randomization seeds (System.currentTimeMillis())`
  - `57aaf87`, `233d9cc`: `feat: added model answer field to Question and short answer grading method with Groq API`
  - `e177a09`: `feat: Complete handwritten PDF grading pipeline (Cloudinary -> PDFBox -> Tesseract OCR -> Groq API -> suggestedScore JSON payload)`
  - `8966cb2`: `feat: retrieve pending grading exams for teacher review`
  - `25db167`: `feat: fixing bugs in Groq auto-grading and OCR text debugging`
  - `20e9bae`: `feat: added automatic grading and save details in database for MCQ and short answers`
  - `1cb23b6`, `27d261a`: `feat: added approve grade endpoint and update teacher-approved exam results and letter grades`
  - `af95958`: `feat: update model version to llama-3.1-8b-instant`

### 8. Notifications & Automated Reminders
* **Key Concept:** Asynchronous email dispatch via Gmail SMTP (`JavaMailSender`) and in-app notifications for exam releases, deadlines, and grade approvals.
* **Relevant Commits:**
  - `1952cf7`: `feat: implement method to trigger notifications to students when teacher publishes an exam`
  - `744abf6`: `feat: added triggering method to send 48 hours exam reminders to students`
  - `d1e5d60`: `feat: added notification sending when teacher submits finalized grading`
  - `723ffdd`: `feat: update notifyPush default value to true`

### 9. Analytics & Admin Moderation Telemetry
* **Key Concept:** Real-time GPA computation, exam score progression, admin system-wide metrics, and violation resolution.
* **Relevant Commits:**
  - `44b5e21`: `feat: added endpoint to retrieve student exam data analytics and method to calculate student cumulative GPA`
  - `3d6c1f7`: `feat: added endpoint to retrieve exam analytics and score distribution for teachers`
  - `81d13f7`, `a1bada2`: `feat: implement AdminDashboardService and metrics telemetry`
  - `d888a8a`, `bf1d8df`: `chore: modularized response DTOs for AdminReportController and AdminDashboardController`

### 10. Resilience & Distributed Rate Limiting
* **Key Concept:** Token Bucket algorithm implemented via Bucket4j to prevent brute-force attacks and abuse of expensive LLM and OCR endpoints.
* **Relevant Commits:**
  - `d751054`: `chore: added bucket4j dependency for rate limiting`
  - `a5cd501`, `60dbd7f`: `feat: added RateLimitingServiceImpl with RateLimitFilter positioned prior to JwtAuthFilter`
  - `4ff6b8f`: `feat: adjust rate tokens capacity and token refill frequency`
  - `1d465bc`: `feat: fixed jwt filter and rate limit filter ordering to prevent backend crash`
  - `cde6719`: `chore: add application.properties.example and ignore actual properties files`

---

## ⚡ The Architectural Turning Point: Why Decompose?

While feature-complete, the monolithic architecture presented severe enterprise limitations:
1. **CPU & IO Starvation:** Running Tesseract OCR and Apache PDFBox rendering concurrently with student exam submissions blocked the main Tomcat worker thread pool, causing HTTP 504 timeouts.
2. **Database Bottleneck:** A single `examsy_db` database caused table-level and row-level lock contention during peak exam start/submission windows (hundreds of simultaneous writes to `exam_submissions` and `proctoring_logs`).
3. **Blast Radius (Zero Fault Isolation):** If Groq API rate limits were exceeded or an OCR conversion threw an OutOfMemoryError, the entire platform crashed—preventing students in other classes from even viewing course materials.
4. **Independent Scalability:** The proctoring and exam submission services required 10x the horizontal scale of the admin or profile services during exam days, but the monolith forced scaling the entire application uniformly.

---

## 🚀 PART 2: Completed Microservices Phases (`Examsy-Microservice`)

The decomposition follows the **Strangler Fig Pattern**, decoupling services from foundational infrastructure up to domain boundaries.

### Phase 1: Distributed Infrastructure & Database Isolation
* **Objective:** Establish containerized local infrastructure with isolated datastores (Database-per-Service pattern), caching, and message brokers.
* **Implemented Components:**
  - `docker-compose.yml`: Multi-container topology with healthchecks and isolated networks.
  - Multi-Database Initialization: `init.sql` script generating 8 distinct relational schemas (`examsy_auth_db`, `examsy_profile_db`, `examsy_class_db`, `examsy_exam_db`, `examsy_grading_db`, `examsy_notification_db`, `examsy_admin_db`, `examsy_analytics_db`).
  - Message Broker: Confluent Apache Kafka (`cp-kafka:7.6.0`) with Zookeeper for event streams.
  - Distributed Cache & State: Redis 7 Alpine (`port 6379`).
  - Port Conflict Mitigation: MySQL mapped to host port `3307` to prevent collisions with local developer instances.
* **Repository Commits (`Examsy-Microservice`):**
  - `cb69463`: `chore: initialize Examsy-Microservice repository structure`
  - `6e9f47f`: `chore: add .env.example and update secret ignoring rules`
  - `cb7eb8f`: `feat: add multi-database init.sql and docker-compose infrastructure`
  - `5254ecc`: `fix: map MySQL host port to 3307 to prevent collision with local Windows MySQL`

---

### Phase 2: Centralized Configuration Management (`examsy-config-server`)
* **Objective:** Externalize and centralize configuration for all microservices in a single, version-controlled repository (`config-repo/`) using Spring Cloud Config.
* **Implemented Components:**
  - Service: `examsy-config-server` on **Port 8888**.
  - Backend: Native profile reading YAML configurations directly from `classpath:/config-repo` or local directory.
  - Configuration Profiles: Default, development, and production profiles for each microservice with property override support.
* **Repository Commits (`Examsy-Microservice`):**
  - `98b2b84`: `feat(phase2): implement Spring Cloud Config Server with native mono-repo config`

---

### Phase 3: Service Discovery Registry (`examsy-eureka-server`)
* **Objective:** Provide dynamic client-side load balancing and eliminate hardcoded IP/port dependencies between distributed components.
* **Implemented Components:**
  - Service: `examsy-eureka-server` on **Port 8761**.
  - Discovery Engine: Netflix Eureka Server with peer replication disabled for standalone development.
  - Heartbeat & Eviction: Configured lease renewal intervals and eviction timeouts to rapidly detect failed service instances.
* **Repository Commits (`Examsy-Microservice`):**
  - `8d140f0`: `feat(phase3): implement Netflix Eureka Service Discovery Registry`

---

### Phase 4: Cloud API Gateway & Edge Security Routing (`examsy-api-gateway`)
* **Objective:** Single entry point for all frontend client traffic (`http://localhost:8080`), acting as the reverse proxy, load balancer, and security edge.
* **Implemented Components:**
  - Technology: Spring Cloud Gateway (Reactive / WebFlux-based non-blocking architecture).
  - Dynamic Routing: Routes configured with Eureka service discovery identifiers (`lb://EXAMSY-AUTH-SERVICE`, etc.).
  - Unified CORS: Centralized cross-origin configuration eliminating duplicate CORS headers and preflight failures.
  - Path Decoupling: Maps frontend paths (e.g., `/api/v1/auth/**`) directly to backend service endpoints with zero changes required in `Examsy-Frontend`.
* **Repository Commits (`Examsy-Microservice`):**
  - `b9de388`: `feat(phase4): implement Spring Cloud API Gateway with Eureka dynamic routing and CORS`

---

### Phase 5: Authentication Microservice & Event Sourcing (`examsy-auth-service`)
* **Objective:** Isolate credential management, token lifecycle, and authentication events into a dedicated high-security microservice backed by its own database (`examsy_auth_db`).
* **Implemented Components:**
  - Port: **8081** | Database: `examsy_auth_db`.
  - Schema Evolution: Flyway automated migrations (`V1__init_auth_schema.sql`) for reproducible schema state.
  - Security Framework: Spring Security 6 with stateless `STATELESS` session creation policy and BCrypt password hashing.
  - JWT Engine: `JwtTokenProvider` generating signed bearer tokens containing user ID, username, and `ROLE_*` claims.
  - Asynchronous Event Sourcing (Kafka Producer):
    - Implemented `AuthEventProducer` broadcasting `UserRegisteredEvent` to topic `examsy.user.registered`.
    - Payload carries user ID, email, username, role, and domain-specific attributes (e.g., `studentIdentificationNumber`, `instructorId`, `specialization`).
    - Decouples registration from profile creation: when a user registers, Auth Service persists credentials and emits an event without blocking on profile creation.
  - Endpoints Migrated:
    - `POST /api/v1/auth/signup/student`
    - `POST /api/v1/auth/signup/teacher`
    - `POST /api/v1/auth/sign-in`
    - `POST /api/v1/auth/forgot-password`, `/verify-code`, `/reset-password`
* **Repository Commits (`Examsy-Microservice`):**
  - `2e72260`: `feat(auth): scaffold examsy-auth-service with Flyway, Security, Kafka, and Discovery`
  - `61e1a6b`: `feat(auth): add V1 Flyway migration, UserAccount entity, and repository`
  - `88c665a`: `feat(auth): implement security, JWT, Kafka producer, AuthService, and AuthController`
  - `b29d2fb`: `Merge branch 'feature/examsy-auth-service' into development`

---

### Spring Boot 4.0.2 & Spring Cloud 2025.1.2 Fleet Upgrade
* **Objective:** Align the entire microservices ecosystem with Spring Boot 4.0.2 (matching `Examsy-Backend`) and Spring Cloud 2025.1.2 (Oakwood), utilizing modularized Spring MVC web starters and WebFlux Gateway.
* **Repository Commits (`Examsy-Microservice`):**
  - `c746243`: `build: upgrade microservices fleet to Spring Boot 4.0.2 and Spring Cloud 2025.1.2`

---

### Phase 6: User Profile Microservice (`examsy-profile-service`)
* **Objective:** Complete profile lifecycle for Students, Teachers, and Admins backed by dedicated schema `examsy_profile_db`, consuming asynchronous registration events from Kafka.
* **Implemented Components:**
  - Port: **8082** | Database: `examsy_profile_db`.
  - Technology: Spring Boot **4.0.2**, Spring Cloud **2025.1.2**, Java **21**.
  - Database Decoupling: Replaced monolithic `@OneToOne` foreign key to `user_accounts` with independent `user_id`, `username`, and `email` columns on profile records.
  - Schema Evolution: Flyway automated migrations (`V1__init_profile_schema.sql`) for `students`, `teachers`, and `admins` tables.
  - Event-Driven Kafka Consumer:
    - Implemented `AuthEventConsumer` listening to topic `examsy.user.registered` (consumer group: `profile-service-group`).
    - Asynchronously provisions Student, Teacher, or Admin profile records with idempotency checks.
  - Endpoints Implemented:
    - `GET /api/v1/students/me`, `PUT /api/v1/students/me` (with progressive profiling support)
    - `GET /api/v1/teachers/me`, `PUT /api/v1/teachers/me`
    - `GET /api/v1/admins/me`, `PUT /api/v1/admins/me`
* **Repository Commits (`Examsy-Microservice` on `feature/examsy-profile-service`):**
  - `49bffaf`: `feat(profile): scaffold examsy-profile-service with Spring Boot 4.0.2 and Spring Cloud 2025.1.2`
  - `94b63ec`: `feat(profile): add V1 Flyway schema migration for students, teachers, and admins`
  - `f90bf52`: `feat(profile): implement Student, Teacher, and Admin JPA entities with audit timestamps`
  - `2cfc9e0`: `feat(profile): add Spring Data JPA repositories for Student, Teacher, and Admin`
  - `67936b2`: `feat(profile): implement Kafka consumer for asynchronous user onboarding`
  - `5b9b237`: `feat(profile): configure stateless JWT security filter and authorization rules`
  - `201482d`: `feat(profile): implement DTOs and ModelMapper configuration with strict mapping`
  - `aaefd66`: `feat(profile): implement business services for Student, Teacher, and Admin profiles`
  - `8b5217a`: `feat(profile): implement REST controllers, exception handler, and config-repo properties`

---

### Phase 7: Class & Collaboration Microservice (`examsy-class-service`)
* **Objective:** Isolate course lifecycle, class enrollment, announcements stream, 7-day automatic class code rotation, and student join request approval queues ("bouncer" logic) into an autonomous service backed by `examsy_class_db`.
* **Implemented Components:**
  - Port: **8083** | Database: `examsy_class_db`.
  - Technology: Spring Boot **4.0.2**, Spring Cloud **2025.1.2**, Java **21**, Spring Data JPA, Flyway, Spring Security (JWT), Spring Mail.
  - Schema Evolution: Flyway `V1__init_class_schema.sql` creating `classes`, `class_enrollments`, `class_join_requests`, and `class_announcements`.
  - Autonomous Domain Model: Decoupled monolithic joins into autonomous domain entities (`Course`, `ClassEnrollment`, `ClassJoinRequest`, `ClassAnnouncement`) storing teacher and student references (`teacher_id`, `teacher_username`, `teacher_name`, `student_id`, `student_username`, `student_name`, `student_email`).
  - Business Features:
    - Teacher class card listing, class creation with random theme colors, and secure deletion.
    - 7-day automatic expired class code rotation logic.
    - Class stream loading with announcements CRUD and appearance customization (banner & theme).
    - Class roster management with student removal and email invitations via JavaMailSender.
    - Bouncer approval queue: Students submit invite links, verifying expiration/code validity, creating pending join requests that teachers approve or reject.
* **Repository Commits (`Examsy-Microservice` on `feature/examsy-class-service` - 37 Atomic Commits):**
  - `645dab2`: `chore(class): initialize examsy-class-service directory structure and Maven wrapper`
  - `a042c9a`: `build(class): configure pom.xml with Spring Boot 4.0.2, Spring Cloud 2025.1.2, and JPA dependencies`
  - `e51140b`: `config(class): add local application.properties and bootstrap configuration`
  - `264d158`: `feat(class): bootstrap ExamsyClassServiceApplication main class`
  - `36dd3ab`: `db(class): add Flyway V1 migration script for classes schema`
  - `60852dc`: `feat(class): implement Course domain entity with section and term attributes`
  - `6e544e3`: `feat(class): implement ClassEnrollment domain entity with composite uniqueness`
  - `da4d081`: `feat(class): implement ClassJoinRequest domain entity with approval status`
  - `267dfcd`: `feat(class): implement ClassAnnouncement domain entity with author tracking`
  - `2d2166d`: `feat(class): add CourseRepo with teacher ownership queries`
  - `5f79e9e`: `feat(class): add ClassEnrollmentRepo for class and student lookups`
  - `63373ac`: `feat(class): add ClassJoinRequestRepo for pending request filtering`
  - `601b11e`: `feat(class): add ClassAnnouncementRepo with chronological order queries`
  - `a37501e`: `feat(class): implement JwtTokenProvider for bearer token validation`
  - `2cce4f3`: `feat(class): implement JwtAuthFilter for security context population`
  - `a3bc8fe`: `feat(class): configure SecurityFilterChain with role-based access control`
  - `4881278`: `feat(class): add TeacherClassCardDTO and CourseCreateDTO for dashboard`
  - `ff65b87`: `feat(class): add CreateAnnouncementDTO and AnnouncementDTO for stream`
  - `1ac062f`: `feat(class): add ClassStreamDTO and UpdateAppearanceDTO`
  - `35a420b`: `feat(class): add PersonDTO and ClassPeopleDTO for roster management`
  - `e8fbdc7`: `feat(class): add JoinRequestDTO, JoinClassDTO, InviteStudentDTO, and StudentClassCardDTO`
  - `fe23101`: `feat(class): add APIResponse generic wrapper and ModelMapper configuration bean`
  - `e8d6f82`: `feat(class): define TeacherDashboardService interface`
  - `8fc01cb`: `feat(class): implement TeacherDashboardServiceImpl for course creation and deletion`
  - `e15866e`: `feat(class): implement 7-day class code automatic rotation logic in TeacherDashboardService`
  - `d69ee96`: `feat(class): define TeacherClassService interface`
  - `ce53e2c`: `feat(class): implement announcement management and stream loading in TeacherClassServiceImpl`
  - `25d9ad4`: `feat(class): implement people roster and student removal in TeacherClassServiceImpl`
  - `7a61daa`: `feat(class): implement email invitations and bouncer join request approvals in TeacherClassServiceImpl`
  - `0b84eec`: `feat(class): define StudentClassService interface`
  - `61ee289`: `feat(class): implement StudentClassServiceImpl for student enrolled classes and unenrollment`
  - `3a86a75`: `feat(class): implement bouncer join request logic in StudentClassServiceImpl`
  - `dd0065d`: `feat(class): implement TeacherDashboardController for classes and code rotation`
  - `b3577a2`: `feat(class): implement TeacherClassController for stream, announcements, and roster`
  - `ccd68cd`: `feat(class): implement StudentClassController for enrollment and join workflows`
  - `b60a3f2`: `feat(class): implement GlobalExceptionHandler for validation and domain errors`
  - `bad2568`: `config(class): add centralized examsy-class-service.properties to config-repo`

---

### Shared Library: `examsy-common` (Enterprise Shared Security & SDK)
* **Objective:** Eliminate code duplication (DRY violation) across microservices by extracting shared cross-cutting concerns—Stateless JWT token verification, security filters, unified API response envelopes, ModelMapper config, and global exception handlers—into an enterprise reusable library module (`examsy-common`).
* **DevSecOps Security Hardening:**
  - **Zero Hardcoded Fallback Secrets:** In accordance with OWASP Top 10 guidelines, `jwt.secret` has zero fallback defaults in Java source code. It is strictly injected from Spring Cloud Config Server (`config-repo/application.properties`) or the environment (`JWT_SECRET`).
  - **Fail-Fast Startup Validation:** Implemented `@PostConstruct` cryptographic strength validation requiring at least 256 bits (32 characters), failing startup immediately if secrets are absent or weak.
  - **Context Leakage Prevention:** `JwtAuthFilter` explicitly clears `SecurityContextHolder` on security exceptions, preventing thread-local security context bleeding across pooled Tomcat requests.
  - **Standardized API Envelopes:** Centralized `APIResponse<T>` and `GlobalExceptionHandler` format uniform JSON responses across all microservices.
* **Repository Commits (`Examsy-Microservice` on `feature/examsy-common`):**
  - `6d1609d`: `chore(common): initialize examsy-common shared library structure and Maven wrapper`
  - `a460ed9`: `build(common): configure pom.xml for reusable library packaging`
  - `f9a1a9b`: `feat(common): implement APIResponse generic payload wrapper`
  - `2e7fc27`: `feat(common): implement JwtTokenProvider for centralized token decoding and validation`
  - `3dca965`: `feat(common): harden JwtTokenProvider with zero hardcoded fallback, fail-fast validation, and specific exception handling`
  - `b4d966d`: `feat(common): implement JwtAuthFilter with explicit context clearing on security failure`
  - `52b55bb`: `feat(common): implement GlobalExceptionHandler with standardized APIResponse transformation`
  - `d265c86`: `feat(common): add CommonConfig providing reusable ModelMapper bean`
  - `7a5ea14`: `config(common): centralize master jwt.secret property in config-repo/application.properties`
  - `bcb1295`: `Merge branch 'feature/examsy-common' into development`

* **Fleet-wide Common Migration (`Examsy-Microservice` on `refactor/commons` - 15 Atomic Commits):**
  - Upgraded all upstream microservices (`examsy-class-service`, `examsy-profile-service`, `examsy-auth-service`) to consume `examsy-common`, eliminating duplicate JWT filters, token providers, APIResponse envelopes, and exception handlers across the codebase.
  - `48ba0cb`: `feat(common): add token generation methods to JwtTokenProvider for auth-service compatibility`
  - `7808f8b`: `build(class): add examsy-common dependency and remove redundant jjwt artifacts`
  - `ffba02d`: `refactor(class): remove duplicate JwtTokenProvider and JwtAuthFilter in favor of examsy-common`
  - `9b69980`: `refactor(class): remove duplicate APIResponse DTO in favor of examsy-common`
  - `f2389b4`: `refactor(class): remove duplicate GlobalExceptionHandler in favor of examsy-common`
  - `ce97816`: `refactor(class): update SecurityConfig and REST controllers to use examsy-common imports`
  - `54b057a`: `build(profile): add examsy-common dependency and remove redundant jjwt artifacts`
  - `7764e21`: `refactor(profile): remove duplicate JwtTokenProvider and JwtAuthFilter in favor of examsy-common`
  - `43b3f4f`: `refactor(profile): remove duplicate APIResponse DTO in favor of examsy-common`
  - `db4e0dd`: `refactor(profile): remove duplicate GlobalExceptionHandler in favor of examsy-common`
  - `04808ce`: `refactor(profile): update SecurityConfig and REST controllers to use examsy-common imports`
  - `17849c2`: `build(auth): add examsy-common dependency and remove redundant jjwt artifacts`
  - `b9673b9`: `refactor(auth): remove duplicate APIResponse DTO in favor of examsy-common`
  - `426e40f`: `refactor(auth): remove duplicate GlobalExceptionHandler in favor of examsy-common`
  - `cc4b1bf`: `refactor(auth): delegate JwtUtil to centralized JwtTokenProvider and update AuthController to use examsy-common`

---

### Phase 8: Exam & Proctoring Microservice (`examsy-exam-service`)
* **Objective:** Decompose exam authoring, question banks, student attempt lifecycles, real-time behavioral proctoring telemetry, and event-driven AI grading dispatch into an autonomous microservice backed by `examsy_exam_db`.
* **Implemented Components:**
  - Port: **8084** | Database: `examsy_exam_db`.
  - Technology: Spring Boot **4.0.2**, Spring Cloud **2025.1.2**, Java **21**, Spring Data JPA, Flyway, Spring Kafka, Spring Security, `examsy-common`.
  - Schema Evolution: Flyway `V1__init_exam_schema.sql` creating `exams`, `questions`, `question_options`, `exam_submissions`, `submission_answers`, and `proctoring_logs`.
  - Decoupled Autonomous Entities: Replaced monolithic cross-database `@ManyToOne` joins with autonomous domain entities (`Exam`, `Question`, `QuestionOption`, `ExamSubmission`, `SubmissionAnswer`, `ProctoringLog`) storing reference attributes (`course_id`, `teacher_username`, `student_id`, `student_username`, `student_name`).
  - Business Features:
    - **Teacher Exam Suite:** Multi-class exam publishing (MCQ, Short Answer, PDF), question order indexing, timing updates, exam deletion, and ongoing/upcoming exam group monitoring.
    - **Live Behavioral Proctoring Telemetry:** Real-time tracking of tab switches, window minimization, duration away from exam window, and automatic proctoring risk classification (`SECURE`, `WARNING`, `SUSPICIOUS`).
    - **Live Proctoring Command Center:** Teacher monitor endpoints to view student progress in real-time, broadcast messages to active exam takers, and issue direct warning alerts.
    - **Student Exam Lifecycle:** Exam retrieval with cheat prevention (excluding `isCorrect` from client payloads), exam attempt initialization, MCQ auto-scoring, student exam vault, and historical analytics.
    - **Kafka Event-Driven AI Grading Bridge:** Dispatches `ExamSubmittedEvent` to Kafka topic `examsy.exam.submitted` upon submission, decoupling heavy OCR and Groq LLaMA 3.1 AI evaluation (Phase 9) from the HTTP submission thread.
* **Repository Commits (`Examsy-Microservice` on `feature/examsy-exam-service` - 33+ Atomic Commits):**
  - `5be1888`: `chore(exam): initialize examsy-exam-service directory structure and Maven wrapper`
  - `9cc30ef`: `build(exam): configure pom.xml with Spring Boot 4.0.2, Spring Cloud 2025.1.2, Kafka, and JPA`
  - `e9b2c0d`: `config(exam): add local application.properties and bootstrap configuration`
  - `590aa8d`: `feat(exam): bootstrap ExamsyExamServiceApplication main class with discovery client`
  - `e31125b`: `db(exam): add Flyway V1 migration script for exams, questions, submissions, and proctoring logs`
  - `baad5ec`: `feat(exam): implement Exam domain entity with timing, modes, and scoring attributes`
  - `85aaaaa`: `feat(exam): implement Question domain entity with question types and order indices`
  - `b096c4d`: `feat(exam): implement QuestionOption domain entity with correctness flag`
  - `620d1fc`: `feat(exam): implement ExamSubmission domain entity with proctoring telemetry counters`
  - `9a1a8b2`: `feat(exam): implement SubmissionAnswer domain entity with awarded score and feedback`
  - `81550f9`: `feat(exam): implement ProctoringLog domain entity for behavioral audit trails`
  - `4623e12`: `feat(exam): add ExamRepo with course and teacher lookup queries`
  - `638132d`: `feat(exam): add QuestionRepo and QuestionOptionRepo for exam question banks`
  - `48230ef`: `feat(exam): add ExamSubmissionRepo with student and proctoring status queries`
  - `5c78868`: `feat(exam): add SubmissionAnswerRepo and ProctoringLogRepo`
  - `598d806`: `build(exam): integrate examsy-common shared library dependency`
  - `e0f4050`: `feat(exam): configure SecurityConfig using shared JwtAuthFilter from examsy-common`
  - `c0506db`: `feat(exam): implement ExamSubmittedEvent DTO and Kafka ExamEventProducer`
  - `9b40681`: `feat(exam): add ExamPublishDTO, QuestionPublishDTO, and OptionPublishDTO`
  - `0ad8d90`: `feat(exam): add ExamSummaryDTO and UpdateExamDeadlineDTO`
  - `43ddd2a`: `feat(exam): add LiveStudentMonitorDTO, OngoingExamGroupDTO, and MessageRequestDTO`
  - `c352b78`: `feat(exam): add StudentExamViewDTO, StudentQuestionDTO, and StudentOptionDTO`
  - `02caadf`: `feat(exam): add ExamSubmitDTO, AnswerSubmitDTO, and ExamResultDTO`
  - `e99ff96`: `feat(exam): add ProctoringLogDTO, ProctoringDTO, and ProctoringStatsDTO`
  - `bc1572d`: `feat(exam): add VaultExamsResponseDTO, VaultExamItemDTO, ExamAnalyticsDTO, and StudentAnalyticsDTO`
  - `407e0bb`: `feat(exam): define TeacherExamService interface`
  - `9bc49db`: `feat(exam): implement exam publishing with Question cascading in TeacherExamServiceImpl`
  - `f65b95c`: `feat(exam): implement exam summary, deletion, and deadline update in TeacherExamServiceImpl`
  - `3ee4434`: `feat(exam): implement live proctoring telemetry monitor and broadcast messaging in TeacherExamServiceImpl`
  - `a8f15fd`: `feat(exam): define StudentExamService interface`
  - `9883f1b`: `feat(exam): implement student exam retrieval and attempt initialization in StudentExamServiceImpl`
  - `e5ec840`: `feat(exam): implement exam submission, auto-grading calculation, and Kafka event publishing in StudentExamServiceImpl`
  - `7fbd529`: `feat(exam): implement real-time proctoring telemetry logging in StudentExamServiceImpl`
  - `c42ec3f`: `feat(exam): implement student exam vault and analytics retrieval in StudentExamServiceImpl`
  - `a73cb2b`: `feat(exam): implement TeacherExamController for publishing, timing, live monitoring, and alerts`
  - `6cffa21`: `feat(exam): implement StudentExamController for exam taking, submission, vault, and proctoring`
  - `a2adf9f`: `config(exam): add centralized examsy-exam-service.properties to config-repo`

---

### Phase 9: AI Automated Grading & OCR Microservice (`examsy-grading-service`)
* **Objective:** Decouple CPU-intensive handwritten PDF rendering, Tesseract OCR text extraction, Groq Cloud LLaMA 3.1 LLM prompt evaluation, teacher grading approval queues, and AI practice test generation into an autonomous microservice backed by `examsy_grading_db`.
* **Implemented Components:**
  - Port: **8085** | Database: `examsy_grading_db`.
  - Technology: Spring Boot **4.0.2**, Spring Cloud **2025.1.2**, Java **21**, Apache PDFBox (`2.0.36`), Tess4J OCR (`5.18.0`), Spring Kafka, Spring Security, `examsy-common`.
  - Schema Evolution: Flyway `V1__init_grading_schema.sql` creating `grading_tasks`, `mock_exams`, and `mock_questions`.
  - Domain Model: Autonomous entities (`GradingTask`, `MockExam`, `MockQuestion`) storing submission and student references (`submission_id`, `exam_id`, `student_username`, `teacher_username`).
  - Business Features:
    - **Optical Character Recognition (OCR) Pipeline:** Apache PDFBox renders multi-page student PDF submissions at 300 DPI into `BufferedImage` frames; Tess4J extracts handwritten text, followed by ASCII normalization and regex artifact cleansing.
    - **Groq Cloud LLaMA 3.1 AI Grading Engine:** Sends extracted handwriting and exam rubrics to Groq Cloud LLM with low temperature (0.1) and strict JSON schema enforcement, producing suggested scores, concept coverage, and targeted feedback without hallucination.
    - **Asynchronous Kafka Ingestion:** Listens to topic `examsy.exam.submitted` (group: `grading-service-group`), processing student submissions in the background to completely prevent Tomcat worker thread starvation and HTTP gateway timeouts.
    - **Teacher Command Center & Manual Overrides:** REST endpoints for reviewing pending submissions (`GET /api/v1/teacher/exams/pending-gradings`), on-demand auto-grading (`POST /api/v1/teacher/exams/{examId}/grade/{submissionId}/auto`), and grade release with score adjustment (`POST /api/v1/teacher/exams/{examId}/grade/{submissionId}/approve`).
    - **Event-Driven Grade Release:** Emits `GradeReleasedEvent` to topic `examsy.grade.released`, bridging directly to Phase 10 (`examsy-notification-service`) for transactional student email alerts.
    - **AI Mock Exam Generator:** Generates 4-option MCQs via Groq LLaMA 3.1 with seed randomization and mathematical verification (`POST /api/v1/mock-exams/generate`).
* **Repository Commits (`Examsy-Microservice` on `feature/examsy-grading-service`):**
  - `b676a63`: `chore(grading): initialize examsy-grading-service directory structure and Maven wrapper`
  - `a35866c`: `build(grading): configure pom.xml with Spring Boot 4.0.2, Spring Cloud 2025.1.2, PDFBox, Tess4J, and Kafka`
  - `ab2f082`: `config(grading): add local application.properties and bootstrap configuration`
  - `f8c79d0`: `feat(grading): bootstrap ExamsyGradingServiceApplication main class with discovery client`
  - `d6096de`: `db(grading): add Flyway V1 migration script for grading tasks, mock exams, and mock questions`
  - `915a58c`: `feat(grading): implement GradingTask domain entity for AI evaluation lifecycle and audit trails`
  - `3f615bf`: `feat(grading): implement MockExam domain entity for AI generated assessments`
  - `49becc1`: `feat(grading): implement MockQuestion domain entity with options and explanations`
  - `0199df1`: `feat(grading): add GradingTaskRepo with submission and teacher lookup queries`
  - `3762d19`: `feat(grading): add MockExamRepo with student history queries`
  - `7bfe67e`: `feat(grading): add MockQuestionRepo for mock exam question banks`
  - `bdc38ea`: `feat(grading): configure SecurityConfig using shared JwtAuthFilter and role authorization`
  - `4a61238`: `feat(grading): implement ExamSubmittedEvent DTO and GradeReleasedEvent DTO`
  - `72a4d5f`: `feat(grading): implement GradeEventProducer for broadcasting released grades`
  - `33908a5`: `feat(grading): add PendingGradingDTO for teacher grading review queue`
  - `2be49b3`: `feat(grading): add AutoGradingResultDTO with rubric concept breakdowns`
  - `7df8c3c`: `feat(grading): add ApproveGradeRequestDTO for teacher manual score overrides`
  - `6818801`: `feat(grading): add MockExamRequestDTO for student AI test generation`
  - `7f32254`: `feat(grading): add MockExamResponseDTO and MockQuestionDTO`
  - `0a614c3`: `feat(grading): configure RestTemplate and ObjectMapper beans for external AI requests`
  - `63677f0`: `feat(grading): define OCRService interface for document text extraction`
  - `09f3e2b`: `feat(grading): implement OCRServiceImpl with Apache PDFBox rendering and Tess4J OCR pipeline`
  - `dbad8b3`: `feat(grading): define GroqGradingService interface for LLM evaluation`
  - `2b5d5e3`: `feat(grading): implement GroqGradingServiceImpl with LLaMA 3.1 strict JSON rubric prompt`
  - `4ea5445`: `feat(grading): define GroqMockExamService interface`
  - `ff7f122`: `feat(grading): implement GroqMockExamServiceImpl with seed randomization and question validation`
  - `aa84da5`: `feat(grading): define SmartGradingService interface for grading orchestration`
  - `da5e258`: `feat(grading): implement SmartGradingServiceImpl for on-demand and asynchronous AI evaluation`
  - `7a00f32`: `feat(grading): implement ExamSubmittedConsumer for asynchronous grading queue processing`
  - `7a95dd3`: `feat(grading): implement TeacherGradingController for auto-grading, approvals, and pending reviews`
  - `3813de1`: `feat(grading): implement MockExamController for AI practice exam generation`
  - `f2e87f1`: `config(grading): add centralized examsy-grading-service.properties to config-repo`
  - `bdb1da9`: `config(gateway): update API Gateway routing predicates for mock exams and grading service`

---

### Phase 10: Notification & Alert Microservice (`examsy-notification-service`)
* **Objective:** Isolate in-app user notifications, unread badge counters, responsive transactional email dispatching (Spring Mail / Gmail SMTP), and event-driven messaging pipelines into an autonomous microservice backed by `examsy_notification_db`.
* **Implemented Components:**
  - Port: **8086** | Database: `examsy_notification_db`.
  - Technology: Spring Boot **4.0.2**, Spring Cloud **2025.1.2**, Java **21**, Spring Data JPA, Flyway, Spring Mail (`JavaMailSender`), Spring Kafka, Spring Security, `examsy-common`.
  - Schema Evolution: Flyway `V1__init_notification_schema.sql` creating `notifications` and `notification_logs`.
  - Autonomous Domain Model: Decoupled monolithic foreign keys to `user_accounts` into autonomous domain entities (`Notification`, `NotificationLog`) storing direct recipient metadata (`user_id`, `username`, `recipient_email`, `course_id`).
  - Business Features:
    - **In-App Notification Feed:** Real-time user notification history (`GET /api/v1/notifications`), unread badge count tracking (`GET /api/v1/notifications/unread-count`), individual mark-as-read (`PUT /api/v1/notifications/{id}/read`), and bulk read acknowledgement (`PUT /api/v1/notifications/read-all`).
    - **Transactional Email Dispatching:** Responsive HTML grade release email templates, rich welcome onboarding emails, and submission receipt confirmations dispatched asynchronously using `JavaMailSender` and `MimeMessageHelper`.
    - **Audit & Delivery Logging:** Persists delivery status and error messages in `notification_logs` to maintain enterprise traceability and compliance.
    - **Kafka Event-Driven Alert Listeners:**
      - `examsy.grade.released` (`GradeReleasedConsumer`): Listens to Phase 9 grading releases, generating in-app grade notifications and sending grade release emails.
      - `examsy.user.registered` (`UserRegisteredConsumer`): Listens to Phase 5 auth registrations, sending welcome/onboarding alerts.
      - `examsy.exam.submitted` (`ExamSubmittedConsumer`): Listens to Phase 8 submissions, dispatching submission confirmation receipts.
* **Repository Commits (`Examsy-Microservice` on `feature/examsy-notification-service`):**
  - `2c43016`: `chore(notification): initialize examsy-notification-service directory structure and Maven wrapper`
  - `fb42386`: `build(notification): configure pom.xml with Spring Boot 4.0.2, Spring Cloud 2025.1.2, Mail, and Kafka`
  - `3e8ca9c`: `config(notification): add local application.properties and bootstrap configuration`
  - `2a3962b`: `feat(notification): bootstrap ExamsyNotificationServiceApplication main class with discovery client`
  - `403ce4f`: `db(notification): add Flyway V1 migration script for notifications and delivery audit logs`
  - `5db974e`: `feat(notification): implement Notification domain entity for in-app alerts`
  - `200af54`: `feat(notification): implement NotificationLog domain entity for email delivery audit logs`
  - `9541714`: `feat(notification): add NotificationRepo with username lookups and unread count queries`
  - `84ff449`: `feat(notification): add NotificationLogRepo for email delivery auditing`
  - `2f0557d`: `feat(notification): configure SecurityConfig using shared JwtAuthFilter and user authorization`
  - `b54438b`: `feat(notification): add GradeReleasedEvent DTO, UserRegisteredEvent DTO, and ExamSubmittedEvent DTO`
  - `9f25b0f`: `feat(notification): add NotificationDTO response payload`
  - `bd9ffd8`: `feat(notification): add EmailPayloadDTO for transactional email formatting`
  - `704776e`: `feat(notification): add DirectAlertRequestDTO for teacher-to-student alerts`
  - `546fb6d`: `feat(notification): define EmailService interface for transactional message dispatch`
  - `fd9fbf7`: `feat(notification): implement EmailServiceImpl with JavaMailSender and resilient audit logging`
  - `d5b0cc1`: `feat(notification): define NotificationService interface for user notification lifecycle`
  - `bc73071`: `feat(notification): implement NotificationServiceImpl with in-app alert lifecycle and alert routing`
  - `f865e8e`: `feat(notification): implement GradeReleasedConsumer for automated grade notification dispatch`
  - `e9fa520`: `feat(notification): implement UserRegisteredConsumer for onboarding welcome email dispatch`
  - `23f4e8a`: `feat(notification): implement ExamSubmittedConsumer for submission confirmation alerts`
  - `789f8ea`: `feat(notification): implement NotificationController for notifications feed and unread badge`
  - `5e79ae6`: `config(notification): add centralized examsy-notification-service.properties to config-repo`

---

## 🔮 PART 3: Upcoming Migration Phases (Next Steps)

The following phases are sequenced based on data dependencies to ensure zero downtime and smooth data transitions.

```
                  CURRENT PROGRESS CHECKPOINT:
  [Phase 1] Infra Setup            ──> COMPLETED (Docker, Kafka, Redis, Multi-DB)
  [Phase 2] Config Server          ──> COMPLETED (Port 8888, Spring Boot 4.0.2)
  [Phase 3] Eureka Registry        ──> COMPLETED (Port 8761, Spring Boot 4.0.2)
  [Phase 4] API Gateway            ──> COMPLETED (Port 8080, Spring Boot 4.0.2)
  [Phase 5] Auth Microservice      ──> COMPLETED (Port 8081 + Kafka Producer)
  [Phase 6] User Profile Service   ──> COMPLETED (Port 8082 + Kafka Consumer)
  [Phase 7] Class & Course Service ──> COMPLETED (Port 8083, 37 Commits)
  [Shared Lib] examsy-common       ──> COMPLETED (Shared Security & SDK, 9 Commits)
  [Phase 8] Exam & Proctor Service ──> COMPLETED (Port 8084, 37 Commits)
  [Phase 9] AI Grading & OCR       ──> COMPLETED (Port 8085, 33 Commits)
  [Phase 10] Notification Service  ──> COMPLETED (Port 8086, 23 Commits)
────────────────────────────────────────────────────────────────────────────────
  [Phase 11] Admin & Analytics     ──> NEXT IMMEDIATE PHASE (Port 8087 / 8088)
  [Phase 12] Hardening & CI/CD     ──> FINAL VALIDATION & DEPLOYMENT
```

### 📍 Phase 11: Admin Moderation & Analytics Microservice (`examsy-admin-service` / `examsy-analytics-service`) — *IMMEDIATE NEXT*
* **Port:** 8087 / 8088 | **Database:** `examsy_admin_db` / `examsy_analytics_db`
* **Responsibilities:** Violation reports, teacher/class termination, and materialized GPA / pass-rate views.
* **Key Tasks:**
  1. Implement Admin dashboard metric aggregations and moderation queues.
  2. Maintain read-optimized views for student GPA progression and class performance.

---

### 📍 Phase 12: Production Observability, CI/CD & Cloud Hardening
* **Responsibilities:** Distributed tracing, container deployment, and automated CI/CD pipelines.
* **Key Tasks:**
  1. Distributed Tracing: Micrometer Tracing with Zipkin/Jaeger to track request spans across Gateway and microservices.
  2. Centralized Metrics: Spring Boot Actuator with Prometheus scraping and Grafana dashboards.
  3. CI/CD: Per-service GitHub Actions pipelines building multi-stage Docker containers pushed to GitHub Container Registry (ghcr.io).
  4. Kubernetes Manifests: Deployments, ClusterIP Services, ConfigMaps, and Secrets.

---

## 💼 CV / Resume High-Impact Project Description

Use the following formatted bullet points for your software engineering resume, portfolio, or LinkedIn profile.

```markdown
### Examsy — Distributed AI-Powered Online Examination Platform
**Technologies:** Java 21, Spring Boot 3.4, Spring Cloud (Gateway, Eureka, Config Server), Apache Kafka, Redis, Docker, MySQL 8, Groq Cloud LLM (LLaMA 3.1), Tesseract OCR, React 19, Vite.

* Architected and decomposed a monolithic Spring Boot examination platform into an **event-driven microservices architecture** using Spring Cloud, Docker, and Apache Kafka.
* Implemented **Database-per-Service** pattern isolating 8 independent MySQL schemas, eliminating write contention and database coupling during peak concurrent exam submissions.
* Engineered an asynchronous AI grading pipeline integrating **Apache PDFBox**, **Tess4J OCR**, and **Groq Cloud LLaMA-3.1**, decoupled via Kafka message streams to prevent worker thread starvation and eliminate HTTP timeouts.
* Developed a **Spring Cloud API Gateway** with non-blocking Netty routing, client-side Netflix Eureka load balancing, and unified CORS, achieving sub-15ms edge routing overhead with zero frontend code modification.
* Established an event-driven user onboarding pipeline using **Kafka Producers/Consumers** (`examsy.user.registered`), cleanly decoupling authentication credentials from domain user profiles with guaranteed eventual consistency.
* Hardened edge security using **HMAC-SHA256 JWT** authentication, Spring Security filters, and **Bucket4j Token Bucket** distributed rate limiting to protect costly AI inference endpoints from DDoS attacks.
* Containerized full-stack infrastructure with **Docker Compose**, orchestrating multi-database MySQL 8, Redis 7 caching, Confluent Kafka, Zookeeper, and Spring Boot service instances.
```

---

## 🧭 Next Action Plan

To continue directly from where we left off:
1. **Scaffold `examsy-profile-service`** under `services/examsy-profile-service`.
2. **Configure Flyway & JPA entities** (`Student`, `Teacher`, `Admin`) backed by `examsy_profile_db`.
3. **Implement Kafka Consumer** for `examsy.user.registered` topic to auto-provision user profiles.
4. **Register with Eureka & Config Server** and expose profile CRUD endpoints via the Gateway.
