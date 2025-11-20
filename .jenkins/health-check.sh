#!/bin/bash

# Health Check Script for Urban Transport Microservices
# This script verifies that all services are running and healthy

set -e

# Configuration
TIMEOUT=300  # 5 minutes timeout
RETRY_INTERVAL=10  # 10 seconds between retries
LOG_FILE="/tmp/health-check.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Service endpoints
declare -A SERVICES=(
    ["api-gateway"]="http://localhost:8082/actuator/health"
    ["user-service"]="http://localhost:8081/actuator/health"
    ["ticket-service"]="http://localhost:8083/actuator/health"
    ["subscription-service"]="http://localhost:8084/actuator/health"
    ["frontend"]="http://localhost:3000"
    ["database"]="postgresql://localhost:5432/transport"
    ["redis"]="redis://localhost:6379"
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

# Check if a service is healthy
check_service_health() {
    local service_name="$1"
    local endpoint="$2"
    local max_attempts=$((TIMEOUT / RETRY_INTERVAL))
    local attempt=1

    print_info "Checking ${service_name} health..."

    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "${endpoint}" > /dev/null 2>&1; then
            print_success "${service_name} is healthy"
            return 0
        fi

        if [ $attempt -eq $max_attempts ]; then
            print_error "${service_name} health check failed after ${TIMEOUT} seconds"
            return 1
        fi

        print_info "Attempt ${attempt}/${max_attempts} failed, retrying in ${RETRY_INTERVAL} seconds..."
        sleep $RETRY_INTERVAL
        ((attempt++))
    done
}

# Check database connectivity
check_database() {
    local max_attempts=$((TIMEOUT / RETRY_INTERVAL))
    local attempt=1

    print_info "Checking database connectivity..."

    while [ $attempt -le $max_attempts ]; do
        if pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
            print_success "Database is accessible"
            return 0
        fi

        if [ $attempt -eq $max_attempts ]; then
            print_error "Database connectivity check failed after ${TIMEOUT} seconds"
            return 1
        fi

        print_info "Database attempt ${attempt}/${max_attempts} failed, retrying in ${RETRY_INTERVAL} seconds..."
        sleep $RETRY_INTERVAL
        ((attempt++))
    done
}

# Check Redis connectivity
check_redis() {
    local max_attempts=$((TIMEOUT / RETRY_INTERVAL))
    local attempt=1

    print_info "Checking Redis connectivity..."

    while [ $attempt -le $max_attempts ]; do
        if redis-cli -h localhost -p 6379 ping > /dev/null 2>&1; then
            print_success "Redis is accessible"
            return 0
        fi

        if [ $attempt -eq $max_attempts ]; then
            print_error "Redis connectivity check failed after ${TIMEOUT} seconds"
            return 1
        fi

        print_info "Redis attempt ${attempt}/${max_attempts} failed, retrying in ${RETRY_INTERVAL} seconds..."
        sleep $RETRY_INTERVAL
        ((attempt++))
    done
}

# Check frontend application
check_frontend() {
    local max_attempts=$((TIMEOUT / RETRY_INTERVAL))
    local attempt=1

    print_info "Checking frontend application..."

    while [ $attempt -le $max_attempts ]; do
        if curl -f -s "http://localhost:3000" > /dev/null 2>&1; then
            print_success "Frontend application is accessible"
            return 0
        fi

        if [ $attempt -eq $max_attempts ]; then
            print_error "Frontend application check failed after ${TIMEOUT} seconds"
            return 1
        fi

        print_info "Frontend attempt ${attempt}/${max_attempts} failed, retrying in ${RETRY_INTERVAL} seconds..."
        sleep $RETRY_INTERVAL
        ((attempt++))
    done
}

# Check Docker containers
check_docker_containers() {
    print_info "Checking Docker containers status..."

    local containers=(
        "postgres"
        "redis"
        "user-service"
        "ticket-service"
        "subscription-service"
        "api-gateway"
        "frontend-app"
    )

    local failed_containers=()

    for container in "${containers[@]}"; do
        if docker ps --format "table {{.Names}}" | grep -q "^${container}$"; then
            local status=$(docker inspect --format='{{.State.Health.Status}}' "${container}" 2>/dev/null || echo "unknown")
            if [ "$status" = "healthy" ] || [ "$status" = "unknown" ]; then
                print_success "Container ${container} is running"
            else
                print_error "Container ${container} is unhealthy (status: ${status})"
                failed_containers+=("$container")
            fi
        else
            print_error "Container ${container} is not running"
            failed_containers+=("$container")
        fi
    done

    if [ ${#failed_containers[@]} -eq 0 ]; then
        print_success "All Docker containers are healthy"
        return 0
    else
        print_error "Failed containers: ${failed_containers[*]}"
        return 1
    fi
}

# Check service dependencies
check_service_dependencies() {
    print_info "Checking service dependencies..."

    # Check if all required ports are listening
    local ports=(5432 6379 8081 8082 8083 8084 3000)
    local failed_ports=()

    for port in "${ports[@]}"; do
        if netstat -tuln | grep -q ":${port} "; then
            print_success "Port ${port} is listening"
        else
            print_error "Port ${port} is not listening"
            failed_ports+=("$port")
        fi
    done

    if [ ${#failed_ports[@]} -eq 0 ]; then
        print_success "All required ports are listening"
        return 0
    else
        print_error "Ports not listening: ${failed_ports[*]}"
        return 1
    fi
}

# Generate health report
generate_health_report() {
    local report_file="/tmp/health-report-$(date +%Y%m%d-%H%M%S).json"
    
    print_info "Generating health report..."

    cat > "${report_file}" << EOF
{
    "timestamp": "$(date -Iseconds)",
    "overall_status": "$1",
    "services": {
EOF

    local first=true
    for service in "${!SERVICES[@]}"; do
        if [ "$first" = true ]; then
            first=false
        else
            echo "," >> "${report_file}"
        fi
        
        local endpoint="${SERVICES[$service]}"
        local status="unknown"
        
        case $service in
            "database")
                if pg_isready -h localhost -p 5432 > /dev/null 2>&1; then
                    status="healthy"
                else
                    status="unhealthy"
                fi
                ;;
            "redis")
                if redis-cli -h localhost -p 6379 ping > /dev/null 2>&1; then
                    status="healthy"
                else
                    status="unhealthy"
                fi
                ;;
            *)
                if curl -f -s "${endpoint}" > /dev/null 2>&1; then
                    status="healthy"
                else
                    status="unhealthy"
                fi
                ;;
        esac

        echo "        \"$service\": {" >> "${report_file}"
        echo "            \"endpoint\": \"$endpoint\"," >> "${report_file}"
        echo "            \"status\": \"$status\"," >> "${report_file}"
        echo "            \"last_checked\": \"$(date -Iseconds)\"" >> "${report_file}"
        echo -n "        }" >> "${report_file}"
    done

    cat >> "${report_file}" << EOF

    },
    "environment": {
        "hostname": "$(hostname)",
        "uptime": "$(uptime -p)",
        "load_average": "$(uptime | awk -F'load average:' '{print $2}')",
        "disk_usage": "$(df -h / | awk 'NR==2 {print $5}')",
        "memory_usage": "$(free | awk 'NR==2{printf "%.2f%%", $3*100/$2}')"
    }
}
EOF

    print_success "Health report generated: ${report_file}"
    echo "${report_file}"
}

# Main health check function
main() {
    echo "========================================="
    echo "Urban Transport Microservices Health Check"
    echo "Started at: $(date)"
    echo "========================================="

    # Initialize log file
    > "${LOG_FILE}"

    local overall_status="healthy"
    local failed_checks=0

    # Check Docker containers
    if ! check_docker_containers; then
        ((failed_checks++))
        overall_status="unhealthy"
    fi

    # Check database
    if ! check_database; then
        ((failed_checks++))
        overall_status="unhealthy"
    fi

    # Check Redis
    if ! check_redis; then
        ((failed_checks++))
        overall_status="unhealthy"
    fi

    # Check service dependencies
    if ! check_service_dependencies; then
        ((failed_checks++))
        overall_status="unhealthy"
    fi

    # Check individual services
    for service in api-gateway user-service ticket-service subscription-service; do
        if ! check_service_health "$service" "${SERVICES[$service]}"; then
            ((failed_checks++))
            overall_status="unhealthy"
        fi
    done

    # Check frontend
    if ! check_frontend; then
        ((failed_checks++))
        overall_status="unhealthy"
    fi

    # Generate report
    local report_file
    report_file=$(generate_health_report "$overall_status")

    echo "========================================="
    if [ "$overall_status" = "healthy" ]; then
        print_success "All health checks passed!"
        echo "Overall Status: HEALTHY ✓"
    else
        print_error "Health check failed! ($failed_checks failed checks)"
        echo "Overall Status: UNHEALTHY ✗"
    fi
    echo "Detailed log: ${LOG_FILE}"
    echo "Health report: ${report_file}"
    echo "Completed at: $(date)"
    echo "========================================="

    # Return appropriate exit code
    if [ "$overall_status" = "healthy" ]; then
        exit 0
    else
        exit 1
    fi
}

# Script entry point
if [ "${BASH_SOURCE[0]}" == "${0}" ]; then
    main "$@"
fi