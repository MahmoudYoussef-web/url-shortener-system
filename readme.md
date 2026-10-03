# URL Shortener

A simple URL shortener I built to learn Spring Boot + MySQL + Redis. You can shorten links, redirect to them, and see click counts. Links can expire.

## What it does

- Shorten a long URL, optionally with your own alias (`/summer-sale`)
- Redirect: `GET /api/v1/urls/{code}` returns 302
- Stats per link (click count)
- Links expire (default 24h) + hourly cleanup job
- Rate limit: 10 req/min per IP per endpoint (shorten / redirect / stats separately) (Redis Lua script, fail-open)
- Redis cache in front of MySQL

## How to run

You need Docker. That's the easiest way.

```bash
git clone https://github.com/MahmoudYoussef-web/url-shortener-system.git
cd url-shortener-system
cp .env.example .env
# open .env and set a DB_PASSWORD, then:
docker-compose up --build
```

- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui/index.html`

Shorten your first link:

```bash
curl -X POST http://localhost:8080/api/v1/urls \
  -H "Content-Type: application/json" \
  -d '{"url": "https://example.com/very/long/page"}'
```

Without Docker you need Java 21, Maven, MySQL 8, Redis 7:

```bash
# set DB_PASSWORD env var first
./mvnw spring-boot:run
```

On Windows there is also `run-local.bat` (same thing, but on port 8081 to avoid clashing with Docker on 8080). Tests:

Tests need MySQL and Redis running first (e.g. `docker-compose up mysql-shard-0 mysql-shard-1 redis`):

```bash
mvn test
```

## API

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/urls` | Shorten a URL |
| `GET` | `/api/v1/urls/{code}` | Redirect (302) |
| `GET` | `/api/v1/urls/{code}/stats` | Click count + metadata |

Request body for shorten:

```json
{
  "url": "https://example.com/very/long/path",
  "expirationSeconds": 3600,
  "customAlias": "my-link"
}
```

- `url` is required, must start with `http`, `https` or `ftp`
- `expirationSeconds` is optional, min 60 (default 24h)
- `customAlias` is optional, 3-20 chars (`a-z A-Z 0-9 _ -`)

Errors all look like this:

```json
{
  "status": 409,
  "message": "Alias already exists: google",
  "path": "/api/v1/urls",
  "timestamp": "2025-01-15T10:30:00"
}
```

`timestamp` is a local datetime (no `Z`/offset).

## How it is built

```
Client
  -> Spring Boot (API)
  -> Redis (cache + ID counter + click counts + rate limit)
  -> MySQL x2 shards (source of truth, picked by hash of the code)
```

- IDs come from Redis `INCR`, encoded with Base62.
- Shard = `floorMod(code.hashCode(), shardCount)`. Same code always goes to the same shard.
- Persistence is `JdbcTemplate` with explicit shard routing. There is also a JPA `@Entity` (`UrlMapping`) used for the default datasource / ddl-auto, the sharded reads/writes go through raw SQL.
- Cache TTL is computed from the row's `expires_at` so cache never outlives the DB row.
- Expired rows are filtered in SQL: `WHERE short_code = ? AND (expires_at IS NULL OR expires_at > NOW())`, plus an hourly `DELETE`.

## Config

| Variable | What | Default |
|---|---|---|
| `DB_PASSWORD` | MySQL password (required) | — |
| `DB_USERNAME` | MySQL user | `dev_user` |
| `REDIS_HOST` / `REDIS_PORT` | Redis location | `localhost` / `6379` (`redis` / `6379` in Compose) |
| `APP_BASE_URL` | Prefix used to build `shortUrl` | `http://localhost:8080/api/v1/urls/` |

## Known limitations

Honest list, things I would fix next:

- Sharding is just `hashCode % N`, so adding a shard breaks old routing. No consistent hashing.
- Click counts live in Redis only, they are lost on restart.
- Cleanup job uses `@Scheduled` with no distributed lock, so it can run twice with 2 app instances.
- Custom alias check-then-insert can race under concurrency (could return 500 instead of 409).
- Schema for shards is created with `CREATE TABLE IF NOT EXISTS` at startup, no Flyway/Liquibase yet.

## Tech

Java 21, Spring Boot 3, MySQL 8, Redis 7, Docker Compose, SpringDoc Swagger, Maven.

## Contributing

Found a bug? Open an issue or a PR. Small PRs please, with a test if it makes sense (`mvn test`).

## Author

Mahmoud Youssef — learning backend with Java/Spring Boot.

- GitHub: [@MahmoudYoussef-web](https://github.com/MahmoudYoussef-web)
