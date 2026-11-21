# ProductStore

A REST API for an e-commerce platform built with Spring Boot. It handles user authentication, a product catalog, and order processing with stock reservation.

## Tech Stack

- Java 21, Spring Boot 4
- Spring Data JPA + MySQL
- Spring Security with JWT (access + refresh tokens)
- springdoc-openapi (Swagger UI)
- Lombok
- Maven

## Project Structure

```
src/main/java/online/vinald/productstore/
├── auth/        # JWT auth: register/login/refresh/logout, security config, token services
├── user/        # User entity, profile endpoint
├── product/     # Product catalog CRUD
├── order/       # Orders (created with their line items)
├── orderItem/   # Order-item management (admin tooling)
├── exception/   # Typed exceptions + global exception handler
└── config/      # Cross-cutting config (OpenAPI/Swagger)
```

Each domain package follows the same shape: `Entity`, `*Repository`, `*RequestDto`/`*ResponseDto`, `*Mapper`, `*Service`, `*Controller`.

## Setup

### Prerequisites

- Java 21+
- Maven
- MySQL running locally (or update `DATABASE_URL` to point elsewhere)

### Configure environment

Copy `.env.example` to `.env` and fill it in:

```
DATABASE_URL=jdbc:mysql://localhost:3306/productstore
DATABASE_USERNAME=root
DATABASE_PASSWORD=

JWT_SECRET=
JWT_EXPIRATION_MS=900000
JWT_REFRESH_EXPIRATION_MS=604800000
```

`JWT_SECRET` must be a long random string (HMAC-SHA key material) — generate one with:

```
openssl rand -base64 64
```

`.env` is loaded at startup (`ProductStoreApplication`) and pushed into system properties, which `application.properties` reads via `${...}` placeholders. Nothing here is committed — `.env` is gitignored, `.env.example` is the template.

### Run it

```
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`. Hibernate auto-creates/updates the schema (`spring.jpa.hibernate.ddl-auto=update`) — no manual migrations needed for local dev.

