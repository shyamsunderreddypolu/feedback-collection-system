-- =============================================================================
-- FEEDBACK COLLECTION SYSTEM (FBCS) - PRODUCTION SCHEMA (REFACTORED & OPTIMIZED)
-- Engine: MySQL 8.0 | Collation: utf8mb4_unicode_ci
-- Architect: Senior Database Architect & Spring Boot Solutions Expert
-- =============================================================================
-- Refactored production-grade relational database design. Key improvements:
--   1. Structured User Profiles: Separation of students & faculty profiles.
--   2. Course Assignments Matrix: Supports teaching assignments histories.
--   3. Granular Feedback Targeting: Academic groups course schedules assignments.
--   4. Multi-Type Answer Model: Rating metrics, comments, and options links.
--   5. CHECK Constraints: Enforces data-level patterns and range limits.
--   6. Redundant Indexes Removed: Optimized execution footprint by cleaning duplicates.
-- =============================================================================

CREATE DATABASE IF NOT EXISTS fbcs_db;
USE fbcs_db;

-- =============================================================================
-- 1. Table: `departments`
-- =============================================================================
-- Purpose: Academic units (CSE, IT, ECE).
-- Relationships: One-to-many with users, student_profiles, courses, feedback_assignments.
-- JPA Mapping: Department Entity -> OneToMany with User, Course, FeedbackAssignment.
-- Reasoning: Normalizes group filters to maintain referential integrity.
-- =============================================================================
CREATE TABLE IF NOT EXISTS `departments` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL UNIQUE,
  `code` VARCHAR(20) NOT NULL UNIQUE, -- E.g. 'CSE', 'IT'
  `active` BOOLEAN NOT NULL DEFAULT TRUE,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 2. Table: `roles`
-- =============================================================================
-- Purpose: Security clearance roles definition.
-- Relationships: One-to-many with users.
-- JPA Mapping: Role Entity -> OneToMany with User.
-- Reasoning: Single lookup table maps roles avoiding redundant text values.
-- =============================================================================
CREATE TABLE IF NOT EXISTS `roles` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(50) NOT NULL UNIQUE -- E.g. 'ROLE_STUDENT', 'ROLE_ADMIN'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 3. Table: `users`
-- =============================================================================
-- Purpose: Login profiles security gateway. Supported by Soft Deletes.
-- Relationships: ManyToOne with departments & roles. OneToOne with student/faculty profiles.
-- JPA Mapping: User Entity -> @ManyToOne with Department, Role. @OneToOne with profile extensions.
-- Reasoning: Isolates login data from structural role attributes to protect session states.
-- =============================================================================
CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(150) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `department_id` BIGINT NULL,
  `role_id` INT NOT NULL,
  `active` BOOLEAN NOT NULL DEFAULT TRUE,
  `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
  `deleted_at` DATETIME NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NULL DEFAULT NULL,
  CONSTRAINT `fk_users_department` FOREIGN KEY (`department_id`) 
    REFERENCES `departments` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`) 
    REFERENCES `roles` (`id`) ON DELETE RESTRICT,
  -- Basic pattern validation for email addresses at database entry level
  CONSTRAINT `chk_users_email` CHECK (`email` LIKE '%_@__%.__%')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 4. Table: `student_profiles`
