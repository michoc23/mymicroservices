#!/bin/bash
# Jenkins Setup Script for Transport Microservices Pipeline
# This script helps configure Jenkins for optimal pipeline visualization

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

JENKINS_URL=${1:-"http://localhost:8080"}
JENKINS_HOME=${2:-"/var/jenkins_home"}

log_info "🚀 Setting up Jenkins for Transport Microservices Pipeline"
log_info "Jenkins URL: $JENKINS_URL"

# Check if Jenkins is running
check_jenkins() {
    log_info "Checking if Jenkins is accessible..."
    if curl -s -f "$JENKINS_URL" > /dev/null; then
        log_success "Jenkins is running at $JENKINS_URL"
    else
        log_error "Jenkins is not accessible at $JENKINS_URL"
        log_info "Please ensure Jenkins is running. You can start it with:"
        log_info "  Docker: docker run -p 8080:8080 jenkins/jenkins:lts"
        exit 1
    fi
}

# Install required Jenkins plugins
install_plugins() {
    log_info "📦 Required Jenkins Plugins for Pipeline Visualization:"
    echo "
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
    "

    log_warning "Please install these plugins in Jenkins:"
    log_info "1. Go to Manage Jenkins > Manage Plugins"
    log_info "2. Search for and install the plugins listed above"
    log_info "3. Restart Jenkins after installation"
}

# Configure Global Tools
configure_tools() {
    log_info "🔧 Configuring Global Tools in Jenkins:"
    echo "
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
    "
}

# Configure Credentials
configure_credentials() {
    log_info "🔐 Setting up Credentials in Jenkins:"
    echo "
    Go to: Manage Jenkins > Manage Credentials > System > Global credentials

    Required Credentials:
    ✅ docker-registry-url          # Type: Secret text (e.g., https://index.docker.io/v1/)
    ✅ docker-registry-credentials  # Type: Username/Password (Docker Hub login)
    ✅ github-token                 # Type: Secret text (GitHub Personal Access Token)
    ✅ k8s-config                  # Type: Secret file (kubeconfig for K8s access)

    Email Configuration:
    ✅ Configure SMTP settings in System Configuration
    ✅ Test email notifications
    "
}

# Create Jenkins Pipeline Job
create_pipeline_job() {
    log_info "📋 Creating Jenkins Pipeline Job:"

    # Create job configuration XML
    cat > pipeline-job-config.xml << 'EOF'
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job@2.40">
  <actions>
    <org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobAction plugin="pipeline-model-definition@1.8.5"/>
    <org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobPropertyTrackerAction plugin="pipeline-model-definition@1.8.5">
      <jobProperties/>
      <triggers/>
      <parameters/>
      <options/>
    </org.jenkinsci.plugins.pipeline.modeldefinition.actions.DeclarativeJobPropertyTrackerAction>
  </actions>
  <description>Transport Microservices CI/CD Pipeline - Automated build, test, and deployment</description>
  <keepDependencies>false</keepDependencies>
  <properties>
    <hudson.plugins.jira.JiraProjectProperty plugin="jira@3.1.1"/>
    <hudson.model.ParametersDefinitionProperty>
      <parameterDefinitions>
        <hudson.model.ChoiceParameterDefinition>
          <name>DEPLOYMENT_ENVIRONMENT</name>
          <description>Target deployment environment</description>
          <choices class="java.util.Arrays$ArrayList">
            <a class="string-array">
              <string>staging</string>
              <string>production</string>
              <string>development</string>
            </a>
          </choices>
        </hudson.model.ChoiceParameterDefinition>
        <hudson.model.BooleanParameterDefinition>
          <name>SKIP_TESTS</name>
          <description>Skip running tests (not recommended for production)</description>
          <defaultValue>false</defaultValue>
        </hudson.model.BooleanParameterDefinition>
        <hudson.model.BooleanParameterDefinition>
          <name>DEPLOY_TO_K8S</name>
          <description>Deploy to Kubernetes after successful build</description>
          <defaultValue>true</defaultValue>
        </hudson.model.BooleanParameterDefinition>
      </parameterDefinitions>
    </hudson.model.ParametersDefinitionProperty>
    <org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>
      <triggers>
        <hudson.triggers.SCMTrigger>
          <spec>H/5 * * * *</spec>
          <ignorePostCommitHooks>false</ignorePostCommitHooks>
        </hudson.triggers.SCMTrigger>
      </triggers>
    </org.jenkinsci.plugins.workflow.job.properties.PipelineTriggersJobProperty>
  </properties>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition" plugin="workflow-cps@2.92">
    <scm class="hudson.plugins.git.GitSCM" plugin="git@4.8.3">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>YOUR_GIT_REPOSITORY_URL</url>
          <credentialsId>github-token</credentialsId>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/main</name>
        </hudson.plugins.git.BranchSpec>
        <hudson.plugins.git.BranchSpec>
          <name>*/develop</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="list"/>
      <extensions/>
    </scm>
    <scriptPath>mymicroservices/Jenkinsfile</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers/>
  <disabled>false</disabled>
</flow-definition>
EOF

    log_success "Pipeline job configuration created: pipeline-job-config.xml"
    log_info "Import this into Jenkins or create manually with the settings above"
}

