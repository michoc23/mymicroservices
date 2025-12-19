# Jenkins Setup Script for Transport Microservices Pipeline (PowerShell)
# This script helps configure Jenkins for optimal pipeline visualization

param(
    [string]$JenkinsUrl = "http://localhost:8080",
    [string]$JenkinsHome = "/var/jenkins_home"
)

$ErrorActionPreference = "Stop"

# Colors for output
function Write-Info { param($Message) Write-Host "🔵 [INFO] $Message" -ForegroundColor Blue }
function Write-Success { param($Message) Write-Host "✅ [SUCCESS] $Message" -ForegroundColor Green }
function Write-Warning { param($Message) Write-Host "⚠️ [WARNING] $Message" -ForegroundColor Yellow }
function Write-Error { param($Message) Write-Host "❌ [ERROR] $Message" -ForegroundColor Red }

Write-Info "🚀 Setting up Jenkins for Transport Microservices Pipeline"
Write-Info "Jenkins URL: $JenkinsUrl"

# Check if Jenkins is running
function Test-Jenkins {
    Write-Info "Checking if Jenkins is accessible..."
    try {
        $response = Invoke-WebRequest -Uri $JenkinsUrl -UseBasicParsing -TimeoutSec 10
        Write-Success "Jenkins is running at $JenkinsUrl"
        return $true
    }
    catch {
        Write-Error "Jenkins is not accessible at $JenkinsUrl"
        Write-Info "Please ensure Jenkins is running. You can start it with:"
        Write-Info "  Docker: docker run -p 8080:8080 jenkins/jenkins:lts"
        return $false
    }
}

# Show required Jenkins plugins
function Show-RequiredPlugins {
    Write-Info "📦 Required Jenkins Plugins for Pipeline Visualization:"

    $plugins = @"

    Core Pipeline Plugins:
    ✅ pipeline-stage-view          # Pipeline Stage View for visual representation
    ✅ blueocean                    # Blue Ocean for modern pipeline UI
    ✅ pipeline-graph-analysis      # Pipeline graph analysis
    ✅ pipeline-milestone-step      # Pipeline milestones

    Build & Test Plugins:
    ✅ maven-plugin                 # Maven integration
    ✅ nodejs                       # Node.js support
    ✅ docker-plugin                # Docker integration
    ✅ docker-workflow              # Docker pipeline support

    Quality & Security:
    ✅ checkstyle                   # Checkstyle reports
    ✅ spotbugs                     # SpotBugs analysis
    ✅ jacoco                       # Code coverage
    ✅ junit                        # Test results
    ✅ htmlpublisher                # HTML reports

    Notifications:
    ✅ email-ext                    # Extended email notifications
    ✅ slack                        # Slack integration (optional)

    Kubernetes:
    ✅ kubernetes                   # Kubernetes deployment
    ✅ kubernetes-cli               # kubectl integration

"@

    Write-Host $plugins
    Write-Warning "Please install these plugins in Jenkins:"
    Write-Info "1. Go to Manage Jenkins > Manage Plugins"
    Write-Info "2. Search for and install the plugins listed above"
    Write-Info "3. Restart Jenkins after installation"
}

# Show global tools configuration
function Show-ToolsConfiguration {
    Write-Info "🔧 Configuring Global Tools in Jenkins:"

    $toolsConfig = @"

    Go to: Manage Jenkins > Global Tool Configuration

    Maven Configuration:
    ✅ Name: Maven-3.9.0
    ✅ Version: 3.9.0 (Auto-install from Apache)
    ✅ Install automatically: ☑

    Node.js Configuration:
    ✅ Name: NodeJS-18
    ✅ Version: 18.x.x (Latest LTS)
    ✅ Install automatically: ☑
    ✅ Global npm packages: eslint, @vue/cli

    Docker Configuration:
    ✅ Name: Docker
    ✅ Installation root: /usr/bin/docker (or auto-detect)
    ✅ Install automatically: ☑

"@

    Write-Host $toolsConfig
}

# Show credentials configuration
function Show-CredentialsConfiguration {
    Write-Info "🔐 Setting up Credentials in Jenkins:"

    $credentialsConfig = @"

    Go to: Manage Jenkins > Manage Credentials > System > Global credentials

    Required Credentials:
    ✅ docker-registry-url          # Type: Secret text (e.g., https://index.docker.io/v1/)
    ✅ docker-registry-credentials  # Type: Username/Password (Docker Hub login)
    ✅ github-credentials           # Type: Username/Password or Token (GitHub access)
    ✅ k8s-config                  # Type: Secret file (kubeconfig for K8s access)

    Email Configuration:
    ✅ Configure SMTP settings in System Configuration
    ✅ Test email notifications

"@

    Write-Host $credentialsConfig
}

