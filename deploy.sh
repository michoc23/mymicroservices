#!/bin/bash
# Transport Microservices - Automated Deployment Script
# Usage: ./deploy.sh [local|staging|production]

set -e

ENVIRONMENT=${1:-local}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")"; pwd)"
PROJECT_ROOT="$SCRIPT_DIR"

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

# Check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        log_error "Docker is not running. Please start Docker and try again."
        exit 1
    fi
    log_success "Docker is running"
}

# Check if kubectl is available for Kubernetes deployments
check_kubectl() {
    if ! command -v kubectl &> /dev/null; then
        log_error "kubectl is not installed. Please install kubectl for Kubernetes deployments."
        exit 1
    fi
    log_success "kubectl is available"
}

# Build all services
build_services() {
    log_info "Building all microservices..."

    # Build backend services
    log_info "Building backend services with Maven..."
    mvn clean package -DskipTests=true

    # Build frontend
    log_info "Building frontend..."
    cd Frontend
    npm ci
    CI=false npm run build
    cd ..

    log_success "All services built successfully"
}

# Deploy locally using Docker Compose
deploy_local() {
    log_info "Deploying to local environment using Docker Compose..."

    check_docker
    build_services

    log_info "Starting all services with Docker Compose..."
    docker-compose down --remove-orphans
    docker-compose up -d --build

    log_info "Waiting for services to be healthy..."
    sleep 30

    # Check service health
    check_service_health_local

    log_success "Local deployment completed!"
    log_info "Access the application at:"
    log_info "  Frontend: http://localhost:3000"
    log_info "  API Gateway: http://localhost:8082"
    log_info "  User Service: http://localhost:8081"
    log_info "  Ticket Service: http://localhost:8083"
    log_info "  Subscription Service: http://localhost:8084"
    log_info "  Route Service: http://localhost:8085"
    log_info "  Bus Geolocation: http://localhost:8086"
    log_info "  pgAdmin: http://localhost:5051 (admin@admin.com / admin)"
}

# Deploy to Kubernetes staging
deploy_staging() {
    log_info "Deploying to Kubernetes staging environment..."

    check_kubectl

    local K8S_NS="transport-staging"

    # Create namespace if it doesn't exist
    kubectl get ns $K8S_NS || kubectl create ns $K8S_NS

    # Apply manifests
    log_info "Applying Kubernetes manifests to $K8S_NS namespace..."
    kubectl apply -n $K8S_NS -f k8s/manifests/

    # Wait for deployments to be ready
    log_info "Waiting for deployments to be ready..."
    kubectl -n $K8S_NS rollout status deployment/user-service --timeout=300s
    kubectl -n $K8S_NS rollout status deployment/ticket-service --timeout=300s
    kubectl -n $K8S_NS rollout status deployment/subscription-service --timeout=300s
    kubectl -n $K8S_NS rollout status deployment/route-service --timeout=300s
    kubectl -n $K8S_NS rollout status deployment/bus-geolocation-service --timeout=300s
    kubectl -n $K8S_NS rollout status deployment/api-gateway --timeout=300s
    kubectl -n $K8S_NS rollout status deployment/frontend-app --timeout=300s

    log_success "Staging deployment completed!"

    # Display service information
    log_info "Service endpoints in staging:"
    kubectl -n $K8S_NS get svc
}

# Deploy to Kubernetes production (with approval)
deploy_production() {
    log_warning "You are about to deploy to PRODUCTION environment!"
    read -p "Are you sure you want to continue? (yes/no): " confirm

    if [[ $confirm != "yes" ]]; then
        log_info "Production deployment cancelled."
        exit 0
    fi

    log_info "Deploying to Kubernetes production environment..."

    check_kubectl

    local K8S_NS="transport-prod"

    # Create namespace if it doesn't exist
    kubectl get ns $K8S_NS || kubectl create ns $K8S_NS

    # Apply manifests
    log_info "Applying Kubernetes manifests to $K8S_NS namespace..."
    kubectl apply -n $K8S_NS -f k8s/manifests/

    # Wait for deployments to be ready
    log_info "Waiting for deployments to be ready..."
    kubectl -n $K8S_NS rollout status deployment/user-service --timeout=600s
    kubectl -n $K8S_NS rollout status deployment/ticket-service --timeout=600s
    kubectl -n $K8S_NS rollout status deployment/subscription-service --timeout=600s
    kubectl -n $K8S_NS rollout status deployment/route-service --timeout=600s
    kubectl -n $K8S_NS rollout status deployment/bus-geolocation-service --timeout=600s
    kubectl -n $K8S_NS rollout status deployment/api-gateway --timeout=600s
    kubectl -n $K8S_NS rollout status deployment/frontend-app --timeout=600s

    log_success "Production deployment completed!"

    # Display service information
    log_info "Service endpoints in production:"
    kubectl -n $K8S_NS get svc
}

