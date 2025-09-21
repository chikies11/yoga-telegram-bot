FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/telegramYogaBot-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

CMD ["java", "-jar", "app.jar"]