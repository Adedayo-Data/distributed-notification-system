# **Distributed Notification System: A Microservices Approach**
<br>

## Overview
A robust and scalable distributed notification system designed to deliver timely and personalized communications across multiple channels. This project employs a microservices architecture, leveraging Node.js, Python, and Java, orchestrating various components for efficient message processing, templating, user management, and API gateway functionality.

## Key Features
*   ✨ **Polyglot Microservices:** Seamless integration of services built with Node.js (NestJS), Python (FastAPI), and Java (Spring Boot) for specialized tasks.
*   🚀 **Asynchronous Messaging:** Utilizes RabbitMQ for reliable, asynchronous message queuing to decouple services and ensure high throughput.
*   💾 **Persistent Data Storage:** Employs PostgreSQL for robust user and template data management.
*   ⚡ **High-Performance Caching:** Integrates Redis for caching, rate limiting, and temporary storage of notification statuses and idempotency keys.
*   🛡️ **Resilient API Gateway:** A central API gateway handles authentication (JWT), request routing, rate limiting, and implements a circuit breaker pattern for enhanced fault tolerance.
*   📧 **Multi-Channel Delivery:** Supports both email and push notifications, with dedicated services for each channel (SendGrid and Firebase Cloud Messaging).
*   🎨 **Dynamic Templating:** A dedicated template service allows for the creation and dynamic rendering of notification content with user-specific variables.
*   🔒 **Secure Authentication:** JWT-based authentication ensures secure access to user data and notification functionalities.
*   🐳 **Containerized Deployment:** Docker and Docker Compose enable easy setup, deployment, and scaling of all services.

## Getting Started

To get this distributed notification system up and running on your local machine, follow these steps.

### Installation

1.  ⬇️ **Clone the Repository**:
    ```bash
    git clone https://github.com/Adedayo-Data/distributed-notification-system
    cd distributed-notification-system
    ```

2.  ⚙️ **Prepare Environment Variables**:
    Create `.env` files for the `api-gateway` and `user-service` by copying their respective `.env.example` files and filling in the `JWT_SECRET` variable.
    
    For `api-gateway`:
    ```bash
    cp api-gateway/.env.example api-gateway/.env
    # Open api-gateway/.env and set JWT_SECRET to a strong, random string
    ```
    
    For `user-service`:
    ```bash
    # No .env.example for user-service, JWT_SECRET is set in docker-compose.yml but can be overridden
    # via environment variable. For local setup, the docker-compose.yml values are usually sufficient
    # or you can set it directly in your shell or global .env if you wish.
    # JWT_SECRET=your_super_secret_jwt_key
    ```
    
    Ensure you set a strong `JWT_SECRET` in your `docker-compose.yml` or global `.env` that is consistent across services relying on it (e.g., `api-gateway` and `user-service`). Also, `email-service` requires `FROM_EMAIL` and `SENDGRID_API_KEY` to be set as environment variables for actual email sending.

3.  🚀 **Start Services with Docker Compose**:
    Navigate to the root directory of the project and run:
    ```bash
    docker-compose up --build
    ```
    This command will build the Docker images for all services and start them along with RabbitMQ, Redis, and PostgreSQL. It might take a few minutes for all services to become fully operational.

### Environment Variables

The project utilizes several environment variables across its services for configuration. Below is a consolidated list of critical variables and their examples.

*   **Global (`docker-compose.yml` or global `.env`):**
    *   `JWT_SECRET`: `your-super-secret-jwt-key-change-in-production` (A strong, secret key for JWT signing across services.)

*   **API Gateway (`api-gateway/.env`):**
    *   `SERVICE_PORT`: `8000`
    *   `RABBITMQ_HOST`: `rabbitmq`
    *   `REDIS_HOST`: `redis`
    *   `JWT_SECRET`: `your-super-secret-jwt-key-change-in-production`
    *   `USER_SERVICE_URL`: `http://user-service:3001`
    *   `RATE_LIMIT_REQUESTS`: `100`
    *   `RATE_LIMIT_WINDOW`: `60`
    *   `CIRCUIT_BREAKER_FAIL_MAX`: `5`
    *   `CIRCUIT_BREAKER_TIMEOUT`: `60`
    *   `NOTIFICATION_STATUS_TTL`: `604800`
    *   `IDEMPOTENCY_TTL`: `86400`

*   **User Service (`docker-compose.yml` environment section):**
    *   `SERVICE_PORT`: `3001`
    *   `DB_HOST`: `postgres`
    *   `DB_USERNAME`: `admin`
    *   `DB_PASSWORD`: `password`
    *   `DB_DATABASE`: `user_db`
    *   `JWT_SECRET`: `your_super_secret_jwt_key`

*   **Template Service (`docker-compose.yml` environment section):**
    *   `SERVICE_PORT`: `8085`
    *   `SPRING_DATASOURCE_URL`: `jdbc:postgresql://postgres:5432/template_db`
    *   `SPRING_DATASOURCE_PASSWORD`: `password`

