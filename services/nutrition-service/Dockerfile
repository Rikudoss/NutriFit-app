# syntax=docker/dockerfile:1.7

# ─────────────────────────────────────────────
# Stage 1 — builder
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

RUN apk add --no-cache maven

WORKDIR /build

COPY pom.xml ./pom.xml
COPY services/nutrition-service/pom.xml ./services/nutrition-service/pom.xml

WORKDIR /build/services/nutrition-service

RUN mvn -B -q dependency:go-offline

COPY services/nutrition-service/src ./src
RUN mvn -B -q clean package -DskipTests

RUN mkdir -p /build/layers \
 && cp target/nutrition-service-0.0.1-SNAPSHOT.jar /build/layers/app.jar \
 && cd /build/layers \
 && java -Djarmode=layertools -jar app.jar extract \
 && rm app.jar

# ─────────────────────────────────────────────
# Stage 2 — runtime
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=builder --chown=spring:spring /build/layers/dependencies/          ./
COPY --from=builder --chown=spring:spring /build/layers/spring-boot-loader/    ./
COPY --from=builder --chown=spring:spring /build/layers/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring /build/layers/application/           ./

USER spring:spring

EXPOSE 8084

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
