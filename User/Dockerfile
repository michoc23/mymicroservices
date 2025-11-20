# Multi-stage build for smaller image size
FROM maven:3.9-eclipse-temurin-17-alpine AS build

# Set working directory
WORKDIR /app


# Copy pom files
COPY pom.xml .
COPY User/pom.xml User/
COPY api-gateway/pom.xml api-gateway/
COPY Ticket/pom.xml Ticket/
COPY Subscription/pom.xml Subscription/
COPY Route/pom.xml Route/
COPY BusGeolocation/pom.xml BusGeolocation/

# Download dependencies (cached layer)
RUN mvn -f User/pom.xml dependency:go-offline

# Copy source code
COPY User/src User/src

# Build the application
RUN mvn -f User/pom.xml clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Add non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/User/target/*.jar app.jar

# Change ownership
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/api/v1/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]