*   **Push Service (`docker-compose.yml` environment section, `application.properties`):**
    *   `SPRING_RABBITMQ_HOST`: `rabbitmq`
    *   `SPRING_REDIS_HOST`: `redis`
    *   `USER_SERVICE_URL`: `http://user-service:3001`
    *   `TEMPLATE_SERVICE_URL`: `http://template-service:8085`
    *   `STATUS_UPDATE_URL`: `http://api-gateway:8000/api/v1/push/status/`
    *   `app.firebase.config-path`: `classpath:firebase-service-account.json` (Requires `firebase-service-account.json` in `push-service/src/main/resources/`)

*   **Email Service (`docker-compose.yml` environment section):**
    *   `RABBITMQ_URL`: `amqp://guest:guest@rabbitmq:5672/`
    *   `REDIS_URL`: `redis://redis:6379/0`
    *   `USER_SERVICE_URL`: `http://user-service:3001`
    *   `TEMPLATE_SERVICE_URL`: `http://template-service:8085`
    *   `STATUS_UPDATE_URL`: `http://api-gateway:8000/api/v1/email/status/`
    *   `FROM_EMAIL`: `your-sender-email@example.com` (Environment variable for `email-service`)
    *   `SENDGRID_API_KEY`: `SG.YOUR_SENDGRID_API_KEY` (Environment variable for `email-service`)

## Usage

Once all services are running via `docker-compose up`, the API Gateway will be accessible on `http://localhost:8000`. This gateway serves as the single entry point for all client interactions with the notification system.

### 1. Register a User (via API Gateway)

First, you need to register a user. The API Gateway will proxy this request to the User Service.

**Request:**
```json
{
  "name": "Jane Doe",
  "email": "jane.doe@example.com",
  "password": "SecurePassword123",
  "push_token": "optional_fcm_device_token_here",
  "preferences": {
    "email": true,
    "push": true
  }
}
```

**Response (Success):**
```json
{
  "success": true,
  "data": {
    "id": "123e4567-e89b-12d3-a456-426614174001",
    "name": "Jane Doe",
    "email": "jane.doe@example.com",
    "push_token": "optional_fcm_device_token_here",
    "preferences": {
      "id": "987f6543-a21b-87c6-d543-210098765432",
      "email_notifications": true,
      "push_notifications": true,
      "created_at": "2023-10-26T10:00:00.000Z",
      "updated_at": "2023-10-26T10:00:00.000Z"
    },
    "created_at": "2023-10-26T10:00:00.000Z",
    "updated_at": "2023-10-26T10:00:00.000Z"
  },
  "message": "User created successfully",
  "meta": {}
}
```

### 2. Login User to Obtain JWT (via API Gateway)

After registration, log in to get an `access_token` required for other API calls.

**Request:**
```json
{
  "email": "jane.doe@example.com",
  "password": "SecurePassword123"
}
```

**Response (Success):**
```json
{
  "success": true,
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "123e4567-e89b-12d3-a456-426614174001",
      "name": "Jane Doe",
      "email": "jane.doe@example.com",
      "push_token": "optional_fcm_device_token_here",
      "preferences": {
        "id": "987f6543-a21b-87c6-d543-210098765432",
        "email_notifications": true,
        "push_notifications": true,
        "created_at": "2023-10-26T10:00:00.000Z",
        "updated_at": "2023-10-26T10:00:00.000Z"
      },
      "created_at": "2023-10-26T10:00:00.000Z",
      "updated_at": "2023-10-26T10:00:00.000Z"
    }
  },
  "message": "Login successful",
  "error": null,
  "meta": null
}
```

**Important:** Copy the `access_token` from the response. You will use it in the `Authorization` header for subsequent requests.

### 3. Create a Notification Template (via Template Service)

Before sending notifications, you need a template. This request goes directly to the Template Service.

**Request:**
```json
{
  "templateKey": "welcome_message",
  "subjectTemplate": "Welcome, {{name}}!",
  "bodyTemplate": "Hello {{name}},\n\nThank you for signing up! Here is your personalized link: {{link}}\n\nBest regards.",
  "type": "EMAIL",
  "version": 1
}
```

**Response (Success):**
```json
{
  "id": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
  "template_code": "welcome_message",
  "subjectTemplate": "Welcome, {{name}}!",
  "bodyTemplate": "Hello {{name}},\n\nThank you for signing up! Here is your personalized link: {{link}}\n\nBest regards.",
  "type": "EMAIL",
  "version": 1
}
```

### 4. Send a Notification (via API Gateway)

Now, you can send an email or push notification using the template created and the user ID obtained after registration. Use the `access_token` from login in the `Authorization: Bearer <token>` header.

**Request:**
```json
{
  "notification_type": "email",
  "user_id": "123e4567-e89b-12d3-a456-426614174001",
  "template_code": "welcome_message",
  "variables": {
    "name": "Jane Doe",
    "link": "https://your-app.com/verify/token123",
    "meta": {
      "source": "web_signup"
    }
  },
  "request_id": "unique-request-id-123456",
  "priority": 3,
  "metadata": {
    "campaign": "onboarding"
  }
}
```

