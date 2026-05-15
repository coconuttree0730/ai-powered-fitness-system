# Redis 缓存与辅助功能集成 规格说明书

## Why

当前系统已引入 Redis 依赖（Lettuce + Redisson），但仅用于 AI Agent 对话检查点和验证码存储。Spring Cache 基础设施缺失（无 `@EnableCaching`、无 `CacheManager`），导致 `@Cacheable` 注解为死代码。多个高频查询（首页轮播图、公告、课程、器材类型）每次都直击数据库，预约模块存在「检查-然后-操作」竞态条件可能导致超售。本次集成将系统性引入 Redis 作为缓存层和分布式协调器，覆盖热数据缓存、并发预约控制、排行榜等场景。

## What Changes

- **新增**: Spring Cache 基础设施启用（`@EnableCaching` + `RedisCacheManager`）
- **新增**: 热数据缓存（Banner、Announcement、Equipment、Course、Dict 等高频只读查询）
- **新增**: 公开课并发预约分布式锁（Redisson RLock），保证原子性和一人一单
- **新增**: 排行榜功能（课程热度、用户积分、教练人气），使用 Redis Sorted Set
- **新增**: API 限流（滑动窗口计数器，防止恶意刷接口）
- **新增**: JWT Token 黑名单机制（登出/刷新时使旧 Token 失效）
- **新增**: 页面浏览量/课程浏览量计数器
- **修复**: 现有 `PermissionServiceImpl` 中的 `@Cacheable` 注解使其生效
- **修复**: 预约模块并发超售问题

## Impact

- Affected specs: init-fitness-system（预约并发控制、缓存层）
- Affected code:
  - `FitnessApplication.java`（添加 `@EnableCaching`）
  - `RedisConfig.java`（添加 `CacheManager` Bean、`RedissonClient` Bean）
  - `BookingServiceImpl.java`（分布式锁改造）
  - `BannerServiceImpl.java`、`AnnouncementServiceImpl.java`、`EquipmentServiceImpl.java`、`CourseServiceImpl.java`、`DictServiceImpl.java`（添加缓存注解）
  - `PermissionServiceImpl.java`（已有注解，即将生效）
  - 新增 `RateLimiterAspect.java`（限流切面）
  - 新增 `RankingService.java`（排行榜服务）
  - 新增 `TokenBlacklistService.java`（Token 黑名单）
  - 新增 `PageViewService.java`（浏览量服务）

---

## ADDED Requirements

### Requirement 1: Spring Cache 基础设施

系统 SHALL 启用 Spring Cache 抽象层，以 Redis 作为缓存后端。

#### Scenario 1.1: 启用缓存
- **GIVEN** 应用启动
- **WHEN** Spring 容器初始化
- **THEN** `@EnableCaching` 激活缓存代理
- **AND** `RedisCacheManager` 作为默认 CacheManager 注册到容器
- **AND** 缓存使用 `GenericJackson2JsonRedisSerializer` 序列化值

#### Scenario 1.2: 缓存 TTL 配置
- **GIVEN** 不同业务数据有不同的缓存时效要求
- **WHEN** 配置 `RedisCacheManager`
- **THEN** 支持按缓存名称设置不同 TTL：
  - `banner`：10 分钟
  - `announcement`：10 分钟
  - `equipment`：5 分钟
  - `course`：5 分钟
  - `dict`：30 分钟
  - `user:permissions`：30 分钟
  - `user:roles`：30 分钟
  - 默认：5 分钟

#### Scenario 1.3: 缓存 Key 命名规范
- **GIVEN** 需要防止不同模块的缓存 Key 冲突
- **WHEN** 生成缓存 Key
- **THEN** Key 格式为 `{模块}:{业务}:{标识}`，如 `equipment:type:all`、`course:category:all`、`user:permissions:{userId}`

---

### Requirement 2: 热数据查询缓存

系统 SHALL 对高频只读查询结果进行缓存，减少数据库压力。

