<<<<<<< HEAD
# 🔗 URL Shortener System

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.x-green)
![MySQL](https://img.shields.io/badge/MySQL-8-blue)
![Redis](https://img.shields.io/badge/Cache-Redis-red)
![Architecture](https://img.shields.io/badge/Design-Distributed%20System-purple)
![Status](https://img.shields.io/badge/Status-Production--Ready-brightgreen)

---

## 🚀 Overview

A **production-grade URL Shortener backend system** designed with **real-world system design principles**:

- Horizontal scalability (database sharding)
- High-performance reads (Redis caching)
- Distributed ID generation
- Fault-tolerant architecture

> This project focuses on **how scalable backend systems are engineered**, not just CRUD.

---

## 🧠 Engineering Highlights

- Cache-aside pattern for ultra-fast reads
- Distributed ID generation using Redis `INCR`
- Base62 encoding for compact URLs
- Deterministic database sharding
- Fail-open rate limiter design
- No ORM (JPA) → full control using JDBC
- Stateless service design (horizontal scaling ready)

---

## ⚙️ How to Run

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/url-shortener-system.git
cd url-shortener-system
````

---

### 2. Requirements

* Java 21
* MySQL (multiple shards)
* Redis

---

### 3. Configuration

Create:

```bash
application.properties
```

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/shard_1
spring.datasource.username=your_user
spring.datasource.password=your_password

spring.redis.host=localhost
spring.redis.port=6379
```

---

### 4. Run the application

```bash
mvn spring-boot:run
```

---

### 5. Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

---

## 📡 API Overview

### 🔗 Create Short URL

```http
POST /api/v1/urls
```

Request:

```json
{
  "url": "https://google.com",
  "expirationSeconds": 3600,
  "customAlias": "google"
}
```

Response:

```json
{
  "shortUrl": "http://localhost:8080/google",
  "code": "google",
  "clickCount": 0
}
```

---

### 🔁 Redirect

```http
GET /api/v1/urls/{code}
```

→ Returns `302 Redirect`

---

## 🏗️ System Architecture

```text
=======
# URL Shortener — Scalable Distributed Backend

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=flat-square&logo=redis&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat-square&logo=docker&logoColor=white)
![Status](https://img.shields.io/badge/Status-Production_Ready-brightgreen?style=flat-square)

A production-grade URL shortener backend engineered around distributed system principles: atomic ID generation, deterministic sharding, cache-aside reads, and Redis-backed rate limiting. Designed to simulate how systems like Bitly operate under high traffic.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [System Design Decisions](#system-design-decisions)
- [Request Flows](#request-flows)
- [Error Handling](#error-handling)
- [API Reference](#api-reference)
- [Running Locally](#running-locally)
- [Tech Stack](#tech-stack)

---

## Overview

The system provides URL shortening with optional custom aliases, ultra-fast redirection via Redis cache, real-time click tracking, expiration with automatic DB cleanup, and Redis-backed rate limiting per IP.

The focus is on **how** the system is built — not just what it does.

---

## Architecture

```
>>>>>>> 009a523 (feat: add Docker support with docker-compose and environment configuration)
Client
   ↓
Spring Boot (Stateless)
   ↓
<<<<<<< HEAD
Spring Boot (Stateless)
   ↓
Redis (Cache | Rate Limiter | ID Generator)
   ↓
Shard Router
   ↓
MySQL Shards
=======
Redis  ──────────────────────────────────────
  ├── Cache (read acceleration)              │
  ├── ID Generator (atomic INCR)             │
  └── Rate Limiter (Lua script)              │
                                             │
Shard Router (hash % N)                      │
   ↓                                         │
MySQL Shards ────────────────────────────────
  └── Source of truth
```

```
src/main/java/com/mahmoudyoussef/url_shortener/
│
├── controller/         # UrlController — shorten, redirect, stats
├── service/
│   ├── impl/           # UrlServiceImpl — core business logic
│   ├── CacheService    # Interface (Redis implementation injected)
│   ├── RateLimiterService  # Interface
│   ├── RedisRateLimiterService  # Lua-script atomic rate limiting
│   ├── RedisCacheService   # Fail-safe Redis cache
│   ├── ShardRouter     # Deterministic shard routing
│   └── UrlCleanupService   # Scheduled expired URL cleanup
├── repository/
│   ├── ShardedUrlRepository  # Raw JDBC — explicit shard routing
│   └── ClickTrackingRepository  # Redis click counters
├── generator/
│   ├── RedisIdGenerator  # Atomic ID via Redis INCR + fallback
│   └── Base62Generator   # ID → short code encoding
├── entity/             # UrlMapping — shortCode, longUrl, expiresAt
├── dto/                # ShortenRequest, ShortenResponse, ErrorResponse
├── config/             # ShardDataSourceConfig, ClientIpResolver, RedisConfig
└── exception/          # GlobalExceptionHandler + typed exceptions
>>>>>>> 009a523 (feat: add Docker support with docker-compose and environment configuration)
```

---

<<<<<<< HEAD
## ⚡ Write Flow

```text
POST /api/v1/urls
 → Rate Limit (Redis)
 → Generate ID (Redis INCR)
 → Base62 Encoding
 → Shard Routing
 → MySQL Insert
 → Cache Write
=======
## System Design Decisions

### No ORM — Raw JDBC

JPA is deliberately excluded. `ShardedUrlRepository` uses `JdbcTemplate` directly with explicit shard routing. This gives full control over query execution, eliminates N+1 risks, and keeps the persistence layer transparent.

### Distributed ID Generation

```
Redis INCR → globally unique integer → Base62 encode → short code
```

`RedisIdGenerator` uses Redis `INCR` for atomic, distributed ID generation. An `AtomicLong` fallback activates automatically if Redis is unavailable — the system continues operating in degraded mode without throwing.

### Cache-Aside Pattern

```
Redirect flow:
  Redis HIT  → return URL immediately
  Redis MISS → query DB → populate cache → return URL
```

Redis is a performance layer only. MySQL is always the source of truth. Cache TTL is derived from the URL's `expires_at` stored in the DB — not a fixed offset — ensuring consistency between the two layers.

### Expiration — DB as Source of Truth

Expiration is persisted in `url_mapping.expires_at`. All DB queries filter expired records at the SQL level:

```sql
WHERE short_code = ? AND (expires_at IS NULL OR expires_at > NOW())
```

This means an expired URL is never returned even on a Redis cache miss. Redis TTL is a hint, not the authority.

### Scheduled Cleanup Job

`UrlCleanupService` runs every hour via `@Scheduled(fixedRate = 3600000)`:

```sql
DELETE FROM url_mapping WHERE expires_at IS NOT NULL AND expires_at <= NOW()
```

Prevents table bloat and maintains query performance over time.

### Deterministic Sharding

```
shardId = Math.abs(shortCode.hashCode()) % SHARD_COUNT
```

No routing table required. Any node can route any request independently. `JdbcTemplate` instances per shard are initialized lazily and cached in a `ConcurrentHashMap` — one connection pool per shard, created on first use.

> MVP runs on `SHARD_COUNT = 1`. Scaling requires incrementing the constant and adding shard datasource entries in configuration.

### Rate Limiting — Atomic Lua Script

The naive approach (`INCR` then `EXPIRE` as separate commands) has a race condition: if the process crashes between the two calls, the key never expires. This is solved with a single Lua script executed atomically:

```lua
local current = redis.call('INCR', KEYS[1])
if current == 1 then
  redis.call('EXPIRE', KEYS[1], ARGV[1])
end
return current
```

Separate rate limit keys per operation type: `rate:shorten:{ip}`, `rate:redirect:{ip}`, `rate:stats:{ip}`. Fail-open: if Redis is unavailable, requests pass through rather than blocking the system.

### Client IP Resolution

`ClientIpResolver` checks `X-Forwarded-For` and `X-Real-IP` headers before falling back to `getRemoteAddr()`. Ensures correct IP identification behind load balancers and reverse proxies.

---

## Request Flows

### Shorten URL

```
POST /api/v1/urls
 → Rate limit check (Redis Lua, per IP)
 → Custom alias? → check DB for collision
 → Auto-generate? → Redis INCR → Base62 encode → retry on collision
 → Insert into DB (correct shard via hashCode % N)
 → Write to Redis cache with TTL
 → Return shortUrl + code
```

### Redirect

```
GET /api/v1/urls/{code}
 → Rate limit check
 → Redis cache lookup
   → HIT  → increment click counter → 302 redirect
   → MISS → DB query (expired filter) → cache write → increment → 302 redirect
 → 404 if not found or expired
```

### Stats

```
GET /api/v1/urls/{code}/stats
 → Rate limit check
 → DB lookup (expired filter)
 → Redis click counter read
 → Return shortUrl + code + clickCount
>>>>>>> 009a523 (feat: add Docker support with docker-compose and environment configuration)
```

---

<<<<<<< HEAD
## ⚡ Read Flow

```text
GET /api/v1/urls/{code}
 → Redis Cache (HIT → redirect)
 → DB (MISS → cache → redirect)
=======
## Error Handling

All errors return a consistent JSON envelope via `GlobalExceptionHandler` (`@RestControllerAdvice`):

```json
{
  "status": 409,
  "message": "Alias already exists: google",
  "path": "/api/v1/urls",
  "timestamp": "2025-01-15T10:30:00Z"
}
```

| Exception | HTTP | Trigger |
|---|---|---|
| `DuplicateAliasException` | 409 | Custom alias already taken |
| `UrlNotFoundException` | 404 | Code not found or expired |
| `TooManyRequestsException` | 429 | Rate limit exceeded |
| `MethodArgumentNotValidException` | 400 | Validation failure |
| `Exception` (fallback) | 500 | Unexpected server error |

---

## API Reference

Full interactive docs: `http://localhost:8080/swagger-ui/index.html`

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/urls` | Shorten a URL |
| `GET` | `/api/v1/urls/{code}` | Redirect to original URL (`302`) |
| `GET` | `/api/v1/urls/{code}/stats` | Get click count and metadata |

### Shorten Request

```json
{
  "url": "https://example.com/very/long/path",
  "expirationSeconds": 3600,
  "customAlias": "my-link"
}
```

| Field | Required | Validation |
|---|---|---|
| `url` | ✅ | Must match `https?` or `ftp` scheme |
| `expirationSeconds` | ❌ | Minimum 60 seconds |
| `customAlias` | ❌ | 3–20 chars, alphanumeric + `_` `-` |

### Shorten Response

```json
{
  "shortUrl": "http://localhost:8080/api/v1/urls/aB3xYz",
  "code": "aB3xYz",
  "clickCount": 0
}
>>>>>>> 009a523 (feat: add Docker support with docker-compose and environment configuration)
```

---

<<<<<<< HEAD
## 🧱 Persistence Strategy

* JDBC (no ORM)
* Explicit query control
* Optimized for performance

---

## ⚖️ Trade-offs

| Decision          | Trade-off               |
| ----------------- | ----------------------- |
| Sharding          | Increased complexity    |
| Redis caching     | Eventual consistency    |
| Fail-open limiter | Potential abuse         |
| No ORM            | More control, more code |

---

## 📊 Observability (Planned)

* Request rate (RPS)
* Cache hit ratio
* DB latency
* Error rate monitoring

Recommended stack:

* Prometheus
* Grafana
* Micrometer

---

## 📁 Project Structure

```text
src/main/java/com/mahmoudyoussef/url_shortener
├── controller
├── service
├── repository
├── dto
├── config
├── exception
├── generator
├── entity
```

---

## 🧪 Testing

* Swagger
* Postman
* Redis failure simulation
* DB validation

---

## 🎯 What This Project Demonstrates

* Designing scalable backend systems
* Handling high read traffic efficiently
* Applying real-world system design concepts
* Building production-ready services

---

## 👨‍💻 Author

**Mahmoud Youssef**
Backend Developer (Spring Boot)

---

## 🏁 Final Result

✔ Scalable
✔ Fault-tolerant
✔ High-performance
✔ Production-ready
✔ Interview-ready

```
=======
## Running Locally

### With Docker (recommended)

```bash
git clone https://github.com/MahmoudYoussef-web/url-shortener-system.git
cd url-shortener-system

cp .env.example .env
# Edit .env and set DB_PASSWORD

docker-compose up --build
```

API: `http://localhost:8080`  
Swagger: `http://localhost:8080/swagger-ui/index.html`

### Without Docker

**Prerequisites:** Java 21, Maven, MySQL 8, Redis 7

```bash
cp src/main/resources/application.example.properties \
   src/main/resources/application.properties
# Edit datasource and Redis config

./mvnw spring-boot:run
```

### Environment Variables

| Variable | Description |
|---|---|
| `DB_URL` | MySQL JDBC URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `REDIS_HOST` | Redis hostname |
| `REDIS_PORT` | Redis port (default `6379`) |

---

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3 |
| Persistence | Raw JDBC (`JdbcTemplate`) — no ORM |
| Cache / ID Gen / Rate Limiting | Redis 7 |
| Database | MySQL 8 |
| Containerization | Docker + Docker Compose |
| Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |

---

## Author
>>>>>>> 009a523 (feat: add Docker support with docker-compose and environment configuration)

**Mahmoud Youssef** — Backend Engineer  
[GitHub](https://github.com/MahmoudYoussef-web)