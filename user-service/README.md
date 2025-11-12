# User Service API Endpoints & Testing Guide

## Base URL
```
http://localhost:3001
```

---

## 1. Health Check Endpoint

### Endpoint
```
GET /health
```

### Purpose
Check if the User Service is running and healthy.

### Request
```bash
curl http://localhost:3001/health
```

### Response
```json
{
  "status": "UP",
  "service": "user-service",
  "timestamp": "2025-11-12T14:58:59.635Z"
}
```

---

## 2. Register User Endpoint

### Endpoint
```
POST /api/v1/users
```

### Purpose
Create a new user account with preferences.

### Request Headers
```
Content-Type: application/json
```

### Request Body
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePass123",
  "push_token": "device-token-123",
  "preferences": {
    "email": true,
    "push": true
  }
}
```

### Fields Explanation
- `name` (string, required): User's full name
- `email` (string, required): Valid email address (must be unique)
- `password` (string, required): At least 8 characters (will be hashed)
- `push_token` (string, optional): Device token for push notifications
- `preferences.email` (boolean, required): Allow email notifications
- `preferences.push` (boolean, required): Allow push notifications

### Test Command
```bash
curl -X POST http://localhost:3001/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "push_token": "device-token-123",
    "preferences": {
      "email": true,
      "push": true
    }
  }'
```

### Response (Success)
```json
{
  "success": true,
  "data": {
    "id": "3afbe1f0-a574-4509-b828-1e218ca0ad42",
    "name": "John Doe",
    "email": "john@example.com",
    "push_token": "device-token-123",
    "password": "$2b$10$zOVykiY12hn66cLxW8pTjuYdPkY53LvjXnw.FJir8PXG6j.O5CXJC",
    "preferences": {
      "id": "68136e27-a934-4307-80c9-642de943c5c5",
      "email_notifications": true,
      "push_notifications": true,
      "created_at": "2025-11-12T14:00:59.796Z",
      "updated_at": "2025-11-12T14:00:59.796Z"
    },
    "created_at": "2025-11-12T14:00:59.796Z",
    "updated_at": "2025-11-12T14:00:59.796Z"
  },
  "message": "User created successfully",
  "meta": {}
}
```

### Response (Error - Email Already Exists)
```json
{
  "success": false,
  "error": "Email already registered",
  "message": "Failed to create user",
  "meta": {}
}
```

---

## 3. Login Endpoint

### Endpoint
```
POST /api/v1/auth/login
```

### Purpose
Authenticate user and receive JWT token.

### Request Headers
```
Content-Type: application/json
```

### Request Body
```json
{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

### Fields Explanation
- `email` (string, required): User's email
- `password` (string, required): User's password (will be verified against hashed password)

### Test Command
```bash
curl -X POST http://localhost:3001/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123"
  }'
```

### Response (Success)
```json
{
  "success": true,
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJjMWIwZDA1ZC1hZWI1LTQ1OGQtYjI5Yy04YTU3ZTdiM2Y3NWEiLCJlbWFpbCI6InRlc3RAZXhhbXBsZS5jb20iLCJuYW1lIjoiVGVzdCBVc2VyIiwiaWF0IjoxNzYyOTYxNzk1LCJleHAiOjE3NjMwNDgxOTV9.itS2vknD9RZm4_Lc91biyg3OXTgzmDxUdya6Mt5Uizk",
    "user": {
      "id": "c1b0d05d-aeb5-458d-b29c-8a57e7b3f75a",
      "name": "Test User",
      "email": "test@example.com",
      "push_token": "device-token-456",
      "preferences": {
        "id": "3e5b204b-9582-4a0a-a34f-6ec931cd3313",
        "email_notifications": true,
        "push_notifications": true,
        "created_at": "2025-11-12T14:36:21.937Z",
        "updated_at": "2025-11-12T14:36:21.937Z"
      },
      "created_at": "2025-11-12T14:36:21.937Z",
      "updated_at": "2025-11-12T14:36:21.937Z"
    }
  },
  "message": "Login successful",
  "meta": {}
}
```

### Response (Error - Wrong Password)
```json
{
  "success": false,
  "error": "Invalid credentials",
  "message": "Login failed",
  "meta": {}
}
```

### Response (Error - User Not Found)
```json
{
  "success": false,
  "error": "User with email invalid@example.com not found",
  "message": "Login failed",
  "meta": {}
}
```

---

## 4. Verify Token Endpoint

### Endpoint
```
POST /api/v1/auth/verify-token
```

### Purpose
Verify if a JWT token is valid (used by API Gateway).

### Request Headers
```
Content-Type: application/json
```

### Request Body
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJjMWIwZDA1ZC1hZWI1LTQ1OGQtYjI5Yy04YTU3ZTdiM2Y3NWEiLCJlbWFpbCI6InRlc3RAZXhhbXBsZS5jb20iLCJuYW1lIjoiVGVzdCBVc2VyIiwiaWF0IjoxNzYyOTYxNzk1LCJleHAiOjE3NjMwNDgxOTV9.itS2vknD9RZm4_Lc91biyg3OXTgzmDxUdya6Mt5Uizk"
}
```

### Test Command
```bash
curl -X POST http://localhost:3001/api/v1/auth/verify-token \
  -H "Content-Type: application/json" \
  -d '{
    "token": "YOUR_JWT_TOKEN_HERE"
  }'
