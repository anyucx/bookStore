# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Frontend (cd frontend/)
npm install                    # install dependencies
npm run dev                    # Vite dev server (port 5173, proxies /api → :8080)
npm run build                  # production build → frontend/dist/

# Backend (project root)
mvn compile -DskipTests=true   # compile only
mvn spring-boot:run            # run directly (triggers frontend build + app start)
mvn package                    # full build → target/bookstore-api-1.0.0-SNAPSHOT.jar
java -jar target/bookstore-api-1.0.0-SNAPSHOT.jar
```

There are no automated tests in this project. `mvn package` skips tests by default (surefire not configured).

## Architecture

**Spring Boot 2.7 + Vue 3 SPA** with MySQL + Redis, single-module Maven project.

### Controllers (split by domain)

```
AuthController       /api/auth/*, /api/admin/auth/login
CatalogController    /api/categories/tree, /api/books
TradeController      /api/cart/items, /api/orders, /api/payments
DashboardController  /api/admin/dashboard
CategoryAdminController  /api/admin/categories
BookAdminController      /api/admin/books
OrderAdminController     /api/admin/orders
UserAdminController      /api/admin/users
FileUploadController     /api/admin/files/upload
```

### Critical: Dual static resource serving

Static resources are served from TWO locations simultaneously (configured in `AppConfig.java:77-104`):

1. `classpath:/static/` — Maven copies `frontend/dist/**` here during `process-resources`
2. `file:frontend/dist/` — direct filesystem access for local dev

Both are registered as `ResourceHandler` for `/**`. A custom `PathResourceResolver` handles SPA fallback: non-`/api/**`, non-`/uploads/**`, non-file-extension paths return `index.html`. This enables Vue Router `history` mode deep links.

### Frontend auto-build on startup

`BookstoreApplication.main()` calls `buildFrontend()` **before** `SpringApplication.run()`. This runs `npm run build` synchronously via `ProcessBuilder`. On Windows, it uses `npm.cmd` instead of `npm`.

### Request flow

```
Browser → /api/**    → Controller → Service → MyBatis-Plus Mapper → MySQL
Browser → /**        → AppConfig ResourceHandler → SPA static files or index.html
Browser → /uploads/** → LocalFileStorageService → filesystem
```

### Auth

- `AuthInterceptor` intercepts `/api/**` (registered in `AppConfig.addInterceptors`)
- Token only accepted via `Authorization: Bearer <token>` header (query param removed)
- Token stored in Redis with prefix `bookstore:token:`, TTL 7 days
- Frontend stores token + user in `localStorage` (see `frontend/src/utils/storage.ts`)
- Vue Router `beforeEach` guard checks `authStore.isAuthenticated` and `authStore.isAdmin`

### Key backend conventions

- `ApiResponse<T>` is the universal response wrapper: `{ success, code, message, data, timestamp }`
- `BusinessException` (status code + message) is caught by `GlobalExceptionHandler`
- All request DTOs under `dto/request/` use `@Valid` for parameter validation
- IDs generated via `AppUtils.nextId()` (timestamp + sequence, no collision across restarts)
- `@Log` annotation on controller methods records to `operation_logs` table; only methods with `@Log` are recorded (GET requests excluded unless annotated)
- Sensitive fields (password, token) are masked in operation logs
- Entities use Lombok `@Data` with `@Accessors(chain = true)`; all fields private
- Logical delete (`@TableLogic`) configured on entities with `deleted` column
- Service layer uses manual `QueryWrapper` construction

### Key frontend conventions

- Vue 3 Composition API (`<script setup lang="ts">`) throughout
- Pinia store (`useAuthStore`) manages auth state; calls `bootstrap()` before `app.mount()`
- Axios instance (`http.ts`) auto-attaches `Authorization: Bearer <token>`, handles 401/403 by clearing session and redirecting to login (no duplicate error message)
- All API responses are typed as `ApiResponse<T>`; `unwrap()` helper extracts `.data`
- Route-based code splitting: all views are `() => import(...)`

## Encoding

This project MUST use UTF-8 everywhere. On Chinese Windows, the JVM defaults to GBK, which causes garbled characters. The `pom.xml` now includes:
- `maven-compiler-plugin` / `maven-resources-plugin` with `<encoding>UTF-8</encoding>`
- `spring-boot-maven-plugin` with `<jvmArguments>-Dfile.encoding=UTF-8</jvmArguments>`

When running from IDEA, add `-Dfile.encoding=UTF-8` to the run configuration VM options.

## Database

MySQL connection string in `application.yml` already includes `useUnicode=true&characterEncoding=UTF-8`. Schema DDL is in `SQL/ddl.sql`, seed data in `SQL/dml.sql`. Spring Boot runs these on startup (`spring.sql.init.mode: always`).

Default accounts (SHA-256 hashed):
- Admin: `admin / 123456`
- User: `demo / 123456`

## Environment Variables

| Variable | Default | Purpose |
|---|---|---|
| `MYSQL_HOST` | `127.0.0.1` | MySQL host |
| `MYSQL_PORT` | `3306` | MySQL port |
| `MYSQL_DB` | `book_store` | Database name |
| `MYSQL_USER` | `root` | DB user |
| `MYSQL_PASSWORD` | `123456` | DB password |
| `REDIS_HOST` | `127.0.0.1` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `REDIS_DB` | `1` | Redis DB number |
