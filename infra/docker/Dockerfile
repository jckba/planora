FROM eclipse-temurin:21-jre-alpine
LABEL authors="dvjmca"

WORKDIR /app

RUN addgroup -S planora \
    && adduser -S -G planora planora

COPY --chown=planora:planora backend/build/libs/planora-backend.jar app.jar

USER planora

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