**Response (Success, status 202 Accepted):**
```json
{
  "success": true,
  "data": {
    "notification_id": "876e5432-d10c-b9a8-7654-3210fedcba98",
    "status": "pending",
    "request_id": "unique-request-id-123456",
    "notification_type": "email"
  },
  "message": "Notification queued for processing",
  "error": null,
  "meta": null
}
```

**Important:** The `notification_id` in the response is crucial for checking the status of the sent notification.

### 5. Check Notification Status (via API Gateway)

Use the `notification_id` from the previous step to check the delivery status.

**Request (HTTP GET):**
`GET /api/v1/notifications/876e5432-d10c-b9a8-7654-3210fedcba98/status`
(Remember to include `Authorization: Bearer <token>` header)

**Response (Success):**
```json
{
  "success": true,
  "data": {
    "notification_id": "876e5432-d10c-b9a8-7654-3210fedcba98",
    "status": "delivered",
    "notification_type": "email",
    "created_at": "2023-10-26T10:05:00.000Z",
    "updated_at": "2023-10-26T10:05:15.000Z",
    "error_message": null,
    "retry_count": 0
  },
  "message": "Notification status retrieved",
  "error": null,
  "meta": null
}
```

### 6. List User Notifications (via API Gateway)

Retrieve a paginated list of all notifications associated with the authenticated user.

**Request (HTTP GET):**
`GET /api/v1/notifications?page=1&limit=10`
(Remember to include `Authorization: Bearer <token>` header)

**Response (Success):**
```json
{
  "success": true,
  "data": [
    {
      "notification_id": "876e5432-d10c-b9a8-7654-3210fedcba98",
      "status": "delivered",
      "notification_type": "email",
      "created_at": "2023-10-26T10:05:00.000Z",
      "user_id": "123e4567-e89b-12d3-a456-426614174001",
      "template_code": "welcome_message",
      "updated_at": "2023-10-26T10:05:15.000Z",
      "error_message": null
    }
    // ... other notifications
  ],
  "message": "Notifications retrieved",
  "error": null,
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

## Microservices Documentation

### API Gateway API

## Overview
A Python-based API Gateway built with FastAPI, acting as the central entry point for the distributed notification system. It handles request authentication, routing to appropriate microservices, rate limiting, and manages notification status with Redis.

## Features
- `FastAPI`: High-performance asynchronous web framework for routing.
- `JWT Authentication`: Validates JWT tokens issued by the User Service.
- `Redis`: Utilized for rate limiting, idempotency caching, and storing real-time notification statuses.
- `RabbitMQ`: Publishes notification requests to specific queues (`email.queue`, `push.queue`) for asynchronous processing.
- `Circuit Breaker`: Implements a fault tolerance mechanism to prevent cascading failures to downstream services.
- `Idempotency`: Prevents duplicate processing of requests using `request_id` and Redis caching.

## Getting Started
### Installation
Managed by the root `docker-compose.yml`. No separate manual installation steps are typically needed after cloning the repository.
```bash
# From the project root, ensure api-gateway/.env is configured
# docker-compose up --build api-gateway 
# (This command is for specific service, typically you'd run all with `docker-compose up --build`)
```
### Environment Variables
*   `SERVICE_NAME`: `api-gateway` (Name of the service)
*   `SERVICE_PORT`: `8000` (Port the service listens on)
*   `ENVIRONMENT`: `development` (Application environment, e.g., development, production)
*   `DEBUG`: `true` (Enable debug mode)
*   `RABBITMQ_HOST`: `rabbitmq` (Hostname for RabbitMQ connection)
*   `RABBITMQ_PORT`: `5672` (Port for RabbitMQ)
*   `RABBITMQ_USER`: `guest` (Username for RabbitMQ)
*   `RABBITMQ_PASS`: `guest` (Password for RabbitMQ)
*   `RABBITMQ_VHOST`: `/` (Virtual host for RabbitMQ)
*   `RABBITMQ_EXCHANGE`: `notifications.direct` (Main exchange name for notifications)
*   `RABBITMQ_EXCHANGE_TYPE`: `direct` (Type of RabbitMQ exchange)
*   `REDIS_HOST`: `redis` (Hostname for Redis connection)
*   `REDIS_PORT`: `6379` (Port for Redis)
*   `REDIS_DB`: `0` (Redis database index)
*   `REDIS_PASSWORD`: `None` (Password for Redis, if any)
*   `REDIS_DECODE_RESPONSES`: `true` (Decode Redis responses to Python strings)
*   `JWT_SECRET`: `your-super-secret-jwt-key-change-in-production-make-it-long-and-random` (Secret key for JWT verification)
*   `JWT_ALGORITHM`: `HS256` (Algorithm for JWT)
*   `JWT_EXPIRATION`: `3600` (JWT expiration time in seconds)
*   `RATE_LIMIT_REQUESTS`: `100` (Max requests allowed in the rate limit window)
*   `RATE_LIMIT_WINDOW`: `60` (Time window in seconds for rate limiting)
*   `CIRCUIT_BREAKER_FAIL_MAX`: `5` (Number of failures before circuit opens)
*   `CIRCUIT_BREAKER_TIMEOUT`: `60` (Duration in seconds for circuit to stay open)
*   `USER_SERVICE_URL`: `http://user-service:3001` (URL for the User Service)
*   `TEMPLATE_SERVICE_URL`: `http://template-service:8085` (URL for the Template Service)
*   `EMAIL_SERVICE_URL`: `http://email-service:8003` (URL for the Email Service)
*   `PUSH_SERVICE_URL`: `http://push-service:8084` (URL for the Push Service)
*   `NOTIFICATION_STATUS_TTL`: `604800` (Time-to-live for notification status in Redis in seconds)
*   `IDEMPOTENCY_TTL`: `86400` (Time-to-live for idempotency keys in Redis in seconds)
*   `LOG_LEVEL`: `INFO` (Logging level)

