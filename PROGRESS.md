# NutriFit AI — Прогресс миграции

> Обновляется в конце каждой рабочей сессии.  
> Формат: ✅ сделано | 🔄 в процессе | ⬜ не начато | ❌ заблокировано

---

## Текущий статус

**Активная фаза:** Фаза 1 — Инфраструктура  
**Активная задача:** 1.4 — API Gateway  
**Последнее обновление:** 2026-04-21  
**Сессия:** #3 (монорепо, multi-module Maven, переезд монолита в services/monolith)

---

## ФАЗА 0 — Стабилизация монолита

> Блокирует все последующие фазы. Выполнять строго по порядку.

| # | Задача | Статус | Заметки |
|---|---|---|---|
| 0.1 | Вынести секреты: `JWT_SECRET`, `DB_PASSWORD`, `OPENAI_API_KEY`, `ALLOWED_ORIGINS` в `.env` | ✅ | `.env`, `.env.example`, `application.properties`, `docker-compose.yml`, `.gitignore` |
| 0.2 | Исправить CORS: `"*"` → `${ALLOWED_ORIGINS}` | ✅ | `CorsConfig.java` |
| 0.3 | Добавить Flyway: зависимость + `V1__init_schema.sql` | ✅ | `flyway-core` + `flyway-database-postgresql` в pom.xml, `V1__init_schema.sql`, `baseline-on-migrate=true` |
| 0.4 | Переключить `ddl-auto=validate` | ✅ | `application.properties`: `update` → `validate` |
| 0.5 | Создать `WorkoutRequest.java` DTO | ✅ | `workout/WorkoutRequest.java` с `@NotBlank`, `@Positive`, `@PositiveOrZero`, `@NotNull` |
| 0.6 | Создать `HealthMetricRequest.java` DTO | ✅ | `metrics/HealthMetricRequest.java` + MetricsController + MetricsService |
| 0.7 | Убрать JPA entity из `@RequestBody` в WorkoutController | ✅ | Выполнено в составе 0.5 (DTO заменил entity в @RequestBody) |
| 0.8 | Убрать JPA entity из `@RequestBody` в MetricsController | ✅ | Выполнено в составе 0.6 (DTO заменил entity в @RequestBody) |
| 0.9 | Починить `ProfileController`: убрать прямой `ProfileRepository`, использовать `ProfileService` | ✅ | Убраны `profileRepository` + `userService`; добавлены `ProfileService.update()` и `.getOnboardingStatus()` |
| 0.10 | Починить `OnboardingController`: убрать дублирование, вызывать `ProfileService.completeOnboarding()` | ✅ | Убраны `profileRepository` + `userService`; логика status() перенесена в `ProfileService.getOnboardingStatus()` |
| 0.11 | Убрать `ProfileRepository`, `MealRepository`, `WorkoutRepository`, `MetricsRepository` из `AIController` | ✅ | 4 репозитория перенесены в `AIService.buildContext()`; контроллер инжектит только AIService + UserService |
| 0.12 | Исправить расчёт калорий: `calories = calories_per_100g * quantity / 100` | ✅ | V2-миграция: `calories` → `calories_per_100g`; `quantity` NOT NULL; формула в CaloriesCalculator и AIService |
| 0.13 | Добавить handler `IllegalStateException` → 400 в `GlobalExceptionHandler` | ✅ | `GlobalExceptionHandler`: новый `@ExceptionHandler(IllegalStateException.class)` → 400 |
| 0.14 | Добавить `@Transactional` на `updateWorkout`, `deleteWorkout` в `WorkoutService` | ✅ | Добавлен и на `createWorkout` — он тоже отсутствовал |
| 0.15 | Добавить `@Transactional` на `updateMetric`, `deleteMetric` в `MetricsService` | ✅ | Добавлен и на `createMetric` — он тоже отсутствовал |
| 0.16 | Исправить N+1 в `NutritionService.addItemsToMeal()`: `saveAll()` вместо цикла | ✅ | `stream().map().toList()` + `saveAll()` |
| 0.17 | Починить пакет теста: `com.example.demo` → `kz.nutrifit.backend` | ✅ | Новый тест + H2 + `@ActiveProfiles("test")` + `application-test.properties`; старый файл удалить вручную |
| 0.18 | Написать unit-тесты: `CaloriesCalculatorTest`, `AuthServiceTest`, `ProfileServiceTest` | ⬜ | |
| 0.19 | Удалить `.idea` из git: `git rm -r --cached .idea` | ⬜ | Пользователь делает сам |

---

## ФАЗА 1 — Инфраструктура

