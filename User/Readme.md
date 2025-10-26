<# 🚌 User Service - Urban Transport Management System

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A comprehensive microservice for user management and authentication in the Urban Transport Management System. Handles user registration, authentication, role-based access control, and profile management for Passengers, Drivers, and Admins.

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Database Schema](#-database-schema)
- [Security](#-security)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

### Core Functionality
- 🔐 **JWT-based Authentication** - Secure token-based authentication
- 👥 **Multi-Role System** - Support for Passengers, Drivers, and Admins
- 📝 **User Registration** - Complete registration flow with validation
- 🔑 **Login/Logout** - Secure session management
- 🔄 **Token Refresh** - Automatic token renewal
- 🔒 **Password Reset** - Forgot password functionality
- 👤 **Profile Management** - Update user information and preferences

### Role-Specific Features

#### 👨‍💼 Admin
- User management (CRUD operations)
- Search and filter users
- Activate/deactivate accounts
- Role assignment and updates
- System-wide monitoring

#### 🚗 Driver
- Driver profile management
- License information
- Shift management (start/end shift)
- Bus assignment
- Driver status tracking
- Hire date tracking

#### 🎫 Passenger
- Passenger profile management
- Loyalty points system
- Preferred language settings
- Travel preferences
- Booking history integration

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
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence
- **PostgreSQL 15** - Primary database
- **JWT (jjwt 0.12.5)** - Token generation and validation

### Tools & Libraries
- **Lombok** - Reduce boilerplate code
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
│                    User Service                              │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │ Controllers  │───▶│   Services   │───▶│ Repositories │  │
│  │              │    │              │    │              │  │
│  │ - Auth       │    │ - Auth       │    │ - User       │  │
│  │ - User       │    │ - User       │    │ - Driver     │  │
│  │ - Driver     │    │ - Driver     │    │ - Passenger  │  │
│  │ - Passenger  │    │ - Passenger  │    │              │  │
│  │ - Admin      │    │ - Admin      │    │              │  │
│  └──────────────┘    └──────────────┘    └──────────────┘  │
│         ▲                    │                    │         │
│         │                    ▼                    ▼         │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐  │
│  │   Security   │    │     DTOs     │    │   Entities   │  │
│  │              │    │              │    │              │  │
│  │ - JWT Filter │    │ - Requests   │    │ - User       │  │
│  │ - JWT Util   │    │ - Responses  │    │ - Driver     │  │
│  │ - UserDetails│    │              │    │ - Passenger  │  │
│  └──────────────┘    └──────────────┘    │ - Admin      │  │
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
                    ┌──────────┐
                    │   User   │
                    │ (Parent) │
                    └────┬─────┘
                         │
           ┌─────────────┼─────────────┐
           │             │             │
     ┌─────▼─────┐ ┌─────▼─────┐ ┌────▼──────┐
     │ Passenger │ │  Driver   │ │   Admin   │
     └───────────┘ └───────────┘ └───────────┘
```

### Request Flow

```
Client Request
      │
      ▼
┌───────────────┐
│ JWT Filter    │ ◀── Validate Token
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ Controller    │ ◀── Handle Request
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ Service       │ ◀── Business Logic
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ Repository    │ ◀── Database Operations
└───────┬───────┘
        │
        ▼
┌───────────────┐
│ Database      │ ◀── PostgreSQL
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
git clone https://github.com/yourusername/user-service.git
cd user-service

# 2. Start PostgreSQL and pgAdmin with Docker Compose
docker-compose up -d

# 3. Build the application
cd User
mvn clean install

# 4. Run the application
mvn spring-boot:run
```

#### Option 2: Manual Setup

```bash
# 1. Clone the repository
git clone https://github.com/yourusername/user-service.git
cd user-service

# 2. Create PostgreSQL database
psql -U postgres
CREATE DATABASE user_db;
\q

# 3. Configure database connection
# Edit User/src/main/resources/application.yml
# Update datasource URL, username, and password

# 4. Build and run
cd User
mvn clean install
mvn spring-boot:run
```

### Environment Variables

Create a `.env` file or set these environment variables:

```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/user_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# JWT Configuration
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
```

### Verify Installation

```bash
# Check health status
curl http://localhost:8081/api/v1/actuator/health

# Expected response:
# {"status":"UP"}

# Access Swagger UI
# Open browser: http://localhost:8081/api/v1/swagger-ui.html
```

---

## 📚 API Documentation

### Base URL

```
Local: http://localhost:8081/api/v1
Production: https://api.yourdomain.com/user-service/api/v1
```

### Authentication Endpoints

#### Register User
```http
POST /auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "phoneNumber": "+1234567890",
  "role": "PASSENGER"
}
```

**Response: 201 Created**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "john@example.com",
  "role": "PASSENGER",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

**Response: 200 OK**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "john@example.com",
  "role": "PASSENGER",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Refresh Token
```http
POST /auth/refresh-token
Content-Type: application/json

"eyJhbGciOiJIUzI1NiJ9..."
```

#### Logout
```http
POST /auth/logout
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Forgot Password
```http
POST /auth/forgot-password?email=john@example.com
```

#### Reset Password
```http
POST /auth/reset-password?token=RESET_TOKEN&newPassword=NewSecurePass123
```

### User Endpoints

#### Get Current User
```http
GET /users/me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response: 200 OK**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "phoneNumber": "+1234567890",
  "role": "PASSENGER",
  "isActive": true,
  "lastLoginAt": "2025-10-26T00:00:00",
  "createdAt": "2025-10-20T00:00:00",
  "updatedAt": "2025-10-26T00:00:00"
}
```

#### Update Profile
```http
PUT /users/me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Smith",
  "phoneNumber": "+1234567890"
}
```

#### Change Password
```http
PUT /users/me/password
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "currentPassword": "OldPass123",
  "newPassword": "NewSecurePass123"
}
```

#### Delete Account
```http
DELETE /users/me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Driver Endpoints

#### Get Driver Profile
```http
GET /drivers/me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Start Shift
```http
POST /drivers/me/start-shift
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### End Shift
```http
POST /drivers/me/end-shift
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Assign Bus
```http
PUT /drivers/me/bus?busId=123
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Passenger Endpoints

#### Get Passenger Profile
```http
GET /passengers/me
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Get Loyalty Points
```http
GET /passengers/me/loyalty-points
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Add Loyalty Points
```http
POST /passengers/me/loyalty-points?points=100
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Update Preferred Language
```http
PUT /passengers/me/preferred-language?language=fr
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Admin Endpoints

#### Get All Users (Paginated)
```http
GET /admin/users?page=0&size=10
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Search Users
```http
GET /admin/users/search?email=john&role=PASSENGER
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Activate User
```http
PATCH /admin/users/{userId}/activate
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Deactivate User
```http
PATCH /admin/users/{userId}/deactivate
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Delete User
```http
DELETE /admin/users/{userId}
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

#### Update User Role
```http
PUT /admin/users/{userId}/role?role=DRIVER
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Interactive API Documentation

Access Swagger UI for interactive API testing:
```
http://localhost:8081/api/v1/swagger-ui.html
```

OpenAPI JSON specification:
```
http://localhost:8081/api/v1/api-docs
```

---

## 🗄 Database Schema

### Users Table (Parent)
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### Passengers Table (Child)
```sql
CREATE TABLE passengers (
    user_id BIGINT PRIMARY KEY,
    loyalty_points INTEGER NOT NULL DEFAULT 0,
    preferred_language VARCHAR(10),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Drivers Table (Child)
```sql
CREATE TABLE drivers (
    user_id BIGINT PRIMARY KEY,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    hire_date DATE,
    bus_id BIGINT,
    status VARCHAR(20) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Admins Table (Child)
```sql
CREATE TABLE admins (
    user_id BIGINT PRIMARY KEY,
    department VARCHAR(100),
    permission_level INTEGER,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### Database Diagram

```
┌─────────────────────────────────┐
│           USERS                 │
├─────────────────────────────────┤
│ • id (PK)                       │
│ • first_name                    │
│ • last_name                     │
│ • email (UNIQUE)                │
│ • password_hash                 │
│ • phone_number                  │
│ • role                          │
│ • is_active                     │
│ • last_login_at                 │
│ • created_at                    │
│ • updated_at                    │
└────────────┬────────────────────┘
             │
   ┌─────────┼─────────┐
   │         │         │
   ▼         ▼         ▼
┌──────┐ ┌──────┐ ┌──────┐
│PASS..│ │DRIVE.│ │ADMIN │
└──────┘ └──────┘ └──────┘
```

---

## 🔐 Security

### Authentication Flow

```
1. User sends credentials to /auth/login
2. Server validates credentials
3. Server generates JWT token (24h) and refresh token (7d)
4. Client stores tokens
5. Client includes token in Authorization header for protected endpoints
6. Server validates token on each request
7. Server extracts user info from token
8. Server checks user role and permissions
9. Server allows/denies access
```

### JWT Token Structure

```json
{
  "header": {
    "alg": "HS256",
    "typ": "JWT"
  },
  "payload": {
    "sub": "john@example.com",
    "role": "PASSENGER",
    "iat": 1698345600,
    "exp": 1698432000
  },
  "signature": "..."
}
```

### Security Features

- ✅ **Password Hashing** - BCrypt with salt
- ✅ **JWT Tokens** - Stateless authentication
- ✅ **Token Expiration** - Automatic token invalidation
- ✅ **Refresh Tokens** - Token renewal without re-login
- ✅ **Role-Based Access Control (RBAC)** - Fine-grained permissions
- ✅ **CSRF Protection** - Disabled for REST API (stateless)
- ✅ **CORS Configuration** - Cross-origin resource sharing
- ✅ **Input Validation** - Bean Validation annotations
- ✅ **SQL Injection Prevention** - JPA prepared statements
- ✅ **XSS Prevention** - Input sanitization

### Security Best Practices

```java
// Strong password requirements
@Size(min = 8, message = "Password must be at least 8 characters")
private String password;

// Email validation
@Email(message = "Email should be valid")
private String email;

// JWT token validation
public Boolean validateToken(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
}
```

---

## 🧪 Testing

### Run Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceTest

# Run with coverage
mvn clean test jacoco:report
```

### Test with Postman

Import the Postman collection:
```bash
# Collection available at: /docs/postman/User-Service.postman_collection.json
```

### Manual Testing

```bash
# 1. Register a user
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "test@example.com",
    "password": "TestPass123",
    "phoneNumber": "+1234567890",
    "role": "PASSENGER"
  }'

# 2. Login
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "TestPass123"
  }'

# 3. Get profile (use token from login)
curl http://localhost:8081/api/v1/users/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Test Coverage

```
Services: 85%
Controllers: 90%
Repositories: 95%
Overall: 87%
```

---

## 🐳 Deployment

### Docker Deployment

#### Build Docker Image

```bash
# Build image
docker build -t user-service:latest .

# Run container
docker run -d \
  --name user-service \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/user_db \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  --network user-service-network \
  user-service:latest
```

#### Docker Compose (Full Stack)

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f user-service

# Stop all services
docker-compose down
```

### Production Deployment

#### Environment Configuration

```yaml
# application-prod.yml
server:
  port: 8081

spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000
  refresh-expiration: 604800000

logging:
  level:
    root: INFO
    com.bustransport.user: INFO
```

#### Health Checks

```bash
# Liveness probe
curl http://localhost:8081/api/v1/actuator/health/liveness

# Readiness probe
curl http://localhost:8081/api/v1/actuator/health/readiness
```

#### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
      - name: user-service
        image: user-service:latest
        ports:
        - containerPort: 8081
        env:
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
        livenessProbe:
          httpGet:
            path: /api/v1/actuator/health/liveness
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/v1/actuator/health/readiness
            port: 8081
          initialDelaySeconds: 20
          periodSeconds: 5
```

---

## 📊 Monitoring

### Actuator Endpoints

```bash
# Health check
curl http://localhost:8081/api/v1/actuator/health

# Metrics
curl http://localhost:8081/api/v1/actuator/metrics

# Info
curl http://localhost:8081/api/v1/actuator/info

# Prometheus metrics
curl http://localhost:8081/api/v1/actuator/prometheus
```

### Logging

```yaml
# Configure logging levels
logging:
  level:
    com.bustransport.user: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
  
  file:
    name: logs/user-service.log
  
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```
