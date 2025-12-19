# Transport Microservices - Automated Deployment Script (PowerShell)
# Usage: .\deploy.ps1 [local|staging|production]

param(
    [string]$Command = "deploy",
    [string]$Environment = "local"
)

$ErrorActionPreference = "Stop"

# Colors for output
function Write-Info { param($Message) Write-Host "[INFO] $Message" -ForegroundColor Blue }
function Write-Success { param($Message) Write-Host "[SUCCESS] $Message" -ForegroundColor Green }
function Write-Warning { param($Message) Write-Host "[WARNING] $Message" -ForegroundColor Yellow }
function Write-Error { param($Message) Write-Host "[ERROR] $Message" -ForegroundColor Red }

# Check if Docker is running
function Test-Docker {
    try {
        docker info | Out-Null
        Write-Success "Docker is running"
        return $true
    }
    catch {
        Write-Error "Docker is not running. Please start Docker and try again."
        exit 1
    }
}

# Check if kubectl is available for Kubernetes deployments
function Test-Kubectl {
    if (!(Get-Command kubectl -ErrorAction SilentlyContinue)) {
        Write-Error "kubectl is not installed. Please install kubectl for Kubernetes deployments."
        exit 1
    }
    Write-Success "kubectl is available"
}

# Build all services
function Build-Services {
    Write-Info "Building all microservices..."

    # Build backend services
    Write-Info "Building backend services with Maven..."
    mvn clean package -DskipTests=true

    # Build frontend
    Write-Info "Building frontend..."
    Push-Location Frontend
    try {
        npm ci
        $env:CI = "false"
        npm run build
    }
    finally {
        Pop-Location
    }

    Write-Success "All services built successfully"
}

# Deploy locally using Docker Compose
function Deploy-Local {
    Write-Info "Deploying to local environment using Docker Compose..."

    Test-Docker
    Build-Services

    Write-Info "Starting all services with Docker Compose..."
    docker-compose down --remove-orphans
    docker-compose up -d --build

    Write-Info "Waiting for services to be healthy..."
    Start-Sleep -Seconds 30

    # Check service health
    Test-ServiceHealthLocal

    Write-Success "Local deployment completed!"
    Write-Info "Access the application at:"
    Write-Info "  Frontend: http://localhost:3000"
    Write-Info "  API Gateway: http://localhost:8082"
    Write-Info "  User Service: http://localhost:8081"
    Write-Info "  Ticket Service: http://localhost:8083"
    Write-Info "  Subscription Service: http://localhost:8084"
    Write-Info "  Route Service: http://localhost:8085"
    Write-Info "  Bus Geolocation: http://localhost:8086"
    Write-Info "  pgAdmin: http://localhost:5051 (admin@admin.com / admin)"
}

# Deploy to Kubernetes staging
function Deploy-Staging {
    Write-Info "Deploying to Kubernetes staging environment..."

    Test-Kubectl

    $K8S_NS = "transport-staging"

    # Create namespace if it doesn't exist
    try {
        kubectl get ns $K8S_NS | Out-Null
    }
    catch {
        kubectl create ns $K8S_NS
    }

    # Apply manifests
    Write-Info "Applying Kubernetes manifests to $K8S_NS namespace..."
    kubectl apply -n $K8S_NS -f k8s/manifests/

    # Wait for deployments to be ready
    Write-Info "Waiting for deployments to be ready..."
    kubectl -n $K8S_NS rollout status deployment/user-service --timeout=300s
    kubectl -n $K8S_NS rollout status deployment/ticket-service --timeout=300s
    kubectl -n $K8S_NS rollout status deployment/subscription-service --timeout=300s
    kubectl -n $K8S_NS rollout status deployment/route-service --timeout=300s
    kubectl -n $K8S_NS rollout status deployment/bus-geolocation-service --timeout=300s
    kubectl -n $K8S_NS rollout status deployment/api-gateway --timeout=300s
    kubectl -n $K8S_NS rollout status deployment/frontend-app --timeout=300s

    Write-Success "Staging deployment completed!"

    # Display service information
    Write-Info "Service endpoints in staging:"
    kubectl -n $K8S_NS get svc
}

# Deploy to Kubernetes production (with approval)
function Deploy-Production {
    Write-Warning "You are about to deploy to PRODUCTION environment!"
    $confirm = Read-Host "Are you sure you want to continue? (yes/no)"

    if ($confirm -ne "yes") {
        Write-Info "Production deployment cancelled."
        exit 0
    }

    Write-Info "Deploying to Kubernetes production environment..."

    Test-Kubectl

    $K8S_NS = "transport-prod"

    # Create namespace if it doesn't exist
    try {
        kubectl get ns $K8S_NS | Out-Null
    }
    catch {
        kubectl create ns $K8S_NS
    }

    # Apply manifests
    Write-Info "Applying Kubernetes manifests to $K8S_NS namespace..."
    kubectl apply -n $K8S_NS -f k8s/manifests/

    # Wait for deployments to be ready
    Write-Info "Waiting for deployments to be ready..."
    kubectl -n $K8S_NS rollout status deployment/user-service --timeout=600s
    kubectl -n $K8S_NS rollout status deployment/ticket-service --timeout=600s
    kubectl -n $K8S_NS rollout status deployment/subscription-service --timeout=600s
    kubectl -n $K8S_NS rollout status deployment/route-service --timeout=600s
    kubectl -n $K8S_NS rollout status deployment/bus-geolocation-service --timeout=600s
    kubectl -n $K8S_NS rollout status deployment/api-gateway --timeout=600s
    kubectl -n $K8S_NS rollout status deployment/frontend-app --timeout=600s

    Write-Success "Production deployment completed!"

    # Display service information
    Write-Info "Service endpoints in production:"
    kubectl -n $K8S_NS get svc
}

