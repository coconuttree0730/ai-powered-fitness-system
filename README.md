# AI-Powered Fitness System 智能健身房管理系统

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.0-green?logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3.5.30-blue?logo=vue.js" alt="Vue3">
  <img src="https://img.shields.io/badge/JDK-17-orange?logo=openjdk" alt="JDK17">
  <img src="https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Redis-7.2-red?logo=redis" alt="Redis">
  <img src="https://img.shields.io/badge/MinIO-Latest-cyan?logo=minio" alt="MinIO">
  <img src="https://img.shields.io/badge/Spring%20AI%20Alibaba-1.1.2.0-purple" alt="Spring AI Alibaba">
  <img src="https://img.shields.io/badge/MyBatis--Plus-3.5.7-yellow" alt="MyBatis-Plus">
  <img src="https://img.shields.io/badge/Flyway-10.14.0-orange" alt="Flyway">
  <img src="https://img.shields.io/badge/Alipay-4.38.10-blue?logo=alipay" alt="Alipay">
</p>

<p align="center">
  <b>基于 Spring Boot + Vue3 的全栈智能健身房管理系统</b>
</p>

<p align="center">
  集成 AI 智能健身计划生成、RAG 知识库问答、课程预约、器材管理、会员卡系统、支付宝支付、数据可视化分析等核心功能，
  <br>
  为健身房提供一站式数字化运营管理解决方案。
</p>

---

## 项目简介

AI-Powered Fitness System 是一个现代化的智能健身房管理系统，采用前后端分离架构，为健身房提供全方位的数字化管理解决方案。

### 系统角色

| 角色 | 说明 | 主要功能 |
|------|------|----------|
| **游客** | 未登录用户 | 浏览首页、查看公开课程、查看器材、查看教练信息 |
| **会员** | 普通注册用户 | 预约课程、生成 AI 健身计划、AI 健身助手、器材报修、购买商品、购买会员卡 |
| **教练** | 健身教练员 | 管理课程、查看学员、管理排期、个人主页管理 |
| **管理员** | 系统管理员 | 用户管理、课程管理、器材管理、数据分析、系统配置、会员卡管理 |

---

## 核心特性

### AI 智能功能

| 功能 | 描述 | 技术实现 |
|------|------|----------|
| **AI 健身计划生成** | 根据用户身体数据、健身目标自动生成个性化周度健身计划 | Spring AI Alibaba + DashScope |
| **AI 健身助手** | 支持流式对话的 AI 健身顾问，提供实时健身指导，支持长期记忆 | WebFlux + SSE 流式响应 + 长期记忆 |
| **RAG 知识库** | 基于向量检索的健身知识问答系统 | pgvector + Ollama Embedding |
| **AI 文本润色** | 智能文本优化与内容生成 | LLM 文本生成 |
| **AI 数据分析** | 智能运营数据分析与报告生成 | 数据可视化 + AI 分析 |
| **AI Agent 工具调用** | 支持天气查询、课程查询、教练查询等工具调用 | Spring AI Agent Framework |

### 业务功能模块

| 模块 | 功能描述 |
|------|----------|
| **用户管理** | 多角色体系（管理员/教练/会员），支持滑块验证码、短信验证码登录、邮箱验证 |
| **课程管理** | 课程发布、分类管理、教练关联、课程预约与取消、视频课程管理 |
| **器材管理** | 器材信息维护、状态跟踪、报修管理、维修记录 |
| **商品管理** | 健身商品管理、订单系统、库存管理 |
| **会员卡系统** | 会员卡类型管理、会员卡购买、会员权益管理、支付宝支付集成 |
| **数据可视化** | 运营数据分析、ECharts 图表展示、AI 智能报告 |
| **内容管理** | 轮播图管理、公告发布、字典管理 |
| **知识库管理** | 文档上传、向量分块、RAG 检索问答 |
| **支付系统** | 支付宝沙箱支付集成、订单管理、支付回调处理 |

### 技术亮点

