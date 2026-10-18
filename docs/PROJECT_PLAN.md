# AgroDoc: AI-Powered Tomato Leaf Disease Detection and Remedy Recommendation System
## Comprehensive Project Plan — Phase 1: Architecture & Design

> **Document Type:** Planning Document (Phase 1)
> **Version:** 1.0
> **Date:** October 2026
> **Author:** Final-Year CSE Project

---

## Table of Contents

1. [Final Project Overview](#1-final-project-overview)
2. [Problem Statement and Objectives](#2-problem-statement-and-objectives)
3. [Functional and Non-Functional Requirements](#3-functional-and-non-functional-requirements)
4. [Complete System Architecture](#4-complete-system-architecture)
5. [Module Descriptions](#5-module-descriptions)
6. [Database Schema and Relationships](#6-database-schema-and-relationships)
7. [Frontend Page List and Navigation Flow](#7-frontend-page-list-and-navigation-flow)
8. [Repository Folder Structure](#8-repository-folder-structure)
9. [Security and Validation Plan](#9-security-and-validation-plan)
10. [Testing Strategy](#10-testing-strategy)
11. [Development Roadmap](#11-development-roadmap)
12. [External AI API Selection Criteria](#12-external-ai-api-selection-criteria)
13. [Assumptions and Unresolved Decisions](#13-assumptions-and-unresolved-decisions)
14. [Phase 2 Task List](#14-phase-2-task-list)

---

## 1. Final Project Overview

**Project Name:** AgroDoc — AI-Powered Tomato Leaf Disease Detection and Remedy Recommendation System

**Project Category:** Agricultural Technology (AgriTech) / Artificial Intelligence Web Application

**Target Users:**
- Smallholder tomato farmers
- Agricultural extension officers
- Agronomists and plant health researchers
- Agricultural students and educators

**Summary:**
AgroDoc is a full-stack web application that enables users to upload tomato leaf images and receive instant AI-powered disease diagnosis, detailed symptom descriptions, and curated organic and chemical treatment recommendations. The platform maintains a personal scan history per user and provides a disease information library accessible without uploading images. The system is built using a modern Java Spring Boot backend, a plain HTML/CSS/JavaScript frontend, and a MySQL relational database, integrating with an external plant disease detection REST API.

**Scope:**
- Disease detection is limited to tomato leaf diseases supported by the chosen external AI API.
- The application does not perform AI inference locally; all predictions are delegated to an external API.
- The system is designed for single-user sessions (no multi-tenancy or role-based admin panel in Phase 1).

---

## 2. Problem Statement and Objectives

### 2.1 Problem Statement

Tomato is one of the most economically important vegetable crops globally. Diseases such as Early Blight, Late Blight, Leaf Mold, Septoria Leaf Spot, and Tomato Yellow Leaf Curl Virus cause significant yield losses — estimated at 20–40% annually in affected regions. Most smallholder farmers lack access to agronomists or timely diagnostic services. Misidentification of diseases leads to incorrect or excessive pesticide use, increasing costs and environmental harm.

There is a critical need for an accessible, low-cost, AI-assisted tool that:
- Rapidly identifies tomato leaf diseases from photographs
- Provides science-backed treatment recommendations
- Stores diagnostic history for trend awareness
- Requires no specialized agricultural training from the user

### 2.2 Project Objectives

**Primary Objectives:**
1. Develop a secure, responsive web application for tomato disease detection using image upload.
2. Integrate with a compatible external plant disease AI REST API for accurate disease classification.
3. Provide detailed disease symptom descriptions and both organic and chemical remedy recommendations.
4. Maintain a persistent, user-specific scan history with timestamps and results.
5. Build a searchable disease information library as a standalone educational resource.

**Secondary Objectives:**
1. Implement user authentication with secure password hashing.
2. Apply input validation, error handling, and safe API communication on both client and server.
3. Design a professional, mobile-responsive UI matching an AgriTech SaaS product standard.
4. Follow a clean, maintainable layered architecture suitable for future extension.
5. Document the project thoroughly for academic submission and GitHub publication.

---

## 3. Functional and Non-Functional Requirements

### 3.1 Functional Requirements

#### Authentication Module
- FR-01: Users can register with a full name, email address, and password.
- FR-02: Passwords must be hashed using BCrypt before storage.
- FR-03: Users can log in using their registered email and password.
- FR-04: Authenticated sessions must be maintained with a JWT token stored in localStorage or as an HttpOnly cookie.
- FR-05: Users can log out, which invalidates their session token.

#### Image Upload and Disease Detection
- FR-06: Authenticated users can upload a tomato leaf image (JPEG, PNG, WebP) from their device.
- FR-07: The frontend must preview the uploaded image before submission.
- FR-08: The backend sends the uploaded image to the external AI API and processes the response.
- FR-09: The application displays the detected disease name, confidence score, and a "healthy/diseased" status indicator.
- FR-10: If the AI API returns no result or an error, a clear, user-friendly error message is shown.
- FR-11: Each successful scan result is automatically saved to the user's scan history.

#### Disease Result and Information
- FR-12: The result page displays the disease name, confidence percentage, a symptom description, and recommended remedies.
- FR-13: Remedy recommendations include both organic and chemical options.
- FR-14: The result page links to the full disease entry in the Disease Information Library.

#### Disease Information Library
- FR-15: The library lists all tomato diseases stored in the database.
- FR-16: Users can search the library by disease name or keyword.
- FR-17: Each disease entry includes: name, scientific name, symptoms, causes, organic remedies, chemical remedies, and prevention tips.

#### Scan History
- FR-18: Users can view a chronological list of all their past scans.
- FR-19: Each history entry shows the scan date, uploaded image thumbnail, detected disease, and confidence score.
- FR-20: Users can click a history entry to revisit the full result and recommendations.
- FR-21: Users can delete individual scan history entries.

#### User Settings
- FR-22: Users can update their display name.
- FR-23: Users can change their password (requires current password verification).

#### Dashboard
- FR-24: The dashboard displays a summary of total scans, most frequently detected disease, and last scan date.
- FR-25: The dashboard shows the three most recent scan results.

### 3.2 Non-Functional Requirements

| ID     | Category        | Requirement                                                                      |
|--------|-----------------|----------------------------------------------------------------------------------|
| NFR-01 | Performance     | Disease detection results must display within 8 seconds of image submission.    |
| NFR-02 | Availability    | The application must function correctly on Chrome, Firefox, Safari, and Edge.   |
| NFR-03 | Responsiveness  | All pages must be fully usable on screens from 360px to 1920px wide.            |
| NFR-04 | Security        | All API endpoints (except login/register) must require a valid JWT token.       |
| NFR-05 | Security        | Uploaded files must be validated for MIME type and size (max 5 MB).             |
| NFR-06 | Security        | API keys and database credentials must never be committed to version control.   |
| NFR-07 | Maintainability | Backend must follow a strict layered architecture pattern.                       |
| NFR-08 | Scalability     | The database schema must support future expansion to other crop types.           |
| NFR-09 | Usability       | The UI must pass basic WCAG 2.1 Level AA accessibility checks.                  |
| NFR-10 | Reliability     | Failed API calls must not crash the application; graceful fallback is required. |

---

## 4. Complete System Architecture

### 4.1 High-Level Architecture Diagram

```
+-------------------------------------------------------------------------+
|                        CLIENT LAYER (Browser)                           |
|  HTML5 + CSS3 + Vanilla JavaScript + Bootstrap 5 + Lucide Icons         |
|  Pages: Landing, Auth, Dashboard, Upload, Results, Library, History     |
+-----------------------------+-------------------------------------------+
                              | HTTP REST (JSON / multipart form)
                              v
+-------------------------------------------------------------------------+
|                     APPLICATION LAYER (Spring Boot)                     |
|                                                                         |
|  +--------------+  +--------------+  +--------------+  +------------+  |
|  |  Controllers  |  |   Services   |  | Repositories |  |  Entities  |  |
|  |  (REST APIs) |  | (Biz Logic)  |  |  (JPA/Data)  |  |  (Models)  |  |
|  +--------------+  +--------------+  +--------------+  +------------+  |
|                                                                         |
|  +--------------+  +--------------+  +--------------+                  |
|  |    DTOs      |  | Config Layer |  |  Exception   |                  |
|  |  (Request/   |  | (Security,   |  |  Handling    |                  |
|  |   Response)  |  |  CORS, Env)  |  |  (Global)    |                  |
|  +--------------+  +--------------+  +--------------+                  |
|                                                                         |
|  Spring Security (JWT) | Spring Data JPA | Hibernate ORM               |
+-------------------+-------------------------------+---------------------+
                    |                               | HTTP (REST)
                    v                               v
+----------------------+       +--------------------------------------+
|     MySQL Database   |       |       External Plant Disease AI API  |
|  (Users, Diseases,   |       |  (Image upload -> Disease name +      |
|  Scan History,       |       |   confidence score response)         |
|  Feedback)           |       +--------------------------------------+
+----------------------+
```

### 4.2 Backend Layer Descriptions

| Layer         | Responsibility                                                                   |
|---------------|----------------------------------------------------------------------------------|
| **Controller**  | Receives HTTP requests, validates input DTOs, delegates to Service layer.      |
| **Service**     | Contains all business logic, calls Repository and external API client.         |
| **Repository**  | Spring Data JPA interfaces for all database CRUD operations.                   |
| **Entity**      | JPA-annotated Java classes mapped to MySQL tables.                             |
| **DTO**         | Plain Java objects (Records or Classes) for request/response data transfer.    |
| **Config**      | Spring Security config, CORS config, environment variable loading, JWT config. |
| **Exception**   | @ControllerAdvice global exception handler returning structured error JSON.    |

### 4.3 Communication Flow — Disease Detection

```
User uploads image (browser)
        |
        v
Frontend JS sends multipart POST to /api/scan/upload (with JWT header)
        |
        v
ScanController receives request -> validates file -> calls ScanService
        |
        v
ScanService calls external AI API (HTTP multipart POST with API key)
        |
        v
API returns: { disease_name, confidence, ... }
        |
        v
ScanService looks up DiseaseInfo from DB by disease_name
        |
        v
ScanService saves ScanHistory record (userId, imageRef, result, timestamp)
        |
        v
ScanController returns ScanResultDTO to frontend (JSON)
        |
        v
Frontend JS renders result page with disease info and recommendations
```

### 4.4 Technology Stack Summary

| Area                 | Technology                              | Version Target  |
|----------------------|-----------------------------------------|-----------------|
| Frontend             | HTML5, CSS3, JavaScript (ES6+)          | Modern browsers |
| UI Framework         | Bootstrap 5                             | 5.3.x           |
| Icons                | Lucide Icons                            | Latest CDN      |
| Fonts                | Google Fonts — Inter                    | Latest CDN      |
| Backend              | Java + Spring Boot                      | Java 17, SB 3.x |
| ORM                  | Spring Data JPA + Hibernate             | Latest with SB  |
| Database             | MySQL                                   | 8.x             |
| Build Tool           | Apache Maven                            | 3.9.x           |
| Security             | Spring Security + JWT (jjwt)            | Latest          |
| HTTP Client (API)    | Spring RestTemplate or WebClient        | With Spring SB  |
| Version Control      | Git + GitHub                            | —               |

---

## 5. Module Descriptions

### Module 1 — Landing Page

**Purpose:** Marketing and entry point for unauthenticated visitors.

**Content:**
- Hero section with headline, sub-headline, and "Get Started" / "Learn More" CTAs
- How It Works section (3-step visual: Upload > Detect > Treat)
- Key features grid (AI Detection, Disease Library, Scan History, Recommendations)
- Supported disease list preview
- Testimonials or usage statistics (static/demonstration)
- Footer with links (About, GitHub, Privacy Policy placeholder)

**Navigation:** Links to Login and Register pages. If user is already authenticated, redirect to Dashboard.

---

### Module 2 — User Registration and Login

**Registration Page:**
- Fields: Full Name, Email, Password, Confirm Password
- Client-side validation before submission
- POST to `/api/auth/register`
- On success, redirect to Login with a success banner

**Login Page:**
- Fields: Email, Password
- "Remember Me" toggle (extends JWT expiry)
- POST to `/api/auth/login`
- On success, store JWT, redirect to Dashboard
- Show error message on invalid credentials

---

### Module 3 — User Dashboard

**Purpose:** Personalized home screen after login.

**Content:**
- Welcome banner with user's name
- Stats cards: Total Scans, Diseases Detected, Last Scan Date
- Quick action buttons: "New Scan", "View History", "Browse Library"
- Recent Scans section: last 3 scan cards (disease, date, confidence)
- Disease of the Week (educational, pulled from DB — static selection)

---

### Module 4 — Tomato Leaf Image Upload

**Purpose:** Core action page for initiating a disease scan.

**Content:**
- Drag-and-drop upload zone with click-to-browse fallback
- Image preview before submission
- File validation feedback (type, size limits)
- Upload instructions and tips (good lighting, close-up, leaf only)
- Submit button triggers POST to `/api/scan/upload`
- Loading spinner shown during API processing
- Result auto-displayed on the same page or redirected to the Result page

---

### Module 5 — AI Disease Detection (Backend Module)

**Purpose:** Backend orchestration of the AI prediction flow.

**Key Operations:**
- Receive and validate image in `ScanController`
- Forward image to external AI API from `ScanService`
- Parse API response (disease name, confidence)
- Query `DiseaseInfoRepository` to retrieve full disease data
- Save `ScanHistory` entity with result, timestamp, and user ID
- Return `ScanResultDTO` to frontend

**Failure Handling:**
- API timeout → return HTTP 504 with user-friendly message
- API returns unknown disease not in DB → show raw API result with disclaimer
- API key invalid → return HTTP 503, log internally, never expose key to client

---

### Module 6 — Disease Result Page

**Purpose:** Display full diagnostic result after a scan.

**Content:**
- Uploaded image thumbnail
- Disease name (prominent) with confidence percentage and severity badge
- Healthy vs. Diseased indicator with color coding
- Symptom description
- Organic remedy recommendations (expandable list)
- Chemical remedy recommendations (expandable list, with safety warning)
- Prevention tips
- "Save to History" note (auto-saved, shown as confirmation)
- Link back to Library entry for full detail

---

### Module 7 — Disease Information Library

**Purpose:** Educational standalone reference; no image upload required.

**Content:**
- Search bar and filter (by severity, type)
- Disease cards grid: name, image, short description, "View Details" button
- Disease Detail page: full entry with scientific name, symptoms, all remedies, prevention

**Data Source:** Entirely from the `disease_info` MySQL table (seeded manually).

---

### Module 8 — Organic and Chemical Remedy Recommendations

**Purpose:** Dedicated recommendation content component embedded in results and library.

**Design:**
- Tabbed layout: Organic | Chemical
- Each remedy listed with: product/method name, application method, dosage/rate, frequency
- Chemical tab includes a disclaimer about safe handling and regulations
- Organic tab emphasizes eco-friendly and availability

**Data Source:** `disease_remedies` table, linked to `disease_info` by foreign key.

---

### Module 9 — Scan History

**Purpose:** Personal history of all past disease scans for the logged-in user.

**Content:**
- Chronological list (newest first) with pagination
- Each entry: image thumbnail, disease name, confidence, date/time, "View Details" button, "Delete" button
- Clicking "View Details" re-renders the full Result page for that scan
- Confirmation dialog before deletion

**Security:** Backend enforces that `userId` in JWT matches `userId` on the requested history records.

---

### Module 10 — User Settings and Logout

**Settings Page:**
- Update display name (PATCH `/api/users/me`)
- Change password (POST `/api/users/me/change-password`) — requires current password
- Account info (email — read-only)
- Logout button: clears JWT from client storage, redirects to Landing

---

### Module 11 — Input Validation and Error Handling

**Client-Side Validation:**
- Required field checks before form submission
- Email format regex validation
- Password minimum 8 characters, must include letters and numbers
- Image file type and size validation before upload
- Inline error messages beneath form fields

**Server-Side Validation:**
- Spring Validation (`@Valid`, `@NotBlank`, `@Email`, `@Size`)
- Custom validators for image MIME type checking
- Global `@ControllerAdvice` returning structured JSON errors

---

## 6. Database Schema and Relationships

### 6.1 Entity Relationship Overview

```
users ─────────────────── scan_history
  |  (1 user : many scans)      |
  |                             | (each scan references one disease_info entry)
  |                        disease_info ────── disease_remedies
  |                             |               (1 disease : many remedies)
  └─── user_feedback            |
       (1 user : many feedback  |
        entries, optional link  |
        to scan_history)        |
```

### 6.2 Table Definitions

#### Table: `users`

| Column          | Type           | Constraints                         | Description                       |
|-----------------|----------------|-------------------------------------|-----------------------------------|
| `id`            | BIGINT         | PK, AUTO_INCREMENT                  | Unique user identifier            |
| `full_name`     | VARCHAR(100)   | NOT NULL                            | User's display name               |
| `email`         | VARCHAR(255)   | UNIQUE, NOT NULL                    | Login email                       |
| `password_hash` | VARCHAR(255)   | NOT NULL                            | BCrypt hashed password            |
| `created_at`    | DATETIME       | NOT NULL, DEFAULT CURRENT_TIMESTAMP | Account creation timestamp        |
| `updated_at`    | DATETIME       | NOT NULL, ON UPDATE CURRENT_TIMESTAMP | Last update timestamp           |
| `is_active`     | BOOLEAN        | DEFAULT TRUE                        | Soft-delete / account status flag |

---

#### Table: `disease_info`

| Column              | Type           | Constraints        | Description                              |
|---------------------|----------------|--------------------|------------------------------------------|
| `id`                | BIGINT         | PK, AUTO_INCREMENT | Unique disease identifier                |
| `disease_name`      | VARCHAR(100)   | UNIQUE, NOT NULL   | Canonical disease name (matches API key) |
| `scientific_name`   | VARCHAR(150)   | NULLABLE           | Scientific/Latin name                    |
| `description`       | TEXT           | NOT NULL           | General overview of the disease          |
| `symptoms`          | TEXT           | NOT NULL           | Observable symptoms description          |
| `causes`            | TEXT           | NOT NULL           | Causal agents (fungus, virus, bacteria)  |
| `prevention_tips`   | TEXT           | NULLABLE           | Cultural/environmental prevention        |
| `severity_level`    | ENUM           | ('LOW','MED','HIGH') | Default severity classification        |
| `image_url`         | VARCHAR(500)   | NULLABLE           | Reference image for library display      |
| `created_at`        | DATETIME       | DEFAULT NOW()      | Record creation timestamp                |

---

#### Table: `disease_remedies`

| Column          | Type           | Constraints                       | Description                              |
|-----------------|----------------|-----------------------------------|------------------------------------------|
| `id`            | BIGINT         | PK, AUTO_INCREMENT                | Unique remedy identifier                 |
| `disease_id`    | BIGINT         | FK -> `disease_info(id)`, NOT NULL | Associated disease                      |
| `remedy_type`   | ENUM           | ('ORGANIC','CHEMICAL'), NOT NULL  | Remedy category                          |
| `remedy_name`   | VARCHAR(200)   | NOT NULL                          | Name of product or method                |
| `application`   | TEXT           | NOT NULL                          | How to apply (method, rate, frequency)   |
| `notes`         | TEXT           | NULLABLE                          | Safety warnings, sourcing notes          |
| `display_order` | INT            | DEFAULT 1                         | Display ordering within a disease        |

---

#### Table: `scan_history`

| Column            | Type           | Constraints                        | Description                              |
|-------------------|----------------|------------------------------------|------------------------------------------|
| `id`              | BIGINT         | PK, AUTO_INCREMENT                 | Unique scan record identifier            |
| `user_id`         | BIGINT         | FK -> `users(id)`, NOT NULL        | Owning user                              |
| `disease_id`      | BIGINT         | FK -> `disease_info(id)`, NULLABLE | Detected disease (null if unrecognized)  |
| `image_filename`  | VARCHAR(500)   | NOT NULL                           | Stored filename / path of the upload     |
| `raw_api_result`  | JSON           | NULLABLE                           | Full raw API response (for debugging)    |
| `disease_label`   | VARCHAR(150)   | NOT NULL                           | Disease name as returned by API          |
| `confidence_score`| DECIMAL(5,2)   | NULLABLE                           | Confidence % from AI API                 |
| `is_healthy`      | BOOLEAN        | DEFAULT FALSE                      | True if API returns "Healthy" prediction |
| `scanned_at`      | DATETIME       | DEFAULT NOW()                      | Timestamp of the scan                    |

---

#### Table: `user_feedback`

| Column        | Type           | Constraints                        | Description                          |
|---------------|----------------|------------------------------------|--------------------------------------|
| `id`          | BIGINT         | PK, AUTO_INCREMENT                 | Unique feedback identifier           |
| `user_id`     | BIGINT         | FK -> `users(id)`, NOT NULL        | User who submitted feedback          |
| `scan_id`     | BIGINT         | FK -> `scan_history(id)`, NULLABLE | Optional link to a specific scan     |
| `rating`      | TINYINT        | CHECK (1–5)                        | Star rating                          |
| `comments`    | TEXT           | NULLABLE                           | Free-text feedback                   |
| `submitted_at`| DATETIME       | DEFAULT NOW()                      | Submission timestamp                 |

---

### 6.3 Security: User-Specific Scan History

- Every request to `/api/scan/history` extracts the authenticated user's ID from the validated JWT.
- The `ScanHistoryRepository` query always filters by `userId`:
  ```java
  List<ScanHistory> findByUserIdOrderByScannedAtDesc(Long userId);
  ```
- The `ScanService` verifies that the `userId` on any retrieved `ScanHistory` record matches the JWT-authenticated user before returning data.
- No direct ID enumeration is possible: a user requesting `/api/scan/history/42` where record 42 belongs to another user will receive HTTP 403.

---

## 7. Frontend Page List and Navigation Flow

### 7.1 Page List

| # | Page Name              | URL Path              | Auth Required | Description                         |
|---|------------------------|-----------------------|---------------|-------------------------------------|
| 1 | Landing Page           | `/`                   | No            | Marketing and entry                 |
| 2 | Register               | `/register.html`      | No            | New user sign-up                    |
| 3 | Login                  | `/login.html`         | No            | User login                          |
| 4 | Dashboard              | `/dashboard.html`     | Yes           | User home, stats, quick actions     |
| 5 | Upload / Scan          | `/scan.html`          | Yes           | Image upload and detection          |
| 6 | Scan Result            | `/result.html`        | Yes           | Disease result and recommendations  |
| 7 | Disease Library        | `/library.html`       | No            | Browse all disease entries          |
| 8 | Disease Detail         | `/disease.html?id=X`  | No            | Single disease full detail          |
| 9 | Scan History           | `/history.html`       | Yes           | User's past scans                   |
| 10 | Settings              | `/settings.html`      | Yes           | Update profile and password         |
| 11 | 404 / Error           | `/404.html`           | No            | Friendly error page                 |

### 7.2 Navigation Flow

```
[Landing]
    +-- [Register] -> success -> [Login]
    +-- [Login] -> success -> [Dashboard]
    +-- [Disease Library] -> [Disease Detail] (public)

[Dashboard] (authenticated)
    +-- [Scan] -> [Result] -> [Disease Detail]
    |                     +-- auto-save -> [History]
    +-- [History] -> [Result] (view old result)
    +-- [Library] -> [Disease Detail]
    +-- [Settings] -> Logout -> [Landing]
```

### 7.3 Navigation Bar (Authenticated)

```
[AgroDoc Logo]   Dashboard | New Scan | Library | History | Settings   [User Avatar v]
                                                                         +-- My Profile
                                                                         +-- Logout
```

### 7.4 Navigation Bar (Unauthenticated)

```
[AgroDoc Logo]   Features | Disease Library | How It Works   [Login]  [Get Started ->]
```

---

## 8. Repository Folder Structure

```
AgroDoc/
|
+-- .github/
|   +-- ISSUE_TEMPLATE/
|       +-- bug_report.md
|
+-- backend/                              # Spring Boot Maven project
|   +-- src/
|   |   +-- main/
|   |   |   +-- java/com/agrodoc/
|   |   |   |   +-- AgroDocApplication.java
|   |   |   |   +-- config/
|   |   |   |   |   +-- SecurityConfig.java
|   |   |   |   |   +-- CorsConfig.java
|   |   |   |   |   +-- AppConfig.java
|   |   |   |   +-- controller/
|   |   |   |   |   +-- AuthController.java
|   |   |   |   |   +-- ScanController.java
|   |   |   |   |   +-- DiseaseController.java
|   |   |   |   |   +-- HistoryController.java
|   |   |   |   |   +-- UserController.java
|   |   |   |   +-- service/
|   |   |   |   |   +-- AuthService.java
|   |   |   |   |   +-- ScanService.java
|   |   |   |   |   +-- DiseaseService.java
|   |   |   |   |   +-- HistoryService.java
|   |   |   |   |   +-- UserService.java
|   |   |   |   +-- repository/
|   |   |   |   |   +-- UserRepository.java
|   |   |   |   |   +-- ScanHistoryRepository.java
|   |   |   |   |   +-- DiseaseInfoRepository.java
|   |   |   |   |   +-- UserFeedbackRepository.java
|   |   |   |   +-- entity/
|   |   |   |   |   +-- User.java
|   |   |   |   |   +-- ScanHistory.java
|   |   |   |   |   +-- DiseaseInfo.java
|   |   |   |   |   +-- DiseaseRemedy.java
|   |   |   |   |   +-- UserFeedback.java
|   |   |   |   +-- dto/
|   |   |   |   |   +-- request/
|   |   |   |   |   |   +-- RegisterRequest.java
|   |   |   |   |   |   +-- LoginRequest.java
|   |   |   |   |   |   +-- ChangePasswordRequest.java
|   |   |   |   |   +-- response/
|   |   |   |   |       +-- AuthResponse.java
|   |   |   |   |       +-- ScanResultDTO.java
|   |   |   |   |       +-- DiseaseDTO.java
|   |   |   |   |       +-- HistoryItemDTO.java
|   |   |   |   +-- exception/
|   |   |   |   |   +-- GlobalExceptionHandler.java
|   |   |   |   |   +-- ResourceNotFoundException.java
|   |   |   |   |   +-- UnauthorizedException.java
|   |   |   |   |   +-- ApiIntegrationException.java
|   |   |   |   +-- util/
|   |   |   |       +-- JwtUtil.java
|   |   |   |       +-- FileStorageUtil.java
|   |   |   +-- resources/
|   |   |       +-- application.properties
|   |   |       +-- application-dev.properties  # gitignored
|   |   +-- test/
|   |       +-- java/com/agrodoc/
|   |           +-- controller/
|   |           +-- service/
|   +-- pom.xml
|
+-- frontend/                             # Static HTML/CSS/JS
|   +-- index.html
|   +-- login.html
|   +-- register.html
|   +-- dashboard.html
|   +-- scan.html
|   +-- result.html
|   +-- library.html
|   +-- disease.html
|   +-- history.html
|   +-- settings.html
|   +-- 404.html
|   +-- css/
|   |   +-- main.css
|   |   +-- landing.css
|   |   +-- auth.css
|   |   +-- dashboard.css
|   |   +-- scan.css
|   |   +-- library.css
|   +-- js/
|   |   +-- api.js
|   |   +-- auth.js
|   |   +-- landing.js
|   |   +-- dashboard.js
|   |   +-- scan.js
|   |   +-- result.js
|   |   +-- library.js
|   |   +-- history.js
|   |   +-- settings.js
|   +-- assets/
|       +-- images/
|       |   +-- logo.svg
|       |   +-- hero-tomato.jpg
|       |   +-- disease/
|       +-- icons/
|
+-- database/
|   +-- schema.sql
|   +-- seed_diseases.sql
|   +-- migrations/
|
+-- docs/
|   +-- PROJECT_PLAN.md                   # This file
|   +-- API_DOCUMENTATION.md
|   +-- SETUP_GUIDE.md
|   +-- screenshots/
|
+-- .gitignore
+-- README.md
```

### `.gitignore` Must Include:
```
# Environment and secrets
.env
application-dev.properties
*.env

# Build artifacts
backend/target/
*.class
*.jar

# IDE files
.idea/
*.iml
.vscode/
*.project
*.classpath

# OS files
.DS_Store
Thumbs.db

# Uploaded files (user images)
backend/uploads/

# Logs
*.log
logs/
```

---

## 9. Security and Validation Plan

### 9.1 Password Security
- All passwords hashed using **BCrypt** with cost factor 12 via Spring Security's `BCryptPasswordEncoder`.
- Plaintext passwords are never logged or stored at any layer.
- Password change requires submission of the current password for re-verification.

### 9.2 JWT Authentication
- JWT tokens signed using HS256 with a secret key loaded from environment variable (`JWT_SECRET`).
- Token payload: `{ "sub": userId, "email": userEmail, "iat": ..., "exp": ... }`.
- Default expiry: 24 hours. "Remember Me" option: 7 days.
- Tokens are validated on every protected endpoint by a `JwtAuthFilter` in the Spring Security filter chain.
- On token expiry or invalidity, return HTTP 401 with a clear JSON message.

### 9.3 API Key Protection
- External AI API key stored in environment variable (`PLANT_API_KEY`) on the server.
- Never included in frontend code, API responses, or logs.
- Accessed only via `@Value("${plant.api.key}")` in Spring configuration.
- A dedicated `PlantApiClient` service class handles all outbound requests.

### 9.4 File Upload Security
- Accepted MIME types: `image/jpeg`, `image/png`, `image/webp` only.
- Maximum file size: 5 MB (enforced in frontend JS and Spring Boot via `spring.servlet.multipart.max-file-size`).
- Uploaded files saved with randomly generated UUIDs as filenames.
- File storage directory is outside the web-accessible path.
- No executable file extensions are permitted.

### 9.5 Authorization Checks
- All `/api/**` endpoints except `/api/auth/**` require a valid JWT.
- All user-specific operations extract the user ID from the JWT and verify it matches the requested resource.
- HTTP 403 is returned if a user attempts to access another user's resources.

### 9.6 CORS Configuration
- CORS configured to allow requests only from the known frontend origin via `ALLOWED_ORIGINS` environment variable.
- `Access-Control-Allow-Origin` is never set to `*` in production.

### 9.7 Input Validation
- Spring Validation annotations on all request DTOs: `@NotBlank`, `@Email`, `@Size(min=8)`, `@Pattern`.
- Global `@ControllerAdvice` catches `MethodArgumentNotValidException` and returns HTTP 400 with field-level errors.
- SQL injection prevented by always using Spring Data JPA parameterized queries.

### 9.8 Error Handling
- Detailed stack traces are never sent to the client.
- Production logs capture full exception details server-side.
- Client receives a structured, generic error message with status code and user-safe message.

---

## 10. Testing Strategy

### 10.1 Backend Unit Tests (JUnit 5 + Mockito)

| Test Class              | Coverage Target                                        |
|-------------------------|--------------------------------------------------------|
| `AuthServiceTest`       | Registration, login, password hashing, JWT generation |
| `ScanServiceTest`       | API call mocking, result parsing, history saving       |
| `DiseaseServiceTest`    | Library queries, remedy retrieval                      |
| `HistoryServiceTest`    | Ownership validation, pagination                       |
| `GlobalExHandlerTest`   | Exception-to-response mapping                          |

### 10.2 Backend Integration Tests
- Use `@SpringBootTest` with an in-memory H2 database for repository and controller integration tests.
- Test full request-response cycles for auth and scan endpoints.

### 10.3 Frontend Manual Testing Checklist

| Feature                     | Test Scenario                                              |
|-----------------------------|------------------------------------------------------------|
| Registration form           | Empty fields, invalid email, weak password                 |
| Login                       | Wrong password, non-existent email                         |
| Image upload                | Too large, wrong type, valid image                         |
| Result page                 | Diseased result, healthy result, API error                 |
| Scan history                | Empty state, populated list, delete confirmation           |
| Protected routes            | Access dashboard without login -> redirect to login        |
| Session expiry              | Expired JWT -> prompt re-login                             |

### 10.4 API Testing (Postman Collection)
- A Postman collection maintained in `docs/AgroDoc.postman_collection.json`.
- Covers all REST endpoints with example request bodies and expected responses.
- Separate environments for `localhost` and any deployed target.

### 10.5 Browser Compatibility Testing
- Tested manually on: Chrome (latest), Firefox (latest), Edge (latest), Safari (latest).
- Responsive layout tested at: 360px, 768px, 1024px, 1440px, 1920px.

---

## 11. Development Roadmap

### Phase Overview

```
Phase 1:  Planning & Design              <- COMPLETE
Phase 2:  Project Setup & Foundation
Phase 3:  Backend Core - Auth & Users
Phase 4:  Frontend Core - Landing, Auth, Dashboard
Phase 5:  Disease Detection Flow - Backend
Phase 6:  Disease Detection Flow - Frontend
Phase 7:  Disease Library - Backend + Frontend
Phase 8:  Scan History - Backend + Frontend
Phase 9:  User Settings + Security Hardening
Phase 10: Testing, Polish & Documentation
```

---

### Phase 1 — Planning & Architecture Design (Complete)
**Output:** This document — `docs/PROJECT_PLAN.md`
**Git Commit:** `docs: add Phase 1 project plan and architecture document`

---

### Phase 2 — Project Setup and Repository Foundation
**Estimated Duration:** 1–2 days

**Tasks:**
1. Initialize GitHub repository with planned folder structure
2. Create `.gitignore` and initial `README.md`
3. Generate Spring Boot Maven project (Spring Initializr)
4. Configure `application.properties` with environment variable placeholders
5. Create MySQL database and run `schema.sql`
6. Verify database connection from Spring Boot
7. Set up frontend folder and `main.css` with design tokens
8. Commit: `chore: initial project setup with Spring Boot and MySQL`

**Dependencies:** None — starting point.

---

### Phase 3 — Backend: Authentication and User Management
**Estimated Duration:** 3–4 days

**Tasks:**
1. Create `User` entity and `UserRepository`
2. Implement request/response DTOs with validation annotations
3. Implement `AuthService` with BCrypt and JWT generation
4. Implement `AuthController` (register, login endpoints)
5. Configure Spring Security filter chain
6. Implement `JwtAuthFilter`
7. Implement `UserController` for profile and password change
8. Add `GlobalExceptionHandler`
9. Write JUnit tests for `AuthService`
10. Commit: `feat: implement JWT authentication and user management`

**Dependencies:** Phase 2 complete.

---

### Phase 4 — Frontend: Landing Page, Auth Pages, Dashboard Shell
**Estimated Duration:** 3–4 days

**Tasks:**
1. Build `index.html` with CSS animations and full design system
2. Build `login.html` and `register.html` with validation and API integration
3. Implement `auth.js` (JWT storage, route guards)
4. Build `dashboard.html` shell with stats and quick actions
5. Implement `api.js` (centralized fetch wrapper with JWT headers)
6. Connect Dashboard to `/api/users/me` for dynamic greeting
7. Commit: `feat: landing page, auth pages, and dashboard shell`

**Dependencies:** Phase 3 complete.

---

### Phase 5 — Backend: Disease Detection Flow
**Estimated Duration:** 3–5 days

**Tasks:**
1. Research and finalize external AI API selection
2. Create `DiseaseInfo` and `DiseaseRemedy` entities and repositories
3. Seed database with `seed_diseases.sql`
4. Implement `PlantApiClient` service for HTTP calls to external API
5. Implement `ScanController` (POST /api/scan/upload)
6. Implement `ScanService` (full detection, DB lookup, history save flow)
7. Implement `FileStorageUtil` for secure image saving
8. Write unit tests for `ScanService` with mocked API responses
9. Commit: `feat: disease detection backend with AI API integration`

**Dependencies:** Phase 3 complete; AI API key obtained.

---

### Phase 6 — Frontend: Scan and Result Pages
**Estimated Duration:** 2–3 days

**Tasks:**
1. Build `scan.html` with drag-and-drop upload zone
2. Implement `scan.js` (file validation, preview, submit)
3. Build `result.html` to render `ScanResultDTO`
4. Implement `result.js` (parse response, render disease info and remedies)
5. Add loading overlay during API processing
6. Handle API error states gracefully
7. Commit: `feat: scan upload and disease result frontend`

**Dependencies:** Phase 5 complete.

---

### Phase 7 — Disease Information Library
**Estimated Duration:** 2–3 days

**Tasks:**
1. Implement `DiseaseController` (GET all diseases, GET by ID)
2. Implement `DiseaseService` and DTOs
3. Build `library.html` with search, filter, and disease card grid
4. Build `disease.html` with full remedy tabbed layout
5. Implement `library.js` and `disease.js`
6. Commit: `feat: disease information library`

**Dependencies:** Phase 5 seed data available.

---

### Phase 8 — Scan History
**Estimated Duration:** 2–3 days

**Tasks:**
1. Implement `HistoryController` (GET list, GET by ID, DELETE)
2. Enforce ownership checks in `HistoryService`
3. Build `history.html` with paginated list and delete functionality
4. Implement `history.js`
5. Update Dashboard to load real recent scans
6. Commit: `feat: scan history backend and frontend`

**Dependencies:** Phase 5 and 6 complete.

---

### Phase 9 — User Settings and Security Hardening
**Estimated Duration:** 2 days

**Tasks:**
1. Build `settings.html` and `settings.js`
2. Connect to profile update and password change endpoints
3. Tighten CORS and security settings
4. Validate all environment variable configurations
5. Add HTTP security headers
6. Commit: `feat: user settings and security hardening`

---

### Phase 10 — Testing, Polish, Documentation, and Final Submission
**Estimated Duration:** 3–5 days

**Tasks:**
1. Complete all JUnit unit tests
2. Run full manual frontend testing checklist
3. Build and test Postman collection
4. Cross-browser and responsive layout review
5. Finalize `README.md` with screenshots and setup guide
6. Write `docs/API_DOCUMENTATION.md` and `docs/SETUP_GUIDE.md`
7. Add screenshots to `docs/screenshots/`
8. Final tag: `git tag v1.0.0`
9. Commit: `docs: final documentation and v1.0.0 release`

---

## 12. External AI API Selection Criteria

### 12.1 Requirement Checklist

| Criterion                   | Requirement                                                                 |
|-----------------------------|-----------------------------------------------------------------------------|
| Tomato Disease Coverage     | Must support: Early Blight, Late Blight, Leaf Mold, Septoria Leaf Spot, Spider Mites, Target Spot, Tomato Mosaic Virus, Tomato Yellow Leaf Curl Virus, Bacterial Spot, Healthy |
| Image Upload Support        | Must accept multipart/form-data or base64-encoded image                     |
| Response Format             | Must return structured JSON with disease name and confidence score          |
| Authentication              | API key authentication (header or query param)                              |
| Usage Limits                | Free tier must allow at least 100 requests/day for development              |
| Cost                        | Prefer free tier sufficient for demonstration; document scaling cost        |
| Privacy / Data Retention    | Confirm whether uploaded images are stored by provider                      |
| Response Latency            | Average under 8 seconds for a standard leaf image                          |
| API Stability               | Documented versioning, uptime SLA, active developer community               |
| Documentation Quality       | Complete, up-to-date API reference documentation                            |

### 12.2 Candidate APIs to Evaluate

| API Name                       | Notes                                                                       |
|--------------------------------|-----------------------------------------------------------------------------|
| **Plant.id API** (kindwise.com)| Specifically designed for plant health. Supports leaf images. Free tier. JSON response. Well-documented. **Recommended starting point.** |
| **PlantNet API**               | Open-science project. Strong on species ID but limited disease output. Evaluate coverage. |
| **Hugging Face Inference API** | Can call pre-trained PlantVillage-trained models. Free tier available. Response format varies per model. |
| **Agrio / Crop.Zone**          | AgriTech-focused APIs. Evaluate availability and terms for non-commercial use. |

### 12.3 Decision Process

1. Register for free developer access to Plant.id API first.
2. Test with at least 5 real tomato leaf images (diseased and healthy).
3. Verify response JSON structure matches what `PlantApiClient` expects.
4. If Plant.id is insufficient, evaluate Hugging Face Inference API.
5. Document the chosen API in `docs/API_DOCUMENTATION.md`.
6. Never hardcode the API key — store only in environment variables.

### 12.4 Internal API Abstraction Contract

Regardless of chosen API, `PlantApiClient` normalizes the response into:

```java
public record ApiPrediction(
    String diseaseName,      // Canonical name matching disease_info.disease_name
    double confidence,       // 0.0 to 1.0
    boolean isHealthy,       // True if classified as healthy
    String rawApiResponse    // Full JSON string for storage in scan_history
) {}
```

This ensures changing the external API only requires updating `PlantApiClient`.

---

## 13. Assumptions and Unresolved Decisions

| # | Item                     | Current Assumption / Status                                    | Action Required                    |
|---|--------------------------|----------------------------------------------------------------|------------------------------------|
| 1 | External AI API          | Plant.id API is the leading candidate but not yet confirmed    | Evaluate and confirm in Phase 5    |
| 2 | Image storage            | Images stored on local filesystem under `backend/uploads/`     | Confirm if cloud storage is needed |
| 3 | Deployment environment   | Assumed to run locally for submission                          | Clarify if live demo URL required  |
| 4 | Frontend serving         | Static files served separately (VS Code Live Server or nginx)  | Could be served by Spring Boot     |
| 5 | Session management       | JWT in localStorage assumed; HttpOnly cookie is more secure    | Confirm approach before Phase 3    |
| 6 | Disease data seed        | Manual SQL seed file planned                                   | Finalize disease list after API    |
| 7 | User feedback UI         | Table planned but UI not scoped for Phase 1 submission         | Implement in Phase 10 if time      |
| 8 | Pagination page size     | TBD                                                            | Decide in Phase 8                  |
| 9 | Multi-language support   | English only                                                   | Out of scope                       |
| 10| API cost for demo        | Assumes free tier sufficient for academic demonstration        | Verify before obtaining API key    |

---

## 14. Phase 2 Task List

### Repository and Version Control
- [ ] Create a new GitHub repository named `AgroDoc`
- [ ] Initialize with `README.md` and `.gitignore` using the template in this plan
- [ ] Clone the repository to the local development machine
- [ ] Create the planned folder structure: `backend/`, `frontend/`, `database/`, `docs/`
- [ ] Move `docs/PROJECT_PLAN.md` (this file) into the repository and commit

### Backend Project Initialization
- [ ] Generate a Spring Boot project using [start.spring.io](https://start.spring.io) with:
  - Group: `com.agrodoc` | Artifact: `agrodoc`
  - Dependencies: Spring Web, Spring Data JPA, Spring Security, MySQL Driver, Validation, Lombok
- [ ] Place the generated project in the `backend/` folder
- [ ] Configure `application.properties` with environment variable placeholders for `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET`
- [ ] Verify the Spring Boot application starts without errors

### Database Setup
- [ ] Install MySQL 8.x locally (if not already installed)
- [ ] Create the `agrodoc_db` database
- [ ] Write and run `database/schema.sql` to create all five tables
- [ ] Verify table creation with a MySQL client
- [ ] Configure Spring Boot to connect to `agrodoc_db`
- [ ] Confirm JPA can connect successfully (check startup logs)

### Frontend Foundation
- [ ] Create all HTML file shells (empty pages with head, nav placeholder, and footer placeholder)
- [ ] Create `frontend/css/main.css` with the full design system:
  - CSS custom properties for colors (#166534 primary, #22C55E accent, #F8FAF7 background)
  - Base styles (reset, body, typography with Inter font)
  - Utility classes (buttons, cards, badges, form elements)
  - Navbar and footer component styles
- [ ] Link Bootstrap 5, Lucide Icons, and Google Fonts (Inter) via CDN in all HTML files
- [ ] Create `frontend/js/api.js` with a centralized `apiFetch()` wrapper function skeleton
- [ ] Create `frontend/js/auth.js` with `getToken()`, `setToken()`, `clearToken()`, and `isLoggedIn()` utility functions
- [ ] Verify the landing page renders correctly with design tokens applied

### Git and Documentation
- [ ] Commit Phase 2 work with logical commit messages:
  - `chore: initialize Spring Boot Maven project structure`
  - `chore: create MySQL schema and database configuration`
  - `chore: add frontend foundation with design system`
- [ ] Ensure no secrets, `.env` files, or IDE-specific files are committed
- [ ] Push all commits to the GitHub remote repository
- [ ] Begin `docs/SETUP_GUIDE.md` with instructions to run the project locally

---

*End of AgroDoc Project Plan — Phase 1*

*Next Phase: Phase 2 — Project Setup and Repository Foundation*
