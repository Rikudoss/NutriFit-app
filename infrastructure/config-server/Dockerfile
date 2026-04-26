# syntax=docker/dockerfile:1.7

# ─────────────────────────────────────────────
# Stage 1 — builder
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

RUN apk add --no-cache maven

WORKDIR /build

# Сначала только POM-файлы — кэшируемый слой зависимостей
COPY pom.xml ./pom.xml
COPY infrastructure/config-server/pom.xml ./infrastructure/config-server/pom.xml

WORKDIR /build/infrastructure/config-server

# Отдельный слой: прогрев локального репозитория Maven
RUN mvn -B -q dependency:go-offline

# Исходники + сборка
COPY infrastructure/config-server/src ./src
RUN mvn -B -q clean package -DskipTests

# Разбираем fat-JAR на слои Spring Boot
RUN mkdir -p /build/layers \
 && cp target/config-server-0.0.1-SNAPSHOT.jar /build/layers/app.jar \
 && cd /build/layers \
 && java -Djarmode=layertools -jar app.jar extract \
 && rm app.jar

# ─────────────────────────────────────────────
# Stage 2 — runtime
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Порядок от редко меняющихся к часто меняющимся слоям
COPY --from=builder --chown=spring:spring /build/layers/dependencies/          ./
COPY --from=builder --chown=spring:spring /build/layers/spring-boot-loader/    ./
COPY --from=builder --chown=spring:spring /build/layers/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /build/layers/application/           ./

USER spring:spring

EXPOSE 8888

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