- **AI 集成**: 采用 Spring AI Alibaba 实现 AI 智能推荐与对话，支持 Agent 工具调用
- **向量检索**: 使用 pgvector 扩展支持向量数据存储与相似度检索
- **对象存储**: MinIO 实现文件管理，支持图片/视频存储
- **支付集成**: 支付宝 SDK 集成，支持沙箱环境支付
- **短信服务**: 阿里云短信服务集成，支持短信验证码登录
- **权限控制**: 基于 RBAC 的细粒度权限控制体系
- **前后端分离**: RESTful API 设计，Vue3 单页应用
- **安全认证**: JWT + Spring Security 安全认证，支持 Token 刷新
- **数据库版本控制**: Flyway 管理数据库迁移
- **长期记忆**: AI 聊天支持长期记忆功能，提升对话连续性

---

## 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.0 | 基础框架 |
| Spring AI Alibaba | 1.1.2.0 | AI 集成框架（DashScope） |
| Spring Security | 6.x | 认证授权 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| Flyway | 10.14.0 | 数据库版本控制 |
| PostgreSQL | 16+ | 业务数据库（含 pgvector） |
| Redis | 7.2+ | 缓存与会话存储 |
| Redisson | 3.45.0 | Redis 分布式锁与高级功能 |
| MinIO | 8.5.11 | 对象存储 |
| JWT | 0.12.6 | 身份认证 |
| Hutool | 5.8.25 | 工具库 |
| Lombok | Latest | 代码简化 |
| Ollama | Latest | 本地 Embedding 模型 |
| Alipay SDK | 4.38.10 | 支付宝支付 |
| 阿里云短信 SDK | 2.0.0 | 短信验证码服务 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.30 | 前端框架 |
| Vue Router | 5.0.3 | 路由管理 |
| Pinia | 3.0.4 | 状态管理 |
| Vite | 8.0.0 | 构建工具 |
| Naive UI | 2.44.1 | UI 组件库（会员端/教练端） |
| Element Plus | 2.13.5 | 组件库（管理端） |
| ECharts | 6.0.0 | 图表库 |
| Axios | 1.13.6 | HTTP 客户端 |
| Marked | 17.0.5 | Markdown 渲染 |
| MarkStream Vue | 0.0.14-beta.5 | 流式文本渲染 |
| DOMPurify | 3.3.3 | XSS 防护 |
| Vue3 Slider Verify | 1.0.5 | 滑块验证码 |

---

## 项目结构

