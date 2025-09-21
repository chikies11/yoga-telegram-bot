# Stage 1: Build the application
FROM maven:3.8.4-openjdk-11 AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=builder /build/target/telegramYogaBot-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]