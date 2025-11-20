# Quick Start Guide

## Step 1: Start Docker Daemon

Open a terminal and run:
```bash
sudo systemctl start docker
```

To enable Docker to start automatically on boot:
```bash
sudo systemctl enable --now docker
```

## Step 2: Start All Services

Once Docker is running, execute:
```bash
cd /mnt/ssd128/S5/projet\ soa/mymicroservices
./start-services.sh
```

Or manually:
```bash
docker compose up -d --build
```

## Step 3: Verify Services

Check service status:
```bash
docker compose ps
```

View logs:
```bash
docker compose logs -f
```

## Step 4: Test the Services

### Health Checks
```bash
# User Service
curl http://localhost:8081/api/v1/actuator/health

# API Gateway
curl http://localhost:8082/actuator/health

# Ticket Service
curl http://localhost:8083/actuator/health

# Subscription Service
curl http://localhost:8084/actuator/health
```

### Access Web Interfaces
- **Frontend**: http://localhost:3000
- **User Service Swagger**: http://localhost:8081/api/v1/swagger-ui.html
- **Ticket Service Swagger**: http://localhost:8083/swagger-ui.html
- **pgAdmin**: http://localhost:5051 (admin@admin.com / admin)

## Step 5: Test User Registration

```bash
# Register a new user
curl -X POST http://localhost:8082/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "test@example.com",
    "password": "TestPass123",
    "phoneNumber": "+1234567890",
    "role": "PASSENGER"
  }'
```

## Troubleshooting

### Docker not starting
```bash
# Check Docker status
sudo systemctl status docker

# Restart Docker
sudo systemctl restart docker
```

### Port already in use
```bash
# Find process using port
sudo lsof -i :8081
sudo lsof -i :8082
sudo lsof -i :8083

# Kill process if needed
sudo kill -9 <PID>
```

### View service logs
```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f user-service
docker compose logs -f api-gateway
docker compose logs -f ticket-service
```

### Stop all services
```bash
docker compose down
```

### Reset everything (removes volumes)
```bash
docker compose down -v
```

