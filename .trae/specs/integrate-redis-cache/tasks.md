# Tasks

- [ ] Task 1: 搭建 Spring Cache 基础设施
  - [ ] SubTask 1.1: 在 `FitnessApplication.java` 添加 `@EnableCaching` 注解
  - [ ] SubTask 1.2: 在 `RedisConfig.java` 中添加 `RedisCacheManager` Bean，按缓存名称配置差异化 TTL
  - [ ] SubTask 1.3: 确认 `application-dev.yml` 中 Redis 连接配置正确
  - [ ] 验证: 启动应用无报错，`/actuator/health` 中 Redis 状态为 UP

- [ ] Task 2: 将 RedissonClient 提升为全局基础设施 Bean
  - [ ] SubTask 2.1: 在 `RedisConfig.java` 中添加 `RedissonClient` Bean 定义
  - [ ] SubTask 2.2: 修改 `AgentRuntimeConfig.java`，注入全局 `RedissonClient` 而非自行创建，删除冗余的 `redissonClient()` 方法
  - [ ] 验证: 编译通过，AI Agent 对话功能正常工作

- [ ] Task 3: 热数据缓存 — Banner 模块
  - [ ] SubTask 3.1: 在 `BannerServiceImpl.getActiveBanners()` 上添加 `@Cacheable(value = "banner", key = "'active'")`
  - [ ] SubTask 3.2: 在 `BannerServiceImpl` 的 create/update/delete 方法上添加 `@CacheEvict(value = "banner", key = "'active'")`
  - [ ] 验证: 首次调用查询数据库，二次调用命中缓存（日志确认），修改后缓存自动清除

- [ ] Task 4: 热数据缓存 — Announcement 模块
  - [ ] SubTask 4.1: 在 `AnnouncementServiceImpl.getPublishedAnnouncements()` 上添加 `@Cacheable(value = "announcement", key = "'published'")`
  - [ ] SubTask 4.2: 在 `AnnouncementServiceImpl` 的 create/update/delete 方法上添加 `@CacheEvict(value = "announcement", key = "'published'")`
  - [ ] 验证: 缓存命中 + 数据变更时自动清除

- [ ] Task 5: 热数据缓存 — Equipment 模块
  - [ ] SubTask 5.1: 在 `EquipmentServiceImpl.getActiveEquipmentList()` 上添加 `@Cacheable(value = "equipment", key = "'active_list'")`
  - [ ] SubTask 5.2: 在 `EquipmentServiceImpl.getAllEquipmentTypes()` 上添加 `@Cacheable(value = "equipment", key = "'types'")`
  - [ ] SubTask 5.3: 在器材 CRUD 方法上添加 `@CacheEvict(value = "equipment", allEntries = true)`
  - [ ] 验证: 两个查询均能命中缓存

- [ ] Task 6: 热数据缓存 — Course 模块
  - [ ] SubTask 6.1: 在 `CourseServiceImpl.getHomePageCourses()` 上添加 `@Cacheable(value = "course", key = "'home_page'")`
  - [ ] SubTask 6.2: 在 `CourseServiceImpl.getCourseCategories()` 上添加 `@Cacheable(value = "course", key = "'categories'")`
  - [ ] SubTask 6.3: 在课程 CRUD 方法上添加 `@CacheEvict(value = "course", allEntries = true)`
  - [ ] 验证: 首页课程查询从 5 次 SQL 减少为缓存命中

- [ ] Task 7: 热数据缓存 — Dict 字典模块
  - [ ] SubTask 7.1: 检查是否存在 Dict 相关的 Service 实现，在 `getDictItemsByType()` 上添加 `@Cacheable(value = "dict", key = "#type")`
  - [ ] SubTask 7.2: 在字典修改方法上添加 `@CacheEvict(value = "dict", key = "#type")`
  - [ ] 验证: 字典查询缓存命中

- [ ] Task 8: 公开课并发预约 — 分布式锁改造
  - [ ] SubTask 8.1: 在 `BookingServiceImpl` 中注入 `RedissonClient`
  - [ ] SubTask 8.2: 修改 `createBooking()` 方法，在关键逻辑前后使用 `RLock.tryLock(3, 10, TimeUnit.SECONDS)` 加锁/解锁（finally 中释放）
  - [ ] SubTask 8.3: 修改 `cancelBooking()` 方法，同样添加分布式锁
  - [ ] SubTask 8.4: 锁 Key 格式：`booking:lock:{sessionId}`
  - [ ] SubTask 8.5: 获取锁失败时抛出 `BusinessException`，提示"预约人数较多，请稍后再试"
  - [ ] 验证: 编写并发测试 — 模拟 100 个用户同时预约只有 20 个名额的课程，最终 booked_count ≤ capacity

