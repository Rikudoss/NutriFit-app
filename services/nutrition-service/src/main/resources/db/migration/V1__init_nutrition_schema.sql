CREATE TABLE meals (
    id             BIGSERIAL    PRIMARY KEY,
    user_id        BIGINT       NOT NULL,
    name           VARCHAR(255) NOT NULL,
    meal_type      VARCHAR(20),
    meal_date      TIMESTAMP    NOT NULL,
    total_calories DOUBLE PRECISION,
    total_protein  DOUBLE PRECISION,
    total_carbs    DOUBLE PRECISION,
    total_fat      DOUBLE PRECISION,
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_meals_user_id   ON meals(user_id);
CREATE INDEX idx_meals_user_date ON meals(user_id, meal_date);

CREATE TABLE meal_items (
    id                BIGSERIAL    PRIMARY KEY,
    meal_id           BIGINT       NOT NULL REFERENCES meals(id) ON DELETE CASCADE,
    name              VARCHAR(255) NOT NULL,
    calories_per_100g DOUBLE PRECISION,
    protein           DOUBLE PRECISION,
    carbs             DOUBLE PRECISION,
    fat               DOUBLE PRECISION,
    quantity          DOUBLE PRECISION NOT NULL
);

CREATE INDEX idx_meal_items_meal_id ON meal_items(meal_id);

CREATE TABLE nutrition_goals (
    id               BIGSERIAL   PRIMARY KEY,
    user_id          BIGINT      NOT NULL UNIQUE,
    daily_calories   DOUBLE PRECISION,
    protein_target   DOUBLE PRECISION,
    carbs_target     DOUBLE PRECISION,
    fat_target       DOUBLE PRECISION,
    created_at       TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_nutrition_goals_user_id ON nutrition_goals(user_id);
