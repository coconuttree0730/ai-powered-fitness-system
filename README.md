# AI-Powered Fitness System 智能健身房管理系统

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.0-green?logo=springboot" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Vue-3.5.30-blue?logo=vue.js" alt="Vue3">
  <img src="https://img.shields.io/badge/JDK-17-orange?logo=openjdk" alt="JDK17">
  <img src="https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/Redis-7.2-red?logo=redis" alt="Redis">
  <img src="https://img.shields.io/badge/Spring%20AI%20Alibaba-1.1.2.0-purple" alt="Spring AI Alibaba">
  <img src="https://img.shields.io/badge/MyBatis--Plus-3.5.7-yellow" alt="MyBatis-Plus">
</p>

<p align="center">
  <b>基于 Spring Boot + Vue3 的全栈智能健身房管理系统</b>
</p>

---

## 项目简介

AI-Powered Fitness System 是一个现代化的智能健身房管理系统，采用前后端分离架构，为健身房提供全方位的数字化管理解决方案。

### 系统角色

| 角色 | 说明 | 主要功能 |
|------|------|----------|
| **游客** | 未登录用户 | 浏览首页、查看公开课程、器材、教练信息 |
| **会员** | 普通注册用户 | 预约课程、AI 健身计划、AI 助手、器材报修、购买商品/会员卡 |
| **教练** | 健身教练员 | 管理课程、查看学员、管理排期、个人主页 |
| **管理员** | 系统管理员 | 用户/课程/器材/商品/会员卡管理、数据分析、系统配置 |

### 核心功能

- **AI 智能功能**: 健身计划生成、流式对话助手（Memory管理）、RAG 知识库问答、Agent 工具调用
- **业务模块**: 课程预约、器材管理、商品商城、会员卡系统、支付宝支付
- **数据可视化**: 运营数据分析、ECharts 图表展示、AI 智能报告

---

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.5.0 | 基础框架 |
| Spring AI Alibaba | 1.1.2.0 | AI 集成（DashScope） |
| Spring Security | 6.x | 认证授权 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| Flyway | 10.14.0 | 数据库版本控制 |
| PostgreSQL | 16+ | 业务数据库（含 pgvector） |
| Redis | 7.2+ | 缓存与会话存储 |
| MinIO | 8.5.11 | 对象存储 |
| JWT | 0.12.6 | 身份认证 |
| Alipay SDK | 4.38.10 | 支付宝支付 |

### 前端

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

---

## 快速开始

### 环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 17+ | Java 开发工具包 |
| Maven | 3.9+ | 项目构建工具 |
| Node.js | 18+ | 前端运行环境 |
| Docker | Latest | 容器化部署（推荐） |

### 1. 克隆项目

```bash
git clone git@github.com:coconuttree0730/ai-powered-fitness-system.git
cd ai-powered-fitness-system
```

### 2. 启动基础设施

```bash
cd docker/fitness-ai-env
docker-compose up -d
```

启动后默认服务：

| 服务 | 端口 | 默认凭据 |
|------|------|----------|
| PostgreSQL | 5432 | `fitness_user` / `myPostgresPass123` |
| Redis | 6379 | `myRedisPass123` |
| MinIO | 9000/9001 | `minioadmin` / `minioPass123` |
| ningx | 9080 |- | - |
| Ollama | 11434 | - |
> 注：本地部署 Ollama 的目的是 运行 词嵌入模型，其他方案：使用模型厂的Embedding模型 

### 3. 配置环境变量

略...

### 4. 运行后端

```bash
# 使用 Maven Wrapper
./mvnw spring-boot:run

# 或打包后运行
./mvnw clean package -DskipTests
java -jar target/ai-powered-fitness-system-1.0.0.jar
```

后端服务启动在 `http://localhost:8088`

### 5. 运行前端

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器启动在 `http://localhost:3000`

### 6. 访问系统