- [ ] Task 9: 排行榜功能
  - [ ] SubTask 9.1: 创建 `RankingService` 接口和 `RankingServiceImpl` 实现类，注入 `RedisTemplate`
  - [ ] SubTask 9.2: 实现课程热度排行：`incrementCourseHotScore(courseId)` 使用 `ZINCRBY`，`getCourseHotRanking(topN)` 使用 `ZREVRANGE`
  - [ ] SubTask 9.3: 实现用户积分排行：`updateUserPointsScore(userId, points)` 使用 `ZADD`，`getUserPointsRanking(topN)` 使用 `ZREVRANGE`
  - [ ] SubTask 9.4: 实现教练人气排行：`updateCoachPopularity(coachId, score)`，`getCoachPopularityRanking(topN)`
  - [ ] SubTask 9.5: 在 `BookingServiceImpl.createBooking()` 成功后调用 `rankingService.incrementCourseHotScore()`
  - [ ] SubTask 9.6: 创建排行榜查询 Controller 端点
  - [ ] 验证: 预约课程后排行榜数据更新，查询返回正确排序

- [ ] Task 10: API 限流切面
  - [ ] SubTask 10.1: 创建 `@RateLimit` 注解，包含 `windowSeconds`、`maxRequests`、`message` 属性
  - [ ] SubTask 10.2: 创建 `RateLimiterAspect` 切面类，注入 `RedisTemplate`
  - [ ] SubTask 10.3: 实现滑动窗口限流逻辑（使用 Sorted Set，`ZREMRANGEBYSCORE` + `ZCARD` + `ZADD` + `EXPIRE`）
  - [ ] SubTask 10.4: 在登录接口、短信发送接口上添加 `@RateLimit` 注解
  - [ ] 验证: 连续快速请求超过阈值时返回 429

- [ ] Task 11: JWT Token 黑名单
  - [ ] SubTask 11.1: 确认 JWT 生成时是否包含 `jti`（JWT ID），若没有则添加
  - [ ] SubTask 11.2: 创建 `TokenBlacklistService`，方法 `blacklist(String jti, long ttlSeconds)` 和 `isBlacklisted(String jti)`
  - [ ] SubTask 11.3: 修改登出接口，调用 `tokenBlacklistService.blacklist(jti, remainingSeconds)`
  - [ ] SubTask 11.4: 修改 `JwtAuthenticationFilter`，在 Token 校验后调用 `tokenBlacklistService.isBlacklisted(jti)` 检查
  - [ ] 验证: 登出后使用原 Token 请求返回 401

- [ ] Task 12: 页面浏览量计数
  - [ ] SubTask 12.1: 创建 `PageViewService`，方法 `incrementCourseView(courseId)` 使用 `INCR`
  - [ ] SubTask 12.2: 在课程详情 Controller 中调用 `pageViewService.incrementCourseView()`
  - [ ] SubTask 12.3: 创建定时任务 `PageViewSyncJob`，每 5 分钟将 Redis 计数批量同步到数据库汇总表
  - [ ] SubTask 12.4: 若数据库无汇总表，创建 `course_view_stats` 表（Flyway 迁移 V48）
  - [ ] 验证: 访问课程详情页后 Redis 计数递增

# Task Dependencies

- Task 3, 4, 5, 6, 7 依赖 Task 1（Spring Cache 基础设施必须先就绪）
- Task 8 依赖 Task 2（RedissonClient 全局化）
- Task 9 依赖 Task 2（需要 RedissonClient 或 RedisTemplate）
- Task 10 依赖 Task 1（需要 RedisTemplate 可用）
- Task 11 无强依赖，但需要 Task 1 完成后 Redis 可用
- Task 12 无强依赖，但需要 Task 1 完成后 Redis 可用
- Task 3, 4, 5, 6, 7 相互独立，可并行执行
- Task 9, 10, 11, 12 相互独立，可并行执行