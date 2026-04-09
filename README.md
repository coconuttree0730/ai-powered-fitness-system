# AI-Powered Fitness System 智能健身房管理系统

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.0-green?logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3.5.30-blue?logo=vue.js" alt="Vue3">
  <img src="https://img.shields.io/badge/JDK-17-orange?logo=openjdk" alt="JDK17">
  <img src="https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Redis-7.2-red?logo=redis" alt="Redis">
  <img src="https://img.shields.io/badge/MinIO-Latest-cyan?logo=minio" alt="MinIO">
  <img src="https://img.shields.io/badge/Spring%20AI%20Alibaba-1.1.2.0-purple" alt="Spring AI Alibaba">
  <img src="https://img.shields.io/badge/Ollama-Embedding-yellow?logo=ollama" alt="Ollama">
</p>

<p align="center">
  <b>基于 Spring Boot + Vue3 的全栈智能健身房管理系统</b>
</p>

<p align="center">
  集成 AI 智能健身计划生成、RAG 知识库问答、课程预约、器材管理、数据可视化分析等核心功能，
  <br>
  为健身房提供一站式数字化运营管理解决方案。
</p>

---

## 目录

- [核心特性](#核心特性)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [配置说明](#配置说明)
- [API 文档](#api-文档)
- [数据库设计](#数据库设计)
- [功能模块详解](#功能模块详解)
- [部署指南](#部署指南)
- [开发规范](#开发规范)
- [常见问题](#常见问题)
- [更新日志](#更新日志)
- [贡献指南](#贡献指南)
- [许可证](#许可证)

---

## 核心特性

### 🤖 AI 智能功能

| 功能 | 描述 | 技术实现 |
|------|------|----------|
| **AI 健身计划生成** | 根据用户身体数据、健身目标自动生成个性化健身计划 | Spring AI Alibaba + DashScope |
| **AI 健身助手** | 支持流式对话的 AI 健身顾问，提供实时健身指导和答疑 | WebFlux + SSE 流式响应 |
| **RAG 知识库** | 基于向量检索的健身知识问答系统，支持文档上传和智能检索 | pgvector + Ollama Embedding |
| **AI 文本润色** | 智能文本优化与内容生成，用于课程描述、公告等内容 | DashScope 大模型 |
| **AI 数据分析** | 智能运营数据分析与报告生成，提供决策支持 | AI 驱动的数据分析引擎 |

### 👥 用户与权限管理

- **多角色体系**：管理员、教练、会员三角色分离
- **多种登录方式**：账号密码、短信验证码、滑块验证码
- **RBAC 权限控制**：基于角色的细粒度权限管理
- **JWT 认证**：无状态安全认证机制

### 📚 课程与预约管理

- **课程管理**：课程发布、分类管理、教练关联
- **预约系统**：在线预约、取消预约、预约限制
- **课程日历**：可视化课程时间安排
- **教练排班**：教练课程安排与管理

### 🏋️ 器材管理

- **器材档案**：器材信息维护、图片管理
- **状态跟踪**：实时器材状态监控
- **报修系统**：在线报修、维修进度跟踪
- **维修记录**：完整的维修历史记录

### 🛒 商品与订单

- **商品管理**：健身商品上架、库存管理
- **订单系统**：在线下单、订单状态跟踪
- **价格计算**：支持多种价格计算策略

### 📊 数据可视化

- **运营仪表盘**：实时数据监控与统计
- **AI 分析报告**：智能运营分析报告
- **图表展示**：丰富的数据可视化图表
- **会员增长**：会员增长趋势分析

### 📢 内容管理

- **轮播图管理**：首页轮播图配置
- **公告发布**：系统公告管理
- **知识库**：健身知识文档管理

---

## 技术栈

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| **Spring Boot** | 3.5.0 | 基础框架，提供自动配置和嵌入式服务器 |
| **Spring AI Alibaba** | 1.1.2.0 | 阿里云 AI 集成框架，支持 DashScope 大模型 |
| **Spring Security** | 6.x | 认证授权框架，提供 JWT 和 RBAC 支持 |
| **Spring Data Redis** | 3.5.0 | Redis 数据访问 |
| **MyBatis-Plus** | 3.5.7 | ORM 框架，简化数据库操作 |
| **Flyway** | 10.14.0 | 数据库版本控制工具 |
| **PostgreSQL** | 16+ | 业务数据库，含 pgvector 向量扩展 |
| **Redis** | 7.2+ | 缓存、会话存储、分布式锁 |
| **MinIO** | Latest | 对象存储，用于文件上传管理 |
| **JWT** | 0.12.6 | JSON Web Token 身份认证 |
| **Hutool** | 5.8.25 | Java 工具类库 |
| **Lombok** | Latest | 代码简化工具 |
| **Ollama** | Latest | 本地 Embedding 模型服务 |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| **Vue** | 3.5.30 | 渐进式前端框架，Composition API |
| **Vue Router** | 5.0.3 | 路由管理 |
| **Pinia** | 3.0.4 | 状态管理，Vuex 替代品 |
| **Vite** | 8.0.0 | 构建工具，快速冷启动 |
| **Naive UI** | 2.44.1 | UI 组件库，Vue3 专用 |
| **Element Plus** | 2.13.5 | 组件库，补充组件 |
| **ECharts** | 6.0.0 | 图表库，数据可视化 |
| **Axios** | 1.13.6 | HTTP 客户端 |
| **Marked** | 17.0.5 | Markdown 渲染 |
| **DOMPurify** | 3.3.3 | XSS 防护 |
| **Vue3 Slider Verify** | 1.0.5 | 滑块验证码组件 |

---

## 系统架构

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              前端层 (Frontend)                               │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │   管理端    │  │   教练端    │  │   会员端    │  │   公开页    │         │
│  │   (Admin)   │  │   (Coach)   │  │  (Member)   │  │   (Public)  │         │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘         │
│                              Vue 3 + Vite                                   │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              网关层 (Gateway)                                │
│                    Nginx / Spring Cloud Gateway (预留)                       │
│                         负载均衡、路由转发、静态资源                          │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              应用层 (Backend)                                │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                     Spring Boot 3.5.0                               │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │   │
│  │  │   AI 模块   │  │  用户模块   │  │  课程模块   │  │  预约模块   │ │   │
│  │  │ integration │  │    user     │  │   course    │  │   booking   │ │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘ │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │   │
│  │  │  器材模块   │  │  计划模块   │  │  商品模块   │  │  知识库模块 │ │   │
│  │  │  equipment  │  │    plan     │  │   product   │  │  knowledge  │ │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘ │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │   │
│  │  │  聊天模块   │  │  仪表盘模块 │  │  公告模块   │  │  分析模块   │ │   │
│  │  │    chat     │  │  dashboard  │  │ announcement│  │   analysis  │ │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              数据层 (Data Layer)                             │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐             │
│  │   PostgreSQL    │  │     Redis       │  │     MinIO       │             │
│  │   (主数据库)     │  │   (缓存/会话)    │  │   (对象存储)     │             │
│  │  + pgvector     │  │                 │  │                 │             │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘             │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              AI 服务层 (AI Services)                         │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐             │
│  │  DashScope API  │  │     Ollama      │  │  Embedding模型   │             │
│  │  (大语言模型)    │  │  (本地AI服务)   │  │  (向量生成)      │             │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 项目结构

```
ai-powered-fitness-system/
├── 📁 src/main/java/com/fitness/           # 后端源码
│   ├── 📄 FitnessApplication.java          # 应用入口
│   ├── 📁 common/                          # 公共模块
│   │   ├── 📁 config/                      # 通用配置
│   │   ├── 📁 constants/                   # 常量定义
│   │   │   ├── 📄 CommonConstants.java     # 通用常量
│   │   │   ├── 📄 ErrorCode.java           # 错误码
│   │   │   └── 📄 RoleConstants.java       # 角色常量
│   │   ├── 📁 exception/                   # 异常处理
│   │   │   ├── 📄 BaseException.java       # 基础异常
│   │   │   ├── 📄 BusinessException.java   # 业务异常
│   │   │   └── 📄 GlobalExceptionHandler.java # 全局异常处理
│   │   ├── 📁 mybatis/                     # MyBatis 扩展
│   │   ├── 📁 result/                      # 统一响应封装
│   │   │   ├── 📄 PageResult.java          # 分页结果
│   │   │   └── 📄 Result.java              # 通用结果
│   │   └── 📁 utils/                       # 工具类
│   │       ├── 📄 DateUtils.java           # 日期工具
│   │       ├── 📄 JsonUtils.java           # JSON 工具
│   │       └── 📄 JwtUtils.java            # JWT 工具
│   ├── 📁 config/                          # 框架配置
│   │   ├── 📄 SecurityConfig.java          # 安全配置
│   │   ├── 📄 MybatisPlusConfig.java       # MyBatis-Plus配置
│   │   ├── 📄 RedisConfig.java             # Redis配置
│   │   └── 📄 ...
│   ├── 📁 integration/                     # 第三方集成
│   │   ├── 📁 ai/                          # Spring AI Alibaba
│   │   │   ├── 📁 config/                  # AI配置
│   │   │   ├── 📁 controller/              # AI接口
│   │   │   ├── 📁 service/                 # AI服务
│   │   │   ├── 📁 prompt/                  # Prompt模板
│   │   │   └── 📁 model/                   # AI相关DTO/VO
│   │   ├── 📁 minio/                       # MinIO文件存储
│   │   └── 📁 security/                    # JWT安全组件
│   └── 📁 modules/                         # 业务模块
│       ├── 📁 user/                        # 用户模块
│       ├── 📁 course/                      # 课程模块
│       ├── 📁 booking/                     # 预约模块
│       ├── 📁 equipment/                   # 器材模块
│       ├── 📁 plan/                        # 健身计划模块
│       ├── 📁 product/                     # 商品模块
│       ├── 📁 chat/                        # AI聊天模块
│       ├── 📁 knowledge/                   # 知识库模块
│       ├── 📁 dashboard/                   # 仪表盘模块
│       ├── 📁 announcement/                # 公告模块
│       ├── 📁 banner/                      # 轮播图模块
│       └── 📁 analysis/                    # 分析模块
├── 📁 src/main/resources/
│   ├── 📁 db/migration/                    # Flyway迁移脚本 (V1-V31)
│   ├── 📁 mapper/                          # MyBatis XML
│   ├── 📄 application.yml                  # 主配置
│   └── 📄 application-dev.yml              # 开发环境配置
├── 📁 frontend/                            # 前端项目 (Vue3)
│   ├── 📁 src/
│   │   ├── 📁 api/                         # API接口封装
│   │   ├── 📁 views/                       # 页面视图
│   │   │   ├── 📁 admin/                   # 管理端页面
│   │   │   ├── 📁 coach/                   # 教练端页面
│   │   │   ├── 📁 member/                  # 会员端页面
│   │   │   └── 📁 public/                  # 公开页面
│   │   ├── 📁 components/                  # 公共组件
│   │   ├── 📁 router/                      # 路由配置
│   │   ├── 📁 stores/                      # Pinia状态管理
│   │   ├── 📁 utils/                       # 工具函数
│   │   └── 📁 styles/                      # 样式文件
│   ├── 📄 package.json
│   └── 📄 vite.config.js
├── 📁 docker/                              # Docker 配置
│   └── 📁 fitness-ai-env/
│       ├── 📄 docker-compose.yml           # 完整环境编排
│       └── 📁 initdb.d/                    # 数据库初始化脚本
├── 📁 .trae/                               # 开发文档
│   ├── 📁 documents/                       # 开发计划文档
│   └── 📁 specs/                           # 功能规格说明
├── 📄 pom.xml                              # Maven配置
└── 📄 README.md                            # 项目说明
```

---

## 快速开始

### 环境要求

| 组件 | 版本要求 | 说明 |
|------|----------|------|
| JDK | 17+ | Java 开发工具包 |
| Maven | 3.9+ | 项目构建工具 |
| Node.js | 18+ | 前端运行环境 |
| PostgreSQL | 16+ | 需启用 pgvector 扩展 |
| Redis | 7.2+ | 缓存数据库 |
| MinIO | Latest | 对象存储服务 |
| Ollama | Latest | 本地 Embedding 服务（可选）|

### 1. 克隆项目

```bash
git clone https://gitee.com/wuzhongpengcode/ai-powered-fitness-system.git
cd ai-powered-fitness-system
```

### 2. 启动基础设施（Docker）

项目提供了完整的 Docker Compose 配置，包含所有依赖服务：

```bash
# 进入 Docker 配置目录
cd docker/fitness-ai-env

# 启动所有服务（在 WSL 中执行）
docker-compose up -d

# 查看服务状态
docker-compose ps
```

启动的服务包括：
- **PostgreSQL** (5432): 主数据库，含 pgvector 扩展
- **Redis** (6379): 缓存服务
- **MinIO** (9000/9001): 对象存储
- **MySQL** (3306): 备用数据库
- **RabbitMQ** (5672/15672): 消息队列
- **Nginx** (9080/9443): 反向代理
- **Ollama** (11434): 本地 AI 服务

### 3. 配置应用

编辑 `src/main/resources/application-dev.yml`：

```yaml
server:
  port: 8088

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fitness_ai_db
    username: fitness_user
    password: myPostgresPass123
  
  data:
    redis:
      host: localhost
      port: 6379
      password: myRedisPass123
  
  ai:
    dashscope:
      api-key: your-dashscope-api-key  # 替换为你的阿里云 API Key

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioPass123
  bucket-name: fitness-bucket

jwt:
  secret: your-256-bit-secret-key-here-must-be-at-least-32-characters-long
```

### 4. 运行后端

```bash
# 使用 Maven Wrapper（推荐）
./mvnw spring-boot:run

# 或打包后运行
./mvnw clean package -DskipTests
java -jar target/ai-powered-fitness-system-1.0.0.jar
```

后端服务启动后访问：
- 应用首页: http://localhost:8088
- Swagger UI: http://localhost:8088/swagger-ui.html

### 5. 运行前端

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器地址：http://localhost:5173

---

## 配置说明

### 数据库配置

```yaml
spring:
  datasource:
    driver-class-name: org.postgresql.Driver
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:fitness_ai_db}
    username: ${DB_USERNAME:fitness_user}
    password: ${DB_PASSWORD:myPostgresPass123}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
```

### AI 配置

```yaml
spring:
  ai:
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY:your-api-key}
      chat:
        enabled: true
        options:
          model: tongyi-xiaomi-analysis-flash  # 或其他模型
          temperature: 0.7
          top-p: 0.8
          max-tokens: 4000
    ollama:
      base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
      embedding:
        model: embeddinggemma:300m  # Embedding 模型
```

### 知识库配置

```yaml
knowledge:
  chunk:
    size: 500      # 文档分块大小
    overlap: 50    # 分块重叠大小
```

### JWT 配置

```yaml
jwt:
  secret: ${JWT_SECRET:your-secret-key}
  expiration: ${JWT_EXPIRATION:86400000}           # 1天
  refresh-expiration: ${JWT_REFRESH_EXPIRATION:604800000}  # 7天
  issuer: ai-fitness-system
  audience: fitness-users
```

---

## API 文档

### 接口前缀

所有 API 接口统一前缀：`/api/v1`

### 认证方式

在请求头中添加：
```
Authorization: Bearer <your-jwt-token>
```

### 核心接口概览

#### 认证接口

| 方法 | 接口 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/v1/auth/register` | 用户注册 | 公开 |
| POST | `/api/v1/auth/login` | 用户登录 | 公开 |
| POST | `/api/v1/auth/login/sms` | 短信验证码登录 | 公开 |
| POST | `/api/v1/auth/logout` | 用户登出 | 已登录 |
| GET | `/api/v1/auth/slider-verify/token` | 获取滑块验证 | 公开 |
| POST | `/api/v1/auth/slider-verify/verify` | 验证滑块 | 公开 |

#### 用户接口

| 方法 | 接口 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/v1/users/me` | 获取当前用户信息 | 已登录 |
| PUT | `/api/v1/users/me` | 更新当前用户信息 | 已登录 |
| GET | `/api/v1/admin/users` | 用户列表（分页） | ADMIN |
| POST | `/api/v1/admin/users` | 创建用户 | ADMIN |
| PUT | `/api/v1/admin/users/{id}` | 更新用户 | ADMIN |
| DELETE | `/api/v1/admin/users/{id}` | 删除用户 | ADMIN |

#### AI 接口

| 方法 | 接口 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/v1/ai/fitness-plan` | AI生成健身计划 | MEMBER |
| POST | `/api/v1/ai/text-polish` | AI文本润色 | 已登录 |
| POST | `/api/v1/chat/messages/stream` | AI流式对话 | MEMBER |
| GET | `/api/v1/chat/sessions` | 获取会话列表 | MEMBER |
| DELETE | `/api/v1/chat/sessions/{id}` | 删除会话 | MEMBER |

#### 课程接口

| 方法 | 接口 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/v1/courses/public/list` | 公开课程列表 | 公开 |
| GET | `/api/v1/courses/public/{id}` | 课程详情 | 公开 |
| GET | `/api/v1/courses` | 课程列表（管理） | COACH/ADMIN |
| POST | `/api/v1/courses` | 创建课程 | COACH/ADMIN |
| PUT | `/api/v1/courses/{id}` | 更新课程 | COACH/ADMIN |

#### 预约接口

| 方法 | 接口 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/v1/bookings` | 我的预约列表 | MEMBER |
| POST | `/api/v1/bookings` | 创建预约 | MEMBER |
| PUT | `/api/v1/bookings/{id}/cancel` | 取消预约 | MEMBER |
| GET | `/api/v1/admin/bookings` | 所有预约列表 | ADMIN |

#### 知识库接口

| 方法 | 接口 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/v1/knowledge/rag/search` | RAG知识检索 | 公开 |
| GET | `/api/v1/knowledge/documents` | 文档列表 | 已登录 |
| POST | `/api/v1/knowledge/documents` | 上传文档 | ADMIN |
| DELETE | `/api/v1/knowledge/documents/{id}` | 删除文档 | ADMIN |

#### 仪表盘接口

| 方法 | 接口 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/v1/dashboard/stats` | 仪表盘统计数据 | ADMIN |
| GET | `/api/v1/dashboard/revenue-trend` | 收入趋势 | ADMIN |
| GET | `/api/v1/dashboard/user-growth` | 用户增长 | ADMIN |
| POST | `/api/v1/dashboard/analysis` | AI数据分析 | ADMIN |

更多接口详见 Swagger UI: http://localhost:8088/swagger-ui.html

---

## 数据库设计

### 核心表结构

#### 用户相关

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `sys_user` | 用户表 | id, username, password, email, phone, role, status |
| `sys_role` | 角色表 | id, name, code, description |
| `sys_permission` | 权限表 | id, name, code, type |
| `sys_user_role` | 用户角色关联 | user_id, role_id |
| `sys_role_permission` | 角色权限关联 | role_id, permission_id |
| `user_fitness_profile` | 用户健身档案 | user_id, height, weight, fitness_goal, experience_level |

#### 课程相关

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `fitness_course` | 课程表 | id, name, category, coach_id, description, price, max_students |
| `fitness_booking` | 预约表 | id, user_id, course_id, booking_time, status |

#### 器材相关

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `fitness_equipment` | 器材表 | id, name, type_id, status, location, images |
| `fitness_equipment_type` | 器材类型表 | id, name, description |
| `fitness_equipment_repair` | 器材报修表 | id, equipment_id, reporter_id, issue, status |
| `repair_record` | 维修记录表 | id, repair_id, handler_id, action, cost |

#### 健身计划

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `fitness_plan` | 健身计划表 | id, user_id, name, goal, ai_generated, status |
| `fitness_plan_detail` | 计划详情表 | id, plan_id, day_number, exercises, duration |

#### 商品订单

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `product` | 商品表 | id, name, description, price, stock, category |
| `product_order` | 订单表 | id, user_id, product_id, quantity, total_price, status |

#### AI 相关

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `chat_session` | 聊天会话表 | id, user_id, title, created_at |
| `chat_message` | 聊天消息表 | id, session_id, role, content, created_at |
| `knowledge_document` | 知识文档表 | id, title, content, category_id, status |
| `knowledge_chunk` | 知识分块表 | id, document_id, content, embedding (vector) |
| `knowledge_category` | 知识分类表 | id, name, description |
| `analysis_report` | 分析报告表 | id, type, title, content, ai_generated |

#### 内容管理

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `banner` | 轮播图表 | id, title, image_url, link, sort_order, status |
| `announcement` | 公告表 | id, title, content, priority, publish_time |
| `sys_file` | 文件记录表 | id, original_name, file_path, size, mime_type |

### 数据库迁移

项目使用 Flyway 管理数据库版本，迁移脚本位于 `src/main/resources/db/migration/`。

执行迁移：
```bash
# 应用启动时自动执行
# 或手动执行
./mvnw flyway:migrate
```

---

## 功能模块详解

### 1. AI 健身计划生成

基于用户身体数据和健身目标，利用大语言模型生成个性化健身计划。

**流程：**
1. 用户填写身体数据（身高、体重、年龄、健身目标等）
2. 系统构建 Prompt 模板
3. 调用 DashScope API 生成计划
4. 解析并保存计划到数据库

**核心代码：**
```java
@Service
public class FitnessPlanServiceImpl implements FitnessPlanService {
    
    @Autowired
    private DashScopeChatModel chatModel;
    
    public FitnessPlan generatePlan(PlanGenerateDTO dto) {
        // 构建 Prompt
        String prompt = PromptTemplates.buildFitnessPlanPrompt(dto);
        
        // 调用 AI
        String response = chatModel.call(prompt);
        
        // 解析并保存
        return parseAndSave(response, dto.getUserId());
    }
}
```

### 2. RAG 知识库

基于向量检索的健身知识问答系统。

**架构：**
- **文档处理**：上传文档 → 文本提取 → 分块处理
- **向量生成**：使用 Ollama Embedding 模型生成向量
- **向量存储**：使用 pgvector 扩展存储向量
- **相似度检索**：基于余弦相似度检索相关文档

**使用：**
```bash
# 1. 上传知识文档
POST /api/v1/knowledge/documents

# 2. RAG 检索问答
POST /api/v1/knowledge/rag/search
{
  "query": "如何正确进行深蹲？",
  "topK": 5
}
```

### 3. AI 健身助手

支持流式对话的 AI 健身顾问。

**技术实现：**
- 使用 WebFlux 实现响应式编程
- SSE (Server-Sent Events) 实现流式输出
- 上下文管理维护对话历史

**前端调用示例：**
```javascript
const eventSource = new EventSource(
  `/api/v1/chat/messages/stream?sessionId=${sessionId}&message=${encodeURIComponent(message)}`
);

eventSource.onmessage = (event) => {
  const data = JSON.parse(event.data);
  appendMessage(data.content);
};
```

### 4. 用户权限系统

基于 RBAC (Role-Based Access Control) 的权限控制。

**角色定义：**
- `ADMIN` - 管理员：拥有所有权限
- `COACH` - 教练：管理课程、查看学员
- `MEMBER` - 会员：预约课程、生成计划

**权限注解使用：**
```java
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<Void> deleteUser(Long id) { }

@PreAuthorize("hasAnyRole('COACH', 'ADMIN')")
public ResponseEntity<Void> createCourse(CourseDTO dto) { }
```

### 5. 数据可视化仪表盘

提供丰富的运营数据可视化。

**数据指标：**
- 会员总数、新增会员趋势
- 课程预约统计
- 器材使用率
- 收入统计
- AI 生成的分析报告

---

## 部署指南

### 生产环境配置

#### 1. 环境变量配置

创建 `.env` 文件：
```bash
# 数据库
DB_HOST=prod-db-host
DB_PORT=5432
DB_NAME=fitness_prod
DB_USERNAME=fitness_user
DB_PASSWORD=your-strong-password

# Redis
REDIS_HOST=prod-redis-host
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password

# MinIO
MINIO_ENDPOINT=https://minio.your-domain.com
MINIO_ACCESS_KEY=your-access-key
MINIO_SECRET_KEY=your-secret-key
MINIO_BUCKET=fitness-prod

# AI
AI_DASHSCOPE_API_KEY=your-production-api-key

# JWT
JWT_SECRET=your-256-bit-production-secret
JWT_EXPIRATION=86400000
```

#### 2. JVM 参数优化

```bash
java -jar \
  -Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -Dspring.profiles.active=prod \
  ai-powered-fitness-system.jar
```

#### 3. Nginx 配置

```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态资源
    location / {
        root /var/www/fitness-frontend;
        try_files $uri $uri/ /index.html;
    }
    
    # 后端 API 代理
    location /api/ {
        proxy_pass http://localhost:8088/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### Docker 部署

```bash
# 构建镜像
./mvnw clean package -DskipTests
docker build -t fitness-system:latest .

# 运行容器
docker run -d \
  -p 8080:8080 \
  --name fitness-system \
  --env-file .env \
  -v /var/log/fitness:/app/logs \
  fitness-system:latest
```

---

## 开发规范

### 代码规范

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化代码
- **强制构造器注入**，禁用 `@Autowired` 字段注入
- 统一使用 `Result<T>` 包装响应
- 接口统一前缀 `/api/v1`

### 包结构规范

```
com.fitness.modules.xxx/
├── controller/          # 控制器层
├── service/            # 服务接口
│   └── impl/          # 服务实现
├── mapper/            # 数据访问层
├── model/
│   ├── entity/        # 实体类
│   ├── dto/           # 数据传输对象
│   └── vo/            # 视图对象
└── enums/             # 枚举类
```

### Git 提交规范

```
feat: 新功能
fix: 修复 bug
docs: 文档变更
style: 代码格式（不影响代码运行的变动）
refactor: 代码重构
perf: 性能优化
test: 测试相关
chore: 构建过程或辅助工具的变动
```

示例：
```bash
git commit -m "feat: 添加 AI 健身计划生成功能"
git commit -m "fix: 修复用户登录时 token 过期问题"
git commit -m "docs: 更新 API 文档"
```

### 分支策略

- `main` - 主分支，稳定版本
- `develop` - 开发分支
- `feature/*` - 功能分支
- `hotfix/*` - 热修复分支

---

## 常见问题

### Q1: 如何获取阿里云 DashScope API Key？

访问 [阿里云 DashScope](https://dashscope.aliyun.com/) 控制台，创建 API Key。

### Q2: pgvector 扩展如何安装？

使用 pgvector 镜像已内置扩展：
```bash
docker pull pgvector/pgvector:pg16
```

手动安装：
```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

### Q3: Ollama Embedding 模型如何配置？

1. 安装 Ollama：
```bash
curl -fsSL https://ollama.com/install.sh | sh
```

2. 拉取 Embedding 模型：
```bash
ollama pull embeddinggemma:300m
```

3. 配置应用连接 Ollama

### Q4: 如何重置数据库？

```bash
# 停止应用
# 清理 Flyway 历史
DELETE FROM flyway_schema_history;

# 删除所有表（谨慎操作）
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

# 重启应用，Flyway 会自动重新执行迁移
```

### Q5: 前端构建失败怎么办？

```bash
# 清理缓存
cd frontend
rm -rf node_modules package-lock.json
npm cache clean --force

# 重新安装
npm install
npm run build
```

---

## 更新日志

### v1.0.0 (2024-XX-XX)

- ✨ 初始版本发布
- 🤖 集成 Spring AI Alibaba
- 👥 多角色用户系统
- 📚 课程预约管理
- 🏋️ 器材管理
- 🛒 商品订单系统
- 📊 数据可视化仪表盘
- 🔍 RAG 知识库

---

## 贡献指南

1. **Fork** 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 **Pull Request**

### 贡献者

感谢所有为项目做出贡献的开发者！

---

## 许可证

本项目基于 [MIT](LICENSE) 许可证开源。

```
MIT License

Copyright (c) 2024 AI-Powered Fitness System

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

## 联系方式

- **作者**: wu.zhongpeng
- **邮箱**: [your-email@example.com]
- **项目主页**: https://gitee.com/wuzhongpengcode/ai-powered-fitness-system

---

<p align="center">
  如果这个项目对您有帮助，请给个 ⭐ Star 支持一下！
</p>

<p align="center">
  <a href="https://gitee.com/wuzhongpengcode/ai-powered-fitness-system/stargazers">
    <img src="https://gitee.com/wuzhongpengcode/ai-powered-fitness-system/badge/star.svg?theme=dark" alt="Star">
  </a>
  <a href="https://gitee.com/wuzhongpengcode/ai-powered-fitness-system/members">
    <img src="https://gitee.com/wuzhongpengcode/ai-powered-fitness-system/badge/fork.svg?theme=dark" alt="Fork">
  </a>
</p>
