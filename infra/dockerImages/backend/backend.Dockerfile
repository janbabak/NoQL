# syntax=docker/dockerfile:1.6

# https://hub.docker.com/r/azul/zulu-openjdk-alpine
FROM azul/zulu-openjdk-alpine:17.0.17

WORKDIR /app

RUN mkdir -p /app/plotService
RUN apk add --no-cache docker-cli

COPY backend/build/libs/noql-backend-*.jar /app/noql-backend.jar
EXPOSE 8080
VOLUME ["/app/plotService"]
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/noql-backend.jar"]