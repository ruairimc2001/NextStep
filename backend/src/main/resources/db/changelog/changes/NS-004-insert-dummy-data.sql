-- liquibase formatted sql

-- changeset ruairi:NS-004-insert-dummy-data
INSERT INTO users (id, email, password_hash, created_at) VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'john.doe@example.com', 'password123', NOW()),
    ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'jane.smith@example.com', 'password456', NOW()),
    ('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'test@test.com', 'test', NOW()),
    ('d3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'admin@nextsteps.com', 'admin123', NOW());

-- Insert dummy profiles for the test users
INSERT INTO profiles (user_id, goal_title, skills_text, interests_text, updated_at) VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Become a Full Stack Developer', 'JavaScript, HTML, CSS, Basic Java', 'Web Development, Mobile Apps, Cloud Computing', NOW()),
    ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Data Scientist Career Path', 'Python, Statistics, SQL', 'Machine Learning, Data Analysis, AI', NOW()),
    ('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Learn DevOps', 'Linux, Git, Basic Scripting', 'CI/CD, Docker, Kubernetes', NOW());

