# Jenkins Docker Image for Transport Microservices Pipeline
# Optimized with pre-installed plugins and tools

FROM jenkins/jenkins:lts

# Switch to root to install dependencies
USER root

# Install required system packages
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    git \
    maven \
    docker.io \
    kubectl \
    jq \
    && rm -rf /var/lib/apt/lists/*

# Install Node.js 18.x
RUN curl -fsSL https://deb.nodesource.com/setup_18.x | bash - \
    && apt-get install -y nodejs

# Install Docker Compose
RUN curl -L "https://github.com/docker/compose/releases/download/v2.21.0/docker-compose-$(uname -s)-$(uname -m)" \
    -o /usr/local/bin/docker-compose \
    && chmod +x /usr/local/bin/docker-compose

# Add jenkins user to docker group
RUN usermod -aG docker jenkins

# Switch back to jenkins user
USER jenkins

# Install Jenkins plugins
COPY jenkins-plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# Copy custom Jenkins configuration
COPY jenkins-config/ /usr/share/jenkins/ref/

# Copy pipeline configuration
COPY Jenkinsfile /usr/share/jenkins/ref/jobs/transport-microservices-pipeline/

# Set Jenkins environment
ENV JENKINS_OPTS="--httpPort=8080"
ENV JAVA_OPTS="-Xmx2g -XX:MaxMetaspaceSize=512m -Djenkins.install.runSetupWizard=false"

# Create initial admin user
ENV JENKINS_USER=admin
ENV JENKINS_PASS=admin123

# Expose ports
EXPOSE 8080 50000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=2m --retries=5 \
    CMD curl -f http://localhost:8080/login || exit 1