## API Documentation
### Base URL
`http://localhost:8000`

### Endpoints
#### POST /api/v1/auth/login
Authenticates a user by proxying credentials to the User Service and returns an access token.

**Request**:
```json
{
  "email": "string",
  "password": "string"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "access_token": "string",
    "token_type": "bearer",
    "expires_in": 3600
  },
  "error": null,
  "message": "Login successful",
  "meta": null
}
```

**Errors**:
- `401 Unauthorized`: Invalid credentials or token format.
- `500 Internal Server Error`: An unexpected error occurred.

#### POST /api/v1/auth/verify
Verifies the validity of a JWT access token.

**Request**:
Headers:
`Authorization: Bearer <JWT_TOKEN>`

**Response**:
```json
{
  "success": true,
  "data": {
    "valid": true,
    "user_id": "string"
  },
  "error": null,
  "message": "Token is valid",
  "meta": null
}
```

**Errors**:
- `401 Unauthorized`: Invalid authorization header, invalid token format, or expired/invalid token.

#### POST /api/v1/notifications/
Sends a notification via email or push, queuing it for asynchronous processing.

**Request**:
Headers:
`Authorization: Bearer <JWT_TOKEN>`
Body:
```json
{
  "notification_type": "email" | "push",
  "user_id": "uuid",
  "template_code": "string",
  "variables": {
    "name": "string",
    "link": "https://example.com/verify/token",
    "meta": {}
  },
  "request_id": "string",
  "priority": 1,
  "metadata": {}
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "notification_id": "string",
    "status": "pending",
    "request_id": "string",
    "notification_type": "email" | "push"
  },
  "error": null,
  "message": "Notification queued for processing",
  "meta": null
}
```

**Errors**:
- `400 Bad Request`: Invalid input (e.g., empty `template_code`).
- `401 Unauthorized`: Invalid authorization header or invalid/expired token.
- `429 Too Many Requests`: Rate limit exceeded for the authenticated user.
- `503 Service Unavailable`: Notification service temporarily unavailable (e.g., RabbitMQ unreachable).
- `500 Internal Server Error`: Failed to process notification request.

#### GET /api/v1/notifications/{notification_id}/status
Retrieves the current status of a specific notification.

**Request**:
Headers:
`Authorization: Bearer <JWT_TOKEN>`

**Response**:
```json
{
  "success": true,
  "data": {
    "notification_id": "string",
    "status": "pending" | "delivered" | "failed",
    "notification_type": "email" | "push",
    "created_at": "ISO 8601 datetime string",
    "updated_at": "ISO 8601 datetime string",
    "error_message": "string",
    "retry_count": 0
  },
  "error": null,
  "message": "Notification status retrieved",
  "meta": null
}
```

**Errors**:
- `401 Unauthorized`: Invalid authorization header or invalid/expired token.
- `404 Not Found`: Notification ID not found.
- `500 Internal Server Error`: Failed to retrieve status.

#### POST /api/v1/{notification_preference}/status/
Updates the status of a notification. This endpoint is primarily for internal service-to-service communication (e.g., Email Service, Push Service updating the status via Gateway).

**Request**:
`notification_preference` can be `email` or `push`.
Body:
```json
{
  "notification_id": "string",
  "status": "delivered" | "pending" | "failed",
  "timestamp": "ISO 8601 datetime string (optional)",
  "error": "string (optional)"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "notification_id": "string",
    "status": "string",
    "updated": true
  },
  "error": null,
  "message": "Notification status updated successfully",
  "meta": null
}
```

**Errors**:
- `400 Bad Request`: Invalid notification preference.
- `404 Not Found`: Notification ID not found.
- `500 Internal Server Error`: Failed to update notification status.

#### GET /api/v1/notifications
Lists all notifications for the authenticated user with pagination.