# Check service health for local deployment
check_service_health_local() {
    local services=(
        "http://localhost:8081/api/v1/actuator/health:User Service"
        "http://localhost:8082/actuator/health:API Gateway"
        "http://localhost:8083/actuator/health:Ticket Service"
        "http://localhost:8084/actuator/health:Subscription Service"
        "http://localhost:8085/actuator/health:Route Service"
        "http://localhost:8086/actuator/health:Bus Geolocation"
        "http://localhost:3000:Frontend"
    )

    log_info "Checking service health..."

    for service in "${services[@]}"; do
        url=${service%%:*}
        name=${service#*:}

        if [[ $name == "Frontend" ]]; then
            # For frontend, just check if it's responding
            if curl -s -o /dev/null -w "%{http_code}" $url | grep -q "200\|301\|302"; then
                log_success "$name is running"
            else
                log_warning "$name might not be ready yet"
            fi
        else
            # For backend services, check health endpoint
            if curl -s $url | grep -q "UP\|status.*:.*UP"; then
                log_success "$name is healthy"
            else
                log_warning "$name health check failed or service not ready"
            fi
        fi
    done
}

# Clean up function
cleanup() {
    log_info "Cleaning up..."
    case $ENVIRONMENT in
        local)
            docker-compose down
            log_success "Local environment cleaned up"
            ;;
        staging|production)
            local K8S_NS="transport-$ENVIRONMENT"
            if [[ $ENVIRONMENT == "production" ]]; then
                log_warning "You are about to delete PRODUCTION environment!"
                read -p "Are you sure? (yes/no): " confirm
                if [[ $confirm != "yes" ]]; then
                    log_info "Cleanup cancelled."
                    exit 0
                fi
            fi
            kubectl delete namespace $K8S_NS
            log_success "Kubernetes $ENVIRONMENT environment cleaned up"
            ;;
    esac
}

# Show usage
show_usage() {
    echo "Usage: $0 [COMMAND] [ENVIRONMENT]"
    echo ""
    echo "Commands:"
    echo "  deploy    Deploy the application (default)"
    echo "  cleanup   Clean up the deployment"
    echo "  status    Show deployment status"
    echo ""
    echo "Environments:"
    echo "  local      Deploy using Docker Compose (default)"
    echo "  staging    Deploy to Kubernetes staging environment"
    echo "  production Deploy to Kubernetes production environment"
    echo ""
    echo "Examples:"
    echo "  $0 deploy local          # Deploy locally"
    echo "  $0 deploy staging        # Deploy to staging"
    echo "  $0 deploy production     # Deploy to production"
    echo "  $0 cleanup local         # Clean up local deployment"
    echo "  $0 status staging        # Show staging deployment status"
}

# Show deployment status
show_status() {
    case $ENVIRONMENT in
        local)
            log_info "Local deployment status:"
            docker-compose ps
            ;;
        staging|production)
            local K8S_NS="transport-$ENVIRONMENT"
            log_info "Kubernetes $ENVIRONMENT deployment status:"
            kubectl -n $K8S_NS get pods
            kubectl -n $K8S_NS get svc
            ;;
    esac
}

# Main script logic
COMMAND=${1:-deploy}
ENVIRONMENT=${2:-local}

case $COMMAND in
    deploy)
        case $ENVIRONMENT in
            local)
                deploy_local
                ;;
            staging)
                deploy_staging
                ;;
            production)
                deploy_production
                ;;
            *)
                log_error "Invalid environment: $ENVIRONMENT"
                show_usage
                exit 1
                ;;
        esac
        ;;
    cleanup)
        cleanup
        ;;
    status)
        show_status
        ;;
    help|--help|-h)
        show_usage
        ;;
    *)
        log_error "Invalid command: $COMMAND"
        show_usage
        exit 1
        ;;
esac