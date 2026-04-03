# 🔗 URL Shortener System (Spring Boot)

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/SpringBoot-3.x-green)
![MySQL](https://img.shields.io/badge/MySQL-8-blue)
![Redis](https://img.shields.io/badge/Cache-Redis-red)
![Architecture](https://img.shields.io/badge/Design-Sharding-purple)
![Status](https://img.shields.io/badge/Status-Production--Ready-brightgreen)

A **production-grade URL Shortener backend system** built with Spring Boot, designed using **real system design principles** such as **sharding, caching, distributed ID generation, and fault tolerance**.

> The goal is to demonstrate **how scalable backend systems are built**, not just simple CRUD.

---

## 🎯 Project Overview

This system simulates a real-world URL shortening service where:

* Users can shorten long URLs into compact short codes
* Redirection is optimized for **high read traffic**
* System scales horizontally using **database sharding**
* Redis is used for **performance optimization and system resilience**

---

## 🚀 Key Features

### 🔗 URL Shortening

* Generate unique short URLs
* Support for custom aliases
* Collision-safe generation

---

### ⚡ Fast Redirection (Read Optimization)

* Redis cache (cache-aside pattern)
* Sub-millisecond response for cached requests

---

### 🧠 Distributed ID Generation

* Redis `INCR` for atomic ID generation
* Base62 encoding for compact URLs
* Globally unique across multiple instances

---

### 🛡 Rate Limiting

* Redis-based rate limiting
* IP-based throttling
* Returns `429 Too Many Requests`

---

### 🗄 Horizontal Database Scaling

* MySQL **sharding across multiple instances**
* Deterministic routing using hashing

---

### 🧯 Resilience & Fault Tolerance

* Redis failures → fallback to database
* Fail-open rate limiter
* High availability design

---

## 🏗️ System Architecture

```text
Client
   ↓
Load Balancer
   ↓
Spring Boot Application (Stateless)
   ↓
Redis (Cache | Rate Limiter | ID Generator)
   ↓
Shard Router
   ↓
MySQL Shards
```

---

## ⚡ URL Generation Flow

```text
POST /shorten
   ↓
Rate Limiter (Redis)
   ↓
ID Generator (Redis INCR)
   ↓
Base62 Encoding
   ↓
Shard Routing
   ↓
MySQL Insert
   ↓
Cache Write
```

---

## 🔁 Redirect Flow

```text
GET /{code}
   ↓
Redis Cache
   ↓
(HIT) → Redirect
(MISS) → MySQL → Cache → Redirect
```

---

## 🧱 Persistence Layer

* Uses **JdbcTemplate**
* No JPA (intentional design decision)
* Full control over queries

---

## 📊 Monitoring & Metrics

To ensure observability and production readiness, the system can be extended with monitoring.

### 🔍 Key Metrics

* Request Rate (RPS)
* Error Rate (4xx / 5xx)
* Cache Hit Ratio
* DB Query Latency
* Redirect Latency
* Rate Limit Violations

---

### 🛠 Recommended Tools

| Tool       | Purpose                    |
| ---------- | -------------------------- |
| Prometheus | Metrics collection         |
| Grafana    | Visualization dashboards   |
| Micrometer | Spring Boot metrics export |
| ELK Stack  | Logging & analysis         |

---

### 📈 Example Monitoring

* Track cache hit vs miss ratio
* Monitor Redis health
* Alert on high latency
* Track failed requests

---

### 🧠 Insight

> Observability is critical in distributed systems to detect bottlenecks early.

---

## 🧭 System Design Diagram

### 📌 Logical Architecture

```text
User
 ↓
DNS
 ↓
Load Balancer
 ↓
Spring Boot App (Stateless)
 ↓
 ├── Redis (Cache | Rate Limit | ID Generator)
 └── Shard Router
        ↓
     MySQL Shards
```

---

### 🔄 Read Flow

```text
GET /{code}
 → Redis → Redirect
 → DB → Cache → Redirect
```

---

### 🔄 Write Flow

```text
POST /shorten
 → Rate Limit
 → Generate ID
 → Store in DB
 → Cache
```
## 🖼️ System Design Diagram (Visual)

![URL Shortener Architecture](https://github.com/user-attachments/assets/565664d6-0fc9-4096-856d-18d60be24517)

---

## 🖼️ Architecture Diagram (PNG)

> 📌 You can find a visual architecture diagram here:

```
/docs/system-design.png
```

💡 (Add this image manually in your repo for better presentation)

---

## ⚖️ Trade-offs

| Decision          | Trade-off               |
| ----------------- | ----------------------- |
| Sharding          | More complexity         |
| Redis caching     | Eventual consistency    |
| Fail-open limiter | Possible abuse          |
| No ORM (JPA)      | More control, more code |

---

## 🧠 Design Principles

* Stateless architecture
* Horizontal scalability
* Fail-safe design
* Performance-first reads

---

## 🧪 Testing

* Swagger UI
* Postman / curl
* DB verification
* Redis failure simulation

---

## 🛠 Tech Stack

| Category  | Technology  |
| --------- | ----------- |
| Language  | Java 21     |
| Framework | Spring Boot |
| Database  | MySQL       |
| Cache     | Redis       |
| Access    | JDBC        |
| Docs      | Swagger     |

---

## 📄 API Documentation

Swagger UI:

```
http://localhost:8080/swagger-ui/index.html
```

---

## 📁 Project Structure

```text
src/main/java/com/project2026/url_shortener
├── controller
├── service
├── repository
├── dto
├── config
├── exception
├── generator
├── model
```

---

## 👨‍💻 Author

**Mahmoud Youssef**
Backend Developer (Spring Boot)

---

## 🎯 Final Result

A backend system that is:

* ✅ Scalable
* ✅ Fault-tolerant
* ✅ High-performance
* ✅ Production-ready
* ✅ Interview-ready