**Request**:
Headers:
`Authorization: Bearer <JWT_TOKEN>`
Query Parameters:
- `page`: `integer` (Default: 1)
- `limit`: `integer` (Default: 10)

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "notification_id": "string",
      "status": "string",
      "notification_type": "string",
      "created_at": "string",
      "updated_at": "string",
      "error_message": "string",
      "retry_count": 0
    }
  ],
  "error": null,
  "message": "Notifications retrieved",
  "meta": {
    "total": 100,
    "limit": 10,
    "page": 1,
    "total_pages": 10,
    "has_next": true,
    "has_previous": false
  }
}
```

**Errors**:
- `401 Unauthorized`: Invalid authorization header or invalid/expired token.
- `500 Internal Server Error`: Failed to retrieve notifications.

#### GET /health
Performs a health check of the API Gateway and its critical dependencies (RabbitMQ, Redis).

**Request**:
None.

**Response**:
```json
{
  "status": "healthy" | "degraded",
  "service": "api-gateway",
  "timestamp": "ISO 8601 datetime string",
  "checks": {
    "rabbitmq": true,
    "redis": true,
    "service": "up"
  },
  "version": "1.0.0"
}
```

**Errors**:
- `500 Internal Server Error`: If an unexpected error occurs during health check.

<br>

### User Service API

## Overview
A Node.js microservice built with NestJS and TypeORM, responsible for managing user accounts, authentication (login, password hashing, JWT generation), and user preferences for notifications. It interacts with a PostgreSQL database.

## Features
- `NestJS`: A progressive Node.js framework for building efficient and scalable server-side applications.
- `TypeORM`: An ORM for PostgreSQL database interaction, supporting entities and repositories.
- `PostgreSQL`: Relational database for storing user profiles and preferences.
- `bcryptjs`: Secure hashing for user passwords.
- `JWT`: Generates JSON Web Tokens for user authentication.
- `User Management`: Provides endpoints for creating, retrieving, updating, and deleting user accounts.
- `User Preferences`: Manages user-specific notification preferences (email, push).

## Getting Started
### Installation
Managed by the root `docker-compose.yml`.
```bash
# From the project root, ensure user-service/.env is configured (or environment variables are passed)
# docker-compose up --build user-service
# (This command is for specific service, typically you'd run all with `docker-compose up --build`)
```
### Environment Variables
*   `SERVICE_PORT`: `3001` (Port the service listens on)
*   `DB_HOST`: `postgres` (PostgreSQL database host)
*   `DB_USERNAME`: `admin` (PostgreSQL username)
*   `DB_PASSWORD`: `password` (PostgreSQL password)
*   `DB_DATABASE`: `user_db` (PostgreSQL database name)
*   `JWT_SECRET`: `your_secret_key` (Secret key for JWT signing)
*   `NODE_ENV`: `development` (Application environment; affects database synchronization and logging)

## API Documentation
### Base URL
`http://localhost:3001`

### Endpoints
#### POST /api/v1/auth/login
Authenticates a user and issues a JWT access token.

**Request**:
```json
{
  "email": "string",
  "password": "string"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "access_token": "string",
    "user": {
      "id": "uuid",
      "name": "string",
      "email": "string",
      "push_token": "string | null",
      "preferences": {
        "id": "uuid",
        "email_notifications": true,
        "push_notifications": true,
        "created_at": "ISO 8601 datetime string",
        "updated_at": "ISO 8601 datetime string"
      },
      "created_at": "ISO 8601 datetime string",
      "updated_at": "ISO 8601 datetime string"
    }
  },
  "message": "Login successful",
  "meta": {}
}
```

**Errors**:
- `400 Bad Request`: Invalid credentials.
- `500 Internal Server Error`: An unexpected error occurred during login.

#### POST /api/v1/auth/verify-token
Verifies a JWT token and returns its payload.

**Request**:
```json
{
  "token": "string"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "sub": "uuid",
    "email": "string",
    "name": "string",
    "iat": 1678886400,
    "exp": 1678972800
  },
  "message": "Token is valid",
  "meta": {}
}
```

**Errors**:
- `400 Bad Request`: Invalid token.
- `500 Internal Server Error`: An unexpected error occurred during token verification.

#### POST /api/v1/users
Registers a new user with their details and notification preferences.

**Request**:
```json
{
  "name": "string",
  "email": "string",
  "password": "string",
  "push_token": "string (optional)",
  "preferences": {
    "email": true,
    "push": true
  }
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "name": "string",
    "email": "string",
    "push_token": "string | null",
    "preferences": {
      "id": "uuid",
      "email_notifications": true,
      "push_notifications": true,
      "created_at": "ISO 8601 datetime string",
      "updated_at": "ISO 8601 datetime string"
    },
    "created_at": "ISO 8601 datetime string",
    "updated_at": "ISO 8601 datetime string"
  },
  "message": "User created successfully",
  "meta": {}
}
```

**Errors**:
- `400 Bad Request`: Email already registered, or invalid input.
- `500 Internal Server Error`: Failed to create user.

#### GET /api/v1/users
Retrieves a paginated list of all registered users.

**Request**:
Query Parameters:
- `page`: `number` (Default: 1)
- `limit`: `number` (Default: 10)

