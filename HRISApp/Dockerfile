# 1️⃣ Build stage (with Maven preinstalled)
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy Maven wrapper and POMs
COPY pom.xml .
COPY HRISApp/pom.xml HRISApp/
COPY TimeKeeping/pom.xml TimeKeeping/
COPY Administrative/pom.xml Administrative/
COPY Common/pom.xml Common/

# Preload dependencies (for speed)
RUN mvn -B -q dependency:go-offline

# Copy all source code
COPY . .

# Build all modules (so HRISApp can find its dependencies)
RUN mvn clean package -DskipTests

# 2️⃣ Runtime stage (lightweight)
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built jar from HRISApp module
COPY --from=builder /app/HRISApp/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]