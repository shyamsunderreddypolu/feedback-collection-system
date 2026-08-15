
---

## 🚀 1. Local Database Setup for Team Members
Every team member needs to set up the database locally before coding:

1. **Install MySQL Server 8.0** and **MySQL Workbench**.
2. **Execute the SQL Scripts**:
   * Open MySQL Workbench, open the SQL files, and execute:
     1. First run **`drop_schema.sql`** (to clean up old tables).
     2. Then run **`schema.sql`** (to build the tables, keys, and seed the default roles/departments).
3. **Spring Boot database connections**:
   * The database username is set to `fbcs_user` and password is `Fbcspassword_2026`. (If they use different local passwords, they must edit their `src/main/resources/application.properties` profile).

---

## ☕ 2. Guide for the Spring Boot Backend Developers (2 Members)
Your job is to map these tables to Java classes (Entities) using JPA. Here is what you need to write in the code:

### A. Mapping User Profiles (`@OneToOne`)
Instead of putting student/faculty data directly in the `users` table, we separated them to keep the database clean:
* Create a **`User`** entity.
* Create a **`StudentProfile`** entity containing fields: `rollNumber`, `year`, `semester`, `section`, `batch`.
  * Link it using `@OneToOne` in the `User` class:
    ```java
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private StudentProfile studentProfile;
    ```
* Create a **`FacultyProfile`** entity containing: `employeeId`, `designation`, `joiningDate`.
  * Link it using `@OneToOne` in the `User` class:
    ```java
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private FacultyProfile facultyProfile;
    ```

### B. Mapping Course and Feedback Assignments (`@ManyToOne`)
We decoupled the assignments so multiple faculty members can teach the same course across sections, and forms can be assigned to specific batches:
* **`CourseAssignment`**: Maps a course ID and a user ID (where role is `ROLE_FACULTY`) for a specific semester/academic year.
* **`FeedbackAssignment`**: Maps a feedback form to a department, semester, section, and batch.
  * In the Spring Boot code, when a student logs in, fetch their `StudentProfile` (e.g. CSE, Semester 6, Section A, Batch 2023-2027) and search `FeedbackAssignment` to find all active forms they need to submit.

### C. Validating Answers population (TEXT vs RATING vs SELECT)
The `answers` table now has 3 columns depending on the question format:
* `rating_value` (1 to 5 stars)
* `text_value` (text suggestion comments)
* `selected_option_id` (foreign key pointing to choice option)
* **Java Validation Rule**: In your `ResponseService.java`, before saving answers, verify the `QuestionType`:
  * If the question type is `RATING`, only set `ratingValue` (others must be `null`).
  * If the type is `TEXTAREA`, only set `textValue` (others must be `null`).
  * If the type is `RADIO` or `CHECKBOX`, only set `selectedOptionId` (others must be `null`).

---

## 🎨 3. Guide for the HTML/CSS/JS Frontend Developers (2 Members)
Your job is to build the HTML forms and send/render the data correctly using JavaScript `fetch()` calls.

### A. Student and Faculty Signups
* When registering a **Student**, your frontend signup page must collect the regular registration fields (`name`, `email`, `password`) AND the profile inputs:
  ```json
  {
    "name": "Abhishek Sharma",
    "email": "student@college.edu",
    "password": "password123",
    "roleId": 1,
    "studentProfile": {
      "rollNumber": "2023CSE045",
      "year": 3,
      "semester": 6,
      "section": "A",
      "batch": "2023-2027"
    }
  }
  ```
* When registering a **Faculty**, collect:
  ```json
  {
    "name": "Dr. Raj Kumar",
    "email": "faculty@college.edu",
    "password": "password123",
    "roleId": 2,
    "facultyProfile": {
      "employeeId": "EMP-CSE-201",
      "designation": "Professor",
      "joiningDate": "2020-06-15"
    }
  }
  ```

### B. Displaying Question Choices (Disable vs Delete)
* In the Admin panel, when configuring surveys, instead of deleting options (which breaks historic analytics charts), you can disable an option by setting `isActive = false` in the database.
* **Frontend Rule**: When rendering a radio/checkbox question, only render option elements where `isActive === true`.

### C. Submitting Responses
* When a student submits a completed form, construct the answers array sending only the relevant answer column (do not send empty strings, send `null` for inactive types):
  ```json
  {
    "feedbackFormId": 1,
    "answers": [
      { "questionId": 1, "ratingValue": 5, "textValue": null, "selectedOptionId": null },
      { "questionId": 2, "ratingValue": null, "textValue": null, "selectedOptionId": 1 },
      { "questionId": 3, "ratingValue": null, "textValue": "Great teaching methods.", "selectedOptionId": null }
    ]
  }
  ```