```
ai-powered-fitness-system/
├── src/main/java/com/fitness/
│   ├── FitnessApplication.java          # 应用入口
│   ├── common/                          # 公共模块
│   │   ├── config/                      # 通用配置
│   │   ├── constants/                   # 常量定义
│   │   ├── exception/                   # 异常处理
│   │   ├── mybatis/                     # MyBatis 类型处理器
│   │   ├── result/                      # 统一响应封装
│   │   └── utils/                       # 工具类
│   ├── config/                          # 框架配置
│   │   ├── SecurityConfig.java          # 安全配置
│   │   ├── MybatisPlusConfig.java       # MyBatis-Plus配置
│   │   ├── RedisConfig.java             # Redis配置
│   │   └── ...
│   ├── integration/                     # 第三方集成
│   │   ├── ai/                          # Spring AI Alibaba
│   │   │   ├── agent/                   # AI Agent 配置
│   │   │   ├── config/                  # AI配置
│   │   │   ├── controller/              # AI接口
│   │   │   ├── model/                   # AI DTO/VO
│   │   │   ├── prompt/                  # Prompt模板
│   │   │   └── service/                 # AI服务
│   │   ├── minio/                       # MinIO文件存储
│   │   ├── payment/                     # 支付宝支付
│   │   ├── security/                    # JWT安全
│   │   └── sms/                         # 阿里云短信
│   └── modules/                         # 业务模块
│       ├── analysis/                    # 分析报表模块
│       ├── announcement/                # 公告模块
│       ├── banner/                      # 轮播图模块
│       ├── booking/                     # 预约模块
│       ├── chat/                        # AI聊天模块（含长期记忆）
│       ├── coach/                       # 教练模块
│       ├── course/                      # 课程模块（含视频课程）
│       ├── dashboard/                   # 仪表盘模块
│       ├── equipment/                   # 器材模块
│       ├── knowledge/                   # 知识库模块
│       ├── membership/                  # 会员卡模块
│       ├── plan/                        # 健身计划模块
│       ├── product/                     # 商品模块
│       ├── system/                      # 系统字典模块
│       └── user/                        # 用户模块（认证/权限/档案）
├── src/main/resources/
│   ├── db/migration/                    # Flyway迁移脚本
│   ├── mapper/                          # MyBatis XML
│   ├── application.yml                  # 应用配置
│   └── application-dev.yml              # 开发环境配置
├── frontend/                            # 前端项目
│   ├── src/
│   │   ├── api/                         # API接口
│   │   ├── components/                  # 公共组件
│   │   ├── layouts/                     # 布局组件
│   │   ├── router/                      # 路由配置
│   │   ├── stores/                      # Pinia状态
│   │   ├── styles/                      # 样式文件
│   │   ├── utils/                       # 工具函数
│   │   └── views/                       # 页面视图
│   │       ├── admin/                   # 管理端页面
│   │       ├── coach/                   # 教练端页面
│   │       ├── member/                  # 会员端页面
│   │       └── public/                  # 公开页面
│   ├── package.json
│   └── vite.config.js
├── docker/                              # Docker 配置
│   └── fitness-ai-env/
│       ├── docker-compose.yml           # 服务编排
│       ├── redis.conf                   # Redis配置
│       └── initdb.d/                    # 数据库初始化脚本
├── knowledge_base_split/                # 知识库文档
├── script/                              # 脚本工具
│   └── setup-env/                       # 环境初始化脚本
├── .trae/                               # 项目文档
│   ├── specs/                           # 规格说明书
│   └── documents/                       # 开发文档
├── pom.xml                              # Maven配置
└── README.md                            # 项目说明
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
| Redis | 7.2+ | 缓存服务 |
| MinIO | Latest | 对象存储服务 |
| Ollama | Latest | 本地 Embedding 模型（可选） |

### 1. 克隆项目

```bash
git clone https://gitee.com/wuzhongpengcode/ai-powered-fitness-system.git
cd ai-powered-fitness-system
```

### 2. 配置基础设施

```bash
# 进入项目 docker 目录
cd docker/fitness-ai-env
# 启动所有服务
docker-compose up -d
```

这将启动以下服务：

| 服务 | 端口 | 说明 |
|------|------|------|
| PostgreSQL | 5432 | 业务数据库（含 pgvector） |
| Redis | 6379 | 缓存服务 |
| MinIO | 9000/9001 | 对象存储/API 控制台 |
| RabbitMQ | 5672/15672 | 消息队列/管理界面 |
| MySQL | 3306 | 备用数据库 |
| Ollama | 11434 | 本地 Embedding 模型 |
| Nginx | 9080/9443 | 反向代理 |

**默认凭据：**

- PostgreSQL: `fitness_user` / `myPostgresPass123`
- Redis: `myRedisPass123`
- MinIO: `minioadmin` / `minioPass123`
- RabbitMQ: `fitness_rabbit` / `myRabbitPass123`

### 3. 配置应用

编辑 `src/main/resources/application-dev.yml`：

```yaml
server:
  port: 8088

spring:
  application:
    name: ai-powered-fitness-system
  
  datasource:
    url: jdbc:postgresql://localhost:5432/fitness_ai_db
    username: fitness_user
    password: myPostgresPass123
    driver-class-name: org.postgresql.Driver
  
  data:
    redis:
      host: localhost
      port: 6379
      password: myRedisPass123
      database: 0
  
  ai:
    dashscope:
      api-key: your-dashscope-api-key  # 阿里云 DashScope API Key
      chat:
        options:
          model: tongyi-xiaomi-analysis-flash
          temperature: 0.7
          top-p: 0.8
          max-tokens: 5000
    ollama:
      base-url: http://localhost:11434
      embedding:
        model: embeddinggemma:300m

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioPass123
  bucket-name: fitness-bucket

