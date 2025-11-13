# 🎫 Ticket Service - Urban Transport Management System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A comprehensive microservice for ticket management and payment processing in the Urban Transport Management System. Handles ticket purchasing, validation, QR code generation, orders, payments, and refunds for urban bus transportation.

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

### Core Functionality
- 🎟️ **Ticket Management** - Create, validate, and manage digital tickets
- 🔢 **QR Code Generation** - Unique QR codes for each ticket
- 📦 **Order Processing** - Complete order lifecycle management
- 💳 **Payment Processing** - Multiple payment methods support
- 💰 **Refund Management** - Automated refund processing
- ✅ **Ticket Validation** - Real-time ticket validation system
- 🚌 **Route & Schedule Integration** - Link tickets to routes and schedules
- 📊 **Usage Tracking** - Monitor ticket usage patterns

### Ticket Types
- 🎫 **Single Ride** - One-time use tickets
- 📅 **Daily Pass** - 24-hour unlimited access
- 📆 **Weekly Pass** - 7-day unlimited access
- 📋 **Monthly Pass** - 30-day unlimited access
- 👥 **Multi-Ride** - Multiple-use tickets with configurable limits

### Business Features
- 🔄 **Automatic Status Management** - Ticket status transitions
- ⏰ **Validity Period Tracking** - Time-based ticket expiration
- 🚫 **Cancellation Rules** - Business logic for ticket cancellation
- 📸 **QR Code Images** - Generate downloadable QR code images
- 📱 **Digital Ticket Delivery** - Instant ticket availability
- 🎯 **Smart Validation** - Multi-use ticket support
- 📈 **Purchase History** - Complete order tracking

### Technical Features
- ✅ Input validation with Bean Validation
- 🔍 Global exception handling
- 📊 Actuator endpoints for monitoring
- 📚 Swagger/OpenAPI documentation
- 🐳 Docker support
- 🔄 Database migration ready
- 🎯 RESTful API design
- 📈 Scalable microservice architecture

---

## 🛠 Tech Stack

### Backend
- **Spring Boot 3.2.0** - Application framework
- **Spring Data JPA** - Data persistence
- **PostgreSQL 15** - Primary database
- **Hibernate** - ORM framework

### Libraries & Tools
- **Lombok** - Reduce boilerplate code
- **MapStruct** - Object mapping
- **ZXing (3.5.3)** - QR code generation
- **iText7 (7.2.5)** - PDF generation
- **SpringDoc OpenAPI** - API documentation
- **Maven** - Dependency management
- **Docker & Docker Compose** - Containerization
- **Spring Boot Actuator** - Monitoring and health checks

### Development
- **Java 17** - Programming language
- **IntelliJ IDEA / Eclipse** - IDEs
- **Postman** - API testing
- **pgAdmin** - Database management

---

## 🏗 Architecture

### Microservice Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Ticket Service                            │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │ Controllers  │───▶│   Services   │───▶│ Repositories │  │
│  │              │    │              │    │              │  │
│  │ - Ticket     │    │ - Ticket     │    │ - Ticket     │  │
│  │ - Order      │    │ - Order      │    │ - Order      │  │
│  │ - Payment    │    │ - Payment    │    │ - Payment    │  │
│  │ - Refund     │    │ - Refund     │    │ - Refund     │  │
│  └──────────────┘    └──────────────┘    └──────────────┘  │
│         ▲                    │                    │         │
│         │                    ▼                    ▼         │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │  Utilities   │    │     DTOs     │    │   Entities   │  │
│  │              │    │              │    │              │  │
│  │ - QR Gen     │    │ - Requests   │    │ - Ticket     │  │
│  │ - PDF Gen    │    │ - Responses  │    │ - Order      │  │
│  │              │    │              │    │ - Payment    │  │
│  └──────────────┘    └──────────────┘    │ - Refund     │  │
│                                           └──────────────┘  │
│                                                   │         │
│                                                   ▼         │
│                                           ┌──────────────┐  │
│                                           │  PostgreSQL  │  │
│                                           └──────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Entity Relationship