# Create Jenkins plugins list file
function New-PluginsFile {
    Write-Info "📝 Creating Jenkins plugins list..."

    $pluginsList = @"
pipeline-stage-view:latest
blueocean:latest
maven-plugin:latest
nodejs:latest
docker-plugin:latest
docker-workflow:latest
checkstyle:latest
spotbugs:latest
jacoco:latest
junit:latest
htmlpublisher:latest
email-ext:latest
kubernetes:latest
kubernetes-cli:latest
pipeline-graph-analysis:latest
pipeline-milestone-step:latest
build-monitor-view:latest
radiator-view:latest
workflow-aggregator:latest
pipeline-model-definition:latest
"@

    $pluginsList | Out-File -FilePath "jenkins-plugins.txt" -Encoding utf8
    Write-Success "Jenkins plugins list created: jenkins-plugins.txt"
}

# Create Docker Compose for Jenkins
function New-JenkinsDocker {
    Write-Info "🐳 Creating Jenkins Docker Setup..."

    $dockerCompose = @"
version: '3.8'

services:
  jenkins:
    image: jenkins/jenkins:lts
    container_name: jenkins-master
    ports:
      - "8080:8080"
      - "50000:50000"
    volumes:
      - jenkins_home:/var/jenkins_home
      - /var/run/docker.sock:/var/run/docker.sock
      - ./jenkins-plugins.txt:/usr/share/jenkins/ref/plugins.txt
    environment:
      - JENKINS_OPTS=--httpPort=8080
      - JAVA_OPTS=-Xmx2g -Djenkins.install.runSetupWizard=false
    restart: unless-stopped
    networks:
      - jenkins-network

  jenkins-agent:
    image: jenkins/inbound-agent:latest
    container_name: jenkins-agent
    environment:
      - JENKINS_URL=http://jenkins:8080
      - JENKINS_SECRET=changeme
      - JENKINS_AGENT_NAME=docker-agent
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
      - maven_cache:/root/.m2
      - npm_cache:/root/.npm
    depends_on:
      - jenkins
    restart: unless-stopped
    networks:
      - jenkins-network

volumes:
  jenkins_home:
    name: jenkins_home
  maven_cache:
    name: maven_cache
  npm_cache:
    name: npm_cache

networks:
  jenkins-network:
    name: jenkins-network
    driver: bridge
"@

    $dockerCompose | Out-File -FilePath "docker-compose.jenkins.yml" -Encoding utf8
    Write-Success "Jenkins Docker setup created: docker-compose.jenkins.yml"
    Write-Info "Start Jenkins with: docker-compose -f docker-compose.jenkins.yml up -d"
}

