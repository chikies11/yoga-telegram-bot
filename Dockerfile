FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/telegramYogaBot-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
CMD ["java", "-jar", "app.jar"]