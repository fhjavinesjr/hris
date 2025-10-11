FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml ./
COPY HRISApp/pom.xml HRISApp/
COPY Common/pom.xml Common/
COPY Administrative/pom.xml Administrative/
COPY TimeKeeping/pom.xml TimeKeeping/
COPY HumanResource/pom.xml HumanResource/

RUN mvn -B -q dependency:go-offline

COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/HRISApp/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]