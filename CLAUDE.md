# CLAUDE.md

本文件为 Claude Code 在此仓库中工作提供指引。

## 项目目标

AI-Powered Fitness System（智能健身房管理系统）— 全栈健身管理平台，核心目标：

- **会员服务**：课程浏览/预约、健身计划（AI 生成）、AI 助手对话（"健小助"）、会员卡购买、商品商城、设备报修
- **教练管理**：课程排期、学员管理、训练套餐、个人主页
- **运营后台**：用户/课程/商品/会员卡/知识库/数据字典等全量 CRUD，数据看板与 AI 分析报告
- **AI 能力**：LLM 对话（SSE 流式）、RAG 知识库检索、AI 健身计划生成、工具调用 Agent

## 技术栈

| 层 | 技术 |
|---|------|
| 后端 | Spring Boot 3.5 + Java 17, MyBatis-Plus 3.5.7, Spring Security, Spring AI Alibaba |
| 前端 | Vue 3.5 + Vite 8, Pinia 3, Vue Router 5, Element Plus (管理端), Naive UI (会员/教练端) |
| 数据库 | PostgreSQL 16+ (pgvector), Redis, MinIO |
| AI | DashScope (kimi-k2.6), Ollama (embeddinggemma:300m), Spring AI Agent Framework |
| 基础设施 | Docker Compose, Flyway 迁移, JWT 无状态认证, Alipay 支付, 阿里云 SMS |

## 常用命令

### 后端 (Maven)
```bash
./mvnw spring-boot:run                        # 启动应用
./mvnw clean package -DskipTests              # 构建 JAR
./mvnw test                                   # 运行全部测试
./mvnw test -Dtest=ClassName                  # 运行单个测试类
./mvnw verify                                 # Checkstyle + 测试
```

### 前端 (npm, 在 `frontend/` 目录)
```bash
cd frontend
npm install          # 安装依赖
npm run dev          # 开发服务器 (端口 3000, 代理 /api → localhost:8088)
npm run build        # 生产构建 → dist/
npm run preview      # 预览生产构建
```

### 基础设施
```bash
cd docker/fitness-ai-env
docker-compose up -d   # PostgreSQL, Redis, MinIO, RabbitMQ, MySQL, Ollama, Nginx
```

默认凭据: PostgreSQL `fitness_user`/`myPostgresPass123`, Redis `myRedisPass123`, MinIO `minioadmin`/`minioPass123`.

## 架构概览

### 后端 (`src/main/java/com/fitness/`)

三层组织:

- **`modules/`** — 15 个业务模块，每个遵循 `controller/ → service/ → mapper/ → model/(entity|dto|vo|enums)` 分层。模块: `user`, `course`, `booking`, `equipment`, `membership`, `product`, `chat`, `plan`, `knowledge`, `dashboard`, `analysis`, `announcement`, `banner`, `system`, `ranking`
- **`integration/`** — 第三方集成: `ai/` (DashScope + Ollama embedding), `minio/`, `payment/` (Alipay), `security/` (JWT), `sms/` (阿里云 SMS)
- **`config/`** — Spring 配置: `SecurityConfig`, `MybatisPlusConfig`, `RedisConfig`, `JacksonConfig`, `AsyncConfig`, `OpenApiConfig`
- **`common/`** — 共享基础设施: `result/` (`Result<T>` + `PageResult<T>`), `exception/` (`BusinessException` + `GlobalExceptionHandler`), `constants/` (`ErrorCode` 枚举, 1xxx-9xxx 按域分组), `cache/` (14 个 Redis 缓存区), `mybatis/` (`JsonbTypeHandler`)

入口: `FitnessApplication.java`, `@MapperScan("com.fitness.**.mapper")`

### 前端 (`frontend/src/`)

