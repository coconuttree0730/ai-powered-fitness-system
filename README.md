# AI-Powered Fitness System 智能健身房管理系统

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.x-green" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3.x-blue" alt="Vue3">
  <img src="https://img.shields.io/badge/JDK-17-orange" alt="JDK17">
  <img src="https://img.shields.io/badge/PostgreSQL-16-blue" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Redis-7.2-red" alt="Redis">
  <img src="https://img.shields.io/badge/MinIO-Latest-cyan" alt="MinIO">
</p>

基于 Spring Boot + Vue3 的智能健身房管理系统，集成 AI 智能健身计划生成、课程预约、器材管理、数据可视化分析等核心功能，为用户提供一体化的健身服务与运营管理平台。

## 功能特性

### 核心模块

- **用户管理** - 支持多角色（管理员/教练/会员），头像上传，权限控制
- **课程管理** - 课程发布、分类管理、教练关联、图片上传
- **AI 健身计划** - 基于 Spring AI Alibaba 的智能健身计划生成
- **器材管理** - 器材信息维护、状态跟踪、报修管理
- **数据可视化** - 运营数据分析、图表展示

### 技术亮点

- 采用 Spring AI Alibaba 实现 AI 智能推荐
- 使用 pgvector 扩展支持向量数据存储
- MinIO 对象存储实现文件管理
- 基于 RBAC 的权限控制体系
- 前后端分离架构，RESTful API 设计

## 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.x | 基础框架 |
| Spring AI Alibaba | 1.1.2.0 | AI 集成框架 |
| Spring Security | 对应版本 | 认证授权 |
| MyBatis-Plus | 3.5.x | ORM 框架 |
| Flyway | 10.x | 数据库版本控制 |
| PostgreSQL | 16 | 业务数据库（含 pgvector） |
| Redis | 7.2 | 缓存层 |
| MinIO | Latest | 对象存储 |
| RabbitMQ | 3.13 | 消息队列 |
| JWT | - | 身份认证 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.x | 前端框架 |
| Vite | - | 构建工具 |
| Naive UI | - | UI 组件库 |
| Axios | - | HTTP 客户端 |
| Pinia | - | 状态管理 |

## 项目结构

```
AI-Powered-Fitness-System-v3/
├── src/main/java/com/fitness/
│   ├── common/                 # 公共模块
│   │   ├── config/            # 配置类
│   │   ├── exception/         # 异常处理
│   │   ├── result/            # 统一响应
│   │   └── utils/             # 工具类
│   ├── modules/               # 业务模块
│   │   ├── user/              # 用户模块
│   │   ├── course/            # 课程模块
│   │   ├── equipment/         # 器材模块
│   │   ├── booking/           # 预约模块
│   │   └── plan/              # 健身计划模块
│   └── integration/           # 集成模块
│       └── minio/             # MinIO 文件存储
├── src/main/resources/
│   ├── db/migration/          # Flyway 迁移脚本
│   └── mapper/                # MyBatis XML
├── frontend/                   # 前端项目
│   ├── src/
│   │   ├── api/               # API 接口
│   │   ├── views/             # 页面视图
│   │   ├── components/        # 公共组件
│   │   └── utils/             # 工具函数
│   └── package.json
└── docker-compose.yml         # Docker 编排
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.9+
- Node.js 18+
- Docker & Docker Compose

### 1. 克隆项目

```bash
git clone https://gitee.com/wuzhongpengcode/ai-powered-fitness-system.git
cd ai-powered-fitness-system
```

### 2. 启动基础设施

```bash
docker-compose up -d
```

将启动以下服务：
- PostgreSQL 16 (端口: 5432)
- Redis 7.2 (端口: 6379)
- MinIO (端口: 9000/9001)
- RabbitMQ (端口: 5672/15672)

### 3. 配置应用

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/fitness
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379

minio:
  endpoint: http://localhost:9000
  access-key: your_access_key
  secret-key: your_secret_key
  bucket-name: fitness
```

### 4. 运行后端

```bash
./mvnw spring-boot:run
```

### 5. 运行前端

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173

## API 文档

后端启动后，访问 Swagger UI：

```
http://localhost:8080/swagger-ui.html
```

### 核心接口

| 接口 | 说明 |
|------|------|
| POST /api/v1/auth/login | 用户登录 |
| POST /api/v1/auth/register | 用户注册 |
| GET /api/v1/admin/users | 用户列表（管理） |
| GET /api/v1/courses/list | 课程列表 |
| POST /api/v1/files/upload | 文件上传 |

## 数据库设计

### 核心表结构

- `sys_user` - 用户表
- `sys_role` - 角色表
- `sys_user_role` - 用户角色关联
- `fitness_course` - 课程表
- `fitness_booking` - 预约表
- `fitness_equipment` - 器材表
- `fitness_plan` - 健身计划表

数据库迁移使用 Flyway 管理，脚本位于 `src/main/resources/db/migration/`。

## 开发规范

### 代码规范

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化代码
- 强制构造器注入，禁用 `@Autowired` 字段注入
- 统一使用 `Result<T>` 包装响应

### Git 提交规范

```
feat: 新功能
fix: 修复 bug
chore: 构建/工具/杂项
docs: 文档变更
style: 代码格式
refactor: 代码重构
```

### 分支策略

- `main` - 主分支，稳定版本
- `develop` - 开发分支
- `feature/*` - 功能分支
- `hotfix/*` - 热修复分支

## 部署指南

### Docker 部署

```bash
# 构建镜像
./mvnw clean package -DskipTests
docker build -t fitness-system .

# 运行容器
docker run -p 8080:8080 --env-file .env fitness-system
```

### 生产环境配置

1. 配置 SSL 证书
2. 启用数据库连接池监控
3. 配置日志收集（ELK）
4. 设置 JVM 参数优化

## 贡献指南

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目基于 [MIT](LICENSE) 许可证开源。

## 联系方式

- 作者: wu.zhongpeng
- 邮箱: [your-email@example.com]
- 项目主页: https://gitee.com/wuzhongpengcode/ai-powered-fitness-system

---

<p align="center">如果这个项目对您有帮助，请给个 ⭐ Star 支持一下！</p>