**Response**:
```json
{
  "success": true,
  "data": [
    {
      "id": "uuid",
      "name": "string",
      "email": "string",
      "push_token": "string | null",
      "preferences": {
        "id": "uuid",
        "email_notifications": true,
        "push_notifications": true,
        "created_at": "ISO 8601 datetime string",
        "updated_at": "ISO 8601 datetime string"
      },
      "created_at": "ISO 8601 datetime string",
      "updated_at": "ISO 8601 datetime string"
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

**Errors**:
- `500 Internal Server Error`: Failed to retrieve users.

#### GET /api/v1/users/:user_id
Retrieves a specific user by their ID.

**Request**:
Path Parameter:
- `user_id`: `string` (UUID of the user)

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "name": "string",
    "email": "string",
    "push_token": "string | null",
    "preferences": {
      "id": "uuid",
      "email_notifications": true,
      "push_notifications": true,
      "created_at": "ISO 8601 datetime string",
      "updated_at": "ISO 8601 datetime string"
    },
    "created_at": "ISO 8601 datetime string",
    "updated_at": "ISO 8601 datetime string"
  },
  "message": "User retrieved successfully",
  "meta": {}
}
```

**Errors**:
- `404 Not Found`: User with the specified ID not found.
- `500 Internal Server Error`: Failed to retrieve user.

#### PUT /api/v1/users/:user_id
Updates an existing user's details or preferences.

**Request**:
Path Parameter:
- `user_id`: `string` (UUID of the user)
Body:
```json
{
  "name": "string (optional)",
  "email": "string (optional)",
  "push_token": "string (optional)",
  "preferences": {
    "email": true,
    "push": true
  } (optional)
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "name": "string",
    "email": "string",
    "push_token": "string | null",
    "preferences": {
      "id": "uuid",
      "email_notifications": true,
      "push_notifications": true,
      "created_at": "ISO 8601 datetime string",
      "updated_at": "ISO 8601 datetime string"
    },
    "created_at": "ISO 8601 datetime string",
    "updated_at": "ISO 8601 datetime string"
  },
  "message": "User updated successfully",
  "meta": {}
}
```

**Errors**:
- `400 Bad Request`: Email already in use, or invalid input.
- `404 Not Found`: User with the specified ID not found.
- `500 Internal Server Error`: Failed to update user.

#### PUT /api/v1/users/:user_id/push-token
Updates the push notification token for a specific user.

**Request**:
Path Parameter:
- `user_id`: `string` (UUID of the user)
Body:
```json
{
  "push_token": "string"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "name": "string",
    "email": "string",
    "push_token": "string | null",
    "preferences": {
      "id": "uuid",
      "email_notifications": true,
      "push_notifications": true,
      "created_at": "ISO 8601 datetime string",
      "updated_at": "ISO 8601 datetime string"
    },
    "created_at": "ISO 8601 datetime string",
    "updated_at": "ISO 8601 datetime string"
  },
  "message": "Push token updated successfully",
  "meta": {}
}
```

**Errors**:
- `404 Not Found`: User with the specified ID not found.
- `500 Internal Server Error`: Failed to update push token.

#### POST /api/v1/users/validate
Validates a user's password against their email.

**Request**:
Body:
```json
{
  "email": "string",
  "password": "string"
}
```

**Response**:
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "name": "string",
    "email": "string",
    "push_token": "string | null",
    "preferences": {
      "id": "uuid",
      "email_notifications": true,
      "push_notifications": true,
      "created_at": "ISO 8601 datetime string",
      "updated_at": "ISO 8601 datetime string"
    },
    "created_at": "ISO 8601 datetime string",
    "updated_at": "ISO 8601 datetime string"
  },
  "message": "User validated successfully",
  "meta": {}
}
```

**Errors**:
- `400 Bad Request`: Invalid credentials.
- `404 Not Found`: User with the specified email not found.
- `500 Internal Server Error`: Validation failed.

#### DELETE /api/v1/users/:user_id
Deletes a user account.

**Request**:
Path Parameter:
- `user_id`: `string` (UUID of the user)

**Response**:
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

**Errors**:
- `404 Not Found`: User with the specified ID not found.
- `500 Internal Server Error`: Failed to delete user.

#### GET /health
Checks the health status of the User Service.

**Request**:
None.

**Response**:
```json
{
  "status": "UP",
  "service": "user-service",
  "timestamp": "ISO 8601 datetime string"
}
```

**Errors**:
- `500 Internal Server Error`: If an unexpected error occurs during health check.

<br>

### Email Service API

## Overview
A Python microservice built with FastAPI, responsible for consuming email notification messages from RabbitMQ, rendering templates with the Template Service, and sending emails via SendGrid. It also updates the notification status back to the API Gateway.

## Features
- `FastAPI`: Provides a minimal API for health checks and status retrieval.
- `aio_pika`: Asynchronously consumes messages from RabbitMQ's `email.queue`.
- `SendGrid`: External service for sending emails.
- `Redis`: Stores temporary email notification statuses.
- `Template Service Integration`: Fetches and renders email templates dynamically.
- `Status Reporting`: Updates notification status (`sent`, `failed`, `skipped`) to the API Gateway.
- `Dead Letter Queue (DLQ)`: Unprocessed or failed messages are routed to a DLQ for later inspection and retry.

## Getting Started
### Installation
Managed by the root `docker-compose.yml`.
```bash
# From the project root
# docker-compose up --build email-service
# (This command is for specific service, typically you'd run all with `docker-compose up --build`)
```
### Environment Variables
*   `RABBITMQ_URL`: `amqp://guest:guest@rabbitmq:5672/` (RabbitMQ connection URL)
*   `REDIS_URL`: `redis://redis:6379/0` (Redis connection URL)
*   `USER_SERVICE_URL`: `http://user-service:3001` (URL for the User Service, though not directly used for email sending, present in the context)
*   `TEMPLATE_SERVICE_URL`: `http://template-service:8085` (URL for the Template Service)
*   `STATUS_UPDATE_URL`: `http://api-gateway:8000/api/v1/email/status/` (URL for reporting status to API Gateway)
*   `FROM_EMAIL`: `your-sender-email@example.com` (Sender email address for SendGrid)
*   `SENDGRID_API_KEY`: `SG.YOUR_SENDGRID_API_KEY` (API key for SendGrid)