```
    ┌────────────┐
    │   Order    │
    │            │
    └─────┬──────┘
          │
          │ 1:N
          │
    ┌─────▼──────┐          ┌────────────┐
    │   Ticket   │          │  Payment   │
    │            │          │            │
    └────────────┘          └─────┬──────┘
                                  │
                                  │ 1:N
                                  │
                            ┌─────▼──────┐
                            │   Refund   │
                            │            │
                            └────────────┘
```

### Request Flow

```
Client Request
      │
      ▼
┌───────────────┐
│ Controller    │ ◀── Handle Request & Validation
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ Service       │ ◀── Business Logic & QR Generation
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ Repository    │ ◀── Database Operations
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ PostgreSQL    │ ◀── Data Persistence
└───────────────┘
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+** - [Download](https://adoptium.net/)
- **Maven 3.8+** - [Download](https://maven.apache.org/download.cgi)
- **Docker & Docker Compose** (optional) - [Download](https://www.docker.com/get-started)
- **PostgreSQL 15** (if not using Docker) - [Download](https://www.postgresql.org/download/)
- **Git** - [Download](https://git-scm.com/downloads)

### Installation

#### Option 1: Using Docker (Recommended)

```bash
# 1. Clone the repository
git clone https://github.com/yourusername/ticket-service.git
cd mymicroservices

# 2. Start all services with Docker Compose
docker-compose up -d

# 3. Verify the service is running
docker-compose ps
docker-compose logs -f ticket-service
```

#### Option 2: Manual Setup

```bash
# 1. Clone the repository
git clone https://github.com/yourusername/ticket-service.git
cd mymicroservices

# 2. Create PostgreSQL database
psql -U postgres
CREATE DATABASE ticket_db;
\q

# 3. Configure database connection
# Edit Ticket/src/main/resources/application.yml
# Update datasource URL, username, and password

# 4. Build and run
cd Ticket
mvn clean install
mvn spring-boot:run
```

### Environment Variables

Create a `.env` file or set these environment variables:

```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/ticket_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres
```

### Verify Installation

```bash
# Check health status
curl http://localhost:8083/actuator/health

# Expected response:
# {"status":"UP"}

# Access Swagger UI
# Open browser: http://localhost:8083/swagger-ui.html
```

---

## 📚 API Documentation

### Base URL

```
Local: http://localhost:8083/api
Production: https://api.yourdomain.com/ticket-service/api
```

### Ticket Endpoints

#### Get Ticket by ID
```http
GET /tickets/{id}
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response: 200 OK**
```json
{
  "id": 1,
  "userId": 123,
  "ticketType": "SINGLE",
  "price": 2.50,
  "purchaseDate": "2025-11-13T10:00:00",
  "validFrom": "2025-11-13T10:00:00",
  "validUntil": "2025-11-13T23:59:59",
  "status": "ACTIVE",
  "qrCode": "TKT-20251113-ABC123",
  "usageCount": 0,
  "maxUsage": 1,
  "routeId": 5,
  "scheduleId": 42,
  "passengerName": "John Doe"
}
```

#### Get Ticket by QR Code
```http
GET /tickets/qr/{qrCode}
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Get User's Tickets
```http
GET /tickets/user/{userId}
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Get Active Tickets
```http
GET /tickets/user/{userId}/active
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response: 200 OK**
```json
[
  {
    "id": 1,
    "ticketType": "WEEKLY",
    "status": "ACTIVE",
    "validUntil": "2025-11-20T23:59:59",
    "usageCount": 5,
    "maxUsage": 999
  }
]
```

#### Validate and Use Ticket
```http
POST /tickets/validate
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "qrCode": "TKT-20251113-ABC123",
  "routeId": 5,
  "validatorId": "VAL-001"
}
```

**Response: 200 OK**
```json
{
  "valid": true,
  "message": "Ticket validated successfully",
  "ticketId": 1,
  "remainingUses": 0,
  "validatedAt": "2025-11-13T14:30:00"
}
```

#### Cancel Ticket
```http
PUT /tickets/{id}/cancel
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response: 200 OK**
```json
{
  "id": 1,
  "status": "CANCELLED",
  "message": "Ticket cancelled successfully"
}
```

