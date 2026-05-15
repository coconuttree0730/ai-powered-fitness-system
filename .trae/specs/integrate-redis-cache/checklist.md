# Checklist

## Spring Cache 基础设施
- [ ] `FitnessApplication.java` 已添加 `@EnableCaching` 注解
- [ ] `RedisConfig.java` 中已定义 `RedisCacheManager` Bean
- [ ] `RedisCacheManager` 按缓存名称配置了差异化 TTL（banner: 10min, announcement: 10min, equipment: 5min, course: 5min, dict: 30min, user:permissions: 30min, user:roles: 30min, default: 5min）
- [ ] 应用启动无报错，Redis 连接健康

## Redisson 全局化
- [ ] `RedisConfig.java` 中已定义全局 `RedissonClient` Bean
- [ ] `AgentRuntimeConfig.java` 已改为注入全局 `RedissonClient`，删除了独立的 `redissonClient()` 方法
- [ ] AI Agent 对话功能正常工作

## Banner 缓存
- [ ] `getActiveBanners()` 添加了 `@Cacheable(value = "banner", key = "'active'")`
- [ ] Banner create/update/delete 方法添加了 `@CacheEvict(value = "banner", key = "'active'")`
- [ ] 缓存命中验证通过（日志或 Redis 客户端确认）

## Announcement 缓存
- [ ] `getPublishedAnnouncements()` 添加了 `@Cacheable(value = "announcement", key = "'published'")`
- [ ] Announcement create/update/delete 方法添加了 `@CacheEvict(value = "announcement", key = "'published'")`
- [ ] 缓存命中验证通过

## Equipment 缓存
- [ ] `getActiveEquipmentList()` 添加了 `@Cacheable(value = "equipment", key = "'active_list'")`
- [ ] `getAllEquipmentTypes()` 添加了 `@Cacheable(value = "equipment", key = "'types'")`
- [ ] Equipment/EquipmentType CRUD 方法添加了 `@CacheEvict(value = "equipment", allEntries = true)`
- [ ] 缓存命中验证通过

## Course 缓存
- [ ] `getHomePageCourses()` 添加了 `@Cacheable(value = "course", key = "'home_page'")`
- [ ] `getCourseCategories()` 添加了 `@Cacheable(value = "course", key = "'categories'")`
- [ ] Course CRUD 方法添加了 `@CacheEvict(value = "course", allEntries = true)`
- [ ] 缓存命中验证通过

## Dict 字典缓存
- [ ] `getDictItemsByType()` 添加了 `@Cacheable(value = "dict", key = "#type")`
- [ ] Dict 修改方法添加了 `@CacheEvict(value = "dict", key = "#type")`
- [ ] 缓存命中验证通过

## User 权限/角色缓存修复
- [ ] `PermissionServiceImpl.getUserPermissions()` 的 `@Cacheable` 注解已生效
- [ ] `PermissionServiceImpl.getUserRoles()` 的 `@Cacheable` 注解已生效
- [ ] 权限变更时缓存正确清除

## 公开课并发预约
- [ ] `BookingServiceImpl` 注入了 `RedissonClient`
- [ ] `createBooking()` 方法使用 `RLock.tryLock(3, 10, SECONDS)` 保护临界区
- [ ] 锁 Key 为 `booking:lock:{sessionId}`
- [ ] 获取锁失败时抛出 `BusinessException`，提示"预约人数较多，请稍后再试"
- [ ] `cancelBooking()` 方法也添加了分布式锁保护
- [ ] `finally` 块中正确释放锁（仅当当前线程持有时）
- [ ] 并发测试：100 用户抢 20 名额，最终 booked_count ≤ 20，不存在超售
- [ ] 一人一单测试：同一用户重复预约同一 session 被拒绝

## 排行榜
- [ ] `RankingService` 接口和实现类已创建
- [ ] 课程热度排行：预约成功后 score 递增，查询返回正确 Top N
- [ ] 用户积分排行：积分变更后更新，支持查排名
- [ ] 教练人气排行：人气变更后更新，首页按人气排序
- [ ] 排行榜查询 API 端点可正常调用

## API 限流
- [ ] `@RateLimit` 注解已创建
- [ ] `RateLimiterAspect` 切面已实现，使用 Redis Sorted Set 滑动窗口
- [ ] 登录接口添加了 `@RateLimit(windowSeconds = 60, maxRequests = 5)`
- [ ] 短信发送接口添加了 `@RateLimit(windowSeconds = 60, maxRequests = 1)`
- [ ] 超限时返回 429 状态码

## JWT Token 黑名单
- [ ] JWT 生成时包含 `jti` 声明
- [ ] `TokenBlacklistService` 已创建
- [ ] 登出接口将 Token 加入黑名单，TTL = Token 剩余有效期
- [ ] `JwtAuthenticationFilter` 校验黑名单
- [ ] 登出后使用原 Token 请求返回 401

## 页面浏览量计数
- [ ] `PageViewService` 已创建
- [ ] 课程详情页调用 `incrementCourseView()`
- [ ] `course_view_stats` 表已创建（Flyway V48）
- [ ] `PageViewSyncJob` 定时任务每 5 分钟同步 Redis 计数到数据库
- [ ] 访问课程详情页后 Redis 计数递增

## 代码质量
- [ ] 无编译错误
- [ ] 无新增 lint 警告
- [ ] 遵循项目现有代码风格
- [ ] 无硬编码配置，TTL 等参数通过配置文件管理