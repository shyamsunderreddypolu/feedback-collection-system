-- Development-only Database Reset Script
-- Drops all FBCS tables in correct dependency order to clean the database

USE fbcs_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS `audit_logs`;
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `answers`;
DROP TABLE IF EXISTS `responses`;
DROP TABLE IF EXISTS `question_options`;
DROP TABLE IF EXISTS `questions`;
DROP TABLE IF EXISTS `feedback_assignments`;
DROP TABLE IF EXISTS `feedback_forms`;
DROP TABLE IF EXISTS `course_assignments`;
DROP TABLE IF EXISTS `courses`;
DROP TABLE IF EXISTS `student_profiles`;
DROP TABLE IF EXISTS `faculty_profiles`;
DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `roles`;
DROP TABLE IF EXISTS `departments`;
SET FOREIGN_KEY_CHECKS = 1;

SELECT 'Database tables dropped successfully' AS Message;