## API Documentation
### Base URL
`http://localhost:8003`

### Endpoints
#### GET /health
Checks the health status of the Email Service.

**Request**:
None.

**Response**:
```json
{
  "status": "ok"
}
```

**Errors**:
- `500 Internal Server Error`: If an unexpected error occurs during health check.

#### GET /status/{notification_id}
Retrieves the current status of an email notification by its ID.

**Request**:
Path Parameter:
- `notification_id`: `string` (Unique identifier for the email notification)

**Response**:
```json
{
  "notification_id": "string",
  "status": "string"
}
```

**Errors**:
- `500 Internal Server Error`: If an unexpected error occurs during status retrieval.

<br>

### Push Service API

## Overview
A Java microservice built with Spring Boot, responsible for consuming push notification messages from RabbitMQ, fetching user push tokens and preferences from the User Service, rendering templates with the Template Service, and sending push notifications via Firebase Cloud Messaging (FCM). It reports status updates back to the API Gateway.

## Features
- `Spring Boot`: Framework for building robust Java applications.
- `RabbitMQ`: Consumes messages from the `push.queue` for asynchronous processing.
- `Redis`: Stores temporary push notification statuses and prevents duplicate processing.
- `Firebase Cloud Messaging (FCM)`: Sends push notifications to mobile devices.
- `User Service Integration`: Fetches user's push tokens and notification preferences.
- `Template Service Integration`: Renders push notification content (title, body, image, action link) dynamically.
- `Status Reporting`: Updates notification status (`delivered`, `failed`, `skipped`) to the API Gateway.

## Getting Started
### Installation
Managed by the root `docker-compose.yml`.
```bash
# From the project root, ensure firebase-service-account.json is in push-service/src/main/resources/
# docker-compose up --build push-service
# (This command is for specific service, typically you'd run all with `docker-compose up --build`)
```
### Environment Variables
*   `SERVICE_PORT`: `8084` (Port the service listens on, internally used by other services)
*   `SPRING_RABBITMQ_HOST`: `rabbitmq` (RabbitMQ host for Spring Boot)
*   `SPRING_REDIS_HOST`: `redis` (Redis host for Spring Boot)
*   `USER_SERVICE_URL`: `http://user-service:3001` (URL for the User Service)
*   `TEMPLATE_SERVICE_URL`: `http://template-service:8085` (URL for the Template Service)
*   `STATUS_UPDATE_URL`: `http://api-gateway:8000/api/v1/push/status/` (URL for reporting status to API Gateway)
*   `app.firebase.config-path`: `classpath:firebase-service-account.json` (Path to Firebase service account key)

## API Documentation
### Base URL
`http://localhost:8084`

### Endpoints
#### GET /health
Checks the health status of the Push Service.

**Request**:
None.

**Response**:
```json
{
  "Status": "UP",
  "Service": "push-service"
}
```

**Errors**:
- `500 Internal Server Error`: If an unexpected error occurs during health check.

<br>

### Template Service API

## Overview
A Java microservice built with Spring Boot and Spring Data JPA, responsible for managing notification templates and rendering their content dynamically using provided variables. It persists templates in a PostgreSQL database.

## Features
- `Spring Boot`: Framework for building Java-based microservices.
- `Spring Data JPA`: Simplifies database access and persistence for PostgreSQL.
- `PostgreSQL`: Stores `NotificationTemplate` entities.
- `Template Management`: Provides API for creating, retrieving, and updating notification templates.
- `Template Rendering`: Dynamically replaces placeholders in subject and body templates with actual variable values.
- `NotificationType`: Supports different template types (e.g., EMAIL, PUSH) to ensure proper rendering for each channel.

## Getting Started
### Installation
Managed by the root `docker-compose.yml`.
```bash
# From the project root
# docker-compose up --build template-service
# (This command is for specific service, typically you'd run all with `docker-compose up --build`)
```
### Environment Variables
*   `SERVICE_PORT`: `8085` (Port the service listens on)
*   `SPRING_DATASOURCE_URL`: `jdbc:postgresql://postgres:5432/template_db` (JDBC URL for PostgreSQL)
*   `SPRING_DATASOURCE_USERNAME`: `admin` (PostgreSQL username)
*   `SPRING_DATASOURCE_PASSWORD`: `password` (PostgreSQL password)
*   `SPRING_JPA_HIBERNATE_DDL_AUTO`: `update` (Hibernate DDL generation strategy)
*   `SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT`: `org.hibernate.dialect.PostgreSQLDialect` (Hibernate dialect for PostgreSQL)