### Explore it

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`

Swagger UI's "Authorize" button accepts a raw JWT (no `Bearer ` prefix needed there — it's added automatically).

## How it works

### Auth

Identity is **email-based**, not username. Register/login return an access token (JWT, short-lived — 15 min by default) and a refresh token (opaque UUID, stored server-side, long-lived — 7 days by default). Send the access token on every request:

```
Authorization: Bearer <token>
```

When it expires, exchange the refresh token for a new access token via `/auth/refresh`. Logging out deletes the refresh token server-side, so it can no longer be used to mint new access tokens — the already-issued access token stays valid until it naturally expires (there's no server-side access-token revocation).

Every endpoint requires authentication **except** `/api/v1/auth/**` and the Swagger/OpenAPI routes.

### Roles

Two roles exist: `USER` (default on registration — a customer) and `OWNER` (full access, including product management and order-item administration). There's no self-service way to become an `OWNER` — it has to be set directly in the database (`UPDATE users SET role='OWNER' WHERE email='...'`). This is intentional: owner access isn't something a public registration endpoint should be able to grant.

### Orders and stock

An order is created with its line items in one request — `items: [{productId, quantity}]`. The server, not the client, computes `totalAmount` from each product's current price, and checks/reserves stock per item. Ordering more than what's in stock (including a product at `quantity: 0`) is rejected with `409 Insufficient Stock`. Deleting an order, or an order item, releases that stock back.

A `USER` can only see and manage their own orders; an `OWNER` can see and manage everyone's. `/api/v1/order-items` is OWNER-only entirely — it's back-office tooling for adjusting an order after the fact (fixing a quantity, swapping a product, removing a line), not something a customer calls directly.

### Error shape

Every error response (except validation errors) looks like:

```json
{
  "timestamp": "2026-11-21T22:16:49.46259",
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have access to this order"
}
```

| Status | When |
|---|---|
| 400 | Request body failed validation (see below for the shape) |
| 401 | Missing/invalid/expired token, or bad login credentials |
| 403 | Authenticated, but not allowed to touch this resource |
| 404 | Resource doesn't exist |
| 409 | Duplicate email on register, or insufficient stock |

Validation errors (`400`) instead return the failing fields:

```json
{
  "timestamp": "2026-11-21T22:16:49.46259",
  "status": 400,
  "error": "Bad Request",
  "errors": {
    "email": "Email must be a valid email address",
    "password": "Password must be 6-100 characters"
  }
}
```

## API Reference

List endpoints (`GET /products`, `GET /orders`, `GET /order-items`) are paginated via Spring Data (`?page=0&size=20&sort=name,asc`) and return a `Page` wrapper (`content`, `totalElements`, `totalPages`, …).

### Auth — `/api/v1/auth` (public)

**Register**
```
POST /api/v1/auth/register
{
  "email": "jane@example.com",
  "password": "password123"
}
```
→ `201`
```json
{
  "token": "eyJhbGciOi...",
  "refreshToken": "56431b64-a14e-4023-8b76-5bdf49c70de2",
  "email": "jane@example.com",
  "role": "USER"
}
```

**Login**
```
POST /api/v1/auth/login
{ "email": "jane@example.com", "password": "password123" }
```
→ `200`, same shape as register.

**Refresh**
```
POST /api/v1/auth/refresh
{ "refreshToken": "56431b64-a14e-4023-8b76-5bdf49c70de2" }
```
→ `200`, a new `token` with the same `refreshToken`.

**Logout**
```
POST /api/v1/auth/logout
{ "refreshToken": "56431b64-a14e-4023-8b76-5bdf49c70de2" }
```
→ `204`

### Users — `/api/v1/users` (authenticated)

**Get my profile**
```
GET /api/v1/users/me
```
→ `200`
```json
{
  "id": 4,
  "email": "jane@example.com",
  "displayName": null,
  "role": "USER",
  "createdAt": "2026-11-21",
  "updatedAt": "2026-11-21"
}
```

**Update my profile**
```
PUT /api/v1/users/me
{ "displayName": "Jane Doe" }
```
→ `200`, updated profile.

### Products — `/api/v1/products`

**List** (any authenticated user) — `GET /api/v1/products?page=0&size=10`

**Get one** (any authenticated user) — `GET /api/v1/products/{id}`

**Create** (`OWNER` only)
```
POST /api/v1/products
{
  "name": "Mechanical Keyboard",
  "description": "Hot-swappable, brown switches",
  "category": "Electronics",
  "price": 89.99,
  "quantity": 50
}
```
→ `201`
```json
{
  "id": 4,
  "name": "Mechanical Keyboard",
  "description": "Hot-swappable, brown switches",
  "category": "Electronics",
  "price": 89.99,
  "quantity": 50,
  "createdAt": "2026-11-21",
  "updatedAt": "2026-11-21",
  "uuid": "c3d4e663-9ad6-4e1f-be27-561e22566e11"
}
```

**Update** / **Delete** (`OWNER` only) — `PUT`/`DELETE /api/v1/products/{id}`, same body shape as create.

### Orders — `/api/v1/orders`

**Create** (any authenticated user — becomes the order's owner)
```
POST /api/v1/orders
{
  "customerName": "Jane Doe",
  "customerEmail": "jane@example.com",
  "status": "PENDING",
  "items": [
    { "productId": 4, "quantity": 2 }
  ]
}
```
→ `201`
```json
{
  "id": 1,
  "userId": 4,
  "customerName": "Jane Doe",
  "customerEmail": "jane@example.com",
  "status": "PENDING",
  "totalAmount": 179.98,
  "createdAt": "2026-11-21",
  "updatedAt": "2026-11-21",
  "uuid": "813b9af1-9450-4268-b427-df2420d53fdc"
}
```
`totalAmount` is computed server-side — it's not accepted as input. Ordering more than available stock returns `409`.

**List** — `GET /api/v1/orders?page=0&size=10`. A `USER` only sees their own; an `OWNER` sees everyone's.

**Get one** / **Update** / **Delete** — `GET`/`PUT`/`DELETE /api/v1/orders/{id}`. `PUT` body is `{customerName, customerEmail, status}` (no `items`/`totalAmount` — those are managed via order-items or computed at creation). Returns `403` if you don't own the order and aren't an `OWNER`.

### Order Items — `/api/v1/order-items` (`OWNER` only)

Back-office tool for editing an order after creation.

**Create**
```
POST /api/v1/order-items
{ "orderId": 1, "productId": 4, "quantity": 1 }
```
→ `201`
```json
{
  "id": 1,
  "quantity": 1,
  "priceAtPurchase": 89.99,
  "createdAt": "2026-11-21",
  "updatedAt": "2026-11-21",
  "uuid": "8ab34e99-2f9b-4bac-b585-1ee779a938a1",
  "orderId": 1,
  "productId": 4
}
```
Reserves stock on create, adjusts the delta on update (quantity change or reassigning to a different product), releases stock on delete, and keeps the parent order's `totalAmount` in sync.

**List** / **Get one** / **Update** / **Delete** — standard `GET`/`GET /{id}`/`PUT /{id}`/`DELETE /{id}`, same body shape as create.

## Not implemented yet

- OAuth2 login (Google/GitHub) — `User.provider`/`providerId` are in place for it, but it needs real OAuth app credentials to wire up and test.
- Automated tests.
- A way to promote a user to `OWNER` other than editing the database directly.