#### Scenario 2.1: Banner 轮播图缓存
- **GIVEN** 首页加载需要查询有效轮播图
- **WHEN** 调用 `getActiveBanners()`
- **THEN** 首次查询从数据库加载并缓存
- **AND** 后续请求直接从 Redis 缓存返回
- **AND** 管理员新增/编辑/删除 Banner 时自动清除缓存

#### Scenario 2.2: Announcement 公告缓存
- **GIVEN** 前台需要展示已发布公告
- **WHEN** 调用 `getPublishedAnnouncements()`
- **THEN** 首次查询从数据库加载并缓存
- **AND** 管理员新增/编辑/删除公告时自动清除缓存

#### Scenario 2.3: Equipment 器材缓存
- **GIVEN** 器材列表页和下拉选择需要器材数据
- **WHEN** 调用 `getActiveEquipmentList()` 或 `getAllEquipmentTypes()`
- **THEN** 首次查询从数据库加载并缓存
- **AND** 后续请求直接从缓存返回
- **AND** 管理员新增/编辑/删除器材或器材类型时自动清除对应缓存

#### Scenario 2.4: Course 课程缓存
- **GIVEN** 首页课程体系和课程列表页
- **WHEN** 调用 `getHomePageCourses()` 或 `getCourseCategories()`
- **THEN** 首次查询从数据库加载并缓存
- **AND** 后续请求直接从缓存返回
- **AND** 教练/管理员新增/编辑/删除课程时自动清除对应缓存

#### Scenario 2.5: Dict 数据字典缓存
- **GIVEN** 多处业务需要查询数据字典（下拉框选项等）
- **WHEN** 调用 `getDictItemsByType()`
- **THEN** 首次查询从数据库加载并缓存
- **AND** 管理员修改字典时自动清除对应缓存

#### Scenario 2.6: User 权限/角色缓存（修复）
- **GIVEN** `PermissionServiceImpl` 中已有 `@Cacheable` 注解但未生效
- **WHEN** Spring Cache 启用后
- **THEN** `getUserPermissions(userId)` 和 `getUserRoles(userId)` 正常缓存
- **AND** 用户权限变更时清除对应缓存

---

### Requirement 3: 公开课并发预约控制

系统 SHALL 使用 Redisson 分布式锁保证公开课预约在高并发场景下的原子性和一人一单约束。

#### Scenario 3.1: 分布式锁加锁
- **GIVEN** 多个用户同时预约同一节公开课（同一个 `sessionId`）
- **WHEN** 每个请求进入 `createBooking()`
- **THEN** 以 `booking:lock:{sessionId}` 为 Key 获取 Redisson 分布式锁
- **AND** 同一时刻只有一个线程能进入临界区
- **AND** 锁等待超时 3 秒，持有超时 10 秒（自动释放防死锁）

#### Scenario 3.2: 临界区内操作
- **GIVEN** 当前线程已获取分布式锁
- **WHEN** 执行预约逻辑
- **THEN** 查询 session 是否存在且未开始
- **AND** 检查 `booked_count < capacity`（已无竞态条件）
- **AND** 检查用户未重复预约该 session
- **AND** 原子更新 `booked_count = booked_count + 1`
- **AND** 创建预约记录

#### Scenario 3.3: 获取锁失败处理
- **GIVEN** 当前 session 的锁被其他线程持有
- **WHEN** 等待 3 秒后仍未获取到锁
- **THEN** 返回错误 "预约人数较多，请稍后再试"

#### Scenario 3.4: 取消预约也需加锁
- **GIVEN** 用户取消预约需要释放名额
- **WHEN** 调用 `cancelBooking()`
- **THEN** 同样获取同一 session 的分布式锁
- **AND** 原子减少 `booked_count`
- **AND** 更新预约状态为已取消

---

### Requirement 4: 排行榜功能

系统 SHALL 使用 Redis Sorted Set 提供多维度排行榜。

#### Scenario 4.1: 课程热度排行榜
- **GIVEN** 课程被预约时会增加热度
- **WHEN** 用户成功预约课程
- **THEN** Redis Sorted Set `ranking:course:hot` 中对应课程 score +1
- **AND** 查询排行榜时按 score 降序返回 Top N
- **AND** 支持按周/月/总三个时间维度（通过不同 Key 区分）

