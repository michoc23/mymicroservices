# 🚀 User Service - Quick Start Guide

Get the User Service up and running in 5 minutes!

## ⚡ 1-Minute Setup (Docker)

```bash
# Start database
docker-compose up -d

# Build and run
cd User
mvn clean install
mvn spring-boot:run
```

**Done!** Service running at http://localhost:8081/api/v1

---

## ✅ Verify Installation

```bash
# Health check
curl http://localhost:8081/api/v1/actuator/health

# Open Swagger UI
# Browser: http://localhost:8081/api/v1/swagger-ui.html
```

---

## 🎯 First API Call

### 1. Register a User

```bash
curl -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "phoneNumber": "+1234567890",
    "role": "PASSENGER"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGci...",
  "userId": 1,
  "email": "john@example.com",
  "role": "PASSENGER"
}
```

### 2. Login

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123"
  }'
```

### 3. Get Your Profile

```bash
# Save your token from register/login
TOKEN="eyJhbGci..."

curl http://localhost:8081/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📱 Using Postman

1. Import collection: `/docs/postman/User-Service.postman_collection.json`
2. Set environment variable: `baseUrl = http://localhost:8081/api/v1`
3. Register a user
4. Token is auto-saved for subsequent requests
5. Try other endpoints!

---

## 🔧 Configuration

Default settings work out-of-the-box. To customize:

**Edit:** `User/src/main/resources/application.yml`

```yaml
server:
  port: 8081  # Change port

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/user_db
    username: postgres
    password: postgres  # Change credentials

jwt:
  expiration: 86400000  # 24 hours
```

---

## 🐛 Troubleshooting

### Port Already in Use
```bash
# Check what's using port 8081
lsof -i :8081

# Or change port in application.yml
server.port: 8082
```

### Database Connection Failed
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Start database
docker-compose up -d postgres
```

### Application Won't Start
```bash
# Clean and rebuild
mvn clean install -U

# Check logs
tail -f logs/user-service.log
```

---

## 🎓 Next Steps

1. ✅ Read the full [README.md](README.md)
2. ✅ Explore [API Documentation](http://localhost:8081/api/v1/swagger-ui.html)
3. ✅ Try all endpoints in Postman
4. ✅ Check [Contribution Guide](#contributing)

---

## 📚 Key Endpoints

| Endpoint | Method | Description | Auth |
|----------|--------|-------------|------|
| `/auth/register` | POST | Register new user | No |
| `/auth/login` | POST | Login | No |
| `/users/me` | GET | Get profile | Yes |
| `/drivers/me` | GET | Driver profile | Yes (Driver) |
| `/passengers/me` | GET | Passenger profile | Yes (Passenger) |
| `/admin/users` | GET | All users | Yes (Admin) |

---

## 💡 Pro Tips

**Tip 1:** Use Swagger UI for interactive testing
```
http://localhost:8081/api/v1/swagger-ui.html
```

**Tip 2:** Save your JWT token
```bash
# Linux/Mac
export TOKEN="your-jwt-token"

# Windows
set TOKEN=your-jwt-token

# Use in requests
curl -H "Authorization: Bearer $TOKEN" ...
```

**Tip 3:** Enable debug logging
```yaml
logging:
  level:
    com.bustransport.user: DEBUG
```

**Tip 4:** Access pgAdmin for database management
```
http://localhost:5050
Email: admin@admin.com
Password: admin
```

---

## ✨ You're All Set!

Service is running and ready to use. Happy coding! 🎉

**Need help?** Check the [full README](README.md) or [open an issue](https://github.com/yourusername/user-service/issues).