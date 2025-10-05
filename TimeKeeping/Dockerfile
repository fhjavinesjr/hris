FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY .. /app
WORKDIR /app/TimeKeeping
RUN ./mvnw clean package -DskipTests -pl TimeKeeping -am
EXPOSE 8080
CMD ["java", "-jar", "target/TimeKeeping-1.0-SNAPSHOT.jar"]