# NutriFit AI — Архитектурный план миграции

> Версия: 1.0 | Дата: 2026-04-17  
> Статус: **АКТИВНЫЙ КОНТРАКТ** — не менять без обсуждения  
> Источник: выработан в рамках дипломного проекта

---

## ОБЗОР ПРОЕКТА

**Название:** NutriFit AI Backend  
**Тип:** Дипломный проект — Spring Boot монолит → микросервисная архитектура  
**Цель:** Персональный цифровой фитнес-ассистент с учётом питания, тренировок, метрик здоровья, интеграцией фитнес-устройств и AI-рекомендациями.

**Три модуля:**
1. **Классическая ИС** — питание, тренировки, метрики, профиль
2. **Аналитический модуль** — BMR/TDEE, macro splits, тренды, прогресс к цели
3. **Интеллектуальный модуль** — AI-рекомендации через OpenAI на основе данных пользователя

---

## РАЗДЕЛ A — ЦЕЛЕВАЯ АРХИТЕКТУРА

### Диаграмма сервисов

```
              ┌─────────────────────────────────────┐
              │             КЛИЕНТЫ                  │
              │  iOS  │  Android  │  Web  │  Postman │
              └───────────────┬─────────────────────┘
                              │ HTTPS
              ┌───────────────▼─────────────────────┐
              │           API GATEWAY                │
              │       (Spring Cloud Gateway)         │
              │  • Роутинг по /api/{service}/**      │
              │  • Валидация JWT (локально)          │
              │  • Rate limiting (Redis)             │
              │  • CORS                              │
              │  • Добавляет X-User-Id заголовок    │
              └──┬──────┬──────┬──────┬──────┬──────┘
                 │      │      │      │      │
    ┌────────────▼──┐ ┌──▼───┐ ┌──▼───┐ ┌───▼────┐ ┌───▼─────┐
    │ auth-service  │ │ user │ │nutri-│ │workout-│ │metrics- │
    │   :8081       │ │:8082 │ │tion  │ │service │ │service  │
    │               │ │      │ │:8083 │ │ :8084  │ │  :8085  │
    │ register      │ │profil│ │meals │ │workouts│ │steps    │
    │ login         │ │onbrd │ │items │ │act.type│ │heartrate│
    │ verify email  │ │sttgs │ │fooddb│ │        │ │sleep    │
    │ refresh token │ │      │ │      │ │        │ │         │
    └───────────────┘ └──────┘ └──────┘ └────────┘ └─────────┘

    ┌──────────────────────────────────────────────────────────┐
    │            wearables-service :8088                       │
    │  OAuth2 (Fitbit, Garmin, Polar) | Strategy adapters      │
    │  Scheduled sync + Webhook receiver                       │
    └──────────────────────────────────────────────────────────┘

              ┌────────────────────────────────────────┐
              │           EVENT BUS (Kafka)             │
              │  user.registered / user.verified        │
              │  meal.created / meal.updated            │
              │  workout.logged / workout.updated       │
              │  metrics.recorded                       │
              │  wearable.synced                        │
              └─────────────────┬──────────────────────┘
                                │
           ┌────────────────────┴──────────────────────┐
           │                                           │
  ┌────────▼───────────┐              ┌────────────────▼───┐
  │ analytics-service  │              │    ai-service      │
  │      :8086         │◄─ /internal ─│      :8087         │
  │ BMR / TDEE         │  /analytics/ │ Recommendations    │
  │ Calorie balance    │  context     │ OpenAI client      │
  │ Macro splits       │              │ Rate limiting      │
  │ Weekly trends      │              │ Cost tracking      │
  └────────────────────┘              └────────────────────┘

              ┌────────────────────────────────────────┐
              │            ИНФРАСТРУКТУРА               │
              │  config-server   :8888                  │
              │  discovery       :8761  (Eureka)        │
              │  Redis           :6379                  │
              │  Zipkin          :9411  (tracing)       │
              │  Prometheus      :9090                  │
              │  Grafana         :3000                  │
              └────────────────────────────────────────┘
```

---

### Сервисы: зоны ответственности