#### Get QR Code Image
```http
GET /tickets/{id}/qr-image
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response: 200 OK**
```
Content-Type: image/png
[PNG binary data]
```

### Order Endpoints

#### Create Order
```http
POST /orders
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "userId": 123,
  "tickets": [
    {
      "ticketType": "SINGLE",
      "routeId": 5,
      "scheduleId": 42,
      "passengerName": "John Doe"
    },
    {
      "ticketType": "DAILY",
      "routeId": 5
    }
  ]
}
```

**Response: 201 Created**
```json
{
  "id": 1,
  "orderNumber": "ORD-20251113-001",
  "userId": 123,
  "totalAmount": 12.50,
  "status": "PENDING",
  "tickets": [
    {
      "id": 1,
      "ticketType": "SINGLE",
      "price": 2.50,
      "qrCode": "TKT-20251113-ABC123"
    },
    {
      "id": 2,
      "ticketType": "DAILY",
      "price": 10.00,
      "qrCode": "TKT-20251113-ABC124"
    }
  ],
  "createdAt": "2025-11-13T10:00:00"
}
```

#### Get Order by ID
```http
GET /orders/{id}
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Get Order by Order Number
```http
GET /orders/order-number/{orderNumber}
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Get User's Orders
```http
GET /orders/user/{userId}
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response: 200 OK**
```json
[
  {
    "id": 1,
    "orderNumber": "ORD-20251113-001",
    "totalAmount": 12.50,
    "status": "COMPLETED",
    "ticketCount": 2,
    "createdAt": "2025-11-13T10:00:00"
  }
]
```

#### Cancel Order
```http
PUT /orders/{id}/cancel
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Payment Endpoints

#### Create Payment
```http
POST /payments
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "orderId": 1,
  "amount": 12.50,
  "paymentMethod": "CREDIT_CARD",
  "transactionId": "TXN-123456789"
}
```

**Response: 201 Created**
```json
{
  "id": 1,
  "orderId": 1,
  "amount": 12.50,
  "paymentMethod": "CREDIT_CARD",
  "status": "COMPLETED",
  "transactionId": "TXN-123456789",
  "paymentDate": "2025-11-13T10:05:00"
}
```

#### Get Payment by ID
```http
GET /payments/{id}
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Get Payments for Order
```http
GET /payments/order/{orderId}
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Get User's Payments
```http
GET /payments/user/{userId}
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Refund Endpoints

#### Create Refund Request
```http
POST /refunds
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "paymentId": 1,
  "reason": "Trip cancelled",
  "amount": 12.50
}
```

**Response: 201 Created**
```json
{
  "id": 1,
  "paymentId": 1,
  "amount": 12.50,
  "reason": "Trip cancelled",
  "status": "PENDING",
  "requestedAt": "2025-11-13T11:00:00"
}
```