# Generate Pipeline Visualization Guide
create_visualization_guide() {
    log_info "📊 Creating Pipeline Visualization Guide..."

    cat > JENKINS_VISUALIZATION.md << 'EOF'
# Jenkins Pipeline Visualization Guide

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

## 📊 Visualization Options

### **1. Blue Ocean (Recommended)**
- **Access:** Jenkins → Blue Ocean
- **Features:**
  - ✅ Modern, intuitive pipeline view
  - ✅ Real-time execution progress
  - ✅ Branch-based pipeline visualization
  - ✅ Detailed log viewer with search
  - ✅ Pipeline editor (visual)

### **2. Classic Pipeline Stage View**
- **Access:** Job Dashboard → Stage View
- **Features:**
  - ✅ Stage execution time tracking
  - ✅ Historical trend analysis
  - ✅ Parallel stage visualization
  - ✅ Build artifacts links

### **3. Pipeline Graph View**
- **Access:** Job → Pipeline Graph
- **Features:**
  - ✅ Node-based pipeline visualization
  - ✅ Dependency mapping
  - ✅ Execution flow analysis

## 🔍 Monitoring Dashboard

### **Key Metrics to Monitor:**

1. **Build Success Rate**
   - Target: >95%
   - Monitor: Last 20 builds

2. **Build Duration**
   - Target: <15 minutes
   - Alert: >20 minutes

3. **Test Coverage**
   - Target: >80%
   - Trend: Increasing

4. **Security Vulnerabilities**
   - Target: 0 High/Critical
   - Action: Immediate fix required

### **Stage Performance Benchmarks:**

| Stage | Expected Duration | Alert Threshold |
|-------|------------------|----------------|
| Checkout | <30s | >1min |
| Environment Setup | <1min | >2min |
| Code Quality | <2min | >5min |
| Build & Test | <5min | >8min |
| Unit Tests | <3min | >5min |
| Integration Tests | <2min | >4min |
| Docker Build | <3min | >6min |
| Security Scan | <2min | >5min |
| E2E Tests | <3min | >5min |
| Deploy | <2min | >4min |

## 🎨 Custom Dashboard Widgets

### **Add to Jenkins Dashboard:**

1. **Build Monitor View**
   ```
   Plugin: Build Monitor View
   Shows: All pipeline statuses at a glance
   ```

2. **Build Pipeline Plugin**
   ```
   Plugin: Build Pipeline
   Shows: Upstream/downstream job relationships
   ```

3. **Radiator View**
   ```
   Plugin: Radiator View
   Shows: Large screen build status display
   ```

## 🔔 Alert Configuration

### **Email Notifications:**
- ✅ Build failures
- ✅ Security vulnerability detection
- ✅ Deployment completion
- ✅ Test coverage drops

### **Slack Integration:**
- ✅ Real-time build status
- ✅ Stage completion notifications
- ✅ Deployment approvals

## 📈 Analytics & Reporting

### **Built-in Reports:**
1. **Test Results Trend**
2. **Code Coverage Trend**
3. **Security Scan Results**
4. **Build Time Analysis**
5. **Deployment Frequency**

### **Custom Reports:**
- Service-specific metrics
- Environment deployment success rates
- Resource usage tracking

## 🚀 Getting Started

### **Quick Setup:**
1. Install required plugins
2. Configure global tools
3. Set up credentials
4. Import pipeline job
5. Run first build
6. Access Blue Ocean for visualization

### **Best Practices:**
- Use descriptive stage names
- Add parallel execution where possible
- Include comprehensive logging
- Set appropriate timeouts
- Configure proper error handling
EOF

    log_success "Visualization guide created: JENKINS_VISUALIZATION.md"
}

# Create Docker Compose for Jenkins setup
create_jenkins_docker() {
    log_info "🐳 Creating Jenkins Docker Setup..."

    cat > docker-compose.jenkins.yml << 'EOF'
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
      - JENKINS_SECRET=${AGENT_SECRET:-changeme}
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
EOF

    # Create plugins list
    cat > jenkins-plugins.txt << 'EOF'
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
EOF

    log_success "Jenkins Docker setup created!"
    log_info "Start Jenkins with: docker-compose -f docker-compose.jenkins.yml up -d"
}

# Main execution
main() {
    echo "
    🚀 Jenkins Pipeline Visualization Setup
    =====================================

    This script will help you set up Jenkins for optimal
    pipeline visualization of the Transport Microservices project.
    "

    check_jenkins
    install_plugins
    configure_tools
    configure_credentials
    create_pipeline_job
    create_visualization_guide
    create_jenkins_docker

    log_success "🎉 Jenkins setup completed!"
    echo "
    📋 Next Steps:
    1. Install the required plugins in Jenkins
    2. Configure global tools (Maven, Node.js, Docker)
    3. Set up credentials for Docker and GitHub
    4. Import the pipeline job configuration
    5. Access Blue Ocean for modern pipeline visualization

    📊 Visualization URLs:
    - Jenkins Dashboard: $JENKINS_URL
    - Blue Ocean: $JENKINS_URL/blue
    - Stage View: $JENKINS_URL/job/transport-microservices/

    📚 Documentation created:
    - JENKINS_VISUALIZATION.md: Complete visualization guide
    - pipeline-job-config.xml: Jenkins job configuration
    - docker-compose.jenkins.yml: Jenkins Docker setup
    "
}

# Show usage
show_usage() {
    echo "Usage: $0 [JENKINS_URL] [JENKINS_HOME]"
    echo ""
    echo "Arguments:"
    echo "  JENKINS_URL   Jenkins URL (default: http://localhost:8080)"
    echo "  JENKINS_HOME  Jenkins home directory (default: /var/jenkins_home)"
    echo ""
    echo "Examples:"
    echo "  $0                                    # Use defaults"
    echo "  $0 http://jenkins.company.com:8080   # Custom Jenkins URL"
    echo "  $0 http://localhost:8080 /opt/jenkins # Custom Jenkins URL and home"
}

# Parse arguments
case "${1:-}" in
    help|--help|-h)
        show_usage
        exit 0
        ;;
    *)
        main "$@"
        ;;
esac