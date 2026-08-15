-- Seed data for the h2 demo profile only. Gives the API a creator to hang forms off,
-- since POST /api/forms resolves the X-User-Id header against a real user row.
INSERT INTO roles (id, name) VALUES (1, 'ROLE_ADMIN');

INSERT INTO users (id, name, email, password, role_id, active, is_deleted, created_at, updated_at)
VALUES (1, 'Demo Admin', 'admin@fbcs.local', 'seeded-not-a-real-hash', 1, TRUE, FALSE,
        CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO departments (id, name, code, active, created_at)
VALUES (1, 'Computer Science and Engineering', 'CSE', TRUE, CURRENT_TIMESTAMP());