- **`views/`** — 四个视图域: `public/` (7), `admin/` (14), `member/` (12), `coach/` (6)
- **`layouts/`** — 三个布局壳: `AdminLayout.vue` (Element Plus), `CoachLayout.vue` (Naive UI), `MemberLayout.vue` (Naive UI, 响应式)
- **`api/`** — 每个后端模块对应一个 API 文件, 镜像 `/api/v1` 端点。`admin/` 子目录放管理端专属 API
- **`stores/`** — Pinia stores (Composition API setup 函数): `auth.js`, `loading.js`, `planGeneration.js`, `schedule.js`
- **`router/index.js`** — 单文件路由，含路由守卫 (认证 + 角色检查)
- **`components/`** — 共享组件: `LoginModal.vue`, `RegisterModal.vue`, `SliderVerifyModal.vue`, `FitnessPlanPreview.vue`
- **`composables/`** — `useUserInfo.js`, `useTouchSwipe.js`
- **`utils/`** — `request.js` (Axios + token 刷新队列), `auth.js` (token 存取), `bookings.js` (预约日期工具)
- **`styles/`** — `main.css` (设计令牌 + 全局样式), `responsive.css` (响应式工具类)

管理端 UI 使用 **Element Plus**；会员/教练端使用 **Naive UI**。通过 `unplugin-vue-components` 自动导入。

### 数据库

- PostgreSQL 16+ 配合 **pgvector** 扩展 (RAG 向量检索)
- **Flyway** 管理迁移: `src/main/resources/db/migration/V{number}__{description}.sql`
- ID 策略: `assign_id` (雪花算法); 逻辑删除字段: `deleted`
- 时间字段: `create_time` / `update_time` (NOT `created_at`/`updated_at`), 每张表有自动更新触发器

### AI 集成

- **对话**: SSE 流式 via WebFlux (`/api/v1/chat/messages/stream`), 长期记忆存储在 `chat_long_term_memory`
- **健身计划**: LLM 基于用户体测数据生成周计划, 异步任务 + 轮询
- **RAG 知识库**: 文档分块 (500/overlap 50) → Ollama embedding → pgvector 存储, 相似度检索
- **Agent 工具**: Spring AI `@Tool` 注解, 9 个查询工具 (RAG、课程、教练、商品、会员卡、位置、日期时间、天气)
- **Graph 调试**: `/api/v1/admin/ai/checkpoints/{threadId}` 查看 Spring AI Alibaba 图状态

### 安全

- 无状态 JWT 认证 (`JwtAuthenticationFilter` → `SecurityFilterChain`)
- RBAC 四角色: `ADMIN`, `COACH`, `MEMBER` + 未认证访客
- 方法级安全: `@PreAuthorize` / `@Secured`
- 安全白名单: `SecurityWhitelist.URLS`

## 风格规范

### 后端代码风格

| 项目 | 规范 |
|------|------|
| 代码检查 | Google Java Style (checkstyle `google_checks.xml`), `verify` 阶段执行, `failsOnError=false` |
| 依赖注入 | 仅构造器注入 (Lombok `@RequiredArgsConstructor`), 禁止 `@Autowired` 字段注入 |
| 响应格式 | Controller 统一返回 `Result<T>`, 分页用 `PageResult<T>` |
| 异常处理 | 业务异常用 `BusinessException(ErrorCode.XXX)`, 全局由 `GlobalExceptionHandler` 兜底 |
| 日志 | Lombok `@Slf4j`, Controller 层 INFO 级别记录每个请求 |
| 事务 | 写操作加 `@Transactional(rollbackFor = Exception.class)` |
| 缓存 | `@Cacheable` / `@CacheEvict` 注解, 14 个命名缓存区, 写操作自动驱逐 |
| API 文档 | 每个 Controller 加 `@Tag`, 每个端点加 `@Operation(summary = "...")` |
| 安全注解 | 每个端点必须加 `@PreAuthorize` 指定角色或认证要求 |
| 控制器分离 | 用户端 `/api/v1/...` 与管理端 `/api/v1/admin/...` 分为两个 Controller 类, 共享 Service |

### 前端代码风格

