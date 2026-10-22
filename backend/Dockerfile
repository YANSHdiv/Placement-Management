# Multi-stage Dockerfile for JECRC Portal Backend
# Optimized for Koyeb Free Instance (512MB RAM, shared CPU)

# Stage 1: Build the Spring Boot application using Maven
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Cache Maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application JAR skipping tests (tests verified locally in CI/dev)
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal, secure, lightweight Eclipse Temurin JRE runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Conservative JVM memory parameters tuned specifically for 512MB memory limits:
# - Max heap capped at 320m to leave headroom for OS, Metaspace, and native memory
# - SerialGC eliminates multi-threaded GC overhead and keeps memory fragmentation low
# - Reduced thread stack size (512k) minimizes per-thread memory footprint
ENV JAVA_OPTS="-Xms128m -Xmx320m -Xss512k -XX:+UseSerialGC -XX:MaxMetaspaceSize=128m -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE="prod"
ENV PORT=8000

# Copy executable jar from builder stage
COPY --from=builder /workspace/target/*.jar app.jar

# Koyeb uses port 8000 by default for web services
EXPOSE 8000

# Run with dynamic port resolution
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT:-8000}"]
