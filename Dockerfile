# syntax=docker/dockerfile:1.7

FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src ./src
RUN ./mvnw -q clean package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends webp \
    && rm -rf /var/lib/apt/lists/* \
    && useradd -m -u 1001 springuser \
    && mkdir -p /app/uploads \
    && chown -R springuser:springuser /app/uploads

COPY --from=builder /app/target/*.jar /app/app.jar

ENV JAVA_OPTS=""
ENV UPLOAD_DIR="/app/uploads"

EXPOSE 8080

USER springuser

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
