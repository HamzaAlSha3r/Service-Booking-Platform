-- Initialize Roles
-- Run this script ONCE after creating the database schema
-- هذا السكريبت يضيف الأدوار الأساسية في النظام

INSERT INTO role (id, name) VALUES (1, 'CUSTOMER') ON CONFLICT (name) DO NOTHING;
INSERT INTO role (id, name) VALUES (2, 'SERVICE_PROVIDER') ON CONFLICT (name) DO NOTHING;
INSERT INTO role (id, name) VALUES (3, 'ADMIN') ON CONFLICT (name) DO NOTHING;

-- Reset sequence for role id
SELECT setval('role_id_seq', (SELECT MAX(id) FROM role));

