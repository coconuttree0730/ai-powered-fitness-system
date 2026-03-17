---
**项目名称**: AI-Powered-Fitness-System - 智能健身系统
---

## 项目概览
基于 Spring Boot框架实现 的智能健身房系统，为用户提供一体化的健身服务与运营管理平台。具体实现目标如下（MVP版本）：

+ 注册登录管理
+ 课程管理与预约
+ AI 智能健身计划生成
+ 器材管理
+ 运营数据可视化分析

## 1. 技术栈与版本
### 1.1 基础框架
| 组件 | 版本 | 说明 |
| --- | --- | --- |
| Spring Boot | 3.5.x | 基础框架 |
| JDK | 17 | Java运行时 |
| Spring AI Alibaba | 1.1.2.0 | AI集成框架 |
| Spring Security | 对应Spring Boot版本 | 认证授权 |
| Maven | 3.9.9 | 使用 `./mvnw` 包装器确保版本一致 |


### 1.2 数据层
| 组件 | 版本 | 说明 |
| --- | --- | --- |
| Flyway | 10.x | 数据库版本控制 |


### 1.3 持久层
| 组件 | 版本 | 说明 |
| --- | --- | --- |
| MyBatis-Plus | 3.5.x | ORM框架 |


---

## 2. 项目环境（WSL Docker部署）
| 组件 | Docker镜像版本 | 实际版本 | 端口 |
| --- | --- | --- | --- |
| PostgreSQL | pgvector/pgvector:pg16 | PostgreSQL 16.13 | 5432 |
| Redis | redis:7.2-alpine | Redis 7.2.13 | 6379 |
| MySQL | mysql:8.0 | MySQL 8.0.45 | 3306 |
| RabbitMQ | rabbitmq:3.13-management-alpine | RabbitMQ 3.13.7 | 5672/15672 |
| MinIO | minio/minio:latest | MinIO RELEASE.2025-09-07T16-13-09Z | 9000/9001 |
| Nginx | nginx:1.26-alpine | Nginx/1.26.3 | 9080/9443 |


---

## 3. 测试框架与要求
### 3.1 单元测试
+ **框架**: JUnit 5 + Mockito
+ **要求**: 对Service层进行充分mock，隔离外部依赖
+ **位置**: `src/test/java`
+ **规范**: 测试方法名需清晰表达测试意图

---

## 4. 核心架构与编码规范
### 4.1 分层架构
严格遵循 **Controller → Service (接口+Impl) → Mapper** 三层结构，**禁止跨层调用**。

### 4.2 API设计规范
| 项目 | 规范 | 示例 |
| --- | --- | --- |
| 基础路径 | `/api/v1/资源名`（复数形式） | `/api/v1/users` |
| 子资源路径 | `/api/v1/父资源/{父资源Id}/子资源名` | `/api/v1/users/{id}/orders` |
| 请求校验 | Controller层必须使用 `@Valid` 注解 | `@Valid UserDTO dto` |
| 响应格式 | 统一使用 `Result<T>` 包裹 | `Result.success(data)` |


### 4.3 依赖注入
+ **强制使用**: Lombok的 `@RequiredArgsConstructor` 进行构造器注入
+ **【禁用】**: `@Autowired` 字段注入

### 4.4 异常处理
+ **统一处理**: 由 `@RestControllerAdvice` 捕获处理
+ **业务异常**: 继承 `RuntimeException`
+ **错误码**: 使用枚举管理，禁止硬编码

```java
// 正确示例
throw new BusinessException(ErrorCode.ORDER_AMOUNT_ERROR);
// 错误示例
throw new BusinessException("订单金额错误"); // 禁止硬编码
```

#### Mapper规范
+ 必须继承 `BaseMapper<T>`

#### 金额处理
+ **必须使用**: `BigDecimal` 类型

### 4.5 安全规范
| 规范 | 要求 |
| --- | --- |
| 敏感信息 | **【严禁】**硬编码密钥、密码、Token，必须通过环境变量或配置中心注入 |
| 密码存储 | 必须使用 `BCryptPasswordEncoder` 进行编码 |



---

## 5. 目录结构
### 5.1 Java源码目录
```plain
src/main/java/com/[公司]/[项目]/
├── common/              # 公共模块（常量、异常、工具、通用配置）
├── modules/             # 业务模块（按业务垂直切分）
│   └── [module]/        # 业务名称（user, order, product...）
│       ├── controller/  # API层
│       ├── service/     # 业务逻辑层
│       │   └── impl/    # 实现类
│       ├── mapper/      # 数据访问层
│       └── model/       # 实体、DTO、VO、枚举
├── config/              # 框架配置（Security, Swagger）
├── integration/         # 外部服务集成（SMS, Email, OSS...）
└── 其他拓展.../  
```

### 5.2 资源文件目录
```plain
src/main/resources/
├── application.yml              # 主配置文件
├── application-[环境].yml          # 开发环境
│
├── mapper/                      # XML映射文件
│   └── [module]/
│       └── [Module]Mapper.xml
├── db/                          # 数据库相关
├── static/                      # 静态资源
└── templates/                   # 模板文件
```

---