| # | Задача | Статус | Заметки |
|---|---|---|---|
| 1.1 | Multi-module Maven: parent pom.xml + services/monolith/, docker/, infrastructure/ | ✅ | `src/` → `services/monolith/src/`; `docker-compose.yml` → `docker/`; `infrastructure/config-server/` и `infrastructure/discovery-server/` заготовлены |
| 1.2 | Создать `infrastructure/config-server` | ✅ | Spring Cloud Config, native backend, classpath:/configs/; конфиги: application.yml (shared) + monolith.yml |
| 1.3 | Создать `infrastructure/discovery-server` (Eureka) | ✅ | @EnableEurekaServer, порт 8761, self-preservation отключён для локальной разработки |
| 1.4 | Создать `services/api-gateway` (Spring Cloud Gateway) | ⬜ | |
| 1.5 | Docker Compose: PostgreSQL, Redis, Kafka+ZK, Zipkin, Prometheus, Grafana | ⬜ | |
| 1.6 | Dockerfile для монолита (multi-stage build) | ⬜ | |
| 1.7 | Подключить монолит к Eureka + Config Server | ⬜ | |
| 1.8 | Настроить роутинг Gateway → монолит | ⬜ | |

---

## ФАЗА 2 — auth-service + email-верификация

| # | Задача | Статус | Заметки |
|---|---|---|---|
| 2.1 | Создать Spring Boot проект auth-service | ⬜ | |
| 2.2 | Перенести: User, UserRepository, AuthService, JwtUtil, JwtConfig | ⬜ | |
| 2.3 | Добавить `email_verifications` таблицу | ⬜ | |
| 2.4 | Реализовать `EmailVerificationService` | ⬜ | |
| 2.5 | Статусы UNVERIFIED/ACTIVE в User | ⬜ | |
| 2.6 | Refresh tokens (таблица + логика) | ⬜ | |
| 2.7 | Rate limiting на верификацию (Redis) | ⬜ | |
| 2.8 | Spring Mail + Mailtrap | ⬜ | |
| 2.9 | Kafka: публикация `user.registered`, `user.verified` | ⬜ | |
| 2.10 | Flyway миграции для auth_db | ⬜ | |
| 2.11 | Unit тесты: AuthService, JwtUtil, EmailVerificationService | ⬜ | |
| 2.12 | Integration тесты (Testcontainers: PostgreSQL + Redis) | ⬜ | |
| 2.13 | Gateway: валидация JWT + проброс X-User-Id заголовка | ⬜ | |
| 2.14 | Монолит: читать userId из X-User-Id заголовка | ⬜ | |

---

## ФАЗА 3 — user-service

| # | Задача | Статус | Заметки |
|---|---|---|---|
| 3.1 | Создать user-service | ⬜ | |
| 3.2 | Перенести Profile entity + ProfileRepository | ⬜ | |
| 3.3 | Перенести ProfileService + ProfileController | ⬜ | |
| 3.4 | Перенести OnboardingController | ⬜ | |
| 3.5 | Добавить поле `activity_level` в профиль | ⬜ | |
| 3.6 | Kafka Consumer: `user.registered` → создать профиль | ⬜ | |
| 3.7 | Internal endpoint `/internal/users/{userId}` | ⬜ | |
| 3.8 | Flyway миграции для user_db | ⬜ | |
| 3.9 | Тесты | ⬜ | |
| 3.10 | Удалить profile-код из монолита | ⬜ | |

---

## ФАЗА 4 — nutrition-service

| # | Задача | Статус | Заметки |
|---|---|---|---|
| 4.1 | Создать nutrition-service | ⬜ | |
| 4.2 | Перенести Meal, MealItem, NutritionService, NutritionController | ⬜ | |
| 4.3 | Добавить `food_database` таблицу | ⬜ | |
| 4.4 | Добавить эндпоинты `/range` и `/foods/search` | ⬜ | |
| 4.5 | Kafka: `meal.created`, `meal.updated`, `meal.deleted` | ⬜ | |
| 4.6 | Internal endpoint `/internal/nutrition/summary` | ⬜ | |
| 4.7 | Flyway + тесты | ⬜ | |
| 4.8 | Удалить nutrition-код из монолита | ⬜ | |

---

## ФАЗА 5 — workout-service

| # | Задача | Статус | Заметки |
|---|---|---|---|
| 5.1 | Создать workout-service | ⬜ | |
| 5.2 | Перенести Workout, WorkoutService, WorkoutController | ⬜ | |
| 5.3 | Добавить `activity_types` таблицу | ⬜ | |
| 5.4 | Добавить `GET /workouts/{id}` (отсутствует в монолите) | ⬜ | |
| 5.5 | Kafka: `workout.logged`, `workout.updated`, `workout.deleted` | ⬜ | |
| 5.6 | Internal endpoint `/internal/workouts/summary` | ⬜ | |
| 5.7 | Flyway + тесты | ⬜ | |
| 5.8 | Удалить workout-код из монолита | ⬜ | |

