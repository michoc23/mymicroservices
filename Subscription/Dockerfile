# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build

WORKDIR /app

# Copy Maven configuration files
COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/
COPY User/pom.xml User/
COPY Ticket/pom.xml Ticket/
COPY Subscription/pom.xml Subscription/

# Download dependencies
RUN mvn -f Subscription/pom.xml dependency:go-offline

# Copy source code
COPY Subscription/src Subscription/src

# Build the application
RUN mvn -f Subscription/pom.xml clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/Subscription/target/*.jar app.jar

# Change ownership
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8084

# Set JVM options
ENV JAVA_OPTS="-Xmx512m -Xms256m -Djava.security.egd=file:/dev/./urandom"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8084/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]