```

### Response (Success)
```json
{
  "success": true,
  "data": {
    "sub": "c1b0d05d-aeb5-458d-b29c-8a57e7b3f75a",
    "email": "test@example.com",
    "name": "Test User",
    "iat": 1762961795,
    "exp": 1763048195
  },
  "message": "Token is valid",
  "meta": {}
}
```

### Response (Error - Invalid Token)
```json
{
  "success": false,
  "error": "Invalid token",
  "message": "Invalid token",
  "meta": {}
}
```

---

## 5. Get All Users Endpoint

### Endpoint
```
GET /api/v1/users?page=1&limit=10
```

### Purpose
Retrieve all users with pagination.

### Query Parameters
- `page` (number, optional, default=1): Page number
- `limit` (number, optional, default=10): Users per page

### Test Command
```bash
curl http://localhost:3001/api/v1/users
```

### Test Command with Pagination
```bash
curl http://localhost:3001/api/v1/users?page=1&limit=5
```

### Response
```json
{
  "success": true,
  "data": [
    {
      "id": "3afbe1f0-a574-4509-b828-1e218ca0ad42",
      "name": "John Doe",
      "email": "john@example.com",
      "push_token": "device-token-123",
      "preferences": {
        "id": "68136e27-a934-4307-80c9-642de943c5c5",
        "email_notifications": true,
        "push_notifications": true,
        "created_at": "2025-11-12T14:00:59.796Z",
        "updated_at": "2025-11-12T14:00:59.796Z"
      },
      "created_at": "2025-11-12T14:00:59.796Z",
      "updated_at": "2025-11-12T14:00:59.796Z"
    }
  ],
  "message": "Users retrieved successfully",
  "meta": {
    "total": 1,
    "limit": 10,
    "page": 1,
    "total_pages": 1,
    "has_next": false,
    "has_previous": false
  }
}
```

---

## 6. Get User by ID Endpoint

### Endpoint
```
GET /api/v1/users/:user_id
```

### Purpose
Retrieve a specific user by their ID.

### URL Parameters
- `user_id` (string, required): UUID of the user

### Test Command
```bash
curl http://localhost:3001/api/v1/users/3afbe1f0-a574-4509-b828-1e218ca0ad42
```

### Response (Success)
```json
{
  "success": true,
  "data": {
    "id": "3afbe1f0-a574-4509-b828-1e218ca0ad42",
    "name": "John Doe",
    "email": "john@example.com",
    "push_token": "device-token-123",
    "preferences": {
      "id": "68136e27-a934-4307-80c9-642de943c5c5",
      "email_notifications": true,
      "push_notifications": true,
      "created_at": "2025-11-12T14:00:59.796Z",
      "updated_at": "2025-11-12T14:00:59.796Z"
    },
    "created_at": "2025-11-12T14:00:59.796Z",
    "updated_at": "2025-11-12T14:00:59.796Z"
  },
  "message": "User retrieved successfully",
  "meta": {}
}
```

### Response (Error - User Not Found)
```json
{
  "success": false,
  "error": "User with ID invalid-id not found",
  "message": "Failed to retrieve user",
  "meta": {}
}
```

---

## 7. Update User Endpoint

### Endpoint
```
PUT /api/v1/users/:user_id
```

### Purpose
Update user information and/or preferences.

### URL Parameters
- `user_id` (string, required): UUID of the user

### Request Headers
```
Content-Type: application/json
```

### Request Body (All fields optional)
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "push_token": "new-device-token",
  "preferences": {
    "email": false,
    "push": true
  }
}
```

