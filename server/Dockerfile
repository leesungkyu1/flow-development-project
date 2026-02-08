# Stage 1: Build the Spring Boot application
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Gradle wrapper and build files
COPY gradlew .
COPY gradlew.bat .
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle ./gradle

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (this step will be cached)
RUN ./gradlew dependencies

# Copy source code
COPY src ./src

# Build the Spring Boot application
RUN ./gradlew bootJar

# Stage 2: Run the Spring Boot application
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose the port Spring Boot runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Xms128m", "-Xmx512m", "-jar", "app.jar"]
