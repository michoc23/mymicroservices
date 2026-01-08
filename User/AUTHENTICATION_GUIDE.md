# Authentication Guide for User Controller

## Overview

The User Controller endpoints are secured with JWT (JSON Web Token) authentication. All endpoints require a valid JWT token in the Authorization header.

## How Authentication Works

1. **User Registration/Login**: Users register or login through `/api/v1/auth/register` or `/api/v1/auth/login`
2. **JWT Token**: Upon successful authentication, a JWT token is returned
3. **Token Usage**: Include the token in the `Authorization` header for all protected endpoints
4. **User ID Extraction**: The system automatically extracts the user ID from the JWT token

## Authentication Flow

### 1. Register a New User

```bash
POST /api/v1/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "SecurePass123",
  "phoneNumber": "+1234567890",
  "role": "PASSENGER"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "email": "john.doe@example.com",
  "role": "PASSENGER",
  "firstName": "John",
  "lastName": "Doe"
}
```

### 2. Login

```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "SecurePass123"
}
```

**Response:** Same as registration response

### 3. Use Protected Endpoints

Include the JWT token in the Authorization header:

```bash
GET /api/v1/users/me
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## Protected Endpoints

All endpoints under `/api/v1/users/me/**` require authentication:

- `GET /api/v1/users/me` - Get current user profile
- `PUT /api/v1/users/me` - Update profile
- `PUT /api/v1/users/me/password` - Change password
- `DELETE /api/v1/users/me` - Delete account
- `PATCH /api/v1/users/me/activate` - Activate account
- `PATCH /api/v1/users/me/deactivate` - Deactivate account
- `GET /api/v1/users/me/tickets/{ticketId}/consumption` - Check ticket consumption by ID
- `GET /api/v1/users/me/tickets/qr/{qrCode}/consumption` - Check ticket consumption by QR code

## Example: Check Ticket Consumption

```bash
# Check by ticket ID
GET /api/v1/users/me/tickets/123/consumption
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Check by QR code
GET /api/v1/users/me/tickets/qr/ABC123XYZ/consumption
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**
```json
{
  "ticketId": 123,
  "qrCode": "ABC123XYZ",
  "status": "ACTIVE",
  "isConsumed": false,
  "isValid": true,
  "usageCount": 0,
  "maxUsage": 1,
  "validUntil": "2025-12-20T10:00:00",
  "message": "Ticket is valid and available for use (0/1 uses)",
  "userId": 1,
  "routeId": 5
}
```

## Token Structure

The JWT token contains:
- **Subject (sub)**: User email
- **userId**: User ID (extracted automatically)
- **role**: User role (PASSENGER, DRIVER, ADMIN)
- **roles**: Array of roles for Gateway compatibility
- **iat**: Issued at timestamp
- **exp**: Expiration timestamp

## Security Features

1. **Automatic User ID Extraction**: User ID is automatically extracted from JWT token
2. **Ownership Verification**: Ticket consumption endpoints verify that tickets belong to the authenticated user
3. **Token Validation**: All tokens are validated for expiration and signature
4. **Role-Based Access**: Some endpoints require specific roles (e.g., ADMIN)

## Error Responses

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

### 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "Access denied"
}
```

## Testing with cURL

```bash
# 1. Login
TOKEN=$(curl -X POST http://localhost:8082/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password"}' \
  | jq -r '.token')

# 2. Use protected endpoint
curl -X GET http://localhost:8082/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN"

# 3. Check ticket consumption
curl -X GET http://localhost:8082/api/v1/users/me/tickets/123/consumption \
  -H "Authorization: Bearer $TOKEN"
```

## Testing with Postman

1. Create a new request
2. Set method to `POST`
3. URL: `http://localhost:8082/api/v1/auth/login`
4. Body (raw JSON):
   ```json
   {
     "email": "user@example.com",
     "password": "password"
   }
   ```
5. Send request and copy the `token` from response
6. For protected endpoints:
   - Go to "Authorization" tab
   - Select "Bearer Token"
   - Paste the token
   - Send request

## Notes

- Tokens expire after 24 hours (configurable via `jwt.expiration`)
- Refresh tokens expire after 7 days (configurable via `jwt.refresh-expiration`)
- User ID is automatically extracted from the JWT token - no need to pass it manually
- All `/me` endpoints automatically use the authenticated user's ID