#### Get Refund by ID
```http
GET /refunds/{id}
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Get User's Refunds
```http
GET /refunds/user/{userId}
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Approve Refund (Admin)
```http
PUT /refunds/{id}/approve
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response: 200 OK**
```json
{
  "id": 1,
  "status": "APPROVED",
  "approvedAt": "2025-11-13T12:00:00",
  "approvedBy": "admin@example.com"
}
```

#### Reject Refund (Admin)
```http
PUT /refunds/{id}/reject
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "reason": "Ticket already used"
}
```

### Interactive API Documentation

Access Swagger UI for interactive API testing:
```
http://localhost:8083/swagger-ui.html
```

OpenAPI JSON specification:
```
http://localhost:8083/api-docs
```

---

## 🗄 Database Schema

### Tickets Table
```sql
CREATE TABLE tickets (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    user_id BIGINT NOT NULL,
    ticket_type VARCHAR(20) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    purchase_date TIMESTAMP NOT NULL,
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    qr_code VARCHAR(255) NOT NULL UNIQUE,
    usage_count INTEGER NOT NULL DEFAULT 0,
    max_usage INTEGER NOT NULL DEFAULT 1,
    route_id BIGINT NOT NULL,
    schedule_id BIGINT,
    passenger_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### Orders Table
```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### Payments Table
```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    transaction_id VARCHAR(100),
    payment_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### Refunds Table
```sql
CREATE TABLE refunds (
    id BIGSERIAL PRIMARY KEY,
    payment_id BIGINT REFERENCES payments(id),
    amount DECIMAL(10,2) NOT NULL,
    reason TEXT,
    status VARCHAR(20) NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    approved_at TIMESTAMP,
    approved_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
```

### Database Diagram

```
┌──────────────────────────┐
│        ORDERS            │
├──────────────────────────┤
│ • id (PK)                │
│ • order_number (UNIQUE)  │
│ • user_id                │
│ • total_amount           │
│ • status                 │
│ • created_at             │
│ • updated_at             │
└────────┬─────────────────┘
         │
         │ 1:N
         │
┌────────▼─────────────────┐
│       TICKETS            │
├──────────────────────────┤
│ • id (PK)                │
│ • order_id (FK)          │
│ • user_id                │
│ • ticket_type            │
│ • price                  │
│ • qr_code (UNIQUE)       │
│ • status                 │
│ • usage_count            │
│ • valid_from/until       │
└──────────────────────────┘

┌──────────────────────────┐
│       PAYMENTS           │
├──────────────────────────┤
│ • id (PK)                │
│ • order_id (FK)          │
│ • amount                 │
│ • payment_method         │
│ • status                 │
│ • transaction_id         │
└────────┬─────────────────┘
         │
         │ 1:N
         │
┌────────▼─────────────────┐
│       REFUNDS            │
├──────────────────────────┤
│ • id (PK)                │
│ • payment_id (FK)        │
│ • amount                 │
│ • reason                 │
│ • status                 │
│ • approved_at            │
└──────────────────────────┘
```

---

## 🧪 Testing

### Run Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=TicketServiceTest

# Run with coverage
mvn clean test jacoco:report
```

### Test with Postman

Import the Postman collection:
```bash
# Collection available at: /docs/postman/Ticket-Service.postman_collection.json
```

### Manual Testing

```bash
# 1. Create an order
curl -X POST http://localhost:8083/api/orders \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 123,
    "tickets": [
      {
        "ticketType": "SINGLE",
        "routeId": 5
      }
    ]
  }'

# 2. Get ticket by ID
curl http://localhost:8083/api/tickets/1 \
  -H "Authorization: Bearer YOUR_TOKEN"

# 3. Validate ticket
curl -X POST http://localhost:8083/api/tickets/validate \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "qrCode": "TKT-20251113-ABC123",
    "routeId": 5
  }'

# 4. Get QR code image
curl http://localhost:8083/api/tickets/1/qr-image \
  -H "Authorization: Bearer YOUR_TOKEN" \
  --output ticket-qr.png
```

---

## 🐳 Deployment

### Docker Deployment

#### Build Docker Image

```bash
# Build image from project root
cd /home/ayoub/mymicroservices
docker build -t ticket-service:latest -f Ticket/Dockerfile .

# Run container
docker run -d \
  --name ticket-service \
  -p 8083:8083 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-ticket:5432/ticket_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  --network microservices-network \
  ticket-service:latest
```

#### Docker Compose (Full Stack)

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f ticket-service

# Stop all services
docker-compose down

# Rebuild and restart
docker-compose up -d --build ticket-service
```

### Production Deployment

#### Environment Configuration

