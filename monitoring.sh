#!/bin/bash
# Transport Microservices - Monitoring and Logging Script
# Usage: ./monitoring.sh [setup|logs|metrics|health|cleanup]

set -e

COMMAND=${1:-health}
ENVIRONMENT=${2:-local}

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

# Setup monitoring stack (Prometheus + Grafana)
setup_monitoring() {
    log_info "Setting up monitoring stack..."

    # Create monitoring docker-compose file
    cat > docker-compose.monitoring.yml << 'EOF'
version: '3.8'

services:
  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--storage.tsdb.retention.time=200h'
      - '--web.enable-lifecycle'
    restart: unless-stopped
    networks:
      - microservices-network

  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    ports:
      - "3001:3000"
    volumes:
      - grafana_data:/var/lib/grafana
      - ./monitoring/grafana/provisioning:/etc/grafana/provisioning
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin
      - GF_USERS_ALLOW_SIGN_UP=false
    restart: unless-stopped
    networks:
      - microservices-network

  loki:
    image: grafana/loki:latest
    container_name: loki
    ports:
      - "3100:3100"
    command: -config.file=/etc/loki/local-config.yaml
    volumes:
      - loki_data:/loki
    restart: unless-stopped
    networks:
      - microservices-network

  promtail:
    image: grafana/promtail:latest
    container_name: promtail
    volumes:
      - /var/log:/var/log
      - /var/lib/docker/containers:/var/lib/docker/containers:ro
      - /var/run/docker.sock:/var/run/docker.sock
      - ./monitoring/promtail.yml:/etc/promtail/config.yml
    command: -config.file=/etc/promtail/config.yml
    restart: unless-stopped
    networks:
      - microservices-network

volumes:
  prometheus_data: {}
  grafana_data: {}
  loki_data: {}

networks:
  microservices-network:
    external: true
EOF

    # Create monitoring directory structure
    mkdir -p monitoring/grafana/provisioning/{dashboards,datasources}

    # Create Prometheus configuration
    cat > monitoring/prometheus.yml << 'EOF'
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'user-service'
    static_configs:
      - targets: ['user-service:8081']
    metrics_path: '/api/v1/actuator/prometheus'

  - job_name: 'api-gateway'
    static_configs:
      - targets: ['api-gateway:8082']
    metrics_path: '/actuator/prometheus'

  - job_name: 'ticket-service'
    static_configs:
      - targets: ['ticket-service:8083']
    metrics_path: '/actuator/prometheus'

  - job_name: 'subscription-service'
    static_configs:
      - targets: ['subscription-service:8084']
    metrics_path: '/actuator/prometheus'

  - job_name: 'route-service'
    static_configs:
      - targets: ['route-service:8085']
    metrics_path: '/actuator/prometheus'

  - job_name: 'bus-geolocation-service'
    static_configs:
      - targets: ['bus-geolocation-service:8086']
    metrics_path: '/actuator/prometheus'

  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
EOF

    # Create Promtail configuration
    cat > monitoring/promtail.yml << 'EOF'
server:
  http_listen_port: 9080
  grpc_listen_port: 0

positions:
  filename: /tmp/positions.yaml

clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: containers
    static_configs:
      - targets:
          - localhost
        labels:
          job: containerlogs
          __path__: /var/lib/docker/containers/*/*log

    pipeline_stages:
      - json:
          expressions:
            output: log
            stream: stream
            attrs:
      - json:
          expressions:
            tag:
          source: attrs
      - regex:
          expression: (?P<container_name>(?:[^|]*))\|(?P<image_name>(?:[^|]*))\|(?P<image_id>(?:[^|]*))
          source: tag
      - timestamp:
          format: RFC3339Nano
          source: time
      - labels:
          stream:
          container_name:
          image_name:
          image_id:
      - output:
          source: output
EOF

    # Create Grafana datasource configuration
    cat > monitoring/grafana/provisioning/datasources/datasources.yml << 'EOF'
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true

  - name: Loki
    type: loki
    access: proxy
    url: http://loki:3100
    editable: true
EOF

    # Create Grafana dashboard configuration
    cat > monitoring/grafana/provisioning/dashboards/dashboards.yml << 'EOF'
apiVersion: 1

providers:
  - name: 'Transport Microservices'
    orgId: 1
    folder: ''
    type: file
    disableDeletion: false
    editable: true
    options:
      path: /etc/grafana/provisioning/dashboards
EOF

    log_success "Monitoring configuration created"
    log_info "Starting monitoring stack..."

    # Start monitoring services
    docker-compose -f docker-compose.monitoring.yml up -d

    log_success "Monitoring stack started!"
    log_info "Access monitoring tools at:"
    log_info "  Prometheus: http://localhost:9090"
    log_info "  Grafana: http://localhost:3001 (admin/admin)"
    log_info "  Loki: http://localhost:3100"
}

# View logs from all services
view_logs() {
    local service=${1:-""}

    if [[ $ENVIRONMENT == "local" ]]; then
        if [[ -z $service ]]; then
            log_info "Viewing logs from all services..."
            docker-compose logs -f --tail=100
        else
            log_info "Viewing logs from $service..."
            docker-compose logs -f --tail=100 $service
        fi
    else
        local K8S_NS="transport-$ENVIRONMENT"
        if [[ -z $service ]]; then
            log_info "Viewing logs from all pods in $K8S_NS..."
            kubectl -n $K8S_NS logs --tail=100 -l app.kubernetes.io/part-of=transport-system --all-containers=true -f
        else
            log_info "Viewing logs from $service in $K8S_NS..."
            kubectl -n $K8S_NS logs --tail=100 -l app.kubernetes.io/name=$service -f
        fi
    fi
}

# Show metrics and health status
show_metrics() {
    if [[ $ENVIRONMENT == "local" ]]; then
        log_info "Service Health Status (Local):"
        echo "======================================"

        local services=(
            "user-service:8081:/api/v1/actuator/health"
            "api-gateway:8082:/actuator/health"
            "ticket-service:8083:/actuator/health"
            "subscription-service:8084:/actuator/health"
            "route-service:8085:/actuator/health"
            "bus-geolocation-service:8086:/actuator/health"
        )

        for service_info in "${services[@]}"; do
            local service_name=$(echo $service_info | cut -d':' -f1)
            local port=$(echo $service_info | cut -d':' -f2)
            local health_path=$(echo $service_info | cut -d':' -f3)
            local url="http://localhost:${port}${health_path}"

            printf "%-25s: " "$service_name"

            if response=$(curl -s "$url" 2>/dev/null); then
                if echo "$response" | grep -q '"status":"UP"'; then
                    echo -e "${GREEN}UP${NC}"
                elif echo "$response" | grep -q '"status":"DOWN"'; then
                    echo -e "${RED}DOWN${NC}"
                else
                    echo -e "${YELLOW}UNKNOWN${NC}"
                fi
            else
                echo -e "${RED}NO RESPONSE${NC}"
            fi
        done

        echo ""
        log_info "Docker Container Status:"
        echo "======================================"
        docker-compose ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"

    else
        local K8S_NS="transport-$ENVIRONMENT"
        log_info "Kubernetes Deployment Status ($ENVIRONMENT):"
        echo "======================================"
        kubectl -n $K8S_NS get pods -o wide

        echo ""
        log_info "Service Endpoints:"
        echo "======================================"
        kubectl -n $K8S_NS get svc
    fi
}

# Check detailed health for all services
check_health() {
    if [[ $ENVIRONMENT == "local" ]]; then
        log_info "Detailed Health Check (Local):"
        echo "======================================"

        local services=(
            "User Service:http://localhost:8081/api/v1/actuator/health"
            "API Gateway:http://localhost:8082/actuator/health"
            "Ticket Service:http://localhost:8083/actuator/health"
            "Subscription Service:http://localhost:8084/actuator/health"
            "Route Service:http://localhost:8085/actuator/health"
            "Bus Geolocation:http://localhost:8086/actuator/health"
            "Frontend:http://localhost:3000"
        )

        for service in "${services[@]}"; do
            local name=$(echo $service | cut -d':' -f1)
            local url=$(echo $service | cut -d':' -f2,3)

            echo ""
            echo "🔍 Checking $name..."
            echo "   URL: $url"

            if [[ $name == "Frontend" ]]; then
                # For frontend, check if page loads
                if http_code=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null); then
                    if [[ $http_code == "200" || $http_code == "301" || $http_code == "302" ]]; then
                        echo -e "   Status: ${GREEN}✓ Running (HTTP $http_code)${NC}"
                    else
                        echo -e "   Status: ${YELLOW}⚠ HTTP $http_code${NC}"
                    fi
                else
                    echo -e "   Status: ${RED}✗ Not accessible${NC}"
                fi
            else
                # For backend services, check health endpoint
                if response=$(curl -s "$url" 2>/dev/null); then
                    if echo "$response" | jq -e '.status == "UP"' >/dev/null 2>&1; then
                        echo -e "   Status: ${GREEN}✓ Healthy${NC}"

                        # Show additional info if available
                        if echo "$response" | jq -e '.components' >/dev/null 2>&1; then
                            echo "   Components:"
                            echo "$response" | jq -r '.components | to_entries[] | "     \(.key): \(.value.status)"' 2>/dev/null || echo "     Details not available"
                        fi
                    else
                        echo -e "   Status: ${RED}✗ Unhealthy${NC}"
                        echo "   Response: $response"
                    fi
                else
                    echo -e "   Status: ${RED}✗ No response${NC}"
                fi
            fi
        done

        # Check database connectivity
        echo ""
        echo "🗃️  Database Health:"
        if docker-compose exec -T postgres psql -U postgres -c "SELECT version();" >/dev/null 2>&1; then
            echo -e "   PostgreSQL: ${GREEN}✓ Connected${NC}"
        else
            echo -e "   PostgreSQL: ${RED}✗ Connection failed${NC}"
        fi

        if docker-compose exec -T redis redis-cli ping 2>/dev/null | grep -q "PONG"; then
            echo -e "   Redis: ${GREEN}✓ Connected${NC}"
        else
            echo -e "   Redis: ${RED}✗ Connection failed${NC}"
        fi

    else
        local K8S_NS="transport-$ENVIRONMENT"
        log_info "Kubernetes Health Check ($ENVIRONMENT):"
        echo "======================================"

        # Check pod status
        kubectl -n $K8S_NS get pods --no-headers | while read line; do
            local pod_name=$(echo $line | awk '{print $1}')
            local status=$(echo $line | awk '{print $3}')
            local ready=$(echo $line | awk '{print $2}')

            echo "Pod: $pod_name"
            if [[ $status == "Running" ]]; then
                echo -e "   Status: ${GREEN}✓ $status ($ready)${NC}"
            else
                echo -e "   Status: ${RED}✗ $status ($ready)${NC}"
            fi
        done

        # Check services
        echo ""
        echo "Services:"
        kubectl -n $K8S_NS get svc --no-headers | while read line; do
            local svc_name=$(echo $line | awk '{print $1}')
            local type=$(echo $line | awk '{print $2}')
            local cluster_ip=$(echo $line | awk '{print $3}')

            echo "   $svc_name ($type): $cluster_ip"
        done
    fi
}

# Cleanup monitoring stack
cleanup_monitoring() {
    log_info "Cleaning up monitoring stack..."

    if [[ -f docker-compose.monitoring.yml ]]; then
        docker-compose -f docker-compose.monitoring.yml down -v
        rm -f docker-compose.monitoring.yml
        rm -rf monitoring/
        log_success "Monitoring stack cleaned up"
    else
        log_warning "No monitoring stack found to clean up"
    fi
}

# Show usage
show_usage() {
    echo "Usage: $0 [COMMAND] [ENVIRONMENT]"
    echo ""
    echo "Commands:"
    echo "  setup     Set up monitoring stack (Prometheus + Grafana + Loki)"
    echo "  logs      View service logs [SERVICE_NAME]"
    echo "  metrics   Show basic metrics and status"
    echo "  health    Detailed health check for all services (default)"
    echo "  cleanup   Remove monitoring stack"
    echo ""
    echo "Environments:"
    echo "  local      Monitor local Docker Compose deployment (default)"
    echo "  staging    Monitor Kubernetes staging environment"
    echo "  production Monitor Kubernetes production environment"
    echo ""
    echo "Examples:"
    echo "  $0 health                    # Check health of local services"
    echo "  $0 logs user-service         # View logs from user-service"
    echo "  $0 setup                     # Set up monitoring stack"
    echo "  $0 metrics staging           # Show staging metrics"
}

# Main script logic
case $COMMAND in
    setup)
        setup_monitoring
        ;;
    logs)
        view_logs $3
        ;;
    metrics)
        show_metrics
        ;;
    health)
        check_health
        ;;
    cleanup)
        cleanup_monitoring
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