alipay:
  app-id: your-alipay-app-id
  private-key: your-private-key
  alipay-public-key: your-alipay-public-key
  server-url: https://openapi.alipaydev.com/gateway.do
  notify-url: http://your-domain/api/v1/payment/alipay/notify
  return-url: http://your-domain/payment/return

jwt:
  secret: your-256-bit-secret-key-for-jwt-signing-must-be-at-least-32-characters-long
  expiration: 86400000  # 24小时
  refresh-expiration: 604800000  # 7天

aliyun:
  sms:
    access-key-id: your-access-key-id
    access-key-secret: your-access-key-secret
```

### 4. 运行后端

```bash
# 使用 Maven Wrapper
./mvnw spring-boot:run

# 或打包后运行
./mvnw clean package -DskipTests
java -jar target/ai-powered-fitness-system-1.0.0.jar
```

后端服务将启动在 `http://localhost:8088`

### 5. 运行前端

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器将启动在 `http://localhost:5173`

---

## 配置说明

### 环境变量配置

项目支持通过环境变量覆盖配置：

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DB_HOST` | 数据库主机 | localhost |
| `DB_PORT` | 数据库端口 | 5432 |
| `DB_NAME` | 数据库名称 | fitness_ai_db |
| `DB_USERNAME` | 数据库用户名 | fitness_user |
| `DB_PASSWORD` | 数据库密码 | myPostgresPass123 |
| `AI_DASHSCOPE_API_KEY` | 阿里云 DashScope API Key | - |
| `AI_DASHSCOPE_MODEL` | AI 模型名称 | tongyi-xiaomi-analysis-flash |
| `OLLAMA_BASE_URL` | Ollama 服务地址 | http://localhost:11434 |
| `OLLAMA_EMBEDDING_MODEL` | Embedding 模型 | embeddinggemma:300m |
| `MINIO_ENDPOINT` | MinIO 服务地址 | http://localhost:9000 |
| `MINIO_ACCESS_KEY` | MinIO Access Key | minioadmin |
| `MINIO_SECRET_KEY` | MinIO Secret Key | minioPass123 |
| `MINIO_BUCKET` | MinIO 存储桶 | fitness-bucket |
| `JWT_SECRET` | JWT 签名密钥 | - |
| `JWT_EXPIRATION` | Token 过期时间 | 86400000 |
| `JWT_REFRESH_EXPIRATION` | Refresh Token 过期时间 | 604800000 |
| `ALIPAY_APP_ID` | 支付宝 App ID | - |
| `ALIPAY_PRIVATE_KEY` | 支付宝私钥 | - |
| `ALIPAY_PUBLIC_KEY` | 支付宝公钥 | - |
| `ALIYUN_SMS_ACCESS_KEY_ID` | 阿里云短信 Access Key | - |
| `ALIYUN_SMS_ACCESS_KEY_SECRET` | 阿里云短信 Secret | - |

---

## API 文档

后端启动后，访问 Swagger UI：

```
http://localhost:8088/swagger-ui.html
```

### 核心接口概览

#### 认证接口

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/v1/auth/register` | POST | 用户注册 | 公开 |
| `/api/v1/auth/login` | POST | 账号密码登录 | 公开 |
| `/api/v1/auth/login/sms` | POST | 短信验证码登录 | 公开 |
| `/api/v1/auth/slider-verify/token` | GET | 获取滑块验证 | 公开 |
| `/api/v1/auth/slider-verify/verify` | POST | 验证滑块 | 公开 |
| `/api/v1/auth/email/code` | POST | 发送邮箱验证码 | 公开 |
| `/api/v1/auth/password/reset` | POST | 重置密码 | 公开 |

