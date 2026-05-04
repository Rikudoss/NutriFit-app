# NutriFit AI — Прогресс миграции

> Обновляется в конце каждой рабочей сессии.  
> Формат: ✅ сделано | 🔄 в процессе | ⬜ не начато | ❌ заблокировано

---

## Текущий статус

**Активная фаза:** Фаза 3 завершена → переход на Фазу 4 (nutrition-service)  
**Активная задача:** 4.1 — создать `services/nutrition-service`  
**Последнее обновление:** 2026-05-04  
**Сессия:** #10 (Фаза 3: создан `user-service` (порт 8083, БД `user_db`), Profile entity с `userId` Long (без FK на User), ActivityLevel enum, Kafka consumer `user.registered` → getOrCreate профиль, `/internal/users/{userId}`, Gateway роуты `/api/profile/**` и `/api/onboarding/**` → `lb://user-service`. Монолит: удалены `profile/*` и `onboarding/*` пакеты, добавлен `UserServiceClient` с `@LoadBalanced RestTemplate` для HTTP-вызова user-service, `AIService` мигрирован на HTTP-клиент. Оба модуля компилируются.)

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
| 1.4 | Создать `services/api-gateway` (Spring Cloud Gateway) | ✅ | Reactive/Netty, порт 8080, статический роут /api/** → localhost:8081, CORS через globalcors; монолит переехал на 8081 |
| 1.5 | Docker Compose: PostgreSQL 16, Redis 7, Kafka+ZK, Zipkin | ✅ | nutrifit-network, named volumes, healthchecks, restart: unless-stopped; Spring-сервисы добавим в 1.6 |
| 1.6 | Dockerfile для монолита (multi-stage build) | ✅ | `services/monolith/Dockerfile` + корневой `.dockerignore`. Builder на `temurin:21-jdk-alpine` + Maven, layered JAR через `jarmode=layertools`, runtime на `temurin:21-jre-alpine` под non-root `spring:spring`. Образ 404 MB, старт 6.6 сек, подключение к `nutrifit-postgres` в сети `docker_nutrifit-network` проверено |
| 1.7 | Подключить монолит к Eureka + Config Server | ✅ | `spring-cloud-starter-config` + `spring-cloud-starter-netflix-eureka-client` в pom.xml монолита. `application.yml`: `spring.application.name=monolith`, `spring.config.import=optional:configserver:http://localhost:8888`, `eureka.client.service-url`, `prefer-ip-address=true`. Конфиги переехали в `configs/monolith.yml` на config-server, секреты остались в `.env`. Монолит регистрируется в Eureka как MONOLITH, тянет конфиг с config-server |
| 1.8 | Настроить роутинг Gateway → монолит (lb://) + docker-compose со Spring-сервисами | ✅ | Gateway → `lb://monolith` через Eureka (вместо статического `http://localhost:8081`). Dockerfile для `config-server`, `discovery-server`, `api-gateway` (multi-stage по аналогии с monolith). `docker-compose.yml` расширен 4 Spring-сервисами с healthchecks `/actuator/health`, `depends_on: service_healthy`, `EUREKA_INSTANCE_HOSTNAME` для контейнерной резолюции. Все 10 контейнеров healthy. E2E: API-GATEWAY + MONOLITH в Eureka registry, `GET /api/profile` через 8080 → 403 (как при прямом запросе на 8081) — `lb://` балансит корректно. В монолит добавлен `spring-boot-starter-actuator` и `/actuator/**` в permitAll для healthcheck |

---

## ФАЗА 2 — auth-service + email-верификация

| # | Задача | Статус | Заметки |
|---|---|---|---|
| 2.1 | Создать Spring Boot проект auth-service (Шаг A) | ✅ | Модуль `services/auth-service/`, отдельная БД `auth_db` в PostgreSQL (создаётся init-скриптом `docker/postgres-init/01-create-databases.sh`), конфиг на Config Server (`auth-service.yml`), порт 8082, multi-stage Dockerfile, в docker-compose с healthcheck `/actuator/health`, регистрация в Eureka как AUTH-SERVICE через lb. Работает параллельно монолиту, gateway пока проксирует на монолит — переключение в Шаге C |
| 2.2 | Перенести: User, UserRepository, AuthService, JwtUtil, JwtConfig | ✅ | Скопировано из монолита `kz.nutrifit.backend.auth.* + .config.{JwtConfig,JwtAuthenticationFilter}` → `kz.nutrifit.auth.*` (controller/service/dto/util/filter/config/entity/repository). User entity без связи с Profile, добавлены поля `status`, `created_at`, `updated_at`. AuthService.register() БЕЗ создания Profile (TODO Фазы 3 — публиковать `user.registered` в Kafka). Status=ACTIVE по дефолту до Шага B. SecurityConfig упрощён: всё permitAll кроме фильтра JWT (auth-service сам не имеет защищённых эндпоинтов). UserDetailsService — inline бин в SecurityConfig поверх UserRepository (отдельный UserService не делал, в Фазе 3 он живёт в user-service) |
| 2.3 | Добавить `email_verifications` таблицу | ✅ | V2 миграция: `ALTER TABLE users SET DEFAULT 'UNVERIFIED'` + `verified_at TIMESTAMP`, существующие сохранены ACTIVE через `UPDATE ... WHERE status='ACTIVE'`. Таблица `email_verifications` (user_id FK CASCADE, code VARCHAR(6), expires_at, attempts, used_at) + индексы по user_id и expires_at |
| 2.4 | Реализовать `EmailVerificationService` | ✅ | Дубль Redis + БД: код пишется в обе. Redis `verification:user:{userId}` TTL 10 мин (быстрая проверка), БД для аудита и fallback. 6-значный код (`SecureRandom.nextInt(1_000_000)` → `%06d`). Max 5 попыток ввода → удаление ключа из Redis |
| 2.5 | Статусы UNVERIFIED/ACTIVE в User | ✅ | `register` создаёт UNVERIFIED, `login` с UNVERIFIED → IllegalStateException → 403, `verify` ставит ACTIVE + `verified_at=now()`. RegisterResponse без JWT, JWT выдаётся только после verify/login |
| 2.6 | Refresh tokens (таблица + логика) | ⬜ | |
| 2.7 | Rate limiting на верификацию (Redis) | ✅ | `verification:resend:user:{userId}` через `setIfAbsent` с TTL 60 сек. Если ключ уже есть — IllegalStateException → 403 |
| 2.8 | Spring Mail + Mailtrap | ✅ | `spring-boot-starter-mail`, JavaMailSender → `sandbox.smtp.mailtrap.io:2525` (STARTTLS+auth). Креды в `.env` (MAILTRAP_*), SMTP пароль через `application.properties` → `${MAILTRAP_PASSWORD}`, остальное в config-server |
| 2.9 | Kafka: публикация `user.registered`, `user.verified` | ✅ | `UserEventPublisher` через `KafkaTemplate<String, Object>`, JsonSerializer, topic `user-events`. Только publish — consumers в Фазе 3 (user-service). Если Kafka недоступна — `log.error`, не падаем (try-catch вокруг send) |
| 2.10 | Flyway миграции для auth_db | ⬜ | |
| 2.11 | Unit тесты: AuthService, JwtUtil, EmailVerificationService | ⬜ | |
| 2.12 | Integration тесты (Testcontainers: PostgreSQL + Redis) | ⬜ | |
| 2.13 | Gateway: валидация JWT + проброс X-User-Id заголовка | ✅ | Шаг C.1: два роута (`/api/auth/**` → `lb://auth-service`, `/api/**` → `lb://monolith`, порядок важен — auth-service первым), `JwtAuthenticationFilter` (GlobalFilter, order=-100) валидирует подпись через общий `${JWT_SECRET}` и пробрасывает `X-User-Id` claim в downstream. Whitelist: `/api/auth/{register,login,verify,resend-code}`, `/actuator/`, `/v3/api-docs`, `/swagger-ui`. CORS preflight (OPTIONS) — без проверки. JWT: `userId` добавлен как extra claim в `JwtUtil.generateToken()` для `User instanceof UserDetails`. Монолит подписан на Kafka topic `user-events` (group `monolith-user-sync`): идемпотентный `UserEventListener` создаёт строку в `users` через native `INSERT ... ON CONFLICT DO NOTHING` + `setval` sequence (т.к. локальный `register` монолита ещё работает, удалим в C.2) |
| 2.14 | Монолит: читать userId из X-User-Id заголовка | ✅ | Шаг C.2: создан `kz.nutrifit.backend.security.XUserIdAuthenticationFilter` (`OncePerRequestFilter`) — читает `X-User-Id`, кладёт `principal=Long userId` в `SecurityContextHolder`. `SecurityConfig` упрощён: убраны `DaoAuthenticationProvider`, `AuthenticationManager`, `PasswordEncoder`, whitelist `/api/auth/**` (роутится на auth-service Gateway-ем). Удалены: `auth/controller/AuthController`, `auth/service/AuthService`, `auth/util/JwtUtil`, `auth/dto/{Auth,Login,Register}*`, `config/JwtAuthenticationFilter`, `config/JwtConfig`, `user/UserService`. Контроллеры (`Profile`, `Onboarding`, `Metrics`, `Workout`, `Nutrition`, `AI`) переключены на `Long userId = (Long) authentication.getPrincipal()`; для FK используется `userRepository.getReferenceById(userId)` (proxy без похода в БД). `ProfileService` теперь работает по `userId`, `getOrCreate` создаёт пустой профиль на лету для свежих юзеров из auth-service. `application.properties`: убран `jwt.secret=${JWT_SECRET}` |

---

## ФАЗА 3 — user-service

| # | Задача | Статус | Заметки |
|---|---|---|---|
| 3.1 | Создать user-service | ✅ | `services/user-service/`, порт 8083, БД `user_db`, Eureka как USER-SERVICE, Dockerfile, pom.xml |
| 3.2 | Перенести Profile entity + ProfileRepository | ✅ | `kz.nutrifit.user.entity.Profile` — `userId Long` вместо FK на User entity; `ProfileRepository.findByUserId()` |
| 3.3 | Перенести ProfileService + ProfileController | ✅ | Логика getOrCreate/patch/update перенесена; контроллер `/api/profile` читает X-User-Id через SecurityContext |
| 3.4 | Перенести OnboardingController | ✅ | `/api/onboarding` и `/api/onboarding/complete` — делегируют в ProfileService |
| 3.5 | Добавить поле `activity_level` в профиль | ✅ | Enum `ActivityLevel` (SEDENTARY..EXTRA_ACTIVE), поле в Profile entity и V1 миграции |
| 3.6 | Kafka Consumer: `user.registered` → создать профиль | ✅ | `UserEventConsumer` (group `user-service`), String deserializer + ObjectMapper, вызывает `getOrCreate` |
| 3.7 | Internal endpoint `/internal/users/{userId}` | ✅ | `InternalUserController` → `GET /internal/users/{userId}`, permitAll в SecurityConfig |
| 3.8 | Flyway миграции для user_db | ✅ | `V1__init_user_schema.sql`: таблица profiles + индекс по user_id; `user_db` создаётся init-скриптом postgres |
| 3.9 | Тесты | ⬜ | |
| 3.10 | Удалить profile-код из монолита | ✅ | Удалены `profile/*` и `onboarding/*` пакеты; `User.java` очищен от @OneToOne Profile; `AIService` → `UserServiceClient` с `@LoadBalanced RestTemplate`; `InternalServiceConfig` + `ProfileSummary` в монолите |

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
| 2026-04-23 | #4 | Задача 1.6: Dockerfile монолита (multi-stage, layered JAR, non-root spring:spring), `.dockerignore` в корне. Образ `nutrifit-monolith:local` 404 MB, запуск в сети `docker_nutrifit-network` проверен — Flyway валидный, Swagger 200 | Задача 1.7 — подключить монолит к Eureka + Config Server |
| 2026-04-26 | #5 | Фаза 1 закрыта (1.7, 1.8): Eureka client везде, lb://monolith, полный docker-compose стек 10/10 healthy | Фаза 2.1 — создать auth-service |
| 2026-04-26 | #6 | Фаза 2 Шаг A: auth-service создан, своя БД auth_db, регистрация в Eureka как AUTH-SERVICE, работает параллельно монолиту | Фаза 2 Шаг B — email-верификация (UNVERIFIED→ACTIVE, Redis, Mailtrap) |
| 2026-04-28 | #7 | Шаг B Фазы 2: email-верификация (V2 миграция, EmailVerificationService с Redis+DB, MailService через Mailtrap SMTP, Kafka publish, rate limiting) | Шаг C — переключить Gateway на auth-service |
| 2026-04-29 | #8 | Шаг C.1 Фазы 2: Gateway проверяет JWT и пробрасывает X-User-Id, маршрут /api/auth/** на auth-service, JWT теперь содержит userId claim, монолит синхронизирует users через Kafka consumer (idempotent native INSERT + setval) | Шаг C.2 — удалить auth из монолита, читать X-User-Id |
| 2026-04-29 | #9 | Шаг C.2 Фазы 2: удалена auth-логика из монолита (controller/service/jwt/dto/UserService), создан `XUserIdAuthenticationFilter` (principal=Long userId), `SecurityConfig` упрощён, контроллеры переключены на `Authentication.getPrincipal()` + `userRepository.getReferenceById`, `ProfileService.getOrCreate` создаёт пустой профиль для новых юзеров из auth-service. **Фаза 2 закрыта** | Фаза 3.1 — создать `user-service` |

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
| TD-3 | `/actuator/gateway/routes` возвращает 404 в Spring Cloud Gateway 4.3.x (2025.0.2) | Разобраться с `management.endpoint.gateway.access` / `enabled` в новой версии. Не блокер — роуты проверяются через `application.yml` и прямые запросы к `/api/**` |
| TD-4 | `mvn ... -Djarmode=layertools` в Dockerfile монолита помечен deprecated в Spring Boot 3.5 | Мигрировать на `-Djarmode=tools extract --layers --launcher` при следующей правке Dockerfile. Сейчас работает, не блокер |
| TD-7 | Смешивание `LocalDateTime` (EmailVerification) и `Instant` (User) в auth-service | Унифицировать на `Instant` — полей `expiresAt`, `createdAt`, `usedAt` в `EmailVerification`, плюс `LocalDateTime.now()` в `EmailVerificationService`. Сейчас работает (Hibernate маппит обоих в `TIMESTAMP`), но даёт несогласованность по timezone-семантике |
| TD-8 | Race condition в `EmailVerificationService.verifyCode`: read из Redis + delete — две операции | Использовать `redisTemplate.opsForValue().getAndDelete(key)` (атомарно). Сейчас при одновременном `verify` от двух воркеров теоретически оба могут увидеть код «успешным» до удаления. Для нагруженного прод-сценария — починить |
| ~~TD-9~~ | ~~`attempts` в `email_verifications` НЕ инкрементируется при неверном коде~~ | **Закрыт в сессии #7.** Создан отдельный `@Service AttemptsTracker.increment()` с `@Transactional(propagation = REQUIRES_NEW)` — инкремент коммитится независимо от внешней транзакции `verifyEmail`. Заодно закрыт связанный баг: после исчерпания 5 попыток ключ удалялся из Redis, но в БД `expires_at` в будущем — fallback позволял пройти 6-ю попытку с правильным кодом. Добавлена проверка `active.getAttempts() >= MAX_ATTEMPTS` в DB-пути ДО сравнения кода. Подтверждено e2e: 5 неверных → attempts=5 + Redis удалён; 6-я с правильным кодом → 400, юзер остался UNVERIFIED |
| TD-10 | Whitelist в Gateway `JwtAuthenticationFilter` использует `startsWith("/actuator/")` — слишком широко, в идеале точечный whitelist `/actuator/health` и `/actuator/info` | Сейчас наружу через Gateway открыты все actuator-эндпоинты сервисов за `lb://`, включая потенциально чувствительные `env`, `configprops`, `mappings`. Сузить prefix-список до `/actuator/health`, `/actuator/info`. На auth-service и монолите остальные actuator-эндпоинты в exposure уже не выставлены (`management.endpoints.web.exposure.include`), но defense-in-depth на Gateway не помешает |
| TD-11 | Gateway warmup задержка ~30 сек после рестарта (Eureka client registry-fetch-interval) | При рестарте Gateway первые 30 сек возвращает 503 на `lb://` — Eureka client ещё не получил registry. Не баг, дизайн Eureka. Можно сократить через `eureka.client.registry-fetch-interval-seconds=5` или добавить retry в Gateway. Не критично для dev, но для production — починить (ускорить fetch-interval либо явный warmup health probe) |
| TD-12 | Монолитный `GlobalExceptionHandler` мапит `NotFoundException` / `IllegalArgumentException` на 400 — семантически "Profile not found" должен быть 404 | Подтверждено в e2e сессии #8: `GET /api/profile` с валидным JWT для юзера без профиля возвращает 400 `"Profile not found for user: ..."`. Создать `ProfileNotFoundException` с `@ResponseStatus(NOT_FOUND)` либо в `GlobalExceptionHandler` различать "not found"-типы и возвращать 404. Затрагивает все домены монолита (profile, meal, workout, metric) — единый паттерн |
