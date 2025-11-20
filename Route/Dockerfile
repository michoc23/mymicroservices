# Build stage
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy parent pom and all module poms
COPY pom.xml .
COPY Route/pom.xml Route/
COPY User/pom.xml User/
COPY api-gateway/pom.xml api-gateway/
COPY Ticket/pom.xml Ticket/
COPY Subscription/pom.xml Subscription/
COPY BusGeolocation/pom.xml BusGeolocation/

# Download dependencies
RUN mvn dependency:go-offline -pl Route -am

# Copy source code
COPY Route/src Route/src

# Build the application
RUN mvn clean package -pl Route -am -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 appgroup && \
    adduser -D -u 1001 -G appgroup appuser

# Copy the built jar file
COPY --from=build /app/Route/target/*.jar app.jar

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8085

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

