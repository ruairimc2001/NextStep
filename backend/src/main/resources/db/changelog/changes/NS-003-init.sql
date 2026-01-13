-- liquibase formatted sql

-- changeset ruairi:NS-003-init
CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255),
                       created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE profiles (
                          user_id UUID PRIMARY KEY,
                          goal_title VARCHAR(255),
                          skills_text TEXT,
                          interests_text TEXT,
                          updated_at TIMESTAMPTZ NOT NULL,
                          CONSTRAINT fk_profiles_user
                              FOREIGN KEY (user_id)
                                  REFERENCES users(id)
                                  ON DELETE CASCADE
);

CREATE TABLE roadmaps (
                          id UUID PRIMARY KEY,
                          user_id UUID NOT NULL,
                          title VARCHAR(255) NOT NULL,
                          raw_ai_output TEXT,
                          created_at TIMESTAMPTZ NOT NULL,
                          CONSTRAINT fk_roadmaps_user
                              FOREIGN KEY (user_id)
                                  REFERENCES users(id)
                                  ON DELETE CASCADE
);

CREATE INDEX idx_roadmaps_user_id ON roadmaps(user_id);
