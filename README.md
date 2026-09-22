# FlowerShop Backend

REST API for the FlowerShop e-commerce platform — product catalog, shopping cart, orders, coupons, wishlists, admin analytics, and an AI-powered shopping assistant.

Built with **Spring Boot 3.4**, **Java 21**, **PostgreSQL**, and **Redis**.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Docker](#docker)
- [API Overview](#api-overview)
- [Authentication](#authentication)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Troubleshooting](#troubleshooting)
- [Related Projects](#related-projects)

---

## Features

| Area | Capabilities |
|------|--------------|
| **Auth** | Email/password registration with OTP verification, JWT access + refresh tokens, password reset, Google Sign-In |
| **Catalog** | Paginated product listing with filters (category, price, search), category CRUD (admin) |
| **Cart** | Redis-backed session cart — add, update quantity, remove, clear |
| **Orders** | Checkout from cart, order history, user cancellation, admin status updates (`PENDING` → `CONFIRMED` → `DELIVERED`) |
| **Coupons** | Percentage/fixed discounts, validation at checkout, admin management |
| **Wishlist** | Save products, toggle, move items to cart |
| **Profile** | View/update user profile with optional avatar upload |
| **Analytics** | Admin dashboard metrics — revenue summary, customer insights |
| **Chatbot** | Spring AI assistant (OpenRouter) with product-aware context and Redis session memory |
| **Seeding** | Auto-creates admin user on startup; seeds 3 categories + 200 sample products when the catalog is empty |

---

## Tech Stack

- **Runtime:** Java 21 (with `--enable-preview`)
- **Framework:** Spring Boot 3.4.6 — Web, Security, Data JPA, Validation, Mail, Thymeleaf
- **Database:** PostgreSQL 17
- **Cache / sessions:** Redis 7+ (carts & chatbot history)
- **Auth:** JWT (jjwt), BCrypt, Google ID token verification
- **AI:** Spring AI → OpenRouter (free-tier models)
- **Docs:** SpringDoc OpenAPI (Swagger UI)
- **Build:** Maven (wrapper included)
- **Container:** Docker multi-stage build + Docker Compose

---

## Prerequisites

| Tool | Version | Notes |
|------|---------|-------|
| Java JDK | 21 | Enable preview features in your IDE |
| Maven | 3.9+ | Or use `./mvnw` / `mvnw.cmd` |
| PostgreSQL | 14+ | Default local DB: `flower_shop` on port `5432` |
| Redis | 6+ | **Required** for local dev — must run on `localhost:6379` |
| Docker & Compose | Optional | For containerized DB + backend + pgAdmin |

---

## Quick Start

### 1. Clone and enter the backend directory

```bash
git clone <repository-url>
cd flowershop
```

### 2. Configure environment

```bash
cp .env.example .env
```

Edit `.env` and set at minimum:

```env
ADMIN_EMAIL=admin@example.com
ADMIN_PASSWORD=your-secure-password
```

`ADMIN_EMAIL` / `ADMIN_PASSWORD` are required — `AdminInitializer` seeds the admin account on first startup.

### 3. Set up PostgreSQL

Create the database (if it does not exist):

```sql
CREATE DATABASE flower_shop;
```

Update connection settings in `src/main/resources/application.properties` to match your local Postgres credentials.

> **Note:** `application.properties` is gitignored. Copy or create it locally before running. See [Configuration](#configuration) for the full list of properties.

### 4. Start Redis

```bash
# Example with Docker
docker run -d --name flowershop-redis -p 6379:6379 redis:7-alpine
```

Redis is **not** included in `docker-compose.yml`. You must run it separately for local development.

### 5. Run the API

**Windows (PowerShell):**

```powershell
.\mvnw.cmd spring-boot:run
```

**macOS / Linux:**

```bash
./mvnw spring-boot:run
```

The API is available at:

```
http://localhost:8080/api
```

Swagger UI (interactive API docs):

```
http://localhost:8080/api/swagger-ui/index.html
```

---

## Configuration

Environment variables are loaded automatically from `.env` in the project root via [spring-dotenv](https://github.com/paulschwarz/spring-dotenv).

### Required variables

| Variable | Description |
|----------|-------------|
| `ADMIN_EMAIL` | Email for the seeded admin account |
| `ADMIN_PASSWORD` | Password for the seeded admin account |

### Optional variables

| Variable | Description | Default |
|----------|-------------|---------|
| `GOOGLE_CLIENT_ID` | Google OAuth client ID for Sign-In | — |
| `OPENROUTER_API_KEY` | OpenRouter API key for the chatbot | `no-api-key-set` |
| `UPLOAD_DIR` | Directory for product image uploads | Path to frontend `public/uploads` |

### Docker Compose variables (`.env`)

| Variable | Description |
|----------|-------------|
| `POSTGRES_USER` | PostgreSQL username |
| `POSTGRES_PASSWORD` | PostgreSQL password |
| `POSTGRES_DB` | Database name |
| `DB_PORT` | Host port mapped to Postgres (default `5432`) |
| `APP_PORT` | Host port mapped to the backend (default `8080`) |
| `PGADMIN_EMAIL` | pgAdmin login email |
| `PGADMIN_PASSWORD` | pgAdmin login password |
| `PGADMIN_PORT` | Host port for pgAdmin (e.g. `5050`) |

### Key `application.properties` settings

| Property | Purpose |
|----------|---------|
| `server.servlet.context-path=/api` | All routes are prefixed with `/api` |
| `spring.datasource.*` | PostgreSQL connection |
| `spring.redis.host` / `port` | Redis connection (`localhost:6379`) |
| `app.jwt.secret` | JWT signing secret — **change in production** |
| `app.jwt.expiration` | Access token TTL (ms) — default 15 minutes |
| `app.refreshToken.expiration` | Refresh token TTL (ms) — default 10 days |
| `product.image.uploadDir` | Where uploaded product images are stored |
| `spring.mail.*` | SMTP settings for OTP and password-reset emails |
| `cors.allowed-origins` | Allowed frontend origins (default `http://localhost:5173`) |

For production, override secrets and connection strings via environment variables or a secure secrets manager — never commit credentials.

---

## Running the Application

### Development

```powershell
# Windows
.\mvnw.cmd spring-boot:run

# Run a single test class
.\mvnw.cmd test -Dtest=CouponServiceTest
```

```bash
# macOS / Linux
./mvnw spring-boot:run
./mvnw test -Dtest=CouponServiceTest
```

### Production build

```powershell
.\mvnw.cmd clean package -DskipTests
java --enable-preview -jar target/flowershop-0.0.1-SNAPSHOT.jar
```

> `--enable-preview` is required at runtime because the project compiles with Java 21 preview features.

---

## Docker

Run PostgreSQL, the backend, and pgAdmin together:

```bash
cp .env.example .env   # fill in all values
docker compose up --build
```

| Service | URL |
|---------|-----|
| API | `http://localhost:${APP_PORT}/api` |
| pgAdmin | `http://localhost:${PGADMIN_PORT}` |

The backend container mounts `../flower-shop-frontend/public/uploads` so product images are shared with the frontend static server.

**Redis is not part of the Compose stack.** For cart and chatbot features in Docker, add a Redis service or point `spring.redis.host` to an external instance.

---

## API Overview

Base URL: `http://localhost:8080/api`

### Public endpoints (no auth)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/auth/register` | Register a new user (sends OTP email) |
| `POST` | `/auth/verify-otp` | Verify registration OTP |
| `POST` | `/auth/resend-otp` | Resend OTP |
| `POST` | `/auth/login` | Login — returns access + refresh tokens |
| `POST` | `/auth/google` | Google Sign-In |
| `POST` | `/auth/forgot-password` | Request password reset |
| `POST` | `/auth/verify-reset-token` | Validate reset token |
| `POST` | `/auth/reset-password` | Set new password |
| `POST` | `/auth/refresh` | Refresh access token |
| `GET` | `/products` | List products (pagination + filters) |
| `GET` | `/products/{id}` | Product detail |
| `GET` | `/categories` | List categories |
| `GET` | `/categories/{id}` | Category detail |
| `GET` | `/coupons` | List active coupons |

### Authenticated endpoints (Bearer JWT)

| Method | Path | Role | Description |
|--------|------|------|-------------|
| `POST` | `/auth/logout` | Any | Invalidate refresh token |
| `GET` | `/carts` | User | Get cart |
| `POST` | `/carts/add` | User | Add item to cart |
| `PUT` | `/carts/update` | User | Update item quantity |
| `DELETE` | `/carts/remove/{productId}` | User | Remove cart item |
| `DELETE` | `/carts/clear` | User | Clear cart |
| `POST` | `/orders` | User | Place order |
| `GET` | `/orders/my` | User | My order history |
| `PUT` | `/orders/{id}/cancel` | User | Cancel own order |
| `POST` | `/coupons/apply` | User | Validate coupon for subtotal |
| `GET` | `/wishlists` | User | Get wishlist |
| `POST` | `/wishlists/{productId}` | User | Add to wishlist |
| `DELETE` | `/wishlists/{productId}` | User | Remove from wishlist |
| `POST` | `/wishlists/{productId}/toggle` | User | Toggle wishlist item |
| `GET` | `/wishlists/{productId}/is-wishlisted` | User | Check wishlist status |
| `POST` | `/wishlists/move-to-cart/{wishlistId}` | User | Move wishlist item to cart |
| `DELETE` | `/wishlists/clear` | User | Clear wishlist |
| `GET` | `/users/me` | User | Get profile |
| `PUT` | `/users/me` | User | Update profile (multipart) |
| `POST` | `/chat` | User | Send chatbot message |
| `GET` | `/chat/history` | User | Get chat history |
| `DELETE` | `/chat/history` | User | Clear chat history |

### Admin-only endpoints (`ROLE_ADMIN`)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/products` | Create product (multipart) |
| `PUT` | `/products/{id}` | Update product |
| `DELETE` | `/products/{id}` | Delete product |
| `POST` | `/categories` | Create category |
| `PUT` | `/categories/{id}` | Update category |
| `DELETE` | `/categories/{id}` | Delete category |
| `GET` | `/orders` | List all orders |
| `PUT` | `/orders/{id}/status` | Update order status |
| `POST` | `/coupons` | Create coupon |
| `PUT` | `/coupons/{id}` | Update coupon |
| `DELETE` | `/coupons/{id}` | Delete coupon |
| `GET` | `/admin/analytics/summary` | Revenue & order analytics |
| `GET` | `/admin/analytics/customers` | Customer analytics |

For full request/response schemas, use **Swagger UI** at `/api/swagger-ui/index.html`.

---

## Authentication

- **Stateless JWT** — no server-side sessions.
- Send the access token in the `Authorization` header:

  ```
  Authorization: Bearer <access_token>
  ```

- Access tokens expire after ~15 minutes. Use `POST /auth/refresh` with the refresh token to obtain a new pair.
- Roles: `ROLE_USER` (customers) and `ROLE_ADMIN` (dashboard access).
- Public routes: `/auth/**` (except `/auth/logout`) and `/products/**`.

---

## Project Structure

```
src/main/java/com/hkp/flowershop/
├── config/          # Security, JWT, CORS, admin seeding
├── controller/      # REST endpoints
├── dto/             # Request/response objects
├── enums/           # Role, OrderStatus, AuthProvider, …
├── exceptions/      # Custom exceptions + global handler
├── mapper/          # Entity ↔ DTO mapping
├── migration/       # GenerateTestData (startup seeding)
├── model/           # JPA entities
├── repository/      # Spring Data repositories
└── service/         # Business logic
    └── ai/          # Chatbot (ChatService, ShopTools)

src/main/resources/
├── application.properties        # Local config (gitignored — create locally)
└── application-docker.properties

src/test/java/       # Unit & integration tests
```

---

## Testing

```powershell
# Run all tests
.\mvnw.cmd test

# Run a specific test class
.\mvnw.cmd test -Dtest=CouponServiceTest
.\mvnw.cmd test -Dtest=CouponControllerTest
.\mvnw.cmd test -Dtest=CouponRepoTest
```

Tests use an in-memory H2 database (see `src/test/resources/application.properties`).

Verify production builds compile cleanly:

```powershell
.\mvnw.cmd clean package
```

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| App fails to start — missing admin config | Set `ADMIN_EMAIL` and `ADMIN_PASSWORD` in `.env` |
| Cart or chatbot returns errors | Ensure Redis is running on `localhost:6379` |
| `Preview features` compile error | Enable `--enable-preview` in your IDE and use JDK 21 |
| CORS errors from frontend | Add your frontend origin to `cors.allowed-origins` (default: `http://localhost:5173`) |
| Product images not showing | Check `product.image.uploadDir` points to the frontend `public/uploads` folder |
| OTP / reset emails not sent | Verify `spring.mail.*` SMTP settings in `application.properties` |
| Chatbot unavailable | Set `OPENROUTER_API_KEY` in `.env` (free key at [openrouter.ai/keys](https://openrouter.ai/keys)) |
| Database connection refused | Confirm PostgreSQL is running and `flower_shop` database exists |

---

## Related Projects

This API is consumed by the [FlowerShop Frontend](../flower-shop-frontend/) React SPA. Run both together for full-stack local development:

1. Start Redis, PostgreSQL, and this backend (`:8080/api`)
2. Start the frontend dev server (`:5173`)

---

## License

This project is part of the FlowerShop monorepo. See the repository root for license information.
