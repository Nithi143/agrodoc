# 🌿 AgroDoc — AI-Powered Tomato Leaf Disease Detection

> A final-year Computer Science Engineering project.
> Detect tomato leaf diseases instantly using AI. Get science-backed organic and chemical treatment recommendations.

![AgroDoc Banner](docs/screenshots/banner.png)

---

## 📌 Project Overview

**AgroDoc** is a full-stack web application that allows farmers and agronomists to:

- 📷 Upload a tomato leaf photograph
- 🤖 Receive an AI-powered disease diagnosis via an external plant disease API
- 💊 View curated organic and chemical treatment recommendations
- 📚 Browse a searchable disease information library
- 🗂️ Manage their personal scan history

---

## 🛠️ Technology Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Frontend     | HTML5, CSS3, Vanilla JavaScript, Bootstrap 5 |
| Backend      | Java 17, Spring Boot 3.x            |
| ORM          | Spring Data JPA + Hibernate         |
| Database     | MySQL 8.x                           |
| Build Tool   | Apache Maven 3.9.x                  |
| Security     | Spring Security + JWT               |
| AI Detection | External Plant Disease REST API     |
| Version Control | Git + GitHub                     |

---

## 📁 Project Structure

```
AgroDoc/
├── backend/          # Spring Boot Maven project
├── frontend/         # HTML/CSS/JS static files
├── database/         # SQL schema and seed scripts
├── docs/             # Project plan, API docs, screenshots
├── .gitignore
└── README.md
```

---

## ⚙️ Prerequisites

Before running this project, ensure you have installed:

| Software        | Minimum Version | Download |
|-----------------|-----------------|----------|
| Java JDK        | 17              | [Adoptium](https://adoptium.net/) |
| Apache Maven    | 3.9.x           | [maven.apache.org](https://maven.apache.org/download.cgi) |
| MySQL Server    | 8.0             | [mysql.com](https://dev.mysql.com/downloads/mysql/) |
| Git             | 2.x             | [git-scm.com](https://git-scm.com/) |
| MySQL Workbench | 8.0 (optional)  | [mysql.com](https://dev.mysql.com/downloads/workbench/) |

---

## 🚀 Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/<your-username>/AgroDoc.git
cd AgroDoc
```

### 2. Set Up the Database

Open MySQL and run the schema script:

```bash
mysql -u root -p < database/schema.sql
```

This creates the `agrodoc_db` database and all required tables.

### 3. Configure Environment Variables

Create a file at `backend/src/main/resources/application-dev.properties`:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/agrodoc_db?useSSL=false&serverTimezone=UTC
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD

# JWT
jwt.secret=YOUR_JWT_SECRET_KEY_MIN_32_CHARS

# External AI API
plant.api.key=YOUR_PLANT_API_KEY
plant.api.base-url=https://api.plant.id/v3
```

> ⚠️ **Never commit `application-dev.properties` to Git.** It is listed in `.gitignore`.

### 4. Build and Run the Backend

```bash
cd backend
mvn clean install
mvn spring-boot:run -Dspring.profiles.active=dev
```

The backend will start on `http://localhost:8080`.

### 5. Run the Frontend

Open `frontend/index.html` in your browser directly, or use VS Code Live Server extension:

1. Install the **Live Server** extension in VS Code
2. Right-click `frontend/index.html` → **Open with Live Server**
3. Frontend will open at `http://127.0.0.1:5500`

---

## 🔑 Environment Variable Reference

| Variable                  | Description                                      |
|---------------------------|--------------------------------------------------|
| `spring.datasource.url`   | MySQL JDBC connection URL                        |
| `spring.datasource.username` | MySQL username                              |
| `spring.datasource.password` | MySQL password                              |
| `jwt.secret`              | HS256 signing secret (minimum 32 characters)     |
| `plant.api.key`           | API key for the external plant disease AI service |
| `plant.api.base-url`      | Base URL of the external plant disease API       |

---

## 📚 Documentation

| Document | Location |
|----------|----------|
| Project Plan (Phase 1) | [`docs/PROJECT_PLAN.md`](docs/PROJECT_PLAN.md) |
| API Documentation | [`docs/API_DOCUMENTATION.md`](docs/API_DOCUMENTATION.md) |
| Setup Guide | This file |

---

## 🗺️ Development Roadmap

| Phase | Description | Status |
|-------|-------------|--------|
| Phase 1 | Planning & Architecture | ✅ Complete |
| Phase 2 | Project Setup & Foundation | ✅ Complete |
| Phase 3 | Backend: Auth & User Management | 🔲 Pending |
| Phase 4 | Frontend: Landing, Auth, Dashboard | 🔲 Pending |
| Phase 5 | Backend: Disease Detection Flow | 🔲 Pending |
| Phase 6 | Frontend: Scan & Result Pages | 🔲 Pending |
| Phase 7 | Disease Library | 🔲 Pending |
| Phase 8 | Scan History | 🔲 Pending |
| Phase 9 | Settings & Security Hardening | 🔲 Pending |
| Phase 10 | Testing, Polish & Documentation | 🔲 Pending |

---

## 🔒 Security Notes

- Passwords are hashed using BCrypt (cost factor 12)
- All protected endpoints require a valid JWT token
- API keys and database credentials are loaded from environment variables only
- Uploaded images are stored outside the web-accessible directory with UUID filenames
- SQL injection is prevented through JPA parameterized queries

---

## 📄 License

This project is developed for educational purposes as a final-year CSE capstone project.

---

*Built with ❤️ for sustainable agriculture technology.*
