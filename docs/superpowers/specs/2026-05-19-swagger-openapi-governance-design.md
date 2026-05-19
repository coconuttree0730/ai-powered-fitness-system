# Swagger / OpenAPI 完整实践设计

## 1. 背景

当前项目已经接入 `springdoc-openapi-starter-webmvc-ui`，`Swagger UI` 可以正常访问，但大量接口仍缺少统一的 OpenAPI 注解，导致文档存在以下问题：

- 分组名称回退为默认值，如 `user-controller`、`auth-controller`
- 很多接口只有路径，没有清晰的中文业务摘要
- 参数、请求体、响应体字段说明缺失，前端需要翻源码理解接口
- 登录鉴权、上传、分页、状态变更等高频接口缺少统一展示方式
- 新增接口没有固定写法，文档质量依赖个人习惯

本设计用于建立一套面向前端和内部联调的 Swagger / OpenAPI 实践，并作为后续接口开发的统一规范。

## 2. 目标

本次治理目标：

- 让前端打开 `http://localhost:8088/swagger-ui/index.html` 后可以直接理解接口用途、入参和出参
- 统一 Swagger 分组、方法摘要、参数说明、DTO/VO 字段说明的写法
- 统一 JWT 鉴权、分页、上传、状态变更类接口的文档展示方式
- 为后续新增接口提供固定模板，避免继续出现默认分组和空摘要

## 3. 非目标

本次治理不包含以下内容：

- 不修改现有接口路径
- 不修改 `Result<T>` 返回结构
- 不进行业务逻辑重构
- 不调整数据库结构
- 不为 Swagger 目的改动领域模型或服务层实现

## 4. 适用范围

本规范面向当前项目的后端 HTTP API，优先服务对象为：

- 前端开发
- 后端自测与联调
- 内部接口排查与调试

不以对外开放平台文档为目标，因此强调业务可读性与联调效率，不追求过度冗长的 OpenAPI 细节描述。

## 5. 总体方案

Swagger / OpenAPI 文档治理分为五层。

### 5.1 全局 OpenAPI 层

职责：

- 定义文档标题、描述、版本、联系方式等基本信息
- 定义 JWT `Bearer Token` 安全方案
- 统一文档的基础约定和使用方式

要求：

- 新增统一的 OpenAPI 配置类
- 通过 `SecurityScheme` 定义 `bearerAuth`
- 对需要鉴权的接口挂接统一安全要求
- 在描述中说明 `Result<T>` 包装结构和基础联调约定

### 5.2 Controller 分组层

职责：

- 决定 Swagger UI 左侧接口分组的名称和语义

要求：

- 每个 Controller 必须声明 `@Tag`
- `@Tag.name` 使用中文业务分组名
- 不允许出现默认分组名，如 `xxx-controller`

示例分组：

- `认证管理`
- `用户个人信息`
- `用户健身档案`
- `教练详情管理`
- `智能聊天助手`
- `文件上传管理`
- `后台会员卡管理`

### 5.3 接口方法层

职责：

- 描述每个接口的业务动作、适用场景与前置条件

要求：

- 每个对外接口方法必须声明 `@Operation`
- `summary` 用一句话概括接口目的
- `description` 仅在存在额外业务上下文时补充说明
- 不使用空泛描述，如“操作接口”“提交数据”“更新信息”

推荐写法：

- `summary = "短信验证码登录"`
- `summary = "上传用户头像"`
- `summary = "取消我的课程预约"`
- `summary = "获取教练详情"`

### 5.4 参数与请求体层

职责：

- 让调用方在 Swagger UI 中明确知道要传什么、怎么传

要求：

- 路径参数、查询参数按需使用 `@Parameter`
- 请求体 DTO 使用 `@Schema` 标注字段含义
- 文件上传接口明确 `multipart/form-data`
- 分页、筛选、状态值、排序值必须说明语义

约束：

- 简单且语义明确的参数可不重复写长描述
- 对业务歧义明显的参数必须补充说明
- 上传接口需要让 Swagger UI 可直接选择文件并提交

### 5.5 响应模型层

职责：

- 让调用方明确返回 `data` 的实际结构与字段语义

要求：

- DTO/VO 字段统一用 `@Schema` 说明
- 状态、布尔、金额、时间等关键字段必须写清含义
- 时间字段说明格式，例如 `yyyy-MM-dd HH:mm:ss`
- 枚举/状态字段说明取值含义

约束：

- 优先标注接口返回使用的 VO，而非数据库 Entity
- 避免把表结构说明直接暴露为接口说明

## 6. 全局规范

### 6.1 鉴权规范

统一采用 JWT Bearer Token。

要求：

- 在 OpenAPI 中定义 `bearerAuth`
- 登录后接口默认支持通过 Swagger 右上角 `Authorize` 调试
- 公开接口不要求携带 Token

公开接口示例：

- 注册
- 用户名密码登录
- 短信验证码发送
- 短信验证码登录
- 滑块验证令牌获取与校验
- 支付回调通知

需鉴权接口示例：

- 获取当前用户信息
- 修改个人资料
- 上传头像
- 我的预约、我的会员卡、我的健身计划
- 后台管理接口

### 6.2 统一返回结构规范

项目接口返回结构统一为 `Result<T>`。

文档要求：

- 在全局描述中说明返回结构包含 `code`、`message`、`data`
- Controller 方法重点描述 `data` 的业务结构
- 不要求每个方法重复解释通用包装字段

对于高风险接口，可补典型错误场景，例如：

- 验证码失效
- 无权限访问
- 库存不足
- 状态不允许变更

### 6.3 分页接口规范