#### auth-service (:8081)
**БД:** `auth_db` | **Внешние:** Redis, SMTP

| Endpoint | Метод | Описание |
|---|---|---|
| `/api/auth/register` | POST | Создать пользователя (status=UNVERIFIED) |
| `/api/auth/verify` | POST | Подтвердить email по 6-digit коду |
| `/api/auth/resend-verification` | POST | Повторно отправить код (rate limited) |
| `/api/auth/login` | POST | Вход → access + refresh token |
| `/api/auth/refresh` | POST | Обновить access token |
| `/api/auth/logout` | POST | Инвалидировать refresh token |
| `/api/auth/forgot-password` | POST | Запросить сброс пароля |
| `/api/auth/reset-password` | POST | Сбросить пароль по токену |
| `/internal/auth/validate` | GET | Валидация токена (для Gateway) |

Публикует: `user.registered`, `user.verified`

---

#### user-service (:8082)
**БД:** `user_db` | Слушает: `user.registered` → создаёт профиль

| Endpoint | Метод | Описание |
|---|---|---|
| `/api/users/profile` | GET / PUT / PATCH | Профиль пользователя |
| `/api/users/onboarding/status` | GET | Статус + прогресс онбординга |
| `/api/users/onboarding/complete` | POST | Завершить онбординг |
| `/internal/users/{userId}` | GET | Для межсервисного доступа |

---

#### nutrition-service (:8083)
**БД:** `nutrition_db` | Публикует: `meal.created`, `meal.updated`, `meal.deleted`

| Endpoint | Метод | Описание |
|---|---|---|
| `/api/nutrition/meals` | POST / GET | CRUD приёмов пищи |
| `/api/nutrition/meals/{id}` | GET / PUT / DELETE | |
| `/api/nutrition/meals/range` | GET | За период (?from=&to=) |
| `/api/nutrition/foods/search` | GET | Поиск по food database |
| `/internal/nutrition/summary` | GET | Для AI/Analytics |

---

#### workout-service (:8084)
**БД:** `workout_db` | Публикует: `workout.logged`, `workout.updated`, `workout.deleted`

| Endpoint | Метод | Описание |
|---|---|---|
| `/api/workouts` | POST / GET | CRUD тренировок |
| `/api/workouts/{id}` | GET / PUT / DELETE | |
| `/api/workouts/range` | GET | За период |
| `/internal/workouts/summary` | GET | |

---

#### metrics-service (:8085)
**БД:** `metrics_db` | Слушает: `wearable.synced` | Публикует: `metrics.recorded`

| Endpoint | Метод | Описание |
|---|---|---|
| `/api/metrics` | POST / GET | CRUD метрик |
| `/api/metrics/daily-summary` | GET | Агрегированные дневные данные |
| `/api/metrics/range` | GET | За период |
| `/internal/metrics/summary` | GET | |

---

#### wearables-service (:8088)
**БД:** `wearables_db` | Публикует: `wearable.synced`

| Endpoint | Метод | Описание |
|---|---|---|
| `/api/wearables/connect/{provider}` | GET | Инициировать OAuth2 |
| `/api/wearables/callback/{provider}` | GET | OAuth2 redirect callback |
| `/api/wearables/devices` | GET | Список устройств |
| `/api/wearables/devices/{id}` | DELETE | Отключить устройство |
| `/api/wearables/sync/{provider}` | POST | Ручная синхронизация |
| `/webhooks/{provider}` | POST | Webhook от провайдера |

Провайдеры: **Fitbit** (первый), Garmin, Polar.  
Apple HealthKit — только через нативный iOS SDK, REST API отсутствует.  
Google Fit REST API — deprecated, не использовать.

---

#### analytics-service (:8086)
**БД:** `analytics_db` + Redis (TTL 1 час)  
Слушает: `meal.created`, `workout.logged`, `metrics.recorded` → обновление кеша