### Test Command
```bash
curl -X PUT http://localhost:3001/api/v1/users/3afbe1f0-a574-4509-b828-1e218ca0ad42 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Doe",
    "preferences": {
      "email": false,
      "push": true
    }
  }'
```

### Response
```json
{
  "success": true,
  "data": {
    "id": "3afbe1f0-a574-4509-b828-1e218ca0ad42",
    "name": "Jane Doe",
    "email": "john@example.com",
    "push_token": "device-token-123",
    "preferences": {
      "id": "68136e27-a934-4307-80c9-642de943c5c5",
      "email_notifications": false,
      "push_notifications": true,
      "created_at": "2025-11-12T14:00:59.796Z",
      "updated_at": "2025-11-12T14:01:58.417Z"
    },
    "created_at": "2025-11-12T14:00:59.796Z",
    "updated_at": "2025-11-12T14:01:58.417Z"
  },
  "message": "User updated successfully",
  "meta": {}
}
```

---

## 8. Update Push Token Endpoint

### Endpoint
```
PUT /api/v1/users/:user_id/push-token
```

### Purpose
Update user's push notification device token.

### URL Parameters
- `user_id` (string, required): UUID of the user

### Request Headers
```
Content-Type: application/json
```

### Request Body
```json
{
  "push_token": "new-device-token-456"
}
```

### Test Command
```bash
curl -X PUT http://localhost:3001/api/v1/users/3afbe1f0-a574-4509-b828-1e218ca0ad42/push-token \
  -H "Content-Type: application/json" \
  -d '{
    "push_token": "new-device-token-456"
  }'
```

### Response
```json
{
  "success": true,
  "data": {
    "id": "3afbe1f0-a574-4509-b828-1e218ca0ad42",
    "name": "Jane Doe",
    "email": "john@example.com",
    "push_token": "new-device-token-456",
    "preferences": {
      "id": "68136e27-a934-4307-80c9-642de943c5c5",
      "email_notifications": false,
      "push_notifications": true,
      "created_at": "2025-11-12T14:00:59.796Z",
      "updated_at": "2025-11-12T14:01:58.417Z"
    },
    "created_at": "2025-11-12T14:00:59.796Z",
    "updated_at": "2025-11-12T14:01:58.417Z"
  },
  "message": "Push token updated successfully",
  "meta": {}
}
```

---

## 9. Validate Password Endpoint

### Endpoint
```
POST /api/v1/users/validate
```

### Purpose
Validate user email and password combination (used for authentication).

### Request Headers
```
Content-Type: application/json
```

### Request Body
```json
{
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

### Test Command
```bash
curl -X POST http://localhost:3001/api/v1/users/validate \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123"
  }'