-- =============================================================================
-- Purpose: Student-specific academic grouping properties.
-- Relationships: One-to-one with users. Many-to-one with departments (inherited via user).
-- JPA Mapping: StudentProfile Entity -> @OneToOne mapping with User.
-- Reasoning: Separates student metadata from generic user credentials, preventing nullable columns bloat.
-- =============================================================================
CREATE TABLE IF NOT EXISTS `student_profiles` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL UNIQUE,
  `roll_number` VARCHAR(50) NOT NULL UNIQUE,
  `year` INT NOT NULL,
  `semester` INT NOT NULL,
  `section` VARCHAR(10) NOT NULL,
  `batch` VARCHAR(30) NOT NULL, -- E.g. '2023-2027'
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NULL DEFAULT NULL,
  CONSTRAINT `fk_student_user` FOREIGN KEY (`user_id`) 
    REFERENCES `users` (`id`) ON DELETE CASCADE,
  -- Restricts academic year bounds between 1st and 4th year
  CONSTRAINT `chk_student_year` CHECK (`year` BETWEEN 1 AND 4),
  -- Restricts semesters between 1 and 8
  CONSTRAINT `chk_student_semester` CHECK (`semester` BETWEEN 1 AND 8),
  -- Enforces standard 4-digit academic batch intervals (e.g. '2023-2027')
  CONSTRAINT `chk_student_batch` CHECK (`batch` REGEXP '^[0-9]{4}-[0-9]{4}$'),
  -- Enforces section input cannot be left empty
  CONSTRAINT `chk_student_section` CHECK (LENGTH(TRIM(`section`)) >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 5. Table: `faculty_profiles`
-- =============================================================================
-- Purpose: Faculty-specific employment details.
-- Relationships: One-to-one with users.
-- JPA Mapping: FacultyProfile Entity -> @OneToOne mapping with User.
-- Reasoning: Isolates employee parameters, making administrative reporting clean.
-- =============================================================================
CREATE TABLE IF NOT EXISTS `faculty_profiles` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL UNIQUE,
  `employee_id` VARCHAR(50) NOT NULL UNIQUE,
  `designation` VARCHAR(100) NOT NULL, -- E.g. 'Assistant Professor'
  `joining_date` DATE NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NULL DEFAULT NULL,
  CONSTRAINT `fk_faculty_user` FOREIGN KEY (`user_id`) 
    REFERENCES `users` (`id`) ON DELETE CASCADE,
  -- Ensures designation string is not empty
  CONSTRAINT `chk_faculty_designation` CHECK (LENGTH(TRIM(`designation`)) >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 6. Table: `courses`
-- =============================================================================
-- Purpose: Academic courses catalog registry.
-- Relationships: ManyToOne with departments. OneToMany with course_assignments.
-- JPA Mapping: Course Entity -> @ManyToOne with Department. OneToMany with CourseAssignment.
-- Reasoning: Decoupled registry allows a course to map to multiple instructors over semesters.
-- =============================================================================
CREATE TABLE IF NOT EXISTS `courses` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(150) NOT NULL,
  `code` VARCHAR(30) NOT NULL UNIQUE, -- E.g. 'CS101'
  `department_id` BIGINT NOT NULL,
  `active` BOOLEAN NOT NULL DEFAULT TRUE,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NULL DEFAULT NULL,
  CONSTRAINT `fk_courses_department` FOREIGN KEY (`department_id`) 
    REFERENCES `departments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 7. Table: `course_assignments`
-- =============================================================================
-- Purpose: Records faculty teaching assignments histories.
-- Relationships: ManyToOne with courses and users (faculty).
-- JPA Mapping: CourseAssignment Entity -> ManyToOne with Course, User.
-- Reasoning: Resolves the multi-faculty assignment requirement across semesters.
-- =============================================================================
CREATE TABLE IF NOT EXISTS `course_assignments` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `course_id` BIGINT NOT NULL,
  `faculty_id` BIGINT NOT NULL,
  `academic_year` VARCHAR(20) NOT NULL, -- E.g. '2025-2026'
  `semester` INT NOT NULL,
  `section` VARCHAR(10) NOT NULL,
  `status` ENUM('ACTIVE', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'ACTIVE',
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NULL DEFAULT NULL,
  CONSTRAINT `fk_course_assign_course` FOREIGN KEY (`course_id`) 
    REFERENCES `courses` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_course_assign_faculty` FOREIGN KEY (`faculty_id`) 
    REFERENCES `users` (`id`) ON DELETE CASCADE,
  -- Prevent teaching duplication overlaps for same code, teacher, year, semester, section
  CONSTRAINT `uk_course_assignment` UNIQUE KEY (`course_id`, `faculty_id`, `academic_year`, `semester`, `section`),
  -- Enforces standard 4-digit interval for academic years
  CONSTRAINT `chk_course_assign_year` CHECK (`academic_year` REGEXP '^[0-9]{4}-[0-9]{4}$'),
  -- Restricts semesters between 1 and 8
  CONSTRAINT `chk_course_assign_sem` CHECK (`semester` BETWEEN 1 AND 8),
  -- Enforces section is not blank
  CONSTRAINT `chk_course_assign_sec` CHECK (LENGTH(TRIM(`section`)) >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 8. Table: `feedback_forms`
-- =============================================================================
-- Purpose: Feedback collection campaigns parameters. Supported by Soft Deletes.
-- Relationships: ManyToOne with users (creator, updater).
-- JPA Mapping: FeedbackForm Entity has ManyToOne with User.
-- Reasoning: Decoupled feedback forms from targeting mappings to support templates.
-- =============================================================================
CREATE TABLE IF NOT EXISTS `feedback_forms` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `title` VARCHAR(255) NOT NULL,
  `description` TEXT NULL,
  `category` VARCHAR(50) NOT NULL DEFAULT 'GENERAL', -- E.g. 'COURSE', 'FACILITIES'
  `start_date` DATETIME NOT NULL,
  `end_date` DATETIME NOT NULL,
  `status` ENUM('DRAFT', 'ACTIVE', 'CLOSED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
  `creator_id` BIGINT NOT NULL,
  `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
  `deleted_at` DATETIME NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NULL DEFAULT NULL,
  `updated_by_id` BIGINT NULL,
  CONSTRAINT `fk_feedbackforms_creator` FOREIGN KEY (`creator_id`) 
    REFERENCES `users` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_feedbackforms_updated_by` FOREIGN KEY (`updated_by_id`) 
    REFERENCES `users` (`id`) ON DELETE SET NULL,
  -- Ensures campaigns end date falls chronologically after the start date
  CONSTRAINT `chk_feedbackform_dates` CHECK (`end_date` > `start_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 9. Table: `feedback_assignments`
-- =============================================================================
-- Purpose: Controls exactly who is eligible to submit feedback for a form.
-- Relationships: ManyToOne with feedback_forms, departments, courses.
-- JPA Mapping: FeedbackAssignment Entity -> ManyToOne with FeedbackForm, Department, Course.
-- Reasoning: Allows fine-grained scheduling (e.g., target a form to IT, 3rd Year, Semester 6, Java Course).
-- =============================================================================
CREATE TABLE IF NOT EXISTS `feedback_assignments` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `feedback_form_id` BIGINT NOT NULL,
  `department_id` BIGINT NOT NULL,
  `course_id` BIGINT NULL, -- Optional course link. If NULL, maps to general department survey
  `semester` INT NOT NULL,
  `section` VARCHAR(10) NOT NULL, -- E.g. 'A', 'ALL'
  `batch` VARCHAR(30) NOT NULL, -- E.g. '2023-2027'
  `academic_year` VARCHAR(20) NOT NULL, -- E.g. '2025-2026'
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NULL DEFAULT NULL,
  CONSTRAINT `fk_feedassign_form` FOREIGN KEY (`feedback_form_id`) 
    REFERENCES `feedback_forms` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_feedassign_dept` FOREIGN KEY (`department_id`) 
    REFERENCES `departments` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_feedassign_course` FOREIGN KEY (`course_id`) 
    REFERENCES `courses` (`id`) ON DELETE SET NULL,
  -- Enforce unique targeted constraints avoiding redundant distributions
  CONSTRAINT `uk_feedback_assignment` UNIQUE KEY (`feedback_form_id`, `department_id`, `course_id`, `semester`, `section`, `batch`, `academic_year`),
  -- Restricts semesters between 1 and 8
  CONSTRAINT `chk_feed_assign_sem` CHECK (`semester` BETWEEN 1 AND 8),
  -- Enforces standard 4-digit interval for academic years
  CONSTRAINT `chk_feed_assign_year` CHECK (`academic_year` REGEXP '^[0-9]{4}-[0-9]{4}$'),
  -- Enforces batch formatting (e.g. '2023-2027')
  CONSTRAINT `chk_feed_assign_batch` CHECK (`batch` REGEXP '^[0-9]{4}-[0-9]{4}$'),
  -- Enforces section is not blank
  CONSTRAINT `chk_feed_assign_sec` CHECK (LENGTH(TRIM(`section`)) >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 10. Table: `questions`
-- =============================================================================
-- Purpose: Form questions configurations.
-- Relationships: ManyToOne with feedback_forms.
-- JPA Mapping: Question Entity -> ManyToOne with FeedbackForm.
-- Reasoning: Contains display ordering unique keys to preserve ordering.
-- =============================================================================
CREATE TABLE IF NOT EXISTS `questions` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `feedback_form_id` BIGINT NOT NULL,
  `question_text` TEXT NOT NULL,
  `question_type` ENUM('TEXT', 'TEXTAREA', 'RADIO', 'CHECKBOX', 'DROPDOWN', 'RATING') NOT NULL,
  `is_mandatory` BOOLEAN NOT NULL DEFAULT FALSE,
  `display_order` INT NOT NULL,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NULL DEFAULT NULL,
  -- Enforce display sequence bounds uniqueness per form
  UNIQUE KEY `uk_feedbackform_question_order` (`feedback_form_id`, `display_order`),
  CONSTRAINT `fk_questions_feedbackform` FOREIGN KEY (`feedback_form_id`) 
    REFERENCES `feedback_forms` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 11. Table: `question_options`
-- =============================================================================
-- Purpose: Predefined choices config (radios, dropdowns). Enables deactivation flags.
-- Relationships: ManyToOne with questions.
-- JPA Mapping: Option Entity -> ManyToOne with Question.
-- Reasoning: Standardized lookup choices to support analysis categorization.
-- =============================================================================
CREATE TABLE IF NOT EXISTS `question_options` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `question_id` BIGINT NOT NULL,
  `option_value` VARCHAR(255) NOT NULL,
  `display_order` INT NOT NULL,
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE, -- Allows soft-deactivating options
  -- Enforce option sequence display orders uniqueness within a question
  UNIQUE KEY `uk_question_option_order` (`question_id`, `display_order`),
  CONSTRAINT `fk_options_question` FOREIGN KEY (`question_id`) 
    REFERENCES `questions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 12. Table: `responses`
-- =============================================================================
-- Purpose: Submission headers tracking individual completed forms.
-- Relationships: ManyToOne with feedback_forms, users (submitter).
-- JPA Mapping: Response Entity -> ManyToOne with FeedbackForm, User.
-- Reasoning: Strictly blocks multiple submissions per form-student combination.
-- Note on Device Audits: Fields like submitted_ip/submitted_browser are omitted 
-- because they create privacy (GDPR) concerns and add database size overhead for 
-- a college project without contributing value to aggregate feedback analysis.
-- =============================================================================
CREATE TABLE IF NOT EXISTS `responses` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `feedback_form_id` BIGINT NOT NULL,
  `submitter_id` BIGINT NOT NULL,
  `submitted_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  -- Prevent duplicate feedback submissions
  UNIQUE KEY `uk_user_feedbackform_submission` (`feedback_form_id`, `submitter_id`),
  CONSTRAINT `fk_responses_feedbackform` FOREIGN KEY (`feedback_form_id`) 
    REFERENCES `feedback_forms` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_responses_user` FOREIGN KEY (`submitter_id`) 
    REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 13. Table: `answers`
-- =============================================================================
-- Purpose: Individual question answers responses metadata.
-- Relationships: ManyToOne with responses, questions, question_options.
-- JPA Mapping: Answer Entity -> ManyToOne with Response, Question, Option.
-- Reasoning: Refactored text field to multi-type values schema to support metrics aggregation.
-- Constraint Details: Enforces that at most one answer type column is populated. 
-- In Spring Boot, validation is further reinforced in the Service layer to match 
-- the database state with QuestionType configurations (e.g. TEXT -> text_value).
-- =============================================================================
CREATE TABLE IF NOT EXISTS `answers` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `response_id` BIGINT NOT NULL,
  `question_id` BIGINT NOT NULL,
  `rating_value` INT NULL, -- Maps rating metrics
  `text_value` TEXT NULL, -- Maps free-text answers
  `selected_option_id` BIGINT NULL, -- Maps multiple choices links
  CONSTRAINT `fk_answers_response` FOREIGN KEY (`response_id`) 
    REFERENCES `responses` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_answers_question` FOREIGN KEY (`question_id`) 
    REFERENCES `questions` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_answers_option` FOREIGN KEY (`selected_option_id`) 
    REFERENCES `question_options` (`id`) ON DELETE SET NULL,
  -- Enforces rating metric bounds between 1 and 5 stars
  CONSTRAINT `chk_answers_rating` CHECK (`rating_value` IS NULL OR `rating_value` BETWEEN 1 AND 5),
  -- Multi-type integrity check: Enforces that AT MOST ONE value field is populated
  CONSTRAINT `chk_answers_value_type` CHECK (
    (rating_value IS NOT NULL AND text_value IS NULL AND selected_option_id IS NULL) OR
    (rating_value IS NULL AND text_value IS NOT NULL AND selected_option_id IS NULL) OR
    (rating_value IS NULL AND text_value IS NULL AND selected_option_id IS NOT NULL) OR
    (rating_value IS NULL AND text_value IS NULL AND selected_option_id IS NULL) -- skips optional answers
  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 14. Table: `notifications`
-- =============================================================================
-- Purpose: Platform alert records.
-- Relationships: ManyToOne with users.
-- JPA Mapping: Notification Entity -> ManyToOne with User.
-- Reasoning: Cascades alerts directly with user accounts profiles deletion status.
-- =============================================================================
CREATE TABLE IF NOT EXISTS `notifications` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `user_id` BIGINT NOT NULL,
  `title` VARCHAR(150) NOT NULL,
  `message` VARCHAR(500) NOT NULL,
  `notification_type` ENUM('SURVEY_ASSIGNED', 'SURVEY_CLOSING', 'RESPONSE_CONFIRMATION', 'SYSTEM_ALERT') NOT NULL DEFAULT 'SYSTEM_ALERT',
  `priority` ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL DEFAULT 'MEDIUM',
  `target_link` VARCHAR(255) NULL,
  `is_read` BOOLEAN NOT NULL DEFAULT FALSE,
  `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) 
    REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- 15. Table: `audit_logs`
-- =============================================================================
-- Purpose: Security logging events ledger.
-- Relationships: ManyToOne with users.
-- JPA Mapping: AuditLog Entity -> ManyToOne with User.
-- Reasoning: Refactored performer name to direct relational id to maintain references integrity.
-- =============================================================================
CREATE TABLE IF NOT EXISTS `audit_logs` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `action` VARCHAR(100) NOT NULL,
  `entity_name` VARCHAR(100) NOT NULL,
  `entity_id` BIGINT NULL,
  `performed_by` BIGINT NOT NULL,
  `performed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  `details` TEXT NULL,
  CONSTRAINT `fk_audit_logs_user` FOREIGN KEY (`performed_by`) 
    REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- RELATIONAL PERFORMANCE INDEXES
-- =============================================================================
-- Optimized queries execution footprints. Redundant indexes on unique columns 
-- (which are automatically indexed by MySQL) have been removed.
-- =============================================================================
CREATE INDEX `idx_users_isdeleted` ON `users` (`is_deleted`);
CREATE INDEX `idx_student_profiles_user` ON `student_profiles` (`user_id`);
CREATE INDEX `idx_faculty_profiles_user` ON `faculty_profiles` (`user_id`);
CREATE INDEX `idx_courses_department` ON `courses` (`department_id`);
CREATE INDEX `idx_course_assignments_faculty` ON `course_assignments` (`faculty_id`);
CREATE INDEX `idx_feedback_forms_status` ON `feedback_forms` (`status`);
CREATE INDEX `idx_feedback_forms_isdeleted` ON `feedback_forms` (`is_deleted`);
CREATE INDEX `idx_feedback_assignments_dept` ON `feedback_assignments` (`department_id`);
CREATE INDEX `idx_feedback_assignments_course` ON `feedback_assignments` (`course_id`);
CREATE INDEX `idx_questions_feedbackform` ON `questions` (`feedback_form_id`);
CREATE INDEX `idx_answers_response` ON `answers` (`response_id`);
CREATE INDEX `idx_answers_question` ON `answers` (`question_id`);
CREATE INDEX `idx_notifications_user_read` ON `notifications` (`user_id`, `is_read`);
CREATE INDEX `idx_audit_logs_user` ON `audit_logs` (`performed_by`);

-- =============================================================================
-- sample SEED DATA
-- =============================================================================
INSERT INTO `roles` (`id`, `name`) VALUES 
  (1, 'ROLE_STUDENT'), 
  (2, 'ROLE_FACULTY'), 
  (3, 'ROLE_ADMIN')
  ON DUPLICATE KEY UPDATE `name`=`name`;
  
INSERT INTO `departments` (`id`, `name`, `code`) VALUES 
  (1, 'Computer Science & Engineering', 'CSE'),
  (2, 'Information Technology', 'IT'),
  (3, 'Electronics & Communication Engineering', 'ECE')
  ON DUPLICATE KEY UPDATE `code`=`code`;

-- Admin User (dev-only): do not store or document default passwords in source control
INSERT INTO `users` (`id`, `name`, `email`, `password`, `department_id`, `role_id`) VALUES
  (1, 'System Administrator', 'admin@college.edu', '$2a$10$8.UnVuG9HHgffUDAlk8qOu5AycPZ9bU7u525fOBd56b0266V2g3S2', NULL, 3)
  ON DUPLICATE KEY UPDATE `email`=`email`;

-- Faculty User (dev-only): do not store or document default passwords in source control
INSERT INTO `users` (`id`, `name`, `email`, `password`, `department_id`, `role_id`) VALUES
  (2, 'Dr. Raj Kumar', 'faculty@college.edu', '$2a$10$8.UnVuG9HHgffUDAlk8qOu5AycPZ9bU7u525fOBd56b0266V2g3S2', 1, 2)
  ON DUPLICATE KEY UPDATE `email`=`email`;

INSERT INTO `faculty_profiles` (`id`, `user_id`, `employee_id`, `designation`, `joining_date`) VALUES
  (1, 2, 'EMP-CSE-201', 'Professor', '2020-06-15')
  ON DUPLICATE KEY UPDATE `employee_id`=`employee_id`;

-- Student User (dev-only): do not store or document default passwords in source control
INSERT INTO `users` (`id`, `name`, `email`, `password`, `department_id`, `role_id`) VALUES
  (3, 'Abhishek Sharma', 'student@college.edu', '$2a$10$8.UnVuG9HHgffUDAlk8qOu5AycPZ9bU7u525fOBd56b0266V2g3S2', 1, 1)
  ON DUPLICATE KEY UPDATE `email`=`email`;

INSERT INTO `student_profiles` (`id`, `user_id`, `roll_number`, `year`, `semester`, `section`, `batch`) VALUES
  (1, 3, '2023CSE045', 3, 6, 'A', '2023-2027')
  ON DUPLICATE KEY UPDATE `roll_number`=`roll_number`;

-- Sample Course
INSERT INTO `courses` (`id`, `name`, `code`, `department_id`) VALUES
  (1, 'Java Enterprise Programming', 'CS302', 1)
  ON DUPLICATE KEY UPDATE `code`=`code`;

-- Sample Course Assignment (Dr. Raj Kumar teaches Java Enterprise Programming)
INSERT INTO `course_assignments` (`id`, `course_id`, `faculty_id`, `academic_year`, `semester`, `section`) VALUES
  (1, 1, 2, '2025-2026', 6, 'A')
  ON DUPLICATE KEY UPDATE `course_id`=`course_id`;

-- Sample Feedback Form (mid-term evaluations)
INSERT INTO `feedback_forms` (`id`, `title`, `description`, `category`, `start_date`, `end_date`, `status`, `creator_id`) VALUES
  (1, 'Mid-Semester Theory Course Feedback', 'Please provide constructive feedback regarding course coverage and clarity.', 'COURSE', '2026-07-01 00:00:00', '2026-07-31 23:59:59', 'ACTIVE', 1)
  ON DUPLICATE KEY UPDATE `title`=`title`;

-- Sample Feedback Form Assignment (Assign CSE, Semester 6, section A, batch 2023-2027 Java evaluation)
INSERT INTO `feedback_assignments` (`id`, `feedback_form_id`, `department_id`, `course_id`, `semester`, `section`, `batch`, `academic_year`) VALUES
  (1, 1, 1, 1, 6, 'A', '2023-2027', '2025-2026')
  ON DUPLICATE KEY UPDATE `feedback_form_id`=`feedback_form_id`;

-- Sample Form Questions
INSERT INTO `questions` (`id`, `feedback_form_id`, `question_text`, `question_type`, `is_mandatory`, `display_order`) VALUES
  (1, 1, 'Rate the instructor\'s coverage of syllabus.', 'RATING', TRUE, 1),
  (2, 1, 'Do you receive timely doubt-solving support?', 'RADIO', TRUE, 2),
  (3, 1, 'Write any specific topics you want extra classes for.', 'TEXTAREA', FALSE, 3)
  ON DUPLICATE KEY UPDATE `question_text`=`question_text`;

-- Options for Radio Question (Doubt solving)
INSERT INTO `question_options` (`id`, `question_id`, `option_value`, `display_order`) VALUES
  (1, 2, 'Always', 1),
  (2, 2, 'Mostly', 2),
  (3, 2, 'Rarely', 3)
  ON DUPLICATE KEY UPDATE `option_value`=`option_value`;
