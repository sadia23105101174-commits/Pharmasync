FROM eclipse-temurin:21-jdk AS builder
WORKDIR /build

COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY domain/pom.xml domain/
COPY domain/src domain/src
COPY branch-app/pom.xml branch-app/
COPY branch-app/src branch-app/src
COPY head-office/pom.xml head-office/
COPY head-office/src head-office/src

RUN chmod +x ./mvnw && ./mvnw clean package -DskipTests -pl head-office -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/head-office/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
