FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace

COPY pom.xml ./
COPY PrimeHR/pom.xml PrimeHR/
RUN mvn -B -q -f PrimeHR/pom.xml dependency:go-offline

COPY PrimeHR/src PrimeHR/src
RUN mvn -B -f PrimeHR/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --create-home primehr
WORKDIR /app
COPY --from=builder --chown=primehr:primehr /workspace/PrimeHR/target/PrimeHR-1.0-SNAPSHOT.jar app.jar

USER primehr
ENV SPRING_PROFILES_ACTIVE=production-postgresql
EXPOSE 8086
HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
  CMD curl --fail --silent "http://127.0.0.1:${PORT:-8086}/actuator/health/readiness" || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
