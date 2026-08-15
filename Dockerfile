# Multi-Stage Dockerfile for Spring Boot Application
# Stage 1: Build the JAR with Maven
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy Maven wrapper & pom.xml first for dependency caching
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Make mvnw executable and download dependencies
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copy source code and build the JAR
COPY src ./src
RUN ./mvnw clean package -Dmaven.test.skip=true

# Stage 2: Minimal Runtime Image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create a non-root user for security
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
USER appuser

# Copy built JAR from builder stage
COPY --from=build /app/target/*.jar app.jar

# Expose dynamic Railway PORT (Default 8080)
EXPOSE 8080

# Run the Spring Boot Application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dserver.port=${PORT:8080}", "-jar", "app.jar"]
