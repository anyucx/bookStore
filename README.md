# 在线书城项目说明

## 1. 项目概览

本项目是一个“Spring Boot 后端 + Vue 3 前端 + MySQL + Redis”的在线书城示例仓库，当前仓库同时保留了两套前端形态：

- `frontend/`：新的 Vue 3 + Vite 单页应用前端；
- `WebRoot/`：历史 JSP 页面与静态资源，当前未删除，便于兼容与回溯；
- `src/main/java`：Spring Boot 业务后端；
- `src/main/resources`：应用配置、建表脚本、初始化数据；
- `frontend/dist`：前端构建产物目录，现已纳入 Spring Boot 统一托管链路。

当前后端接口统一使用 `/api/**` 前缀，前端使用 Vue Router 的 `history` 模式，后端已补齐对 SPA 静态资源托管与路由回退的支撑代码。

## 2. 项目架构

### 2.1 总体架构

```text
浏览器
  ├─ 访问 SPA 静态资源（/, /assets/**, /admin/** 等）
  │    └─ Spring Boot ResourceHandler
  │         ├─ classpath:/static/         <- Maven 编译时从 frontend/dist 复制
  │         └─ file:frontend/dist/        <- 本地仓库直接读取 dist
  │
  ├─ 调用业务接口 /api/**
  │    └─ Controller
  │         └─ Service
  │              └─ MyBatis-Plus Mapper
  │                   └─ MySQL
  │
  └─ 登录态校验
       └─ AuthInterceptor
            └─ Redis（token 与会话信息）
```

### 2.2 技术栈

- 后端框架：Spring Boot 2.7.18
- ORM：MyBatis-Plus 3.5.5
- 数据库：MySQL 8.x
- 缓存：Redis
- 前端：Vue 3、Vite、Pinia、Vue Router、Element Plus
- 构建工具：Maven、npm

## 3. 模块划分

### 3.1 后端模块

| 模块 | 目录 | 说明 |
| --- | --- | --- |
| 启动入口 | `src/main/java/com/bookstore/BookstoreApplication.java` | Spring Boot 启动类 |
| 配置层 | `src/main/java/com/bookstore/config` | MVC、Redis、分页等基础配置 |
| 控制层 | `src/main/java/com/bookstore/controller` | 对外 REST API |
| 服务层 | `src/main/java/com/bookstore/service` | 认证、商品、交易、管理业务 |
| 持久层 | `src/main/java/com/bookstore/mapper` | MyBatis-Plus Mapper |
| 实体层 | `src/main/java/com/bookstore/model/entity` | 数据表实体映射 |
| 安全层 | `src/main/java/com/bookstore/security` | 登录拦截、上下文用户 |
| 通用层 | `src/main/java/com/bookstore/common` | 统一响应、异常处理 |
| 文件存储 | `src/main/java/com/bookstore/storage` | 本地文件上传与访问 URL 封装 |
| 工具类 | `src/main/java/com/bookstore/util` | Redis 工具、ID/摘要等辅助方法 |

### 3.2 前端模块

| 模块 | 目录 | 说明 |
| --- | --- | --- |
| 应用入口 | `frontend/src/main.ts` | Vue 应用启动入口 |
| 路由 | `frontend/src/router` | 商城端、登录、管理端路由定义 |
| 页面布局 | `frontend/src/layouts` | 商城与管理后台布局 |
| 页面视图 | `frontend/src/views` | 首页、图书、购物车、订单、后台管理 |
| 状态管理 | `frontend/src/stores` | 当前用户与鉴权状态 |
| 接口层 | `frontend/src/api` | Axios 请求封装与业务 API |
| 构建产物 | `frontend/dist` | Vite 打包输出目录 |

### 3.3 历史模块

| 模块 | 目录 | 说明 |
| --- | --- | --- |
| JSP 前端 | `WebRoot/` | 历史书城页面与资源，保留但不参与当前 SPA 托管链路 |

## 4. 核心功能

### 4.1 用户与鉴权

- 用户注册、登录、退出登录；
- 管理员独立登录入口；
- 登录态通过 Redis 保存，避免本地内存会话导致多节点不一致；
- `AuthInterceptor` 对 `/api/**` 做统一鉴权，并按角色区分管理员权限。

### 4.2 商品与分类

- 分类树查询；
- 图书分页查询、关键字搜索、详情查看；
- 管理端分类新增、编辑、删除；
- 管理端图书新增、编辑、删除、上下架。

### 4.3 交易链路

- 购物车增删改查；
- 创建订单、查看订单详情；
- 取消订单、确认收货；
- 模拟支付预下单；
- 支付回调落库与订单状态推进。

### 4.4 管理后台

- 首页统计看板；
- 分类、图书、订单状态、用户管理；
- 文件上传与资源访问。

## 5. 库表设计概览