| Endpoint | Метод | Описание |
|---|---|---|
| `/api/analytics/bmr-tdee` | GET | BMR + TDEE по профилю |
| `/api/analytics/calorie-balance` | GET | Баланс калорий за период |
| `/api/analytics/macros` | GET | Распределение макронутриентов |
| `/api/analytics/progress` | GET | Прогресс к цели |
| `/api/analytics/weekly-summary` | GET | Недельный отчёт |
| `/api/analytics/weight-trend` | GET | Тренд веса |
| `/internal/analytics/context/{userId}` | GET | Полный контекст для ai-service |

---

#### ai-service (:8087)
**БД:** `ai_db` + Redis (кеш TTL 4 ч, rate limit 10 req/day)  
Слушает: `meal.created`, `workout.logged` → инвалидация кеша  
Зависит от: `analytics-service` (internal вызов для контекста)

| Endpoint | Метод | Описание |
|---|---|---|
| `/api/ai/recommend` | POST | Персонализированные рекомендации |
| `/api/ai/history` | GET | История рекомендаций |
| `/api/ai/usage` | GET | Статистика токенов |

---

### Технологический стек

| Компонент | Технология | Версия |
|---|---|---|
| Язык | Java | 21 |
| Фреймворк | Spring Boot | 3.5.x |
| API Gateway | Spring Cloud Gateway | 4.x |
| Service Discovery | Spring Cloud Netflix Eureka | 4.x |
| Config | Spring Cloud Config | 4.x |
| Межсервисные HTTP | Spring Cloud OpenFeign | 4.x |
| Event Bus | **Apache Kafka** | 3.7.x |
| Cache / Rate limit | Redis | 7.x |
| БД | PostgreSQL | 15 |
| Миграции | Flyway | (через Spring Boot) |
| Auth | JWT (JJWT 0.11.5) | |
| Трейсинг | Micrometer Tracing + Zipkin | |
| Метрики | Micrometer + Prometheus + Grafana | |
| Логи | Loki + Grafana Alloy | |
| Тесты | JUnit 5 + Mockito + Testcontainers | |
| API Docs | springdoc-openapi (Swagger UI) | 2.x |
| Email dev | Mailtrap | |
| Email prod | SendGrid | |

**Kafka выбран vs RabbitMQ:** analytics-service строит read-model из событий с возможностью replay — это ключевое преимущество Kafka для аналитики.

---

### Межсервисная аутентификация

```
Клиент → API Gateway:
  1. Извлечь JWT из Authorization header
  2. Валидировать подпись локально (shared JWT_SECRET)
  3. Декодировать: userId, email, role
  4. Добавить к downstream запросу:
       X-User-Id:    42
       X-User-Email: user@example.com
       X-User-Role:  USER

Сервис → Сервис (internal endpoints):
  Фаза 1-7: заголовок X-Internal-Secret: ${INTERNAL_API_SECRET}
            endpoints доступны только внутри Docker network
  Фаза 8+:  service account JWT (при переходе на K8s)
```

---

## РАЗДЕЛ B — МОДЕЛИ ДАННЫХ

### auth_db

```sql
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(50)  NOT NULL DEFAULT 'USER',
    status        VARCHAR(50)  NOT NULL DEFAULT 'UNVERIFIED',
    -- UNVERIFIED | ACTIVE | SUSPENDED | DELETED
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE email_verifications (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    code       VARCHAR(6) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    attempts   INT        NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_verification UNIQUE (user_id)
);

CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMP    NOT NULL,
    revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    device_info VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE password_reset_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL UNIQUE,
    expires_at  TIMESTAMP    NOT NULL,
    used        BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);
```

Redis: `email_verify:{userId}` TTL=600s, `resend_limit:{userId}` для rate limiting.

### user_db