## API Documentation
### Base URL
`http://localhost:8085`

### Endpoints
#### POST /api/v1/templates
Creates a new notification template.

**Request**:
```json
{
  "templateKey": "string",
  "subjectTemplate": "string",
  "bodyTemplate": "string",
  "type": "EMAIL" | "PUSH",
  "version": 1
}
```

**Response**:
```json
{
  "id": "uuid",
  "template_code": "string",
  "subjectTemplate": "string",
  "bodyTemplate": "string",
  "type": "EMAIL",
  "version": 1
}
```

**Errors**:
- `500 Internal Server Error`: Failed to create template.

#### GET /api/v1/templates/{templateKey}
Retrieves a notification template by its unique key.

**Request**:
Path Parameter:
- `templateKey`: `string` (Unique identifier for the template)

**Response**:
```json
{
  "id": "uuid",
  "template_code": "string",
  "subjectTemplate": "string",
  "bodyTemplate": "string",
  "type": "EMAIL" | "PUSH",
  "version": 1
}
```

**Errors**:
- `500 Internal Server Error`: Failed to retrieve template or template key not applicable.

#### POST /api/v1/templates/render
Renders a template by replacing placeholders with provided variables.

**Request**:
```json
{
  "notificationType": "EMAIL" | "PUSH",
  "templateKey": "string",
  "variables": {
    "key1": "value1",
    "key2": "value2"
  }
}
```

**Response**:
```json
{
  "success": true,
  "message": "Template rendered successfully",
  "data": {
    "renderedSubject": "string",
    "renderedBody": "string"
  },
  "error": null,
  "meta": null
}
```

**Errors**:
- `400 Bad Request`: Template rendering failed (e.g., template not found for key/type, missing variables).

#### GET /health
Checks the health status of the Template Service.

**Request**:
None.

**Response**:
```json
{
  "Status": "UP",
  "Service": "template-service"
}
```

**Errors**:
- `500 Internal Server Error`: If an unexpected error occurs during health check.

<br>

## Technologies Used

| Category        | Technology                                                                                                  | Description                                                                    |
| :-------------- | :---------------------------------------------------------------------------------------------------------- | :----------------------------------------------------------------------------- |
| **Languages**   | [Node.js](https://nodejs.org/en/) (TypeScript), [Python](https://www.python.org/), [Java](https://www.java.com/en/) | Core programming languages for microservices.                                  |
| **Frameworks**  | [NestJS](https://nestjs.com/), [FastAPI](https://fastapi.tiangolo.com/), [Spring Boot](https://spring.io/projects/spring-boot) | Robust frameworks for building scalable backend services.                      |
| **Databases**   | [PostgreSQL](https://www.postgresql.org/), [Redis](https://redis.io/)                                       | Relational database for persistent storage and in-memory data store/cache.     |
| **Messaging**   | [RabbitMQ](https://www.rabbitmq.com/)                                                                       | Message broker for asynchronous communication between services.                |
| **Authentication**| [JWT](https://jwt.io/)                                                                                      | JSON Web Tokens for secure API authentication.                                 |
| **Containerization**| [Docker](https://www.docker.com/), [Docker Compose](https://docs.docker.com/compose/)                       | For containerizing services and orchestrating multi-container applications.    |
| **Email Service**| [SendGrid](https://sendgrid.com/)                                                                           | Third-party service for sending emails.                                        |
| **Push Notifications**| [Firebase Cloud Messaging (FCM)](https://firebase.google.com/docs/cloud-messaging)                         | Cross-platform messaging solution for sending push notifications.              |
| **ORM/ODM**     | [TypeORM](https://typeorm.io/) (for NestJS), [Spring Data JPA](https://spring.io/projects/spring-data-jpa) (for Spring Boot) | Object-Relational Mapping for database interactions.                           |

## Author Info
*   **LinkedIn**: [Your LinkedIn Profile](https://www.linkedin.com/in/your_username)
*   **Twitter**: [Your Twitter Profile](https://twitter.com/your_username)
*   **Portfolio**: [Your Portfolio Site](https://yourportfolio.com)

---

[![Node.js](https://img.shields.io/badge/Node.js-339933?style=for-the-badge&logo=nodedotjs&logoColor=white)](https://nodejs.org/)
[![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)](https://www.python.org/)
[![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)
[![NestJS](https://img.shields.io/badge/NestJS-E0234E?style=for-the-badge&logo=nestjs&logoColor=white)](https://nestjs.com/)
[![FastAPI](https://img.shields.io/badge/FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)](https://www.rabbitmq.com/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

[![Readme was generated by Dokugen](https://img.shields.io/badge/Readme%20was%20generated%20by-Dokugen-brightgreen)](https://www.npmjs.com/package/dokugen)