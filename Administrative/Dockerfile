# Use official OpenJDK 17 image
FROM eclipse-temurin:17-jdk

# Set the working directory
WORKDIR /app

# Copy the entire HRIS project (needed to build dependencies)
COPY .. /app

# Move into administrative module
WORKDIR /app/Administrative

# Build only this module and its dependencies (like Common)
RUN ./mvnw clean package -DskipTests -pl Administrative -am

# Expose the Spring Boot default port
EXPOSE 8080

# Run the built Administrative Spring Boot JAR
CMD ["java", "-jar", "target/Administrative-1.0-SNAPSHOT.jar"]