```yaml
# application-prod.yml
server:
  port: 8083

spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

logging:
  level:
    root: INFO
    com.bustransport.ticket: INFO
```

#### Health Checks

```bash
# Liveness probe
curl http://localhost:8083/actuator/health

# Readiness probe
curl http://localhost:8083/actuator/health/readiness
```

---

## 📊 Monitoring

### Actuator Endpoints

```bash
# Health check
curl http://localhost:8083/actuator/health

# Metrics
curl http://localhost:8083/actuator/metrics

# Info
curl http://localhost:8083/actuator/info

# Prometheus metrics
curl http://localhost:8083/actuator/prometheus
```

### Database Management

#### Using pgAdmin

Access pgAdmin at http://localhost:5051

**Credentials:**
- Email: admin@admin.com
- Password: admin

**Database Connection:**
- Host: ticket-service-postgres
- Port: 5432
- Database: ticket_db
- Username: postgres
- Password: postgres

#### Direct PostgreSQL Connection

```bash
# Connect to ticket database
psql -h localhost -p 5435 -U postgres -d ticket_db

# List tables
\dt

# Query tickets
SELECT * FROM tickets WHERE status = 'ACTIVE';
```

---

## 🔧 Business Rules

### Ticket Validation

A ticket is valid if:
- Status is `ACTIVE`
- Current time is before `validUntil`
- `usageCount < maxUsage`

```java
public boolean isValid() {
    return status == TicketStatus.ACTIVE
        && LocalDateTime.now().isBefore(validUntil)
        && usageCount < maxUsage;
}
```

### Ticket Cancellation

A ticket can be cancelled if:
- Status is `ACTIVE`
- `usageCount` is 0 (never used)
- Current time is at least 2 hours before `validFrom`

```java
public boolean canBeCancelled() {
    return status == TicketStatus.ACTIVE
        && usageCount == 0
        && LocalDateTime.now().isBefore(validFrom.minusHours(2));
}
```

### Ticket Types & Pricing

| Type | Duration | Max Usage | Typical Price |
|------|----------|-----------|---------------|
| SINGLE | Trip | 1 | $2.50 |
| DAILY | 24 hours | Unlimited | $10.00 |
| WEEKLY | 7 days | Unlimited | $35.00 |
| MONTHLY | 30 days | Unlimited | $120.00 |

---

## 🐛 Troubleshooting

### Common Issues

1. **Database connection error**
   ```bash
   # Verify PostgreSQL is running
   docker-compose ps postgres-ticket

   # Check logs
   docker-compose logs postgres-ticket

   # Restart database
   docker-compose restart postgres-ticket
   ```

2. **Port already in use**
   ```bash
   # Find process using port 8083
   lsof -i :8083

   # Kill the process
   kill -9 <PID>

   # Or change port in application.yml
   ```

3. **QR code generation fails**
   - Check ZXing library is included in dependencies
   - Verify temp directory is writable
   - Check logs for detailed error messages

4. **Docker container won't start**
   ```bash
   # Check container logs
   docker-compose logs ticket-service

   # Remove and rebuild
   docker-compose down
   docker-compose up -d --build ticket-service
   ```

### Logs

```bash
# View real-time logs
docker-compose logs -f ticket-service

# View last 100 lines
docker-compose logs --tail=100 ticket-service

# Application logs (if running locally)
tail -f logs/ticket-service.log
```

---

## 🤝 Contributing

1. Follow Java code conventions
2. Write unit tests for new features
3. Update API documentation
4. Ensure all tests pass before committing
5. Create pull requests with detailed descriptions

---

## 📄 License

This project is part of the Bus Transport Microservices System.

---

## 📞 Contact

For issues and questions, please refer to the main project documentation or create an issue in the repository.

---

## 🔗 Related Services

- **User Service** - User authentication and management (Port 8081)
- **API Gateway** - Central API gateway with rate limiting (Port 8082)
- **Route Service** - Bus route and schedule management (Coming soon)

---

**Made with ❤️ for Urban Transport Management**