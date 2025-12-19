#!/bin/bash
# Jenkins Docker Setup - Transport Microservices
# Quick start script for Jenkins with complete pipeline visualization

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Logging functions
log_info() { echo -e "${BLUE}ℹ️ [INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}✅ [SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}⚠️ [WARNING]${NC} $1"; }
log_error() { echo -e "${RED}❌ [ERROR]${NC} $1"; }
log_step() { echo -e "${PURPLE}🔄 [STEP]${NC} $1"; }

# Configuration
JENKINS_PORT=8080
JENKINS_URL="http://localhost:${JENKINS_PORT}"
PROJECT_NAME="transport-microservices"

# Banner
show_banner() {
    echo -e "${CYAN}"
    cat << 'EOF'
    ╔══════════════════════════════════════════════════════════════╗
    ║               🚀 JENKINS DOCKER SETUP                       ║
    ║                                                              ║
    ║         Transport Microservices Pipeline Visualization      ║
    ║                                                              ║
    ║   • Complete Jenkins setup with Docker                      ║
    ║   • All plugins pre-installed                               ║
    ║   • Blue Ocean visualization ready                          ║
    ║   • Pipeline configured and ready to run                    ║
    ║                                                              ║
    ╚══════════════════════════════════════════════════════════════╝
EOF
    echo -e "${NC}"
}

# Check prerequisites
check_prerequisites() {
    log_step "Checking prerequisites..."

    # Check Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed. Please install Docker first."
        exit 1
    fi

    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed. Please install Docker Compose first."
        exit 1
    fi

    # Check if Docker is running
    if ! docker info > /dev/null 2>&1; then
        log_error "Docker is not running. Please start Docker first."
        exit 1
    fi

    log_success "All prerequisites met"
}

# Create microservices network if not exists
create_network() {
    log_step "Setting up Docker network..."

    if ! docker network ls | grep -q microservices-network; then
        docker network create microservices-network
        log_success "Created microservices-network"
    else
        log_info "Network microservices-network already exists"
    fi
}

# Stop existing Jenkins if running
stop_existing_jenkins() {
    log_step "Checking for existing Jenkins containers..."

    if docker ps | grep -q jenkins-microservices; then
        log_warning "Stopping existing Jenkins container..."
        docker-compose -f docker-compose.jenkins.yml down
        log_success "Stopped existing Jenkins"
    fi
}

# Build and start Jenkins
start_jenkins() {
    log_step "Building and starting Jenkins..."

    # Build the custom Jenkins image
    log_info "Building custom Jenkins image with plugins..."
    docker-compose -f docker-compose.jenkins.yml build --no-cache

    # Start Jenkins services
    log_info "Starting Jenkins services..."
    docker-compose -f docker-compose.jenkins.yml up -d

    log_success "Jenkins containers started"
}

# Wait for Jenkins to be ready
wait_for_jenkins() {
    log_step "Waiting for Jenkins to be ready..."

    local max_attempts=60
    local attempt=1

    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "$JENKINS_URL/login" > /dev/null 2>&1; then
            log_success "Jenkins is ready!"
            break
        fi

        if [ $attempt -eq $max_attempts ]; then
            log_error "Jenkins failed to start within expected time"
            log_info "Check logs with: docker-compose -f docker-compose.jenkins.yml logs jenkins"
            exit 1
        fi

        echo -ne "\r🔄 Waiting for Jenkins... ($attempt/$max_attempts)"
        sleep 5
        ((attempt++))
    done
    echo ""
}

# Create initial pipeline job
create_pipeline_job() {
    log_step "Setting up pipeline job..."

    # Wait a bit more for Jenkins to be fully initialized
    sleep 10

    # Create job using Jenkins CLI or API
    local job_config="jenkins-job-config.xml"
    if [ -f "$job_config" ]; then
        log_info "Creating pipeline job from configuration..."

        # Use curl to create the job
        curl -X POST "$JENKINS_URL/createItem?name=$PROJECT_NAME-pipeline" \
             -u admin:admin123 \
             --header "Content-Type: application/xml" \
             --data-binary "@$job_config" \
             > /dev/null 2>&1 || log_warning "Job creation via API failed (might already exist)"

        log_success "Pipeline job configuration applied"
    else
        log_warning "Job configuration file not found. Create manually in Jenkins UI."
    fi
}