| 项目 | 规范 |
|------|------|
| 组件库 | 管理端: Element Plus; 会员/教练端: Naive UI, 不可混用 |
| 状态管理 | Pinia Composition API (`defineStore('name', () => {...})`) |
| 路由命名 | PascalCase (如 `MemberProfile`, `AdminDashboard`), 全部懒加载 |
| API 函数 | 动词+名词: `getCoachList`, `createCourse`, `updateEquipment` |
| 模态框 | `v-model:visible` + `Teleport to="body"` |
| Token 管理 | Axios 拦截器自动注入 + 401 自动刷新队列 |
| 响应式 | 移动优先, 断点: xs(360) sm(480) md(768) lg(1024) xl(1280) |
| 主题色 | 管理端蓝色 `#1890ff`, 会员/教练端橙色 `#FF6B35` |

### 数据库规范

- 列名 `snake_case`, Java 字段 `camelCase` (MyBatis-Plus `map-underscore-to-camel-case`)
- 所有表必须有: `id` (BIGSERIAL), `create_time`, `update_time`, `deleted` (逻辑删除)
- 迁移文件: `V{序号}__{snake_case描述}.sql`, 双下划线分隔

### Git 提交规范

格式: `type(scope): 主题`

类型: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `chore`

**禁止**在 commit message 中添加 Co-Authored-By 或任何署名信息。

## 关键约束

### 后端约束

1. **Spring Boot 3.5 + Java 17** — 不得降级版本
2. **MyBatis-Plus** 为唯一 ORM, 不引入 JPA/Hibernate
3. **PostgreSQL** 为唯一关系型数据库, 可用 pgvector/jsonb 等 PG 特性
4. **Flyway 迁移** — Schema 变更必须通过迁移脚本, 不得手动改库
5. **`Result<T>` 统一响应** — 所有 Controller 端点必须返回 `Result<T>`, 不得返回裸对象
6. **ErrorCode 枚举** — 错误码按域分组 (1xxx 用户, 2xxx 课程, 3xxx 设备, 4xxx 计划, 5xxx 文件, 6xxx 分析, 7xxx 商品/订单, 8xxx 支付, 9xxx 通用)
7. **敏感配置外化** — 所有密钥/凭据必须通过 `${ENV_VAR:default}` 引用环境变量, 不得硬编码
8. **异步任务安全上下文** — 异步线程必须通过 `DelegatingSecurityContextAsyncTaskExecutor` 传播认证上下文

### 前端约束

1. **双 UI 库隔离** — Element Plus 仅用于 `views/admin/` 和 `AdminLayout`, Naive UI 仅用于 `views/member/`、`views/coach/` 及其 Layout, 不可交叉使用
2. **API 层镜像后端** — 每个后端模块对应一个 `api/*.js` 文件, 管理端 API 放 `api/admin/`
3. **路由守卫** — 所有需认证路由必须在 `meta` 中声明 `requiresAuth` 和 `roles`
4. **Token 安全** — 访问令牌存储在 localStorage (记住我) 或 sessionStorage, 不得存 cookie
5. **流式请求** — SSE 流式端点必须绕过 Axios, 使用原生 `fetch()` + `Bearer` token

### 全局约束

1. **UTF-8 编码** — 源文件、数据库连接、构建输出全部使用 UTF-8
2. **时区** — 数据库使用 `TIMESTAMP WITH TIME ZONE`, 后端统一 `yyyy-MM-dd HH:mm:ss` 格式
3. **软删除** — 所有业务表使用 `deleted` 字段逻辑删除, 禁止物理删除
4. **API 版本** — 所有端点以 `/api/v1` 为前缀

## 关键配置文件

- `src/main/resources/application.yml` — 主配置 (DB, Redis, MinIO, AI, JWT, Alipay, 日志)
- `frontend/vite.config.js` — Vite 配置 (代理, 自动导入, chunk 分割, Terser 压缩)
- `.env` — 本地开发环境变量覆盖
- `pom.xml` — Maven 依赖, checkstyle 插件
- `frontend/package.json` — 前端依赖与脚本
