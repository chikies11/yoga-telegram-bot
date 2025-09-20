FROM maven:3.8.6-openjdk-11

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

CMD ["java", "-jar", "target/telegramYogaBot-1.0-SNAPSHOT-jar-with-dependencies.jar"]