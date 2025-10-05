# 1️⃣ Build stage
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Copy Maven settings
COPY pom.xml .
COPY HRISApp/pom.xml HRISApp/
COPY TimeKeeping/pom.xml TimeKeeping/
COPY Administrative/pom.xml Administrative/
COPY Common/pom.xml Common/
RUN mvn -B -q dependency:go-offline

# Copy source code
COPY . .

# Build only HRISApp module
RUN mvn -pl HRISApp clean package -DskipTests

# 2️⃣ Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy JAR
COPY --from=builder /app/HRISApp/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]