### 5.1 核心表

| 表名 | 作用 | 关键字段 |
| --- | --- | --- |
| `roles` | 角色字典 | `id`、`code`、`name` |
| `users` | 用户表 | `username`、`password_hash`、`role_id`、`status`、`deleted` |
| `categories` | 分类表 | `parent_id`、`name`、`sort`、`status`、`deleted` |
| `books` | 图书表 | `category_id`、`name`、`price`、`stock`、`status`、`sales`、`deleted` |
| `cart_items` | 购物车项 | `user_id`、`book_id`、`quantity`、`selected`、`deleted` |
| `orders` | 订单主表 | `order_no`、`user_id`、`status`、`total_amount`、`deleted` |
| `order_items` | 订单明细 | `order_id`、`book_id`、`price`、`amount` |
| `payment_records` | 支付记录 | `order_id`、`pay_channel`、`pay_status`、`transaction_no` |
| `file_resources` | 文件资源元数据 | `storage_path`、`access_url`、`uploader_id`、`deleted` |

### 5.2 关系说明

- `users.role_id -> roles.id`
- `books.category_id -> categories.id`
- `cart_items.user_id -> users.id`
- `cart_items.book_id -> books.id`
- `orders.user_id -> users.id`
- `order_items.order_id -> orders.id`
- `order_items.book_id -> books.id`
- `payment_records.order_id -> orders.id`

### 5.3 约束与审计设计

- 关键业务表提供 `created_time`、`updated_time` 等审计字段；
- `users`、`categories`、`books`、`cart_items`、`orders`、`file_resources` 预留 `deleted` 字段，作为逻辑删除设计基础；
- `schema.sql` 已补充核心外键约束，保证主要关系链路可校验；

### 5.4 初始化数据

`src/main/resources/data.sql` 默认内置两类角色与两个演示账号：

- 管理员：`admin / 123456`
- 普通用户：`demo / 123456`

密码在库内以 SHA-256 摘要形式保存。

## 6. 接口分层

### 6.1 控制层

| 控制器 | 主要职责 |
| --- | --- |
| `AuthController` | 注册、登录、退出、当前用户 |
| `CatalogController` | 分类树、图书列表、图书详情 |
| `TradeController` | 购物车、订单、支付 |
| `AdminController` | 管理端登录、统计、类目/图书/订单/用户/文件管理 |

### 6.2 服务层

| 服务 | 主要职责 |
| --- | --- |
| `AuthService` | 登录注册、当前用户、用户管理、Redis token 写入 |
| `CatalogService` | 分类树构建、图书分页、图书与分类维护 |
| `TradeService` | 购物车、下单、库存扣减、支付记录、订单状态流转 |
| `AdminService` | 后台统计、文件上传落库 |

### 6.3 通用与基础设施层

- `ApiResponse`：统一返回结构；
- `GlobalExceptionHandler`：统一异常转响应；
- `AuthInterceptor`：鉴权拦截；
- `RedisCacheUtil`：Redis 操作封装；
- `LocalFileStorageService`：本地文件存储；
- `AppConfig`：MVC 资源托管、RedisTemplate、分页拦截器。

## 7. 前端构建产物统一托管说明

### 7.1 目标

将 `frontend/dist` 统一纳入 Spring Boot 托管，满足以下场景：

- 本地仓库已有 `frontend/dist` 时，后端可直接从仓库路径读取；
- Maven 构建时，将 `frontend/dist` 同步复制到 `classpath:/static/`；
- 对 Vue Router `history` 路由做 SPA 回退，尽量兼容 `/books/3001`、`/admin/orders` 等深链访问；
- 不影响 `/api/**` 与 `/uploads/**` 的独立处理。

### 7.2 当前实现链路

1. `pom.xml`
   - 新增 `build.resources` 配置；
   - 将 `frontend/dist/**` 复制到 `target/classes/static/**`；
   - 保留 `src/main/resources` 原有资源。

2. `application.yml`
   - 新增：
     - `bookstore.frontend.dist-path: frontend/dist`
     - `bookstore.frontend.index-file: index.html`

3. `AppConfig`
   - 新增静态资源位置：
     - `classpath:/static/`
     - `file:frontend/dist/`（通过绝对路径解析）
   - 新增 SPA 回退逻辑：
     - 非 `/api/**`
     - 非 `/uploads/**`
     - 非带扩展名的真实静态资源
     - 则回退到 `index.html`

### 7.3 为什么从代码上能直接看出可行路径

- Maven 复制路径已写死在 `pom.xml` 的 `build.resources`；
- 运行时文件系统路径已写在 `application.yml` 与 `AppConfig`；
- SPA 回退规则已写在 `AppConfig#shouldServeSpaIndex`；
- 因此即使不启动服务，也能直接从代码判断：
  - 本地开发可读取 `frontend/dist`
  - Maven 构建产物可读取 `classpath:/static/`
  - 深链路由会回退到 `index.html`

