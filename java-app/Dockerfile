FROM eclipse-temurin:24-jre
WORKDIR /app
COPY build/libs/kafka-client-app-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]