# Check service health for local deployment
function Test-ServiceHealthLocal {
    $services = @(
        @{ Url = "http://localhost:8081/api/v1/actuator/health"; Name = "User Service" }
        @{ Url = "http://localhost:8082/actuator/health"; Name = "API Gateway" }
        @{ Url = "http://localhost:8083/actuator/health"; Name = "Ticket Service" }
        @{ Url = "http://localhost:8084/actuator/health"; Name = "Subscription Service" }
        @{ Url = "http://localhost:8085/actuator/health"; Name = "Route Service" }
        @{ Url = "http://localhost:8086/actuator/health"; Name = "Bus Geolocation" }
        @{ Url = "http://localhost:3000"; Name = "Frontend" }
    )

    Write-Info "Checking service health..."

    foreach ($service in $services) {
        try {
            if ($service.Name -eq "Frontend") {
                # For frontend, just check if it's responding
                $response = Invoke-WebRequest -Uri $service.Url -UseBasicParsing -TimeoutSec 5
                if ($response.StatusCode -in @(200, 301, 302)) {
                    Write-Success "$($service.Name) is running"
                }
                else {
                    Write-Warning "$($service.Name) might not be ready yet"
                }
            }
            else {
                # For backend services, check health endpoint
                $response = Invoke-WebRequest -Uri $service.Url -UseBasicParsing -TimeoutSec 5
                if ($response.Content -match "UP" -or $response.Content -match "status.*:.*UP") {
                    Write-Success "$($service.Name) is healthy"
                }
                else {
                    Write-Warning "$($service.Name) health check failed or service not ready"
                }
            }
        }
        catch {
            Write-Warning "$($service.Name) health check failed: $_"
        }
    }
}

# Clean up function
function Invoke-Cleanup {
    Write-Info "Cleaning up..."
    switch ($Environment) {
        "local" {
            docker-compose down
            Write-Success "Local environment cleaned up"
        }
        { $_ -in @("staging", "production") } {
            $K8S_NS = "transport-$Environment"
            if ($Environment -eq "production") {
                Write-Warning "You are about to delete PRODUCTION environment!"
                $confirm = Read-Host "Are you sure? (yes/no)"
                if ($confirm -ne "yes") {
                    Write-Info "Cleanup cancelled."
                    exit 0
                }
            }
            kubectl delete namespace $K8S_NS
            Write-Success "Kubernetes $Environment environment cleaned up"
        }
    }
}

# Show usage
function Show-Usage {
    Write-Host "Usage: .\deploy.ps1 [COMMAND] [ENVIRONMENT]"
    Write-Host ""
    Write-Host "Commands:"
    Write-Host "  deploy    Deploy the application (default)"
    Write-Host "  cleanup   Clean up the deployment"
    Write-Host "  status    Show deployment status"
    Write-Host ""
    Write-Host "Environments:"
    Write-Host "  local      Deploy using Docker Compose (default)"
    Write-Host "  staging    Deploy to Kubernetes staging environment"
    Write-Host "  production Deploy to Kubernetes production environment"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\deploy.ps1 deploy local          # Deploy locally"
    Write-Host "  .\deploy.ps1 deploy staging        # Deploy to staging"
    Write-Host "  .\deploy.ps1 deploy production     # Deploy to production"
    Write-Host "  .\deploy.ps1 cleanup local         # Clean up local deployment"
    Write-Host "  .\deploy.ps1 status staging        # Show staging deployment status"
}

# Show deployment status
function Show-Status {
    switch ($Environment) {
        "local" {
            Write-Info "Local deployment status:"
            docker-compose ps
        }
        { $_ -in @("staging", "production") } {
            $K8S_NS = "transport-$Environment"
            Write-Info "Kubernetes $Environment deployment status:"
            kubectl -n $K8S_NS get pods
            kubectl -n $K8S_NS get svc
        }
    }
}

# Main script logic
switch ($Command) {
    "deploy" {
        switch ($Environment) {
            "local" { Deploy-Local }
            "staging" { Deploy-Staging }
            "production" { Deploy-Production }
            default {
                Write-Error "Invalid environment: $Environment"
                Show-Usage
                exit 1
            }
        }
    }
    "cleanup" { Invoke-Cleanup }
    "status" { Show-Status }
    { $_ -in @("help", "--help", "-h") } { Show-Usage }
    default {
        Write-Error "Invalid command: $Command"
        Show-Usage
        exit 1
    }
}