```

### Response (Success)
```json
{
  "success": true,
  "data": {
    "id": "3afbe1f0-a574-4509-b828-1e218ca0ad42",
    "name": "Jane Doe",
    "email": "john@example.com",
    "push_token": "device-token-123",
    "preferences": {
      "id": "68136e27-a934-4307-80c9-642de943c5c5",
      "email_notifications": false,
      "push_notifications": true,
      "created_at": "2025-11-12T14:00:59.796Z",
      "updated_at": "2025-11-12T14:01:58.417Z"
    },
    "created_at": "2025-11-12T14:00:59.796Z",
    "updated_at": "2025-11-12T14:01:58.417Z"
  },
  "message": "User validated successfully",
  "meta": {}
}
```

### Response (Error - Wrong Password)
```json
{
  "success": false,
  "error": "Invalid credentials",
  "message": "Validation failed",
  "meta": {}
}
```

---

## 10. Delete User Endpoint

### Endpoint
```
DELETE /api/v1/users/:user_id
```

### Purpose
Delete a user account and all associated data.

### URL Parameters
- `user_id` (string, required): UUID of the user

### Test Command
```bash
curl -X DELETE http://localhost:3001/api/v1/users/3afbe1f0-a574-4509-b828-1e218ca0ad42
```

### Response
```json
{
  "success": true,
  "data": {
    "message": "User deleted successfully"
  },
  "message": "User deleted successfully",
  "meta": {}
}
```

---

## Testing Workflow

### Complete Test Sequence

**1. Check Health**
```bash
curl http://localhost:3001/health
```

**2. Register a User**
```bash
curl -X POST http://localhost:3001/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "SecurePass123",
    "push_token": "device-token-123",
    "preferences": {
      "email": true,
      "push": true
    }
  }'
```
Save the `user_id` from response.

**3. Login**
```bash
curl -X POST http://localhost:3001/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123"
  }'
```
Save the `access_token` from response.

**4. Verify Token**
```bash
curl -X POST http://localhost:3001/api/v1/auth/verify-token \
  -H "Content-Type: application/json" \
  -d '{
    "token": "YOUR_ACCESS_TOKEN_HERE"
  }'
```

**5. Get User by ID** (Use user_id from step 2)
```bash
curl http://localhost:3001/api/v1/users/YOUR_USER_ID_HERE
```

**6. Update User**
```bash
curl -X PUT http://localhost:3001/api/v1/users/YOUR_USER_ID_HERE \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Doe",
    "preferences": {
      "email": false,
      "push": true
    }
  }'
```

**7. Get All Users**
```bash
curl http://localhost:3001/api/v1/users
```

**8. Update Push Token**
```bash
curl -X PUT http://localhost:3001/api/v1/users/YOUR_USER_ID_HERE/push-token \
  -H "Content-Type: application/json" \
  -d '{
    "push_token": "new-device-token-456"
  }'
```

**9. Validate Password**
```bash
curl -X POST http://localhost:3001/api/v1/users/validate \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "SecurePass123"
  }'
```

**10. Delete User** (Use user_id from step 2)
```bash
curl -X DELETE http://localhost:3001/api/v1/users/YOUR_USER_ID_HERE
```

---

## Response Format

All responses follow this format:

```json
{
  "success": boolean,
  "data": T (generic type),
  "error": string (optional, only on error),
  "message": string,
  "meta": {
    "total": number (only on list endpoints),
    "limit": number (only on list endpoints),
    "page": number (only on list endpoints),
    "total_pages": number (only on list endpoints),
    "has_next": boolean (only on list endpoints),
    "has_previous": boolean (only on list endpoints)
  }
}
```

---

## Environment Variables Required

In `.env` file:

```
NODE_ENV=development
PORT=3001
DB_HOST=localhost
DB_PORT=5432
DB_USERNAME=postgres
DB_PASSWORD=password
DB_NAME=notification_db
JWT_SECRET=your-secret-key-here
```