#### 用户接口

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/v1/users/me` | GET | 获取当前用户 | 已登录 |
| `/api/v1/users/me/profile` | PUT | 更新用户信息 | 已登录 |
| `/api/v1/users/me/password` | PUT | 修改密码 | 已登录 |
| `/api/v1/users/me/fitness-profile` | GET/PUT | 健身档案管理 | 已登录 |
| `/api/v1/admin/users` | GET | 用户列表 | ADMIN |
| `/api/v1/admin/users/{id}` | GET/PUT/DELETE | 用户管理 | ADMIN |

#### 课程接口

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/v1/courses/public/list` | GET | 公开课程列表 | 公开 |
| `/api/v1/courses/public/{id}` | GET | 课程详情 | 公开 |
| `/api/v1/courses` | POST | 创建课程 | ADMIN/COACH |
| `/api/v1/courses/{id}` | PUT/DELETE | 课程管理 | ADMIN/COACH |
| `/api/v1/video-courses` | GET/POST | 视频课程 | ADMIN/COACH |
| `/api/v1/bookings` | POST | 预约课程 | MEMBER |
| `/api/v1/bookings/my` | GET | 我的预约 | MEMBER |

#### AI 接口

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/v1/ai/fitness-plan` | POST | AI生成健身计划 | MEMBER |
| `/api/v1/ai/text-polish` | POST | AI文本润色 | 已登录 |
| `/api/v1/chat/messages/stream` | POST | AI流式对话 | MEMBER |
| `/api/v1/chat/sessions` | GET/POST | 会话管理 | MEMBER |
| `/api/v1/knowledge/rag/search` | POST | RAG知识检索 | 公开 |

#### 会员卡接口

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/v1/membership-cards` | GET | 会员卡列表 | 公开 |
| `/api/v1/membership-cards/{id}` | GET | 会员卡详情 | 公开 |
| `/api/v1/membership-orders` | POST | 创建订单 | MEMBER |
| `/api/v1/membership-orders/{id}/pay` | POST | 支付宝支付 | MEMBER |
| `/api/v1/admin/membership-cards` | GET/POST | 会员卡管理 | ADMIN |