# Create visualization guide
function New-VisualizationGuide {
    Write-Info "📊 Creating Pipeline Visualization Guide..."

    $guide = @"
# Jenkins Pipeline Visualization - Quick Start Guide

## 🎯 Pipeline Overview

### **10-Stage CI/CD Pipeline:**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   1. Checkout   │ -> │ 2. Environment  │ -> │ 3. Code Quality │
│                 │    │     Setup       │    │   & Security    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ 4. Build & Test │ <- │   Unit Tests    │ <- │                 │
│   (Parallel)    │    │   (Parallel)    │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
        │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ 6. Integration  │ -> │ 7. Docker Build │ -> │ 8. Security     │
│     Tests       │    │   (Parallel)    │    │    Scanning     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
        │
┌─────────────────┐    ┌─────────────────┐
│ 9. E2E Tests    │ -> │ 10. Deploy      │
│                 │    │  (K8s/Registry) │
└─────────────────┘    └─────────────────┘
```

## 🚀 Quick Setup Steps

### **1. Start Jenkins**
```powershell
# Using Docker (Recommended)
docker-compose -f docker-compose.jenkins.yml up -d

# Wait for Jenkins to start
Start-Sleep -Seconds 60

# Access Jenkins at http://localhost:8080
```

### **2. Install Required Plugins**
1. Go to **Manage Jenkins** → **Manage Plugins**
2. Install plugins from the generated `jenkins-plugins.txt`
3. **Restart Jenkins** after installation

### **3. Configure Global Tools**
1. Go to **Manage Jenkins** → **Global Tool Configuration**
2. Add **Maven-3.9.0**, **NodeJS-18**, and **Docker**
3. Enable **Install automatically** for all tools

### **4. Set Up Credentials**
1. Go to **Manage Jenkins** → **Manage Credentials**
2. Add required credentials:
   - `docker-registry-credentials` (Docker Hub)
   - `github-credentials` (GitHub access)
   - `k8s-config` (Kubernetes config file)

### **5. Create Pipeline Job**
1. Go to **New Item** → **Pipeline**
2. Configure SCM to point to your Git repository
3. Set **Script Path** to `Jenkinsfile`
4. Save and run the pipeline

## 📊 Visualization Options

### **🎨 Blue Ocean (Recommended)**
- **URL:** http://localhost:8080/blue
- **Features:**
  - ✅ Modern, intuitive pipeline view
  - ✅ Real-time execution progress
  - ✅ Branch-based pipeline visualization
  - ✅ Detailed log viewer with search
  - ✅ Visual pipeline editor

### **📈 Classic Stage View**
- **URL:** http://localhost:8080/job/[job-name]/
- **Features:**
  - ✅ Stage execution time tracking
  - ✅ Historical trend analysis
  - ✅ Parallel stage visualization
  - ✅ Build artifacts access

### **📊 Build Monitor**
- **URL:** http://localhost:8080/plugin/build-monitor-view/
- **Features:**
  - ✅ Large screen display
  - ✅ Multi-project overview
  - ✅ Status at a glance

## 🔔 Monitoring & Alerts

### **Key Metrics to Watch:**
- ✅ Build Success Rate (Target: >95%)
- ✅ Build Duration (Target: <15 minutes)
- ✅ Test Coverage (Target: >80%)
- ✅ Security Vulnerabilities (Target: 0 High/Critical)

### **Alert Configuration:**
- ✅ Email notifications for failures
- ✅ Slack integration for team updates
- ✅ Dashboard widgets for visibility

## 🎯 Best Practices

### **Pipeline Visualization:**
1. **Use descriptive stage names**
2. **Group related steps in parallel**
3. **Add meaningful echo statements**
4. **Configure proper timeouts**
5. **Include comprehensive error handling**

### **Monitoring:**
1. **Set up build trends**
2. **Configure email notifications**
3. **Use Blue Ocean for modern UI**
4. **Monitor stage duration trends**
5. **Track test coverage over time**

## 🚨 Troubleshooting

### **Common Issues:**
- **Plugin conflicts:** Check plugin dependencies
- **Build timeouts:** Increase timeout values
- **Resource issues:** Monitor Jenkins system load
- **Git access:** Verify credentials configuration

### **Performance Optimization:**
- **Use parallel stages** where possible
- **Optimize Docker builds** with multi-stage builds
- **Cache dependencies** (Maven, npm)
- **Use lightweight agents** for simple tasks

## 📞 Support

For issues with this setup:
1. Check Jenkins logs: `docker logs jenkins-master`
2. Verify plugin installation
3. Confirm tool configurations
4. Test credentials access

Happy building! 🚀
"@

    $guide | Out-File -FilePath "JENKINS_PIPELINE_GUIDE.md" -Encoding utf8
    Write-Success "Visualization guide created: JENKINS_PIPELINE_GUIDE.md"
}

# Main execution
function Main {
    $banner = @"

    🚀 Jenkins Pipeline Visualization Setup
    =====================================

    This script will help you set up Jenkins for optimal
    pipeline visualization of the Transport Microservices project.

"@

    Write-Host $banner -ForegroundColor Cyan

    if (Test-Jenkins) {
        Show-RequiredPlugins
        Show-ToolsConfiguration
        Show-CredentialsConfiguration
        New-PluginsFile
        New-JenkinsDocker
        New-VisualizationGuide

        Write-Success "🎉 Jenkins setup completed!"

        $nextSteps = @"

    📋 Next Steps:
    1. Install the required plugins in Jenkins
    2. Configure global tools (Maven, Node.js, Docker)
    3. Set up credentials for Docker and GitHub
    4. Import the pipeline job configuration
    5. Access Blue Ocean for modern pipeline visualization

    📊 Visualization URLs:
    - Jenkins Dashboard: $JenkinsUrl
    - Blue Ocean: $JenkinsUrl/blue
    - Pipeline Job: $JenkinsUrl/job/transport-microservices/

    📚 Documentation created:
    - JENKINS_PIPELINE_GUIDE.md: Complete setup guide
    - jenkins-job-config.xml: Jenkins job configuration
    - docker-compose.jenkins.yml: Jenkins Docker setup
    - jenkins-plugins.txt: Required plugins list

"@

        Write-Host $nextSteps -ForegroundColor Green
    }
}

# Show usage
function Show-Usage {
    Write-Host "Usage: .\jenkins-setup.ps1 [JenkinsUrl] [JenkinsHome]"
    Write-Host ""
    Write-Host "Parameters:"
    Write-Host "  -JenkinsUrl   Jenkins URL (default: http://localhost:8080)"
    Write-Host "  -JenkinsHome  Jenkins home directory (default: /var/jenkins_home)"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\jenkins-setup.ps1"
    Write-Host "  .\jenkins-setup.ps1 -JenkinsUrl 'http://jenkins.company.com:8080'"
    Write-Host "  .\jenkins-setup.ps1 -JenkinsUrl 'http://localhost:8080' -JenkinsHome '/opt/jenkins'"
}

# Parse arguments and run
if ($args -contains "help" -or $args -contains "--help" -or $args -contains "-h") {
    Show-Usage
}
else {
    Main
}