## 8. 启动与构建方式

### 8.1 环境要求

- JDK 8
- Maven 3.8+
- MySQL 8.x
- Redis 6.x+
- Node.js 18+（前端重新打包时需要）

### 8.2 数据库与缓存配置

后端主要通过环境变量读取：

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `MYSQL_HOST` | `127.0.0.1` | MySQL 地址 |
| `MYSQL_PORT` | `3306` | MySQL 端口 |
| `MYSQL_DB` | `bookstore` | 数据库名 |
| `MYSQL_USER` | `root` | 数据库账号 |
| `MYSQL_PASSWORD` | `root` | 数据库密码 |
| `REDIS_HOST` | `127.0.0.1` | Redis 地址 |
| `REDIS_PORT` | `6379` | Redis 端口 |
| `REDIS_DB` | `1` | Redis 库编号 |

### 8.3 前端构建

```bash
cd frontend
npm install
npm run build
```

构建完成后产物位于 `frontend/dist/`。

### 8.4 后端编译

```bash
mvn compile -T 1.5C -o -DskipTests=true
```

### 8.5 后端启动

```bash
mvn spring-boot:run
```

或者：

```bash
mvn package
java -jar target/bookstore-api-1.0.0-SNAPSHOT.jar
```

## 9. 自测步骤

### 9.1 静态资源与 SPA 路由

1. 启动前端构建，确保 `frontend/dist/index.html` 与 `frontend/dist/assets/*` 已生成；
2. 启动后端服务；
3. 浏览器访问 `/`，检查是否加载首页；
4. 直接访问 `/books/3001`、`/admin/dashboard`，检查是否都能回退到 SPA 页面；
5. 访问 `/assets/*.js`，检查静态资源能否正常返回；
6. 访问 `/api/books`，检查接口请求未被静态资源托管拦截。

### 9.2 用户链路

1. 启动项目；
2. 使用 `demo / 123456` 登录；
3. 查询图书列表、加入购物车、创建订单；
4. 在订单页执行支付预下单，再点击“模拟支付成功”；
5. 检查订单状态是否从 `CREATED` 变为 `PAID`，随后可确认收货。

### 9.3 管理链路

1. 启动项目；
2. 使用 `admin / 123456` 登录管理端；
3. 查看统计看板；
4. 新增或编辑分类、图书；
5. 调整订单状态并检查表格状态标签变化；
6. 上传文件并检查返回的 `accessUrl` 是否可访问。

## 10. 已知限制

- 当前支付流程为模拟链路，未对接真实支付网关；
- 订单取消后未做库存回补；
- 密码使用 SHA-256 摘要，未引入更强的带盐密码算法；
- 购物车、订单并发扣减库存逻辑较为基础，高并发下仍需要进一步强化防超卖方案；
- 历史 `WebRoot/` JSP 资源仍保留在仓库中，但当前主流程以前后端分离 SPA 为主；
- 文件存储默认走本地磁盘，适用于单机演示，生产环境建议切换对象存储；
- 当前仓库已提交 `frontend/dist`，适合演示 with 统一托管；若后续改为 CI 产物驱动，需要补充前端构建流水线。

## 11. 日志与审计

### 11.1 后台日志精简
- 控制台输出已通过 `logback-spring.xml` 精简。
- 所有日志同步记录至项目根目录的 `logs/app.log`，并支持按天滚动归档。
- `logs/` 目录已加入 `.gitignore`。

### 11.2 操作日志审计
- 系统引入了通用的 `@Log` 注解，用于记录核心业务操作。
- 审计内容包括：操作人、IP 地址、接口路径、请求方法、请求参数、响应结果、执行耗时等。
- 数据持久化在 `operation_logs` 表中，方便后续安全排查与审计。
- **移植建议**：若要将此功能迁移至其他项目，只需复制 `com.bookstore.common.annotation.Log` 和 `com.bookstore.common.aspect.LogAspect`，并确保数据库中存在 `operation_logs` 表即可。

## 12. 关键目录速览

```text
.
├─ README.md
├─ pom.xml
├─ frontend/
│  ├─ src/
│  └─ dist/
├─ src/main/
│  ├─ java/com/bookstore/
│  └─ resources/
├─ WebRoot/
└─ uploads/              （运行后可能生成）
```

## 12. 维护建议

- 前端每次重新发布后，同步更新 `frontend/dist`；
- 若走 Maven/Jar 部署，优先依赖 `classpath:/static/` 中的复制产物；
- 若走源码目录直启，确保 `bookstore.frontend.dist-path` 指向实际 dist 目录；
- 后续若扩展多节点部署，建议把上传文件从本地磁盘迁移到对象存储，并继续保留 Redis 登录态方案。
