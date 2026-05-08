#FROM openjdk:21-jdk-slim
FROM eclipse-temurin:21-jdk-jammy
RUN apt-get update && apt-get upgrade -y && apt-get install -y netcat-openbsd && apt-get clean
ARG JAR_FILE
ENV JAR_FILE=$JAR_FILE
RUN echo $JAR_FILE
WORKDIR /activity-log-service
COPY  /build/libs/activity-log-service-0.0.1-SNAPSHOT.jar   /app.jar
cmd ["java", "-jar", "/app.jar"]
