#!/bin/bash

# Deployment Script for Urban Transport Microservices
# This script handles deployment to different environments

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
LOG_FILE="/tmp/deploy-$(date +%Y%m%d-%H%M%S).log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Default values
ENVIRONMENT=""
VERSION=""
DRY_RUN=false
ROLLBACK=false
FORCE=false

# Service configuration
declare -A SERVICES=(
    ["user-service"]="8081"
    ["ticket-service"]="8083"
    ["subscription-service"]="8084"
    ["api-gateway"]="8082"
    ["frontend-app"]="3000"
)

# Environment configurations
declare -A ENV_CONFIGS=(
    ["development"]="docker-compose.dev.yml"
    ["staging"]="docker-compose.staging.yml"
    ["production"]="docker-compose.prod.yml"
)

# Logging function
log() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1" | tee -a "${LOG_FILE}"
}

# Color output functions
print_success() {
    echo -e "${GREEN}✓${NC} $1"
    log "SUCCESS: $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
    log "ERROR: $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
    log "WARNING: $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
    log "INFO: $1"
}

# Help function
show_help() {
    cat << EOF
Urban Transport Microservices Deployment Script

USAGE:
    $0 [OPTIONS] <ENVIRONMENT>

ENVIRONMENTS:
    development    Deploy to development environment
    staging        Deploy to staging environment  
    production     Deploy to production environment

OPTIONS:
    -v, --version <VERSION>     Specify version to deploy (default: latest)
    -d, --dry-run              Show what would be deployed without actually deploying
    -r, --rollback             Rollback to previous version
    -f, --force                Force deployment even if health checks fail
    -h, --help                 Show this help message

EXAMPLES:
    $0 development                          # Deploy latest to development
    $0 staging --version v1.2.3            # Deploy specific version to staging
    $0 production --dry-run                 # Show production deployment plan
    $0 staging --rollback                   # Rollback staging to previous version

ENVIRONMENT VARIABLES:
    DOCKER_REGISTRY            Docker registry URL (default: docker.io)
    DOCKER_USERNAME            Docker registry username
    DOCKER_PASSWORD            Docker registry password
    HEALTH_CHECK_TIMEOUT        Health check timeout in seconds (default: 300)
    BACKUP_BEFORE_DEPLOY        Create backup before deployment (default: true)

EOF
}

# Parse command line arguments
parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -v|--version)
                VERSION="$2"
                shift 2
                ;;
            -d|--dry-run)
                DRY_RUN=true
                shift
                ;;
            -r|--rollback)
                ROLLBACK=true
                shift
                ;;
            -f|--force)
                FORCE=true
                shift
                ;;
            -h|--help)
                show_help
                exit 0
                ;;
            development|staging|production)
                ENVIRONMENT="$1"
                shift
                ;;
            *)
                print_error "Unknown argument: $1"
                show_help
                exit 1
                ;;
        esac
    done

    # Validate required arguments
    if [[ -z "$ENVIRONMENT" ]]; then
        print_error "Environment is required"
        show_help
        exit 1
    fi

    # Set default version if not specified
    if [[ -z "$VERSION" && "$ROLLBACK" != true ]]; then
        VERSION="latest"
    fi
}

# Validate environment
validate_environment() {
    print_info "Validating environment: $ENVIRONMENT"

    # Check if environment configuration exists
    local config_file="${ENV_CONFIGS[$ENVIRONMENT]}"
    if [[ -z "$config_file" ]]; then
        print_error "Unknown environment: $ENVIRONMENT"
        exit 1
    fi

    # Check if docker-compose file exists
    if [[ ! -f "$PROJECT_DIR/$config_file" ]]; then
        print_warning "Docker-compose file not found: $config_file"
        print_info "Using default docker-compose.yml"
        ENV_CONFIGS[$ENVIRONMENT]="docker-compose.yml"
    fi

    print_success "Environment validation passed"
}

# Check prerequisites
check_prerequisites() {
    print_info "Checking prerequisites..."

    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed"
        exit 1
    fi

    # Check Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed"
        exit 1
    fi

    # Check if Docker daemon is running
    if ! docker info &> /dev/null; then
        print_error "Docker daemon is not running"
        exit 1
    fi

    print_success "Prerequisites check passed"
}

# Login to Docker registry
docker_login() {
    if [[ -n "$DOCKER_USERNAME" && -n "$DOCKER_PASSWORD" ]]; then
        print_info "Logging in to Docker registry..."
        echo "$DOCKER_PASSWORD" | docker login "${DOCKER_REGISTRY:-docker.io}" -u "$DOCKER_USERNAME" --password-stdin
        print_success "Docker registry login successful"
    else
        print_warning "Docker registry credentials not provided, assuming already logged in"
    fi
}

