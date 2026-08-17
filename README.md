# 🎓 Feedback Collection System (FBCS)

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.16-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://reactjs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![JWT](https://img.shields.io/badge/JWT-Auth-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![Railway](https://img.shields.io/badge/Deploy-Railway-0B0D0E?style=for-the-badge&logo=railway&logoColor=white)](https://railway.app/)
[![Vercel](https://img.shields.io/badge/Deploy-Vercel-000000?style=for-the-badge&logo=vercel&logoColor=white)](https://vercel.com/)
[![Tests](https://img.shields.io/badge/Tests-134%20Passing-brightgreen?style=for-the-badge)](https://github.com/)

> An enterprise-grade, secure, role-based Feedback Collection and Course Evaluation platform engineered with **Spring Boot 3**, **React 18**, and **MySQL**.

---

## 📸 Application Screenshots

### 🔐 Unified Authentication Portal
![Unified Login Portal](screenshots/01_login_page.png)

### 📊 Administrator Executive Dashboard
![Admin Dashboard](screenshots/02_admin_dashboard.png)

### 🛠️ Survey Form Builder Studio
![Form Builder Studio](screenshots/03_form_builder.png)

### 👥 User Directory & Role Management
![User Management Directory](screenshots/04_user_management.png)

### 📈 Reports & PDF/CSV Export Portal
![Reports Export Portal](screenshots/05_reports_export.png)

### 👨‍🏫 Faculty Course Feedback Analytics
![Faculty Course Analytics](screenshots/06_faculty_analytics.png)

### 👨‍🎓 Student Active Surveys & Evaluation Portal
![Student Survey Portal](screenshots/07_student_portal.png)

---

## 🏛️ System Architecture

```mermaid
graph TD
    subgraph Client ["Client Layer (React 18 / Vercel)"]
        A1[👨‍🎓 Student Portal]
        A2[👨‍🏫 Faculty Analytics]
        A3[👨‍💼 Admin Studio]
    end

    subgraph Security ["Security Layer (Spring Security 6)"]
        B1[JwtAuthenticationFilter]
        B2[Role-Based Authorization]
        B3[BCrypt Password Encoder]
    end

    subgraph Backend ["Service Layer (Spring Boot 3 / Railway)"]
        C1[Auth & Profile Service]
        C2[Survey & Question Engine]
        C3[Submission & Validation Service]
        C4[Analytics & Aggregation Engine]
        C5[iText PDF & CSV Export Engine]
    end

    subgraph Storage ["Database Layer (MySQL 8 / Railway)"]
        D1[(users, roles, departments)]
        D2[(feedback_forms, questions, options)]
        D3[(responses, answers)]
        D4[(assignments, courses)]
    end

    Client -->|HTTPS + Bearer JWT| Security
    Security --> Backend
    Backend --> Storage
```

---

## 🗄️ Database Schema & Relationships

```mermaid
erDiagram
    USERS ||--o{ STUDENT_PROFILES : has
    USERS ||--o{ FACULTY_PROFILES : has
    USERS }o--|| ROLES : assigned
    USERS }o--|| DEPARTMENTS : belongs_to

    FEEDBACK_FORMS ||--o{ QUESTIONS : contains
    QUESTIONS ||--o{ QUESTION_OPTIONS : has
    FEEDBACK_FORMS ||--o{ FEEDBACK_ASSIGNMENTS : targeted_to
    FEEDBACK_FORMS ||--o{ RESPONSES : receives

    RESPONSES ||--o{ ANSWERS : includes
    QUESTIONS ||--o{ ANSWERS : answered_in
    COURSES ||--o{ COURSE_ASSIGNMENTS : assigned_to
```

---

## 🌟 Key Role Capabilities

| Role | Target Portal | Core Permissions & Functionality |
| :--- | :--- | :--- |
| **👨‍💼 Administrator** | `/dashboard`<br>`/form-builder`<br>`/users`<br>`/reports` | • Build dynamic forms with Star Ratings, Radio options, Checkboxes, and Text questions.<br>• Target academic cohorts by Department (`CSE`, `ECE`, `MECH`), Semester, and Section.<br>• Publish draft forms to active status.<br>• Manage user directory with role filtering and soft-deactivation.<br>• One-click export of executive PDF summary reports and CSV datasets. |
| **👨‍🏫 Faculty Member** | `/faculty-analytics` | • Real-time course feedback rating score (e.g. `4.65 / 5.0`).<br>• Response volume and participation rate analytics.<br>• Review qualitative student comments with guaranteed student anonymity. |
| **👨‍🎓 Student** | `/student-surveys`<br>`/take-survey/:id` | • Access surveys assigned to student's department and semester.<br>• Submit anonymous evaluations with interactive 5-star ratings and feedback.<br>• Automatic prevention of duplicate submissions. |

---

## 🏗️ Technology Stack Breakdown

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Frontend UI** | React 18 SPA | Built with React Router v6, Axios, and React Icons. |
| **State Management** | Context API (`AuthContext`) | Manages JWT token lifecycle, session persistence, and role state. |
| **Data Visualization** | Chart.js & `react-chartjs-2` | Interactive feedback analytics charts and response trends. |
| **Backend REST API** | Spring Boot 3.5.x | Java 17 enterprise REST architecture with Spring Data JPA. |
| **Security & Auth** | Spring Security 6 & JWT | Stateless HMAC-SHA256 tokens and BCrypt (`$2a$10$...`) hashing. |
| **Relational Database** | MySQL 8.x | 15 relational tables with foreign keys and soft-deletions. |
| **Reporting Engine** | OpenPDF / iText & OpenCSV | Real-time binary PDF streaming and formatted CSV export. |
| **Cloud Hosting** | Railway & Vercel | Multi-stage Docker container on Railway + Single Page App on Vercel. |

---

## 🔑 Default Demo Accounts

The system automatically seeds initial accounts on first startup:

| Role | Email | Password | Accessible Portals |
| :--- | :--- | :--- | :--- |
| **👨‍💼 Administrator** | `admin@fbcs.local` | `admin123` | Dashboard, Form Builder, Users, Reports |
| **👨‍🏫 Faculty** | `faculty@fbcs.local` | `faculty123` | Course Feedback Analytics |
| **👨‍🎓 Student** | `student@fbcs.local` | `student123` | My Surveys & Submission Portal |

---

## 🚀 Quick Start (Local Setup)

### Prerequisites
- **Java 17+** & **Maven**
- **Node.js 18+** & **npm**
- **MySQL 8+** running on `localhost:3306`

---

### 1. Backend Setup

```bash
# Clone the repository
git clone https://github.com/shyamsunderreddypolu/feedback-collection-system.git
cd feedback-collection-system

# Run Spring Boot backend
./mvnw spring-boot:run
```
> Backend starts on **`http://localhost:8080`**.

---

### 2. Frontend Setup

```bash
# Navigate to frontend folder
cd frontend

# Install dependencies
npm install

# Start React development server
npm start
```
> Frontend launches on **`http://localhost:3000`**.

---

## 🌐 Production Cloud Deployment

### 1. Backend on Railway
1. Provision a **MySQL** database on [Railway](https://railway.app/).
2. Deploy the Spring Boot application using the included multi-stage `Dockerfile`.
3. Railway automatically binds database connection variables (`MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD`) and dynamic `${PORT}`.

### 2. Frontend on Vercel
1. Import the repository on [Vercel](https://vercel.com/) with root directory set to `frontend/`.
2. Add environment variable:
   - `REACT_APP_API_BASE_URL` = `https://<your-railway-backend-url>/api`
3. Click **Deploy**.

---

## 📡 REST API Reference

### 🔐 Authentication
- `POST /api/auth/login` - Authenticate credentials and issue JWT token
- `POST /api/auth/register` - Register a new user account

### 📝 Survey Forms & Questions
- `GET /api/forms/active` - Fetch active surveys for logged-in user
- `POST /api/forms` - Create draft feedback form *(Admin)*
- `PUT /api/forms/{id}/publish` - Publish feedback form *(Admin)*
- `POST /api/questions` - Add dynamic question item *(Admin)*
- `POST /api/assignments` - Assign form to academic cohort *(Admin)*

### ✍️ Submissions & Analytics
- `POST /api/submissions` - Submit survey response *(Student)*
- `GET /api/analytics/faculty/{facultyId}` - Get aggregate ratings & comments *(Faculty/Admin)*
- `GET /api/reports/export/pdf` - Download PDF Report *(Admin)*
- `GET /api/reports/export/csv` - Download CSV Dataset *(Admin)*

### 👥 User Management
- `GET /api/users/active` - List all active users
- `GET /api/users/role/{role}` - Filter users by role (`ROLE_STUDENT`, `ROLE_FACULTY`, `ROLE_ADMIN`)
- `DELETE /api/users/{id}` - Soft-deactivate a user account

---

## 🧪 Testing & Verification

```bash
# Run full automated test suite
./mvnw test
```

- **Automated Tests**: **`134 / 134 PASSED (100%)`** across Controllers, Services, Security, and DTOs.
- **End-to-End Manual QA**: Complete 11-step verification flow tested and validated.

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
