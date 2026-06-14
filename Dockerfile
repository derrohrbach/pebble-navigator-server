# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Gradle Wrapper und Konfiguration zuerst (Layer-Cache)
COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle/ gradle/

# Subprojekt
COPY public-transport-enabler/ public-transport-enabler/

# Quellcode
COPY src/ src/

RUN chmod +x gradlew && ./gradlew installDist --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/install/pebble-navigator-server/ .

EXPOSE 8080

# API_KEY muss beim Start gesetzt sein
ENV API_KEY=""

CMD ["bin/pebble-navigator-server"]