# Show connection info
show_connection_info() {
    echo ""
    log_success "🎉 Jenkins setup completed successfully!"
    echo ""
    echo -e "${CYAN}📊 ACCESS INFORMATION:${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo -e "🌐 ${YELLOW}Jenkins Dashboard:${NC}     $JENKINS_URL"
    echo -e "🎨 ${YELLOW}Blue Ocean (Modern UI):${NC} $JENKINS_URL/blue"
    echo -e "📋 ${YELLOW}Pipeline Job:${NC}          $JENKINS_URL/job/$PROJECT_NAME-pipeline/"
    echo -e "📊 ${YELLOW}Build Monitor:${NC}         $JENKINS_URL/view/Pipeline%20Monitor/"
    echo ""
    echo -e "🔐 ${YELLOW}Login Credentials:${NC}"
    echo -e "   Username: ${GREEN}admin${NC}"
    echo -e "   Password: ${GREEN}admin123${NC}"
    echo ""
    echo -e "${CYAN}🚀 QUICK ACTIONS:${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo -e "1. ${BLUE}Access Blue Ocean:${NC}      Open $JENKINS_URL/blue"
    echo -e "2. ${BLUE}Run Pipeline:${NC}           Go to job and click 'Build Now'"
    echo -e "3. ${BLUE}View Logs:${NC}              docker-compose -f docker-compose.jenkins.yml logs -f jenkins"
    echo -e "4. ${BLUE}Stop Jenkins:${NC}           ./start-jenkins.sh stop"
    echo -e "5. ${BLUE}Restart Jenkins:${NC}        ./start-jenkins.sh restart"
    echo ""
    echo -e "${YELLOW}💡 TIP:${NC} For best visualization experience, use Blue Ocean interface!"
    echo ""
}

# Show usage
show_usage() {
    echo "Usage: $0 [command]"
    echo ""
    echo "Commands:"
    echo "  start     Start Jenkins (default)"
    echo "  stop      Stop Jenkins"
    echo "  restart   Restart Jenkins"
    echo "  logs      Show Jenkins logs"
    echo "  status    Show Jenkins status"
    echo "  clean     Stop and remove all data"
    echo ""
    echo "Examples:"
    echo "  $0              # Start Jenkins"
    echo "  $0 start        # Start Jenkins"
    echo "  $0 stop         # Stop Jenkins"
    echo "  $0 restart      # Restart Jenkins"
    echo "  $0 logs         # Follow Jenkins logs"
}

# Stop Jenkins
stop_jenkins() {
    log_step "Stopping Jenkins..."
    docker-compose -f docker-compose.jenkins.yml down
    log_success "Jenkins stopped"
}

# Show Jenkins logs
show_logs() {
    log_info "Following Jenkins logs (Ctrl+C to exit)..."
    docker-compose -f docker-compose.jenkins.yml logs -f jenkins
}

# Show Jenkins status
show_status() {
    log_info "Jenkins container status:"
    docker-compose -f docker-compose.jenkins.yml ps

    echo ""
    log_info "Jenkins accessibility:"
    if curl -s -f "$JENKINS_URL/login" > /dev/null 2>&1; then
        log_success "Jenkins is accessible at $JENKINS_URL"
    else
        log_warning "Jenkins is not accessible"
    fi
}

# Clean Jenkins (remove all data)
clean_jenkins() {
    log_warning "This will remove all Jenkins data including jobs, configurations, and build history!"
    read -p "Are you sure? (yes/no): " confirm

    if [ "$confirm" = "yes" ]; then
        log_step "Cleaning Jenkins..."
        docker-compose -f docker-compose.jenkins.yml down -v
        docker volume rm jenkins_microservices_data jenkins_maven_cache jenkins_npm_cache jenkins_docker_cache jenkins_agent_workdir 2>/dev/null || true
        log_success "Jenkins cleaned"
    else
        log_info "Clean operation cancelled"
    fi
}

# Restart Jenkins
restart_jenkins() {
    log_step "Restarting Jenkins..."
    stop_jenkins
    sleep 5
    start_jenkins_full
}

# Full start process
start_jenkins_full() {
    show_banner
    check_prerequisites
    create_network
    stop_existing_jenkins
    start_jenkins
    wait_for_jenkins
    create_pipeline_job
    show_connection_info
}

# Main script logic
case "${1:-start}" in
    start)
        start_jenkins_full
        ;;
    stop)
        stop_jenkins
        ;;
    restart)
        restart_jenkins
        ;;
    logs)
        show_logs
        ;;
    status)
        show_status
        ;;
    clean)
        clean_jenkins
        ;;
    help|--help|-h)
        show_usage
        ;;
    *)
        log_error "Invalid command: $1"
        show_usage
        exit 1
        ;;
esac