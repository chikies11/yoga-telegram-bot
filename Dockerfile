FROM openjdk:17-jdk-slim

WORKDIR /app

# Копируем исходный код
COPY pom.xml .
COPY src ./src

# Собираем проект
RUN mvn clean package -DskipTests

# Запускаем приложение
CMD ["java", "-jar", "target/telegramYogaBot-1.0-SNAPSHOT-jar-with-dependencies.jar"]