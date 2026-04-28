-- Шаг B Фазы 2: email-верификация.

-- Изменяем default статус для НОВЫХ пользователей. Существующие остаются ACTIVE.
ALTER TABLE users ALTER COLUMN status SET DEFAULT 'UNVERIFIED';

-- Добавляем поле когда юзер подтвердил email
ALTER TABLE users ADD COLUMN verified_at TIMESTAMP;

-- Существующих пользователей помечаем как уже верифицированных (миграция данных)
UPDATE users SET verified_at = created_at WHERE status = 'ACTIVE';

-- Таблица email_verifications для аудита
CREATE TABLE email_verifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    used_at TIMESTAMP
);

CREATE INDEX idx_email_verifications_user_id ON email_verifications(user_id);
CREATE INDEX idx_email_verifications_expires_at ON email_verifications(expires_at);
