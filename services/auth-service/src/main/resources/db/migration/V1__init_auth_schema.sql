-- Шаг A Фазы 2: только User. status пока всегда ACTIVE.
-- Шаг B добавит email_verifications и переключит дефолт на UNVERIFIED.

CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(50)  NOT NULL DEFAULT 'USER',
    status      VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
