CREATE TABLE profiles (
    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT NOT NULL UNIQUE,
    full_name            VARCHAR(255),
    age                  INTEGER,
    gender               VARCHAR(20),
    height_cm            DOUBLE PRECISION,
    weight_kg            DOUBLE PRECISION,
    goal                 VARCHAR(50),
    activity_level       VARCHAR(50),
    onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_profiles_user_id ON profiles(user_id);