```sql
CREATE TABLE profiles (
    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT       NOT NULL UNIQUE,
    full_name            VARCHAR(255),
    age                  INT,
    gender               VARCHAR(10),   -- MALE | FEMALE
    height_cm            DECIMAL(5,2),
    weight_kg            DECIMAL(5,2),
    goal                 VARCHAR(30),   -- LOSE_WEIGHT | GAIN_MUSCLE_MASS | KEEP_FIT
    activity_level       VARCHAR(20),   -- SEDENTARY | LIGHT | MODERATE | ACTIVE | VERY_ACTIVE
    onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### nutrition_db

```sql
CREATE TABLE food_database (
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    brand             VARCHAR(255),
    calories_per_100g DECIMAL(7,2) NOT NULL,
    protein_per_100g  DECIMAL(7,2),
    carbs_per_100g    DECIMAL(7,2),
    fat_per_100g      DECIMAL(7,2),
    source            VARCHAR(50)   -- USER_ADDED | MANUAL
);

CREATE TABLE meals (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT        NOT NULL,
    name           VARCHAR(255)  NOT NULL,
    meal_date      TIMESTAMP     NOT NULL,
    total_calories DECIMAL(8,2),
    total_protein  DECIMAL(7,2),
    total_carbs    DECIMAL(7,2),
    total_fat      DECIMAL(7,2),
    created_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE meal_items (
    id             BIGSERIAL PRIMARY KEY,
    meal_id        BIGINT       NOT NULL REFERENCES meals(id) ON DELETE CASCADE,
    food_id        BIGINT       REFERENCES food_database(id),
    name           VARCHAR(255) NOT NULL,
    quantity_grams DECIMAL(7,2) NOT NULL,
    -- пре-рассчитано: calories = calories_per_100g * quantity_grams / 100
    calories       DECIMAL(7,2) NOT NULL,
    protein        DECIMAL(7,2),
    carbs          DECIMAL(7,2),
    fat            DECIMAL(7,2)
);
```

**Исправление бага монолита:** calories теперь учитывает quantity_grams.

### workout_db

```sql
CREATE TABLE activity_types (
    id        BIGSERIAL PRIMARY KEY,
    name      VARCHAR(100) NOT NULL,
    met_value DECIMAL(4,2),
    category  VARCHAR(50)
);

CREATE TABLE workouts (
    id               BIGSERIAL PRIMARY KEY,
    user_id          BIGINT       NOT NULL,
    type             VARCHAR(100) NOT NULL,
    duration_minutes INT,
    calories_burned  DECIMAL(7,2),
    workout_date     TIMESTAMP    NOT NULL,
    source           VARCHAR(20)  DEFAULT 'MANUAL',
    notes            TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);
```

### metrics_db

```sql
CREATE TABLE health_metrics (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    steps           INT,
    heart_rate      INT,
    calories_burned DECIMAL(7,2),
    sleep_hours     DECIMAL(4,2),
    recorded_at     TIMESTAMP    NOT NULL,
    source          VARCHAR(20)  DEFAULT 'MANUAL',
    device_id       BIGINT
);

CREATE TABLE daily_summaries (
    id                    BIGSERIAL PRIMARY KEY,
    user_id               BIGINT NOT NULL,
    summary_date          DATE   NOT NULL,
    total_steps           INT,
    avg_heart_rate        INT,
    total_calories_burned DECIMAL(7,2),
    total_sleep_hours     DECIMAL(4,2),
    CONSTRAINT uq_user_date UNIQUE (user_id, summary_date)
);
```

### wearables_db

```sql
CREATE TABLE connected_devices (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    provider          VARCHAR(30)  NOT NULL,  -- FITBIT | GARMIN | POLAR
    external_user_id  VARCHAR(255),
    access_token_enc  TEXT,           -- AES-256 зашифрован
    refresh_token_enc TEXT,
    token_expires_at  TIMESTAMP,
    scopes            TEXT[],
    last_synced_at    TIMESTAMP,
    is_active         BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_provider UNIQUE (user_id, provider)
);

CREATE TABLE sync_logs (
    id                BIGSERIAL PRIMARY KEY,
    device_id         BIGINT    NOT NULL REFERENCES connected_devices(id),
    sync_started_at   TIMESTAMP NOT NULL,
    sync_completed_at TIMESTAMP,
    records_synced    INT       DEFAULT 0,
    date_range_from   DATE,
    date_range_to     DATE,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    error_message     TEXT,
    trigger_type      VARCHAR(20)
);
```

---

## РАЗДЕЛ C — ПЛАН МИГРАЦИИ (фазы)

### Фаза 0 — Стабилизация монолита (БЛОКИРУЮЩАЯ)

| # | Задача | Ключевые файлы |
|---|---|---|
| 0.1 | Вынести секреты в env-переменные | `application.properties`, `.env`, `.gitignore` |
| 0.2 | Исправить CORS (`"*"` → конкретные origins) | `CorsConfig.java` |
| 0.3 | Добавить Flyway + `ddl-auto=validate` | `pom.xml`, `V1__init_schema.sql` |
| 0.4 | Создать DTO для Workout и HealthMetric | новые файлы DTO |
| 0.5 | Починить ProfileController (убрать прямой доступ к репозиторию) | `ProfileController.java` |
| 0.6 | Починить OnboardingController (убрать дублирование) | `OnboardingController.java` |
| 0.7 | Убрать репозитории из AIController | `AIController.java` |
| 0.8 | Исправить расчёт калорий (учесть quantity) | `CaloriesCalculator.java`, `MealItem.java` |
| 0.9 | Добавить handler для `IllegalStateException` | `GlobalExceptionHandler.java` |
| 0.10 | Добавить `@Transactional` в WorkoutService, MetricsService | service файлы |
| 0.11 | Исправить N+1 в NutritionService | `NutritionService.java` |
| 0.12 | Починить пакет теста + unit-тесты | test файлы |
| 0.13 | Удалить `.idea` из git | `.gitignore` |

### Фаза 1 — Инфраструктура

| # | Задача |
|---|---|
| 1.1 | `config-server` (Spring Cloud Config) |
| 1.2 | `discovery-server` (Eureka) |
| 1.3 | `api-gateway` (Spring Cloud Gateway) → роутинг на монолит |
| 1.4 | Docker Compose: PostgreSQL, Redis, Kafka+ZK, Zipkin, Prometheus, Grafana |
| 1.5 | Dockerfile для монолита (multi-stage) |
| 1.6 | Подключить монолит к Eureka + Config Server |

### Фаза 2 — auth-service + email-верификация

| # | Задача |
|---|---|
| 2.1 | Создать Spring Boot проект auth-service |
| 2.2 | Перенести: User, UserRepository, AuthService, JwtUtil |
| 2.3 | Добавить email_verifications + EmailVerificationService |
| 2.4 | Статусы UNVERIFIED/ACTIVE в User |
| 2.5 | Refresh tokens |
| 2.6 | Rate limiting на верификацию (Redis) |
| 2.7 | Spring Mail + Mailtrap |
| 2.8 | Kafka: `user.registered`, `user.verified` |
| 2.9 | Flyway для auth_db |
| 2.10 | Unit + Integration тесты (Testcontainers: PostgreSQL + Redis) |
| 2.11 | Gateway: валидация JWT + проброс X-User-Id |
| 2.12 | Монолит: читать userId из заголовка |

### Фаза 3 — user-service

| # | Задача |
|---|---|
| 3.1 | Создать user-service |
| 3.2 | Перенести Profile + ProfileService + OnboardingController |
| 3.3 | Добавить `activity_level` в профиль |
| 3.4 | Слушать `user.registered` → создавать профиль |
| 3.5 | Internal endpoint `/internal/users/{userId}` |
| 3.6 | Flyway + тесты |
| 3.7 | Удалить profile-код из монолита |

### Фазы 4–6 — nutrition / workout / metrics (по шаблону)

Каждая фаза: создать сервис → перенести код → Flyway → Kafka events → internal endpoint → тесты → удалить из монолита.

### Фаза 7 — analytics-service

| # | Задача |
|---|---|
| 7.1 | Создать analytics-service |
| 7.2 | BMR/TDEE (формула Миффлина-Сан Жеора) |
| 7.3 | Calorie balance за период |
| 7.4 | Macro splits (по цели) |
| 7.5 | Weekly summary + weight trend |
| 7.6 | Kafka Consumer → read-model из событий |
| 7.7 | Redis кеш (TTL 1 час) |
| 7.8 | Internal `/internal/analytics/context/{userId}` |
| 7.9 | Тесты для расчётов |

### Фаза 8 — wearables-service

| # | Задача |
|---|---|
| 8.1 | Создать wearables-service |
| 8.2 | `WearableProvider` интерфейс (Strategy pattern) |
| 8.3 | `FitbitProvider` (первый) |
| 8.4 | OAuth2 flow: connect → callback → зашифрованные tokens |
| 8.5 | Scheduled sync (каждые 30 мин) |
| 8.6 | Webhook receiver для Fitbit |
| 8.7 | Kafka: `wearable.synced` |
| 8.8 | Конфликт DEVICE vs MANUAL |
| 8.9 | Flyway + тесты |
| 8.10 | (Опционально) Garmin, Polar |

### Фаза 9 — ai-service

| # | Задача |
|---|---|
| 9.1 | Создать ai-service (вынести из монолита) |
| 9.2 | Feign-клиент к analytics-service |
| 9.3 | Redis кеш рекомендаций (TTL 4 ч) |
| 9.4 | Kafka Consumer: инвалидация кеша |
| 9.5 | Rate limiting 10 req/day (Redis) |
| 9.6 | Трекинг стоимости токенов |
| 9.7 | Тесты |
| 9.8 | Удалить AI-код из монолита |

---

## РАЗДЕЛ D — ДЕТАЛИ КЛЮЧЕВЫХ ФИЧ

### Email-верификация: состояния и flow

```
Состояния: UNVERIFIED → ACTIVE → (SUSPENDED | DELETED)

POST /register
  → User(status=UNVERIFIED)
  → code = String.format("%06d", random.nextInt(1_000_000))
  → Redis: key="email_verify:{userId}", value=code, TTL=600s
  → Отправить email
  → Ответ: 201 {message, userId}  (БЕЗ JWT!)

POST /verify {userId, code}
  → Нет ключа в Redis → 410 GONE "Code expired"
  → Неверный код → attempts++
  → attempts >= 5 → удалить ключ → 429
  → Верный → DELETE Redis → status=ACTIVE → вернуть JWT

POST /resend-verification
  → Redis: "resend_limit:{userId}" — интервал 60s, макс 5/час
  → Превышение → 429
  → Новый код → обновить Redis

EDGE CASES:
  Повторная регистрация с тем же email:
    ACTIVE → 409 "Email already registered"
    UNVERIFIED + < 10 мин → "Code already sent, check email"
    UNVERIFIED + > 10 мин → перезаписать, новый код

  Forgot password:
    → UUID-токен (не 6-значный), hash в password_reset_tokens, TTL 15 мин
    → Ответ /forgot-password ВСЕГДА 200 (защита от email enumeration)
    → При сбросе: инвалидировать ВСЕ refresh_tokens пользователя
```

### Wearables: Strategy pattern

```java
interface WearableProvider {
    ProviderType getType();
    String buildAuthorizationUrl(String userId, String redirectUri, String state);
    TokenPair exchangeCode(String code, String redirectUri);
    TokenPair refreshAccessToken(String refreshToken);
    List<NormalizedMetric> fetchMetrics(String accessToken, LocalDate from, LocalDate to);
    List<NormalizedActivity> fetchActivities(String accessToken, LocalDate from, LocalDate to);
    boolean supportsWebhooks();
    boolean verifyWebhookSignature(String payload, String signature);
}

// Нормализованная модель (одинакова для всех провайдеров)
NormalizedMetric { LocalDate date, Integer steps, Integer avgHeartRate,
                   Double caloriesBurned, Double sleepHours, ProviderType source }
```

**Конфликт DEVICE vs MANUAL:** для MVP — DEVICE перезаписывает MANUAL за тот же день.

### Аналитика: формулы

```
BMR (Mifflin-St Jeor):
  Мужчины:  10×weight_kg + 6.25×height_cm − 5×age + 5
  Женщины:  10×weight_kg + 6.25×height_cm − 5×age − 161

TDEE = BMR × activity_factor:
  SEDENTARY   → × 1.200  |  LIGHT     → × 1.375
  MODERATE    → × 1.550  |  ACTIVE    → × 1.725
  VERY_ACTIVE → × 1.900

Дефицит/профицит:
  calorie_balance = total_consumed − TDEE
  projected_weekly_change_kg = (calorie_balance × 7) / 7700

Macro targets (на кг веса):
  LOSE_WEIGHT:  protein=2.2g/kg, fat=0.8g/kg
  GAIN_MUSCLE:  protein=2.5g/kg, fat=1.0g/kg
  KEEP_FIT:     protein=1.8g/kg, fat=0.9g/kg
  Carbs = остаток от TDEE после protein×4 + fat×9
```

### AI-сервис: поток данных

```
POST /api/ai/recommend
  1. Проверить rate limit (Redis: "ai_rate:{userId}:{date}", 10/day)
  2. Проверить кеш (Redis: "ai_rec:{userId}:{date}", TTL 4h)
  3. GET /internal/analytics/context/{userId}
  4. Построить prompt (лимит ~2000 tokens контекста)
  5. OpenAI gpt-4o-mini, max_tokens=600
  6. Сохранить в recommendation_history (tokens, cost)
  7. Записать в кеш → вернуть клиенту

Инвалидация кеша: Kafka Consumer на meal.created, workout.logged
Стоимость: ~$0.00036/запрос, 10 req/day × 1000 users ≈ $108/мес
```

---

## РАЗДЕЛ E — СТРУКТУРА МОНОРЕПО (целевая)

```
nutrifit-app/
├── services/
│   ├── api-gateway/
│   ├── auth-service/
│   ├── user-service/
│   ├── nutrition-service/
│   ├── workout-service/
│   ├── metrics-service/
│   ├── wearables-service/
│   ├── analytics-service/
│   └── ai-service/
├── infrastructure/
│   ├── config-server/
│   ├── discovery-server/
│   └── configs/           (конфиг-файлы для config-server)
├── docker/
│   ├── docker-compose.yml
│   ├── docker-compose.monitoring.yml
│   └── .env.example
├── docs/
│   ├── ARCHITECTURE.md    (этот файл)
│   └── PROGRESS.md
├── .github/workflows/
└── pom.xml                (parent POM, multi-module Maven)
```

---

## РАЗДЕЛ F — ТЕСТИРОВАНИЕ

**Приоритет: Unit → Integration → E2E → Contract**

| Уровень | Инструменты | Что покрывать |
|---|---|---|
| Unit | JUnit 5 + Mockito | AuthService, JwtUtil, CaloriesCalculator, BMR/TDEE, ProfileService |
| Integration | Testcontainers | Каждый сервис с PostgreSQL + Redis + Kafka |
| E2E | Newman (Postman CLI) | Через API Gateway, основные user journeys |
| Contract | Spring Cloud Contract | auth ↔ gateway, analytics ↔ ai (в последнюю очередь) |

### Шаблон Testcontainers

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Testcontainers
class AuthServiceIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.redis.host", redis::getHost);
        r.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }
}
```

---

## РАЗДЕЛ G — РИСКИ

| Риск | Уровень | Митигация |
|---|---|---|
| 8 микросервисов для одного разработчика — избыточность | ВЫСОКИЙ | Выделять сервисы поэтапно, начать с auth; остальные — по мере боли |
| Distributed transactions (Outbox Pattern нужен) | СРЕДНИЙ | Принять eventual consistency, добавить idempotency keys |
| Apple HealthKit — нет REST API | ВЫСОКИЙ | Исключить из диплома, заменить ручным импортом |
| Google Fit deprecated | СРЕДНИЙ | Не реализовывать |
| 11+ Docker контейнеров — высокое потребление RAM | СРЕДНИЙ | docker-compose profiles, минимальный набор для разработки |
| Garmin требует одобрения developer account | СРЕДНИЙ | Начать с Fitbit (без ограничений) |
| Стоимость OpenAI токенов при росте | НИЗКИЙ | Rate limiting + кеш с первого дня |

---

*Этот файл — источник истины по архитектуре. Обновлять только после обсуждения.*
