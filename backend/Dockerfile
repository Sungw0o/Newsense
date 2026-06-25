FROM gradle:9.5.1-jdk21 AS builder

WORKDIR /workspace

COPY settings.gradle build.gradle gradlew ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x ./gradlew && ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080 \
    TZ=Asia/Seoul

RUN groupadd --system newsense && useradd --system --gid newsense newsense

COPY --from=builder /workspace/build/libs/*.jar app.jar

USER newsense

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
