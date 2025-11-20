# Jenkins Pipeline Setup Guide

## Overview
This guide helps you set up the Jenkins pipeline for the Urban Transport Management System microservices project.

## Prerequisites

### 1. Jenkins Installation & Plugins
Ensure Jenkins has the following plugins installed:
- Pipeline
- Docker Pipeline
- NodeJS Plugin
- Maven Integration
- Blue Ocean (optional, for better UI)
- Email Extension
- JUnit Plugin
- Jacoco Plugin
- OWASP Dependency Check Plugin
- Checkstyle Plugin
- SpotBugs Plugin

### 2. Global Tools Configuration
Configure the following in Jenkins > Manage Jenkins > Global Tool Configuration:

#### Maven Configuration
- Name: `Maven-3.9.0`
- Install automatically: ✓
- Version: `3.9.0`

#### NodeJS Configuration
- Name: `NodeJS-18`
- Install automatically: ✓
- Version: `18.x.x`

#### Docker Configuration
- Name: `Docker`
- Install automatically: ✓
- Or specify Docker installation path

### 3. Credentials Configuration
Add the following credentials in Jenkins > Manage Jenkins > Credentials:

#### Docker Registry Credentials
- Kind: Username with password
- ID: `docker-registry-credentials`
- Username: Your Docker Hub username
- Password: Your Docker Hub password

#### Docker Registry URL
- Kind: Secret text
- ID: `docker-registry-url`
- Secret: `docker.io` (for Docker Hub) or your private registry URL

## Pipeline Configuration

### 1. Create New Pipeline Job
1. Go to Jenkins Dashboard
2. Click "New Item"
3. Enter job name: `urban-transport-microservices`
4. Select "Pipeline"
5. Click "OK"

### 2. Configure Pipeline
In the pipeline configuration:

#### General Settings
- ✓ GitHub project: `https://github.com/your-username/your-repo`
- ✓ Discard old builds: Keep last 10 builds

#### Build Triggers
- ✓ GitHub hook trigger for GITScm polling
- ✓ Poll SCM: `H/5 * * * *` (every 5 minutes)

#### Pipeline Definition
- Definition: `Pipeline script from SCM`
- SCM: `Git`
- Repository URL: Your repository URL
- Credentials: Your Git credentials
- Branch Specifier: `*/main` and `*/develop`
- Script Path: `soa/mymicroservices/Jenkinsfile`

## Environment-Specific Configuration

### Development Environment
```properties
# .env.development
DATABASE_URL=jdbc:postgresql://localhost:5432/transport_dev
REDIS_URL=redis://localhost:6379
API_GATEWAY_PORT=8082
USER_SERVICE_PORT=8081
TICKET_SERVICE_PORT=8083
SUBSCRIPTION_SERVICE_PORT=8084
```

### Staging Environment
```properties
# .env.staging
DATABASE_URL=jdbc:postgresql://staging-db:5432/transport_staging
REDIS_URL=redis://staging-redis:6379
API_GATEWAY_PORT=8082
USER_SERVICE_PORT=8081
TICKET_SERVICE_PORT=8083
SUBSCRIPTION_SERVICE_PORT=8084
```

### Production Environment
```properties
# .env.production
DATABASE_URL=jdbc:postgresql://prod-db:5432/transport_prod
REDIS_URL=redis://prod-redis:6379
API_GATEWAY_PORT=8082
USER_SERVICE_PORT=8081
TICKET_SERVICE_PORT=8083
SUBSCRIPTION_SERVICE_PORT=8084
```

## Security Configuration

### 1. Code Quality Gates
The pipeline includes several quality gates:
- **Checkstyle**: Code style validation
- **SpotBugs**: Static analysis for bugs
- **OWASP Dependency Check**: Security vulnerability scanning
- **ESLint**: Frontend code quality

### 2. Container Security
- **Trivy**: Container vulnerability scanning
- **Docker image scanning**: Automated security checks

## Notification Configuration

### Email Notifications
Configure SMTP in Jenkins > Manage Jenkins > Configure System:

```
SMTP Server: smtp.gmail.com
SMTP Port: 587
Username: your-email@company.com
Password: your-app-password
Use SSL/TLS: ✓
```

Update email addresses in the Jenkinsfile:
```groovy
to: "developer@company.com,devops-team@company.com"
```

## Monitoring & Metrics

### 1. Build Health
- Test coverage reports (Jacoco)
- Code quality metrics
- Build duration tracking
- Success/failure rates

### 2. Application Health
- Service health checks via actuator endpoints
- Docker container health checks
- Database connectivity checks

## Troubleshooting

### Common Issues

#### Maven Build Failures
```bash
# Clear local repository
rm -rf ~/.m2/repository
mvn clean install
```

#### Docker Build Issues
```bash
# Clean Docker cache
docker system prune -a -f
docker builder prune -a -f
```

#### Frontend Build Issues
```bash
# Clear npm cache
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

### Pipeline Debugging
Enable verbose logging in Jenkinsfile:
```groovy
environment {
    MAVEN_OPTS = '-X -Dmaven.test.failure.ignore=false'
}
```

## Performance Optimization

### 1. Parallel Execution
The pipeline uses parallel stages for:
- Service builds
- Test execution
- Docker image builds
- Security scans

### 2. Caching Strategy
- Maven local repository caching
- NPM cache for frontend builds
- Docker layer caching

### 3. Resource Management
- Limit concurrent builds: 2
- Clean workspace after builds
- Prune Docker images regularly

## Deployment Strategies

### 1. Blue-Green Deployment
```groovy
stage('Blue-Green Deploy') {
    steps {
        script {
            // Switch traffic between blue and green environments
            sh 'kubectl patch service frontend-service -p \'{"spec":{"selector":{"version":"green"}}}\''
        }
    }
}
```

### 2. Rolling Updates
```groovy
stage('Rolling Update') {
    steps {
        script {
            // Update deployment with new image version
            sh "kubectl set image deployment/user-service user-service=user-service:${APP_VERSION}"
        }
    }
}
```

### 3. Canary Deployment
```groovy
stage('Canary Deploy') {
    steps {
        script {
            // Deploy to small subset of users
            sh "kubectl scale deployment user-service-canary --replicas=1"
        }
    }
}
```

## Backup & Recovery

### 1. Database Backups
```bash
# Automated database backup
pg_dump -h ${DB_HOST} -U ${DB_USER} ${DB_NAME} > backup_$(date +%Y%m%d_%H%M%S).sql
```

### 2. Configuration Backups
- Jenkins job configurations
- Environment variables
- Secrets and credentials

## Compliance & Auditing

### 1. Build Artifacts
- All builds are archived
- Test reports are preserved
- Security scan results are stored

### 2. Audit Trail
- All deployments are logged
- Approval workflows for production
- Change tracking via Git commits

---

## Quick Start Checklist

- [ ] Install required Jenkins plugins
- [ ] Configure global tools (Maven, NodeJS, Docker)
- [ ] Set up credentials for Docker registry
- [ ] Create pipeline job with SCM configuration
- [ ] Test pipeline with feature branch
- [ ] Configure email notifications
- [ ] Set up monitoring and alerts
- [ ] Document deployment procedures

For additional support, contact the DevOps team or refer to the project documentation.