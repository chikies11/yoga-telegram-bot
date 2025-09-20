FROM maven:3.6.0-openjdk-17

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests -Dmaven.compiler.source=17 -Dmaven.compiler.target=17

CMD ["java", "-jar", "target/telegramYogaBot-1.0-SNAPSHOT-jar-with-dependencies.jar"]