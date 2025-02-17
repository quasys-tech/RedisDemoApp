FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y curl telnet

WORKDIR /app

COPY target/redis-0.0.1-SNAPSHOT.jar /app/redis.jar

# RUN jar tf /app/redis.jar

ENTRYPOINT ["java", "-jar", "/app/redis.jar"]
# ENTRYPOINT ["java", "-cp", "/app/BeyondInsight.jar:/app/lib/json-20240303.jar", "com.quasys.Main"]