# Pull latest images
pull_images() {
    print_info "Pulling Docker images for version: $VERSION"

    for service in "${!SERVICES[@]}"; do
        local image_name="${DOCKER_REGISTRY:-docker.io}/${service}:${VERSION}"
        print_info "Pulling $image_name"
        
        if [[ "$DRY_RUN" == true ]]; then
            print_info "[DRY RUN] Would pull: $image_name"
        else
            if ! docker pull "$image_name"; then
                print_error "Failed to pull image: $image_name"
                exit 1
            fi
        fi
    done

    print_success "Image pull completed"
}

# Backup current deployment
backup_deployment() {
    if [[ "${BACKUP_BEFORE_DEPLOY:-true}" == "true" && "$ROLLBACK" != true ]]; then
        print_info "Creating deployment backup..."

        local backup_dir="/tmp/deployment-backup-$(date +%Y%m%d-%H%M%S)"
        local compose_file="${ENV_CONFIGS[$ENVIRONMENT]}"

        if [[ "$DRY_RUN" == true ]]; then
            print_info "[DRY RUN] Would create backup in: $backup_dir"
        else
            mkdir -p "$backup_dir"
            
            # Backup docker-compose file
            cp "$PROJECT_DIR/$compose_file" "$backup_dir/"
            
            # Export current container states
            docker-compose -f "$PROJECT_DIR/$compose_file" config > "$backup_dir/current-config.yml"
            
            # Save current image versions
            docker images --format "table {{.Repository}}:{{.Tag}}\t{{.ID}}" | grep -E "(user-service|ticket-service|subscription-service|api-gateway|frontend-app)" > "$backup_dir/current-images.txt"
            
            print_success "Backup created: $backup_dir"
            echo "$backup_dir" > "/tmp/last-backup-location"
        fi
    fi
}

# Update docker-compose with new version
update_compose_file() {
    print_info "Updating docker-compose file with version: $VERSION"

    local compose_file="${ENV_CONFIGS[$ENVIRONMENT]}"
    local temp_file="/tmp/docker-compose-temp.yml"

    if [[ "$DRY_RUN" == true ]]; then
        print_info "[DRY RUN] Would update image versions in: $compose_file"
        return
    fi

    # Create temporary file with updated versions
    sed "s/:latest/:$VERSION/g" "$PROJECT_DIR/$compose_file" > "$temp_file"
    
    # Validate the updated compose file
    if docker-compose -f "$temp_file" config &> /dev/null; then
        mv "$temp_file" "$PROJECT_DIR/$compose_file"
        print_success "Docker-compose file updated"
    else
        print_error "Updated docker-compose file is invalid"
        rm -f "$temp_file"
        exit 1
    fi
}

# Deploy services
deploy_services() {
    print_info "Deploying services to $ENVIRONMENT environment..."

    local compose_file="${ENV_CONFIGS[$ENVIRONMENT]}"
    
    cd "$PROJECT_DIR"

    if [[ "$DRY_RUN" == true ]]; then
        print_info "[DRY RUN] Would execute: docker-compose -f $compose_file up -d"
        docker-compose -f "$compose_file" config
        return
    fi

    # Stop existing services
    print_info "Stopping existing services..."
    docker-compose -f "$compose_file" down

    # Start new services
    print_info "Starting services with new version..."
    if ! docker-compose -f "$compose_file" up -d; then
        print_error "Deployment failed"
        
        if [[ "$FORCE" != true ]]; then
            print_info "Attempting automatic rollback..."
            rollback_deployment
            exit 1
        fi
    fi

    print_success "Services deployed successfully"
}

# Health check after deployment
post_deployment_health_check() {
    print_info "Running post-deployment health checks..."

    if [[ "$DRY_RUN" == true ]]; then
        print_info "[DRY RUN] Would run health checks"
        return
    fi

    # Wait for services to start
    sleep 30

    # Run health check script
    if [[ -f "$SCRIPT_DIR/health-check.sh" ]]; then
        if bash "$SCRIPT_DIR/health-check.sh"; then
            print_success "Health checks passed"
        else
            print_error "Health checks failed"
            
            if [[ "$FORCE" != true ]]; then
                print_info "Attempting automatic rollback due to health check failure..."
                rollback_deployment
                exit 1
            else
                print_warning "Continuing despite health check failures (--force specified)"
            fi
        fi
    else
        print_warning "Health check script not found, skipping health checks"
    fi
}

