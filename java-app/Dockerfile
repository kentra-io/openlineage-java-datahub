FROM eclipse-temurin:21-jdk
RUN apt-get update && apt-get install -y libgcc1
WORKDIR /app
COPY java-app/build/libs/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
