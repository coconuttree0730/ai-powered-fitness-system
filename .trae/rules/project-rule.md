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


## 5. 项目开发中严格遵守的规则

- 当前项目使用postgresql 16 （包含向量扩展(pgvector)作为业务数据和向量数据的存储; 数据库操作(DDL,DML,查询,事务)时，必须严格使用postgresql的语法和特性，且使用MyBatis-Plus进行数据库操作,必须使用Postgresql支持的语法进行数据库操作
- 项目使用redis 7.2 作为缓存层，缓存数据必须严格遵守redis的语法和特性
- 类型转换：
  - 所有从数据库查询到的实体对象，必须在转换为VO对象之前，进行必要的类型转换。
  - 所有从VO对象转换为实体对象的操作，必须在存储到数据库之前，进行必要的类型转换。
  - 使用Hutool工具的相关包工具进行类型转换，如`BeanUtil.copyProperties`等。

---
