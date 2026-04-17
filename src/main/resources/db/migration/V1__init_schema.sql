-- V1: Initial schema
-- CREATE TABLE IF NOT EXISTS — safe for existing DBs (no-op) and fresh DBs (creates tables)
-- baseline-on-migrate=true means Flyway marks version 0 as baseline and runs V1 on first start

CREATE TABLE IF NOT EXISTS users (
    id       BIGSERIAL PRIMARY KEY,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS profiles (
    id                   BIGSERIAL PRIMARY KEY,
    full_name            VARCHAR(255),
    age                  INTEGER,
    gender               VARCHAR(255),
    height_cm            DOUBLE PRECISION,
    weight_kg            DOUBLE PRECISION,
    goal                 VARCHAR(255),
    onboarding_completed BOOLEAN NOT NULL DEFAULT false,
    user_id              BIGINT  NOT NULL UNIQUE,
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS meals (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255),
    meal_date       TIMESTAMP,
    total_calories  DOUBLE PRECISION,
    user_id         BIGINT,
    CONSTRAINT fk_meals_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS meal_items (
    id       BIGSERIAL PRIMARY KEY,
    name     VARCHAR(255),
    calories DOUBLE PRECISION,
    protein  DOUBLE PRECISION,
    carbs    DOUBLE PRECISION,
    fat      DOUBLE PRECISION,
    quantity DOUBLE PRECISION,
    meal_id  BIGINT,
    CONSTRAINT fk_meal_items_meal FOREIGN KEY (meal_id) REFERENCES meals (id)
);

CREATE TABLE IF NOT EXISTS workouts (
    id               BIGSERIAL PRIMARY KEY,
    type             VARCHAR(255),
    duration_minutes INTEGER,
    calories_burned  DOUBLE PRECISION,
    workout_date     TIMESTAMP,
    user_id          BIGINT,
    CONSTRAINT fk_workouts_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS health_metrics (
    id              BIGSERIAL PRIMARY KEY,
    steps           INTEGER,
    heart_rate      INTEGER,
    calories_burned DOUBLE PRECISION,
    sleep_hours     DOUBLE PRECISION,
    recorded_at     TIMESTAMP,
    user_id         BIGINT,
    CONSTRAINT fk_health_metrics_user FOREIGN KEY (user_id) REFERENCES users (id)
);
