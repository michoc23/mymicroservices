# Multi-stage build for smaller image size
FROM maven:3.9-eclipse-temurin-17-alpine as build

# Set working directory
WORKDIR /workspace/app


# Copy pom files
COPY pom.xml .
COPY User/pom.xml User/
COPY Ticket/pom.xml Ticket/
COPY Subscription/pom.xml Subscription/
COPY Route/pom.xml Route/
COPY BusGeolocation/pom.xml BusGeolocation/
COPY api-gateway/pom.xml api-gateway/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY User/src User/src

# Build the application
RUN mvn package -pl User -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Add non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Set working directory
WORKDIR /app

# Copy JAR from build stage
COPY --from=build /workspace/app/User/target/*.jar app.jar

# Change ownership
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://127.0.0.1:8081/api/v1/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]