- **前端**: http://localhost:3000
- **后端 API**: http://localhost:8088/api/v1
- **Swagger 文档**: http://localhost:8088/swagger-ui.html
- **MinIO 控制台**: http://localhost:9001

---

## 项目结构

```
ai-powered-fitness-system/
├── src/main/java/com/fitness/
│   ├── FitnessApplication.java          # 应用入口
│   ├── common/                          # 公共模块（异常处理、响应封装、工具类）
│   ├── config/                          # 框架配置（Security、MyBatis、Redis）
│   ├── integration/                     # 第三方集成（AI、MinIO、支付、短信）
│   └── modules/                         # 业务模块（15 个模块）
│       ├── user/                        # 用户模块
│       ├── course/                      # 课程模块
│       ├── booking/                     # 预约模块
│       ├── equipment/                   # 器材模块
│       ├── chat/                        # AI 聊天模块
│       ├── plan/                        # 健身计划模块
│       ├── knowledge/                   # 知识库模块
│       ├── product/                     # 商品模块
│       ├── membership/                  # 会员卡模块
│       └── ...                          # 其他模块
├── src/main/resources/
│   ├── db/migration/                    # Flyway 迁移脚本
│   └── application.yml                  # 应用配置
├── frontend/                            # 前端项目
│   ├── src/
│   │   ├── api/                         # API 接口
│   │   ├── views/                       # 页面视图（admin/member/coach/public）
│   │   ├── components/                  # 公共组件
│   │   ├── stores/                      # Pinia 状态管理
│   │   └── router/                      # 路由配置
│   ├── package.json
│   └── vite.config.js
├── docker/fitness-ai-env/               # Docker 配置
├── pom.xml                              # Maven 配置
└── README.md
```

---

## 开发规范

### 代码规范

- **后端**: 遵循 Google Java Style，使用 Lombok 简化代码，强制构造器注入
- **前端**: 管理端使用 Element Plus，会员/教练端使用 Naive UI，不可混用
- **响应格式**: 统一使用 `Result<T>` 包装，接口前缀 `/api/v1`
- **数据库**: 字段使用 `snake_case`，时间字段统一 `create_time` / `update_time`，软删除使用 `deleted` 字段

### Git 提交规范

提交信息格式：`type(scope): 主题`

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
git commit -m 'feat(user): 添加用户注册功能'
git commit -m 'fix(booking): 修复预约时间冲突问题'
git commit -m 'docs: 更新 API 文档'
```

### 分支策略

- `main` - 主分支，稳定版本
- `develop` - 开发分支
- `feature/*` - 功能分支
- `hotfix/*` - 热修复分支

---

## 常用命令

### 后端

```bash
./mvnw spring-boot:run                    # 启动应用
./mvnw clean package -DskipTests          # 构建 JAR
./mvnw test                               # 运行测试
./mvnw verify                             # Checkstyle + 测试
```

### 前端

```bash
cd frontend
npm install                               # 安装依赖
npm run dev                               # 启动开发服务器
npm run build                             # 生产构建
npm run preview                           # 预览构建结果
```

### 基础设施

```bash
cd docker/fitness-ai-env
docker-compose up -d                      # 启动所有服务
docker-compose down                       # 停止所有服务
docker-compose logs -f [service]          # 查看服务日志
```

---

## 贡献指南

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/your-feature`)
3. 提交更改 (`git commit -m 'feat: 添加新功能'`)
4. 推送到分支 (`git push origin feature/your-feature`)
5. 创建 Pull Request

### Pull Request 要求

- 确保代码通过 Checkstyle 检查 (`./mvnw verify`)
- 确保前端构建通过 (`npm run build`)
- 更新相关文档（如有必要）
- 遵循 Git 提交规范

---

## 许可证

本项目基于 [MIT](LICENSE) 许可证开源。

---

## 联系方式

- **作者**: wu.zhongpeng
- **项目主页**: https://gitee.com/wuzhongpengcode/ai-powered-fitness-system