# Rollback deployment
rollback_deployment() {
    print_info "Rolling back deployment..."

    local backup_location
    if [[ -f "/tmp/last-backup-location" ]]; then
        backup_location=$(cat "/tmp/last-backup-location")
        
        if [[ -d "$backup_location" ]]; then
            print_info "Restoring from backup: $backup_location"
            
            # Restore docker-compose file
            local compose_file="${ENV_CONFIGS[$ENVIRONMENT]}"
            cp "$backup_location/$compose_file" "$PROJECT_DIR/"
            
            # Redeploy with backup configuration
            cd "$PROJECT_DIR"
            docker-compose -f "$compose_file" down
            docker-compose -f "$compose_file" up -d
            
            print_success "Rollback completed"
        else
            print_error "Backup location not found: $backup_location"
            exit 1
        fi
    else
        print_error "No backup location found for rollback"
        exit 1
    fi
}

# Cleanup old images
cleanup_old_images() {
    print_info "Cleaning up old Docker images..."

    if [[ "$DRY_RUN" == true ]]; then
        print_info "[DRY RUN] Would cleanup old images"
        return
    fi

    # Remove dangling images
    docker image prune -f

    # Remove old service images (keep last 3 versions)
    for service in "${!SERVICES[@]}"; do
        local images
        images=$(docker images "${service}" --format "{{.Tag}}" | grep -v latest | sort -V | head -n -3)
        
        for tag in $images; do
            if [[ -n "$tag" ]]; then
                print_info "Removing old image: ${service}:${tag}"
                docker rmi "${service}:${tag}" || true
            fi
        done
    done

    print_success "Cleanup completed"
}

# Generate deployment report
generate_deployment_report() {
    local report_file="/tmp/deployment-report-$(date +%Y%m%d-%H%M%S).json"
    
    print_info "Generating deployment report..."

    cat > "$report_file" << EOF
{
    "timestamp": "$(date -Iseconds)",
    "environment": "$ENVIRONMENT",
    "version": "$VERSION",
    "status": "success",
    "services": {
EOF

    local first=true
    for service in "${!SERVICES[@]}"; do
        if [[ "$first" == true ]]; then
            first=false
        else
            echo "," >> "$report_file"
        fi
        
        local port="${SERVICES[$service]}"
        local image_id=""
        
        if [[ "$DRY_RUN" != true ]]; then
            image_id=$(docker inspect "${service}:${VERSION}" --format='{{.Id}}' 2>/dev/null || echo "unknown")
        fi

        cat >> "$report_file" << EOF
        "$service": {
            "port": "$port",
            "version": "$VERSION",
            "image_id": "$image_id",
            "status": "deployed"
        }
EOF
    done

    cat >> "$report_file" << EOF
    },
    "deployment_details": {
        "deployed_by": "${USER:-unknown}",
        "deployment_time": "$(date -Iseconds)",
        "environment": "$ENVIRONMENT",
        "dry_run": $DRY_RUN,
        "rollback": $ROLLBACK,
        "force": $FORCE
    }
}
EOF

    print_success "Deployment report generated: $report_file"
}

# Main deployment function
main() {
    echo "========================================="
    echo "Urban Transport Microservices Deployment"
    echo "Started at: $(date)"
    echo "========================================="

    # Initialize log file
    > "$LOG_FILE"

    # Parse arguments
    parse_args "$@"

    print_info "Deployment Configuration:"
    print_info "  Environment: $ENVIRONMENT"
    print_info "  Version: ${VERSION:-previous}"
    print_info "  Dry Run: $DRY_RUN"
    print_info "  Rollback: $ROLLBACK"
    print_info "  Force: $FORCE"
    
    # Validate environment
    validate_environment

    # Check prerequisites
    check_prerequisites

    # Handle rollback
    if [[ "$ROLLBACK" == true ]]; then
        rollback_deployment
        exit 0
    fi

    # Login to Docker registry
    docker_login

    # Pull latest images
    pull_images

    # Backup current deployment
    backup_deployment

    # Update compose file with new version
    update_compose_file

    # Deploy services
    deploy_services

    # Post-deployment health check
    post_deployment_health_check

    # Cleanup old images
    cleanup_old_images

    # Generate deployment report
    generate_deployment_report

    echo "========================================="
    print_success "Deployment completed successfully!"
    print_info "Environment: $ENVIRONMENT"
    print_info "Version: $VERSION"
    print_info "Log file: $LOG_FILE"
    echo "Completed at: $(date)"
    echo "========================================="
}

# Script entry point
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi