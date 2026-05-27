# AGENTS.md

## Commands

- Compile backend: `mvn compile -DskipTests=true`
- Run full app: `mvn spring-boot:run` (auto-builds frontend via ProcessBuilder)
- Frontend dev server: `cd frontend && npm run dev` (port 5173, proxies `/api` → `:8080`)
- Type-check frontend: `npx vue-tsc --noEmit` (not wired into any script)
- **No lint, format, or test commands exist** — no ESLint, Prettier, or test framework configured.

## Critical gotchas

- **DB wipes on every startup**: `spring.sql.init.mode=always` re-runs `SQL/ddl.sql` + `SQL/dml.sql` (drops tables). Set to `never` in production.
- **MySQL + Redis must be running** before starting the app. No Docker setup.
- **`frontend/dist/` is checked into git** — rebuild it when changing frontend code by running `npm run build` in `frontend/`.
- **Token only via `Authorization: Bearer <token>` header** — query-param auth was removed.
- **`@Log` annotation is opt-in**: only annotated controller methods log to `operation_logs`. GET requests are excluded unless annotated.

## Architecture notes

- Single-module Maven `pom.xml` (no multi-module). All code under `src/main/java/com/bookstore/`.
- Dual static serving for SPA: `classpath:/static/` (Maven build) + `file:frontend/dist/` (dev). Both registered in `AppConfig.java`.
- Custom `PathResourceResolver` serves `index.html` for non-API, non-file-extension routes (Vue Router history mode).
- `dto/response/` is empty — responses use `Map` literals or raw `ApiResponse<T>`.
- Legacy `WebRoot/` (JSP) is preserved but inactive. Ignore it.
- `.trae/` contains planning specs but is gitignored. Reference it for historical context only.
- Entity IDs generated via `AppUtils.nextId()` (not DB auto-increment). All entities use `@TableId(type = IdType.INPUT)`.
- Operations logged via AOP: `@Log` annotation → `LogAspect` → `operation_logs` table. Sensitive fields masked in logs.
- Frontend: Vue 3 `<script setup>` Composition API, Pinia, Element Plus, route-level code splitting.
- Auth state stored in `localStorage` via `frontend/src/utils/storage.ts`; `useAuthStore.bootstrap()` called before `app.mount()`.