#### Scenario 4.2: 用户积分排行榜
- **GIVEN** 用户通过签到、消费等行为获取积分
- **WHEN** 用户积分发生变更
- **THEN** Redis Sorted Set `ranking:user:points` 中更新对应用户的 score
- **AND** 支持查询用户排名及附近排名

#### Scenario 4.3: 教练人气排行榜
- **GIVEN** 教练的学员数、好评率反映人气
- **WHEN** 教练数据发生变更（新学员加入、收到评价）
- **THEN** Redis Sorted Set `ranking:coach:popularity` 中更新 score
- **AND** 首页教练展示按人气排序

---

### Requirement 5: API 限流

系统 SHALL 对敏感 API 进行请求频率限制，防止恶意刷接口。

#### Scenario 5.1: 滑动窗口限流
- **GIVEN** 需要限制用户在单位时间内的请求次数
- **WHEN** 用户请求受保护的 API
- **THEN** 使用 Redis 滑动窗口计数器（Sorted Set 实现）
- **AND** 记录每次请求的时间戳
- **AND** 统计当前窗口内的请求次数
- **AND** 超过阈值时返回 429 Too Many Requests

#### Scenario 5.2: 限流配置
- **GIVEN** 不同 API 有不同的限流需求
- **WHEN** 通过 `@RateLimit` 注解标记接口
- **THEN** 支持配置：窗口时间（秒）、最大请求数、限流提示信息
- **AND** 默认 60 秒内最多 30 次请求
- **AND** 短信发送接口 60 秒内最多 1 次
- **AND** 登录接口 60 秒内最多 5 次

---

### Requirement 6: JWT Token 黑名单

系统 SHALL 在用户登出或刷新 Token 时将旧 Token 加入黑名单。

#### Scenario 6.1: 登出时加入黑名单
- **GIVEN** 用户已登录并持有有效 JWT
- **WHEN** 用户执行登出操作
- **THEN** 当前 JWT 加入 Redis String 黑名单 `token:blacklist:{jti}`
- **AND** Key 的 TTL 设为 Token 剩余有效期

#### Scenario 6.2: 请求时校验黑名单
- **GIVEN** 用户携带 JWT 发起请求
- **WHEN** JWT 认证过滤器校验 Token
- **THEN** 在验证签名和有效期后，额外检查 `token:blacklist:{jti}` 是否存在
- **AND** 若在黑名单中则拒绝请求（401）

---

### Requirement 7: 页面浏览量计数

系统 SHALL 使用 Redis 记录页面和课程的浏览量。

#### Scenario 7.1: 课程详情页浏览计数
- **GIVEN** 用户访问课程详情页
- **WHEN** 页面加载
- **THEN** Redis Key `pv:course:{courseId}` 自增 1
- **AND** 定期（每 5 分钟）批量同步到数据库持久化

#### Scenario 7.2: 浏览量查询
- **GIVEN** 需要展示课程热度
- **WHEN** 查询课程浏览量
- **THEN** 优先从 Redis 获取实时计数
- **AND** 结合数据库历史数据返回总量

---

### Requirement 8: Redisson 配置

系统 SHALL 将 RedissonClient 从 AI 模块配置提升为全局基础设施 Bean。

#### Scenario 8.1: RedissonClient 全局化
- **GIVEN** 当前 RedissonClient 仅在 `AgentRuntimeConfig` 中定义
- **WHEN** 重构配置
- **THEN** 在 `RedisConfig` 中定义全局 `RedissonClient` Bean
- **AND** `AgentRuntimeConfig` 复用全局 Bean 而非独立创建
- **AND** 支持分布式锁、分布式集合等 Redisson 高级功能

---

## MODIFIED Requirements

### Requirement: 预约模块并发控制（来自 init-fitness-system）

**Before**: 仅使用 `@Transactional`，存在 check-then-act 竞态条件，可能超售。

**After**: 引入 Redisson 分布式锁，以 `sessionId` 为粒度串行化预约操作，确保原子性和一人一单。

---

## REMOVED Requirements

无。