#### 管理接口

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/api/v1/dashboard/stats` | GET | 仪表盘统计 | ADMIN |
| `/api/v1/admin/analysis/report` | POST | AI数据分析 | ADMIN |
| `/api/v1/admin/equipment` | GET/POST | 器材管理 | ADMIN |
| `/api/v1/admin/products` | GET/POST | 商品管理 | ADMIN |
| `/api/v1/admin/announcements` | GET/POST | 公告管理 | ADMIN |
| `/api/v1/admin/banners` | GET/POST | 轮播图管理 | ADMIN |
| `/api/v1/admin/dict` | GET/POST | 字典管理 | ADMIN |
| `/api/v1/admin/video-courses` | GET/POST | 视频课程管理 | ADMIN |

---

## 数据库设计

### 核心表结构

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| `sys_user` | 用户表 | id, username, phone, password, email, avatar, status, points, balance |
| `sys_role` | 角色表 | id, name, code, description |
| `sys_permission` | 权限表 | id, name, code, type, parent_id |
| `sys_user_role` | 用户角色关联 | user_id, role_id |
| `sys_role_permission` | 角色权限关联 | role_id, permission_id |
| `sys_dict` | 字典表 | id, dict_code, dict_name, description |
| `sys_dict_item` | 字典项表 | id, dict_id, item_code, item_value, sort_order |
| `fitness_course` | 课程表 | id, name, description, coach_id, category, schedule, capacity |
| `fitness_video_course` | 视频课程表 | id, title, description, video_url, cover_image, duration |
| `fitness_booking` | 预约表 | id, user_id, course_id, status, booking_time |
| `fitness_equipment` | 器材表 | id, name, type, location, status, images, description |
| `fitness_equipment_repair` | 器材报修表 | id, equipment_id, user_id, description, images, status |
| `fitness_repair_record` | 维修记录表 | id, repair_id, handler_id, action, result, cost |
| `fitness_plan` | 健身计划表 | id, user_id, name, goal, ai_generated, status |
| `fitness_plan_detail` | 健身计划详情表 | id, plan_id, day, exercises, duration, calories |
| `coach_detail` | 教练详情表 | user_id, tags, work_years, specialties, certifications |
| `product` | 商品表 | id, name, category, price, stock, description, images |
| `product_order` | 商品订单表 | id, user_id, product_id, quantity, total_price, status |
| `membership_card` | 会员卡表 | id, type_id, name, price, duration_days, points_reward, status |
| `membership_card_type` | 会员卡类型表 | id, code, name, description |
| `membership_order` | 会员订单表 | id, user_id, card_id, total_price, status, pay_time |
| `user_membership` | 用户会员关系表 | id, user_id, card_id, start_date, end_date, status |
| `chat_session` | 聊天会话表 | id, user_id, title, last_message_time |
| `chat_message` | 聊天消息表 | id, session_id, role, content, create_time |
| `chat_long_term_memory` | 长期记忆表 | id, user_id, memory_content, memory_type, importance |
| `knowledge_category` | 知识分类表 | id, name, description, sort_order |
| `knowledge_document` | 知识文档表 | id, category_id, title, content, file_url, status |
| `knowledge_chunk` | 知识分块表 | id, document_id, content, vector(1536), chunk_index |
| `sys_banner` | 轮播图表 | id, title, image_url, link, sort_order, status |
| `sys_announcement` | 公告表 | id, title, content, type, priority, publish_time |
| `sys_file` | 文件记录表 | id, file_name, original_name, file_url, file_size, mime_type |
| `user_fitness_profile` | 用户健身档案 | user_id, height, weight, age, gender, fitness_goal, experience |
| `analysis_report` | 分析报告表 | id, type, title, content, data_snapshot, create_time |

数据库迁移使用 Flyway 管理，脚本位于 `src/main/resources/db/migration/`。

---

## 功能模块详解

### 1. 用户认证与权限管理

- **多角色体系**: 支持管理员、教练、会员、游客四种角色
- **多种登录方式**: 账号密码、短信验证码、滑块验证码
- **安全机制**: JWT Token、密码加密、登录限流、Token 刷新机制
- **权限控制**: 基于 RBAC 的细粒度权限控制

### 2. 课程管理与预约

- **课程发布**: 管理员/教练可发布课程，设置时间、容量、分类
- **视频课程**: 支持上传和管理视频课程内容
- **课程预约**: 会员可查看课程列表，预约感兴趣的课程
- **预约管理**: 支持取消预约，自动释放名额
- **分类筛选**: 支持按课程分类、时间筛选

### 3. AI 智能健身计划

- **信息收集**: 收集用户身高、体重、年龄、健身经验等信息
- **目标设定**: 支持增肌、减脂、塑形等多种健身目标
- **AI 生成**: 调用 LLM 生成个性化周度健身计划
- **计划展示**: 精美的前端展示效果，支持按天查看

### 4. 器材管理

- **器材信息**: 维护器材名称、类型、位置、状态、使用说明
- **状态跟踪**: 实时显示器材可用、占用、维修等状态
- **报修管理**: 会员可上报器材故障，上传图片
- **维修记录**: 管理员可处理报修，记录维修过程

### 5. 商品与会员卡系统

- **商品管理**: 支持商品增删改查，设置价格、库存
- **订单系统**: 会员可下单购买商品
- **会员卡管理**: 支持多种会员卡类型，设置价格、时长、权益
- **会员权益**: 积分奖励、专属优惠等

### 6. 支付系统

- **支付宝集成**: 支付宝沙箱支付，支持扫码支付
- **订单管理**: 创建订单、查询订单、处理支付回调
- **超时处理**: 订单超时自动取消，释放库存

### 7. AI 健身助手

- **流式对话**: 支持 SSE 流式响应，实时显示 AI 回复
- **上下文记忆**: 维护对话历史，支持多轮对话
- **长期记忆**: 记住用户偏好和重要信息，提升个性化体验
- **工具调用**: 支持天气查询、课程查询、教练查询等工具
- **课程推荐**: 根据用户需求推荐相关课程
- **器材推荐**: 根据训练目标推荐合适器材

### 8. RAG 知识库

- **文档管理**: 支持上传健身知识文档
- **智能分块**: 自动将文档分块并生成向量
- **向量检索**: 基于 pgvector 实现相似度检索
- **问答系统**: 支持自然语言提问，返回精准答案

### 9. 数据可视化分析

- **运营数据**: 展示会员卡销量、用户增长、课程预约等数据
- **图表展示**: 使用 ECharts 展示各类统计图表
- **AI 分析**: 调用 LLM 分析运营数据，生成智能报告
- **高峰时段**: 分析用户到店高峰时间

---

## 开发规范

### 代码规范

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化代码
- 强制构造器注入，禁用 `@Autowired` 字段注入
- 统一使用 `Result<T>` 包装响应
- 接口统一前缀 `/api/v1`
- 数据库字段使用下划线命名，Java 实体使用驼峰命名
- 时间字段统一使用 `create_time` / `update_time`

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

### 分支策略

- `main` - 主分支，稳定版本
- `develop` - 开发分支
- `feature/*` - 功能分支
- `hotfix/*` - 热修复分支

---

## 部署指南

### 生产环境配置

1. **配置 SSL 证书**
2. **启用数据库连接池监控**
3. **配置日志收集（ELK）**
4. **设置 JVM 参数优化**

### Docker 部署

```bash
# 构建镜像
./mvnw clean package -DskipTests
docker build -t fitness-system:latest .

# 运行容器
docker run -d \
  -p 8088:8088 \
  --name fitness-system \
  --env-file .env \
  fitness-system:latest
```

### 环境变量文件 `.env` 示例

```bash
# Database
DB_HOST=postgres
DB_PORT=5432
DB_NAME=fitness_ai_db
DB_USERNAME=fitness_user
DB_PASSWORD=your_secure_password

# Redis
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# AI
AI_DASHSCOPE_API_KEY=your_api_key

# MinIO
MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=your_secure_password

# JWT
JWT_SECRET=your-256-bit-secret-key
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Alipay
ALIPAY_APP_ID=your_app_id
ALIPAY_PRIVATE_KEY=your_private_key
ALIPAY_PUBLIC_KEY=your_public_key

# Aliyun SMS
ALIYUN_SMS_ACCESS_KEY_ID=your_access_key
ALIYUN_SMS_ACCESS_KEY_SECRET=your_secret
```

---

## 常见问题

### Q: 如何获取阿里云 DashScope API Key？

A: 访问 [阿里云 DashScope](https://dashscope.aliyun.com/) 控制台，创建 API Key。

### Q: pgvector 扩展如何启用？

A: 项目使用 `pgvector/pgvector:pg16` 镜像，已内置 pgvector 扩展。如需手动启用，执行：

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

### Q: 前端跨域问题如何解决？

A: 后端已配置 CORS，如需修改，编辑 `SecurityConfig.java` 中的 CORS 配置。

### Q: 如何重置管理员密码？

A: 执行 SQL 更新密码（使用 BCrypt 加密）：

```sql
UPDATE sys_user SET password = '$2a$10$...' WHERE username = 'admin';
```

### Q: Ollama Embedding 模型如何下载？

A: 进入 WSL 执行：

```bash
ollama pull embeddinggemma:300m
```

### Q: 支付宝沙箱环境如何配置？

A: 
1. 登录 [支付宝开放平台](https://open.alipay.com/)
2. 进入沙箱环境获取 APP ID、私钥和公钥
3. 配置回调地址（需外网可访问或使用内网穿透工具）

---

## 贡献指南

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

---

## 许可证

本项目基于 [MIT](LICENSE) 许可证开源。

---

## 联系方式

- **作者**: wu.zhongpeng
- **邮箱**: [your-email@example.com]
- **项目主页**: https://gitee.com/wuzhongpengcode/ai-powered-fitness-system

---

<p align="center">
  如果这个项目对您有帮助，请给个 ⭐ Star 支持一下！
</p>