---

## ФАЗА 6 — metrics-service

| # | Задача | Статус | Заметки |
|---|---|---|---|
| 6.1 | Создать metrics-service | ⬜ | |
| 6.2 | Перенести HealthMetric, MetricsService, MetricsController | ⬜ | |
| 6.3 | Добавить `daily_summaries` таблицу | ⬜ | |
| 6.4 | Kafka Consumer: `wearable.synced` → создать метрику | ⬜ | |
| 6.5 | Kafka: `metrics.recorded` | ⬜ | |
| 6.6 | Internal endpoint `/internal/metrics/summary` | ⬜ | |
| 6.7 | Flyway + тесты | ⬜ | |
| 6.8 | Удалить metrics-код из монолита | ⬜ | |

---

## ФАЗА 7 — analytics-service

| # | Задача | Статус | Заметки |
|---|---|---|---|
| 7.1 | Создать analytics-service | ⬜ | |
| 7.2 | Реализовать BMR/TDEE (формула Миффлина-Сан Жеора) | ⬜ | |
| 7.3 | Реализовать calorie balance за период | ⬜ | |
| 7.4 | Реализовать macro splits по цели | ⬜ | |
| 7.5 | Реализовать weekly summary + weight trend | ⬜ | |
| 7.6 | Kafka Consumer: построение read-model из событий | ⬜ | |
| 7.7 | Redis кеш (TTL 1 ч, инвалидация по событиям) | ⬜ | |
| 7.8 | Internal endpoint `/internal/analytics/context/{userId}` | ⬜ | |
| 7.9 | Тесты расчётов | ⬜ | |

---

## ФАЗА 8 — wearables-service

| # | Задача | Статус | Заметки |
|---|---|---|---|
| 8.1 | Создать wearables-service | ⬜ | |
| 8.2 | Интерфейс `WearableProvider` (Strategy pattern) | ⬜ | |
| 8.3 | `FitbitProvider` реализация | ⬜ | Первый провайдер |
| 8.4 | OAuth2 flow: connect → callback → зашифрованные tokens | ⬜ | |
| 8.5 | Scheduled sync (каждые 30 мин) | ⬜ | |
| 8.6 | Webhook receiver для Fitbit | ⬜ | |
| 8.7 | Kafka: `wearable.synced` | ⬜ | |
| 8.8 | Конфликт-разрешение: DEVICE vs MANUAL | ⬜ | |
| 8.9 | Flyway + тесты | ⬜ | |
| 8.10 | (Опц.) GarminProvider, PolarProvider | ⬜ | |

---

## ФАЗА 9 — ai-service

| # | Задача | Статус | Заметки |
|---|---|---|---|
| 9.1 | Создать ai-service (вынести из монолита) | ⬜ | |
| 9.2 | Feign-клиент к analytics-service | ⬜ | |
| 9.3 | Redis кеш рекомендаций (TTL 4 ч) | ⬜ | |
| 9.4 | Kafka Consumer: инвалидация кеша | ⬜ | |
| 9.5 | Rate limiting 10 req/day/user (Redis) | ⬜ | |
| 9.6 | Трекинг стоимости токенов | ⬜ | |
| 9.7 | Тесты | ⬜ | |
| 9.8 | Удалить AI-код из монолита | ⬜ | |

---

## Лог сессий

| Дата | Сессия | Выполнено | Следующий шаг |
|---|---|---|---|
| 2026-04-17 | #1 | Создание ARCHITECTURE.md, PROGRESS.md, задачи 0.1 и 0.2 выполнены | Задача 0.3 — добавить Flyway |
| 2026-04-17 | #2 | Задача 0.3 выполнена: Flyway добавлен, V1__init_schema.sql создан | Задача 0.4 — переключить ddl-auto=validate |
| 2026-04-21 | #3 | Фаза 0 закрыта (0.1–0.17 ✅). Задача 1.1: монорепо — parent pom.xml, services/monolith/, docker/, infrastructure/ | Задача 1.2 — config-server |

---

## Известные блокеры и вопросы

*Здесь фиксируем всё, что требует решения перед продолжением.*

| # | Описание | Решение |
|---|---|---|
| — | — | — |

---

## Технический долг (после Фазы 0)

| # | Описание | Где |
|---|---|---|
| TD-1 | Добавить `spring.jpa.open-in-view=false` в `application.properties` | Hibernate предупреждает при старте: Open Session In View держит транзакцию открытой на время HTTP-запроса, что провоцирует lazy-loading в контроллерах и скрывает N+1 проблемы |
