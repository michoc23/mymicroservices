FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven configuration files
COPY pom.xml .
COPY Subscription/pom.xml ./Subscription/

# Copy source code
COPY Subscription/src ./Subscription/src

# Build the application
RUN apt-get update && apt-get install -y maven && \
    mvn clean package -pl Subscription -am -DskipTests && \
    cp Subscription/target/subscription-service-*.jar app.jar && \
    rm -rf /root/.m2 /var/lib/apt/lists/*

# Expose port
EXPOSE 8084

# Set JVM options
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]