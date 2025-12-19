# Jenkins Docker Setup - Transport Microservices (PowerShell)
# Quick start script for Jenkins with complete pipeline visualization

param(
    [string]$Command = "start"
)

$ErrorActionPreference = "Stop"

# Configuration
$JenkinsPort = 8080
$JenkinsUrl = "http://localhost:$JenkinsPort"
$ProjectName = "transport-microservices"

# Colors for output
function Write-Info { param($Message) Write-Host "ℹ️ [INFO] $Message" -ForegroundColor Blue }
function Write-Success { param($Message) Write-Host "✅ [SUCCESS] $Message" -ForegroundColor Green }
function Write-Warning { param($Message) Write-Host "⚠️ [WARNING] $Message" -ForegroundColor Yellow }
function Write-Error { param($Message) Write-Host "❌ [ERROR] $Message" -ForegroundColor Red }
function Write-Step { param($Message) Write-Host "🔄 [STEP] $Message" -ForegroundColor Magenta }

# Banner
function Show-Banner {
    Write-Host @"

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

"@ -ForegroundColor Cyan
}

# Compose command detection
$ComposeCmd = if (Get-Command docker-compose -ErrorAction SilentlyContinue) { 'docker-compose' } else { 'docker compose' }

# Check prerequisites
function Test-Prerequisites {
    Write-Step "Checking prerequisites..."

    # Check Docker
    if (!(Get-Command docker -ErrorAction SilentlyContinue)) {
        Write-Error "Docker is not installed. Please install Docker Desktop first."
        exit 1
    }

    # Check Docker Compose
    if (!(Get-Command docker-compose -ErrorAction SilentlyContinue)) {
        if (-not (Get-Command docker compose -ErrorAction SilentlyContinue)) {
            Write-Error "Docker Compose is not installed. Please install Docker Desktop first."
            exit 1
        }
    }

    # Check if Docker is running
    try {
        docker info | Out-Null
        Write-Success "All prerequisites met"
    }
    catch {
        Write-Error "Docker is not running. Please start Docker Desktop first."
        exit 1
    }
}

# Create microservices network if not exists
function New-DockerNetwork {
    Write-Step "Setting up Docker network..."

    $networkExists = docker network ls --format "{{.Name}}" | Where-Object { $_ -eq "microservices-network" }

    if (-not $networkExists) {
        docker network create microservices-network
        Write-Success "Created microservices-network"
    }
    else {
        Write-Info "Network microservices-network already exists"
    }
}

# Stop existing Jenkins if running
function Stop-ExistingJenkins {
    Write-Step "Checking for existing Jenkins containers..."

    $existingContainer = docker ps --format "{{.Names}}" | Where-Object { $_ -eq "jenkins-microservices" }

    if ($existingContainer) {
        Write-Warning "Stopping existing Jenkins container..."
        docker-compose -f docker-compose.jenkins.yml down
        Write-Success "Stopped existing Jenkins"
    }
}

# Build and start Jenkins
function Start-Jenkins {
    Write-Step "Building and starting Jenkins..."

    # Build the custom Jenkins image
    Write-Info "Building custom Jenkins image with plugins..."
    & $ComposeCmd -f docker-compose.jenkins.yml build --no-cache

    # Start Jenkins services
    Write-Info "Starting Jenkins services..."
    & $ComposeCmd -f docker-compose.jenkins.yml up -d

    Write-Success "Jenkins containers started"
}

# Wait for Jenkins to be ready
function Wait-ForJenkins {
    Write-Step "Waiting for Jenkins to be ready..."

    $maxAttempts = 60
    $attempt = 1

    while ($attempt -le $maxAttempts) {
        try {
            $response = Invoke-WebRequest -Uri "$JenkinsUrl/login" -UseBasicParsing -TimeoutSec 5
            if ($response.StatusCode -eq 200) {
                Write-Success "Jenkins is ready!"
                return
            }
        }
        catch {
            # Continue waiting
        }

        if ($attempt -eq $maxAttempts) {
            Write-Error "Jenkins failed to start within expected time"
            Write-Info "Check logs with: $ComposeCmd -f docker-compose.jenkins.yml logs jenkins"
            exit 1
        }

        Write-Host "`r🔄 Waiting for Jenkins... ($attempt/$maxAttempts)" -NoNewline
        Start-Sleep -Seconds 5
        $attempt++
    }
    Write-Host ""
}

# Create initial pipeline job
function New-PipelineJob {
    Write-Step "Setting up pipeline job..."

    # Wait a bit more for Jenkins to be fully initialized
    Start-Sleep -Seconds 10

    $jobConfig = "jenkins-job-config.xml"
    if (Test-Path $jobConfig) {
        Write-Info "Creating pipeline job from configuration..."

        try {
            # Create authentication header
            $auth = [System.Convert]::ToBase64String([System.Text.Encoding]::UTF8.GetBytes("admin:admin123"))
            $headers = @{
                "Authorization" = "Basic $auth"
                "Content-Type" = "application/xml"
            }

            # Read job configuration
            $jobXml = Get-Content $jobConfig -Raw

            # Create the job
            Invoke-WebRequest -Uri "$JenkinsUrl/createItem?name=$ProjectName-pipeline" -Method Post -Body $jobXml -Headers $headers -UseBasicParsing | Out-Null
            Write-Success "Pipeline job configuration applied"
        }
        catch {
            Write-Warning "Job creation via API failed (might already exist): $($_.Exception.Message)"
        }
    }
    else {
        Write-Warning "Job configuration file not found. Create manually in Jenkins UI."
    }
}

