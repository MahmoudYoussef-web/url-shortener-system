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
Client
   ↓
Load Balancer
   ↓
Spring Boot (Stateless)
   ↓
Redis (Cache | Rate Limiter | ID Generator)
   ↓
Shard Router
   ↓
MySQL Shards
```

---

## ⚡ Write Flow

```text
POST /api/v1/urls
 → Rate Limit (Redis)
 → Generate ID (Redis INCR)
 → Base62 Encoding
 → Shard Routing
 → MySQL Insert
 → Cache Write
```

---

## ⚡ Read Flow

```text
GET /api/v1/urls/{code}
 → Redis Cache (HIT → redirect)
 → DB (MISS → cache → redirect)
```

---

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

