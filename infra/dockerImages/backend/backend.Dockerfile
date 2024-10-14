# https://hub.docker.com/r/azul/zulu-openjdk
FROM azul/zulu-openjdk-alpine:17.0.11-17.50

WORKDIR /app

RUN mkdir plotService
RUN apk add --no-cache docker-cli

COPY ../../backend/build/libs/noql-backend-*.jar /app/noql-backend.jar
EXPOSE 8080
CMD ["java", "-jar", "/app/noql-backend.jar", "--stacktrace", "--info"]