# Show connection info
function Show-ConnectionInfo {
    Write-Host ""
    Write-Success "🎉 Jenkins setup completed successfully!"
    Write-Host ""
    Write-Host "📊 ACCESS INFORMATION:" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Green
    Write-Host ""
    Write-Host "🌐 Jenkins Dashboard:     $JenkinsUrl" -ForegroundColor Yellow
    Write-Host "🎨 Blue Ocean (Modern UI): $JenkinsUrl/blue" -ForegroundColor Yellow
    Write-Host "📋 Pipeline Job:          $JenkinsUrl/job/$ProjectName-pipeline/" -ForegroundColor Yellow
    Write-Host "📊 Build Monitor:         $JenkinsUrl/view/Pipeline%20Monitor/" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "🔐 Login Credentials:" -ForegroundColor Yellow
    Write-Host "   Username: admin" -ForegroundColor Green
    Write-Host "   Password: admin123" -ForegroundColor Green
    Write-Host ""
    Write-Host "🚀 QUICK ACTIONS:" -ForegroundColor Cyan
    Write-Host "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" -ForegroundColor Green
    Write-Host ""
    Write-Host "1. Access Blue Ocean:      Open $JenkinsUrl/blue" -ForegroundColor Blue
    Write-Host "2. Run Pipeline:           Go to job and click 'Build Now'" -ForegroundColor Blue
    Write-Host "3. View Logs:              .\start-jenkins.ps1 logs" -ForegroundColor Blue
    Write-Host "4. Stop Jenkins:           .\start-jenkins.ps1 stop" -ForegroundColor Blue
    Write-Host "5. Restart Jenkins:        .\start-jenkins.ps1 restart" -ForegroundColor Blue
    Write-Host ""
    Write-Host "💡 TIP: For best visualization experience, use Blue Ocean interface!" -ForegroundColor Yellow
    Write-Host ""
}

# Show usage
function Show-Usage {
    Write-Host "Usage: .\start-jenkins.ps1 [command]"
    Write-Host ""
    Write-Host "Commands:"
    Write-Host "  start     Start Jenkins (default)"
    Write-Host "  stop      Stop Jenkins"
    Write-Host "  restart   Restart Jenkins"
    Write-Host "  logs      Show Jenkins logs"
    Write-Host "  status    Show Jenkins status"
    Write-Host "  clean     Stop and remove all data"
    Write-Host ""
    Write-Host "Examples:"
    Write-Host "  .\start-jenkins.ps1              # Start Jenkins"
    Write-Host "  .\start-jenkins.ps1 start        # Start Jenkins"
    Write-Host "  .\start-jenkins.ps1 stop         # Stop Jenkins"
    Write-Host "  .\start-jenkins.ps1 restart      # Restart Jenkins"
    Write-Host "  .\start-jenkins.ps1 logs         # Follow Jenkins logs"
}

# Stop Jenkins
function Stop-Jenkins {
    Write-Step "Stopping Jenkins..."
    docker-compose -f docker-compose.jenkins.yml down
    Write-Success "Jenkins stopped"
}

# Show Jenkins logs
function Show-Logs {
    Write-Info "Following Jenkins logs (Ctrl+C to exit)..."
    & $ComposeCmd -f docker-compose.jenkins.yml logs -f jenkins
}

# Show Jenkins status
function Show-Status {
    Write-Info "Jenkins container status:"
    & $ComposeCmd -f docker-compose.jenkins.yml ps

    Write-Host ""
    Write-Info "Jenkins accessibility:"
    try {
        $response = Invoke-WebRequest -Uri "$JenkinsUrl/login" -UseBasicParsing -TimeoutSec 5
        Write-Success "Jenkins is accessible at $JenkinsUrl"
    }
    catch {
        Write-Warning "Jenkins is not accessible"
    }
}

# Clean Jenkins (remove all data)
function Remove-JenkinsData {
    Write-Warning "This will remove all Jenkins data including jobs, configurations, and build history!"
    $confirm = Read-Host "Are you sure? (yes/no)"

    if ($confirm -eq "yes") {
        Write-Step "Cleaning Jenkins..."
        & $ComposeCmd -f docker-compose.jenkins.yml down -v

        # Remove volumes
        $volumes = @("jenkins_microservices_data", "jenkins_maven_cache", "jenkins_npm_cache", "jenkins_docker_cache", "jenkins_agent_workdir")
        foreach ($volume in $volumes) {
            try {
                docker volume rm $volume 2>$null
            }
            catch {
                # Volume might not exist, continue
            }
        }

        Write-Success "Jenkins cleaned"
    }
    else {
        Write-Info "Clean operation cancelled"
    }
}

# Restart Jenkins
function Restart-Jenkins {
    Write-Step "Restarting Jenkins..."
    Stop-Jenkins
    Start-Sleep -Seconds 5
    Start-JenkinsFull
}

# Full start process
function Start-JenkinsFull {
    Show-Banner
    Test-Prerequisites
    New-DockerNetwork
    Stop-ExistingJenkins
    Start-Jenkins
    Wait-ForJenkins
    New-PipelineJob
    Show-ConnectionInfo
}

# Main script logic
switch ($Command.ToLower()) {
    "start" {
        Start-JenkinsFull
    }
    "stop" {
        Stop-Jenkins
    }
    "restart" {
        Restart-Jenkins
    }
    "logs" {
        Show-Logs
    }
    "status" {
        Show-Status
    }
    "clean" {
        Remove-JenkinsData
    }
    { $_ -in @("help", "--help", "-h") } {
        Show-Usage
    }
    default {
        Write-Error "Invalid command: $Command"
        Show-Usage
        exit 1
    }
}