分页接口必须统一说明分页与筛选语义。

要求：

- 说明分页参数，例如 `pageNum`、`pageSize`
- 说明筛选参数的业务含义
- 如果存在排序参数，说明可选值与默认行为
- 响应中让调用方清楚区分列表数据和分页元信息

### 6.4 上传接口规范

上传接口必须能在 Swagger UI 中直接可测。

要求：

- 明确 `consumes = multipart/form-data`
- 文件字段声明为二进制文件
- 伴随参数需要说明用途与默认值
- 返回值需要说明关键字段，如文件 URL、文件名、大小、访问路径

适用接口：

- 用户头像上传
- 通用文件上传
- 图片上传
- 知识库文档上传

### 6.5 状态变更接口规范

对于发布、下架、确认、取消、启用、禁用等接口：

- `summary` 直接写业务动作
- `description` 说明状态变更前提和结果
- 如涉及状态字段，字段说明中补清取值含义

避免使用泛化描述：

- 错误示例：`更新状态`
- 正确示例：`启用商品`
- 正确示例：`取消我的预约`

### 6.6 错误码展示规范

不要求每个接口堆满所有 HTTP 状态码与业务错误码。

原则：

- 全局说明通用错误语义
- 重点接口补充典型业务失败场景
- 以联调价值为标准，避免注解噪音

重点补充对象：

- 登录注册
- 上传
- 支付
- 预约
- 计划生成
- AI 调用类接口

## 7. 注解使用规范

### 7.1 Controller 层

必须使用：

- `@Tag`

示例：

```java
@Tag(name = "认证管理", description = "注册、登录、验证码与令牌刷新相关接口")
```

### 7.2 方法层

必须使用：

- `@Operation`

按需使用：

- `@ApiResponse`
- `@SecurityRequirement`

示例：

```java
@Operation(summary = "用户名密码登录", description = "校验用户名和密码并返回访问令牌与刷新令牌")
```

### 7.3 参数层

按需使用：

- `@Parameter`

适用情况：

- 路径变量含义不直观
- 查询参数为状态值、分页值、筛选值
- 上传接口附带参数存在默认值或业务约束

### 7.4 DTO / VO 层

优先使用：

- `@Schema`

适用对象：

- 请求 DTO
- 响应 VO
- 统计类返回对象
- 上传结果对象

不建议：

- 在 Entity 层大面积堆 Swagger 注解

## 8. 新增接口模板

新增接口默认应遵循以下模板。

### 8.1 Controller 模板

```java
@Tag(name = "用户个人信息", description = "当前登录用户的资料维护相关接口")
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
}
```

### 8.2 方法模板

```java
@Operation(summary = "获取当前用户信息", description = "返回当前登录用户的基础资料与展示信息")
@GetMapping("/me")
public Result<UserVO> getCurrentUserInfo() {
    ...
}
```

### 8.3 DTO 字段模板

```java
@Schema(description = "新手机号")
private String phone;

@Schema(description = "短信验证码")
private String code;
```

### 8.4 上传接口模板

```java
@Operation(summary = "上传用户头像", description = "上传头像图片并更新当前用户头像地址")
@PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public Result<UserVO> uploadAvatar(
        @Parameter(description = "头像图片文件")
        @RequestPart("file") MultipartFile file) {
    ...
}
```

## 9. 推进策略

采用四批次治理，先建立样板，再批量推广。

### 9.1 第一批：用户侧高频接口

优先补齐以下 Controller：

- `AuthController`
- `UserController`
- `UserFitnessProfileController`
- `CoachDetailController`
- `CourseController`
- `FitnessPlanController`
- `ChatAssistantController`
- `FileController`

目标：

- 让前端最常联调的接口先具备完整可读性

### 9.2 第二批：后台管理接口

优先补齐以下类型：

- 用户后台
- 商品后台
- 会员卡后台
- 设备后台
- 课程后台
- 预约后台
- AI 调试后台
- 数据字典

目标：

- 统一后台接口的分组命名、筛选说明、分页说明、状态操作描述

### 9.3 第三批：模型文档

集中补齐 DTO / VO 的 `@Schema`：

- 请求参数对象
- 响应对象
- 分页对象
- 上传结果对象
- 统计对象

目标：

- 降低前端对后端源码的依赖

### 9.4 第四批：全局模板与守护

内容：

- 增加统一 OpenAPI 配置
- 在开发规范中纳入 Swagger 要求
- 后续新增接口按模板补齐注解

目标：

- 防止文档质量再次回退

## 10. 验收标准

完成后，Swagger UI 应满足以下标准：

- 不再出现默认分组名，如 `user-controller`
- 所有对外接口具备中文业务摘要
- 高频接口的参数与请求体字段无需翻源码即可理解
- 登录后接口可通过 Swagger `Authorize` 直接调试
- 上传、分页、状态变更类接口具备统一展示方式
- 新增接口开发有固定模板可复用

## 11. 风险与控制

风险：

- 一次性全量补齐可能改动过散，难以 review
- DTO / VO 分布广，注解补充容易出现风格不一致
- 部分接口返回结构较复杂，短期内可能只能先覆盖高频路径

控制策略：

- 先用 `AuthController`、`UserController`、`FileController` 做样板
- 样板确认后再批量铺开
- 严格限定本次改动只服务 Swagger 文档，不夹带业务重构

## 12. 结论

本项目应采用“文档补齐 + 全局规范 + 落地治理”的 Swagger / OpenAPI 完整实践。

执行上先从高频用户侧接口建立样板，再逐步推广到后台接口与 DTO / VO 模型层，最终形成一套对前端友好、对后续开发可持续的接口文档规范。
