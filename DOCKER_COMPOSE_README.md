# Docker Compose - Microservices Architecture

This project uses a single Docker Compose file to orchestrate all microservices and infrastructure.

## Structure

```
├── docker-compose.yml                    # Main orchestrator - all services
├── User/Dockerfile                       # User Service Dockerfile
├── api-gateway/Dockerfile               # API Gateway Dockerfile
└── Ticket/Dockerfile                    # Ticket Service Dockerfile
```

## Quick Start

### Start All Services
```bash
# Build and start all services
docker-compose up -d --build

# View logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f user-service
docker-compose logs -f api-gateway
docker-compose logs -f ticket-service

# Check service status
docker-compose ps

# Stop all services
docker-compose down

# Stop and remove volumes (reset databases)
docker-compose down -v
```

## Service Ports

| Service | Port | URL |
|---------|------|-----|
| **Microservices** |
| User Service | 8081 | http://localhost:8081 |
| User Service API Docs | 8081 | http://localhost:8081/swagger-ui.html |
| API Gateway | 8082 | http://localhost:8082 |
| API Gateway Health | 8082 | http://localhost:8082/actuator/health |
| Ticket Service | 8083 | http://localhost:8083 |
| Ticket Service API Docs | 8083 | http://localhost:8083/swagger-ui.html |
| **Infrastructure** |
| PostgreSQL (User) | 5434 | localhost:5434 |
| PostgreSQL (Ticket) | 5435 | localhost:5435 |
| Redis | 6380 | localhost:6380 |
| pgAdmin | 5051 | http://localhost:5051 |

### pgAdmin Credentials
- **Email**: admin@admin.com
- **Password**: admin

### Database Credentials
- **Username**: postgres
- **Password**: postgres

## Development Workflow

### Full Stack Development
```bash
# Start everything
docker-compose up -d --build

# Follow all service logs
docker-compose logs -f user-service api-gateway ticket-service

# Follow infrastructure logs
docker-compose logs -f postgres postgres-ticket redis
```

### Start Only Infrastructure
```bash
# Start only databases and Redis
docker-compose up -d postgres postgres-ticket redis pgadmin
```

### Rebuild After Code Changes
```bash
# Rebuild specific service
docker-compose up -d --build user-service

# Rebuild all services
docker-compose up -d --build

# Rebuild without cache
docker-compose build --no-cache
docker-compose up -d
```

### Restart Specific Service
```bash
# Restart one service
docker-compose restart user-service

# Restart multiple services
docker-compose restart user-service api-gateway
```

## Services Overview

### Infrastructure Services
1. **postgres** - PostgreSQL database for User Service (port 5434)
2. **postgres-ticket** - PostgreSQL database for Ticket Service (port 5435)
3. **redis** - Redis cache for API Gateway rate limiting (port 6380)
4. **pgadmin** - PostgreSQL web admin interface (port 5051)

### Microservices
1. **user-service** - User authentication and management (port 8081)
2. **api-gateway** - API Gateway with JWT auth and rate limiting (port 8082)
3. **ticket-service** - Ticket, Order, Payment, and Refund management (port 8083)

## Health Checks

All services have health checks configured:

```bash
# Check all service health
docker-compose ps

# Check specific service health
curl http://localhost:8081/api/v1/actuator/health  # User Service
curl http://localhost:8082/actuator/health          # API Gateway
curl http://localhost:8083/actuator/health          # Ticket Service
```

## Network

All services use a shared network: **`microservices-network`**

Services communicate using their container names:
- `user-service-app` or `user-service`
- `api-gateway`
- `ticket-service-app` or `ticket-service`
- `postgres` (User DB)
- `postgres-ticket` (Ticket DB)
- `redis`

## Volumes

Persistent data volumes:
- `postgres_data` - User Service database
- `postgres_ticket_data` - Ticket Service database
- `redis_data` - Redis cache
- `pgadmin_data` - pgAdmin configuration

### Reset Databases
```bash
# Stop services and remove all data
docker-compose down -v

# Start fresh
docker-compose up -d
```

### Backup Database
```bash
# Backup User database
docker exec user-service-postgres pg_dump -U postgres user_db > user_db_backup.sql

# Backup Ticket database
docker exec ticket-service-postgres pg_dump -U postgres ticket_db > ticket_db_backup.sql
```

### Restore Database
```bash
# Restore User database
docker exec -i user-service-postgres psql -U postgres user_db < user_db_backup.sql

# Restore Ticket database
docker exec -i ticket-service-postgres psql -U postgres ticket_db < ticket_db_backup.sql
```

## Troubleshooting

### Port Already in Use
```bash
# Check which process is using the port
sudo netstat -tulpn | grep :8081

# Or use lsof
sudo lsof -i :8081

# Stop all services and restart
docker-compose down
docker-compose up -d
```

### Service Not Starting
```bash
# Check logs for errors
docker-compose logs service-name

# Check health status
docker-compose ps

# Restart specific service
docker-compose restart service-name

# Rebuild and restart
docker-compose up -d --build service-name
```

### Network Issues
```bash
# Recreate network
docker-compose down
docker network prune
docker-compose up -d
```

### Database Connection Issues
```bash
# Check if database is ready
docker-compose exec postgres pg_isready -U postgres

# Access database directly
docker-compose exec postgres psql -U postgres -d user_db

# View database logs
docker-compose logs postgres
```

### Clean Everything and Start Fresh
```bash
# Stop all services
docker-compose down -v

# Remove all images
docker-compose down --rmi all

# Rebuild everything
docker-compose up -d --build
```

## Monitoring

### View Resource Usage
```bash
# All containers
docker stats

# Specific service
docker stats user-service-app
```

### Access Container Shell
```bash
# Access User Service shell
docker-compose exec user-service sh

# Access PostgreSQL shell
docker-compose exec postgres psql -U postgres -d user_db

# Access Redis CLI
docker-compose exec redis redis-cli
```

## API Testing

### User Service
```bash
# Register user
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","firstName":"Test","lastName":"User"}'

# Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

### API Gateway
```bash
# Health check
curl http://localhost:8082/actuator/health

# Route through gateway
curl http://localhost:8082/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

### Ticket Service
```bash
# Health check
curl http://localhost:8083/actuator/health

# View API docs
open http://localhost:8083/swagger-ui.html
```

## Production Deployment

For production, consider:

1. **Environment Variables**: Use `.env` file or secret management
   ```bash
   # Create .env file
   POSTGRES_PASSWORD=secure_password_here
   JWT_SECRET=your_secure_jwt_secret_here
   ```

2. **External Databases**: Use managed databases instead of containers

3. **Scaling**: Use Docker Swarm or Kubernetes for production

4. **Monitoring**: Add Prometheus, Grafana, ELK stack

5. **Security**:
   - Don't expose database ports publicly
   - Use HTTPS/TLS
   - Implement proper authentication
   - Regular security updates

6. **Backup Strategy**: Regular automated backups of databases

7. **CI/CD Pipeline**: Automated testing and deployment
