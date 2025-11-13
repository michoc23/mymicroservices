# Multi-stage Docker build for API Gateway

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy parent POM
COPY pom.xml ./
COPY api-gateway/pom.xml ./api-gateway/
COPY User/pom.xml ./User/
COPY Ticket/pom.xml ./Ticket/

# Download dependencies
RUN mvn dependency:go-offline -B -pl api-gateway -am

# Copy source code
COPY api-gateway/src ./api-gateway/src

# Build the application
RUN mvn clean package -DskipTests -pl api-gateway -am

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built artifact from the build stage
COPY --from=build /app/api-gateway/target/*.jar app.jar

# Expose port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
