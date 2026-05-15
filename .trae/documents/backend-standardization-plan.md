# 后端规范优化实施计划

> **For agentic workers:** Use subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 系统性修复前次扫描发现的严重/高优先级问题，统一代码规范，消除安全隐患

**Architecture:** 分 6 个阶段执行，每阶段聚焦一类问题。阶段间无强依赖，但建议按序执行以确保每次改动后项目仍可编译运行。

**Tech Stack:** Spring Boot 3.5.14, MyBatis-Plus 3.5.7, PostgreSQL, Maven, Java 17, Lombok

---

## Phase 1: 稳定性修复（运行时错误 + 编译正确性）

### Task 1.1: 修复 SysDictItemMapper.selectByDictId 缺少 SQL 映射

**Files:**
- Modify: `src/main/java/com/fitness/modules/system/mapper/SysDictItemMapper.java`

**问题:** `selectByDictId(Long dictId)` 方法既无 `@Select` 注解也无 XML 映射，调用时抛 `BindingException`。

- [ ] **Step 1: 添加 @Select 注解**

在 `selectByDictId` 方法上添加 `@Select` 注解：

```java
@Mapper
public interface SysDictItemMapper extends BaseMapper<SysDictItem> {

    @Select("SELECT * FROM sys_dict_item WHERE dict_id = #{dictId} AND deleted = false ORDER BY sort_order")
    List<SysDictItem> selectByDictId(Long dictId);
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn compile -q
```
预期：编译通过。

---

### Task 1.2: 添加 @Mapper 注解到 4 个缺失的 Mapper

**Files:**
- Modify: `src/main/java/com/fitness/modules/user/mapper/UserMapper.java`
- Modify: `src/main/java/com/fitness/modules/user/mapper/PermissionMapper.java`
- Modify: `src/main/java/com/fitness/modules/plan/mapper/FitnessPlanMapper.java`
- Modify: `src/main/java/com/fitness/modules/plan/mapper/FitnessPlanDetailMapper.java`

**问题:** 虽然项目启用了 `@MapperScan("com.fitness.**.mapper")`，但部分 Mapper 缺少 `@Mapper` 注解，与项目中其他 Mapper 风格不一致。

- [ ] **Step 1: 在 UserMapper 添加 @Mapper**

```java
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
```

- [ ] **Step 2: 在 PermissionMapper 添加 @Mapper + 继承 BaseMapper**

```java
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
```

- [ ] **Step 3: 在 FitnessPlanMapper 添加 @Mapper**

```java
@Mapper
public interface FitnessPlanMapper extends BaseMapper<FitnessPlan> {
```

- [ ] **Step 4: 在 FitnessPlanDetailMapper 添加 @Mapper**

```java
@Mapper
public interface FitnessPlanDetailMapper extends BaseMapper<FitnessPlanDetail> {
```

- [ ] **Step 5: 编译验证**

```bash
mvn compile -q
```

---

### Task 1.3: 修复 SecurityUtils.requireCurrentUserId() 实际 Bug

**Files:**
- Modify: `src/main/java/com/fitness/integration/security/SecurityUtils.java`

**问题:** Javadoc 声明"未登录时抛异常"，但实际调用 `getCurrentUserId()` 在未登录时返回 `null`，不会抛异常。与方法名 `require` 语义不符。

- [ ] **Step 1: 修复 requireCurrentUserId 实现**

将 [第 156-158 行](file:///g:/ai-powered-fitness-system-v2/src/main/java/com/fitness/integration/security/SecurityUtils.java#L156-L158) 改为：

```java
public static Long requireCurrentUserId() {
    return getCurrentUser()
            .map(UserDetailsImpl::getId)
            .orElseThrow(() -> new IllegalStateException("当前用户未登录"));
}
```

---

## Phase 2: 安全加固

### Task 2.1: 添加异步线程池配置

**Files:**
- Create: `src/main/java/com/fitness/config/AsyncConfig.java`
- Modify: `src/main/java/com/fitness/FitnessApplication.java`

**问题:** `@EnableAsync` 启用了异步但没有任何自定义线程池，默认 `SimpleAsyncTaskExecutor` 每次创建新线程，生产环境存在线程耗尽风险。同时需要确保 `SecurityContext` 在异步线程中可传递。

- [ ] **Step 1: 创建 AsyncConfig 配置类**

```java
package com.fitness.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        return new DelegatingSecurityContextAsyncTaskExecutor(executor);
    }
}
```

- [ ] **Step 2: 移除 FitnessApplication 上的 @EnableAsync**

```java
// 删除第 12 行的 @EnableAsync，AsyncConfig 会提供
@SpringBootApplication
@MapperScan("com.fitness.**.mapper")
public class FitnessApplication {
```

- [ ] **Step 3: 编译验证**

```bash
mvn compile -q
```

---

### Task 2.2: 修复 RedisConfig 反序列化安全风险

**Files:**
- Modify: `src/main/java/com/fitness/config/RedisConfig.java`

**问题:** `LaissezFaireSubTypeValidator` + `NON_FINAL` 允许反序列化几乎所有非 final 类型，存在 RCE 风险。

- [ ] **Step 1: 替换为 BasicPolymorphicTypeValidator + 白名单**

将 [第 26-27 行](file:///g:/ai-powered-fitness-system-v2/src/main/java/com/fitness/config/RedisConfig.java#L26-L27) 改为：

```java
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

ObjectMapper mapper = new ObjectMapper();
mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
        .allowIfBaseType(Object.class)
        .build();
mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
```

同时移除不再需要的 import：`import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;`

- [ ] **Step 2: 编译验证**

```bash
mvn compile -q
```

---

### Task 2.3: MybatisPlusConfig 添加防全表更新/删除插件

**Files:**
- Modify: `src/main/java/com/fitness/config/MybatisPlusConfig.java`

- [ ] **Step 1: 添加 BlockAttackInnerInterceptor**

在分页插件后添加：

```java
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;

@Bean
public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.POSTGRE_SQL));
    interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
    return interceptor;
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn compile -q
```

---

### Task 2.4: 同步 SecurityWhitelist 与 JwtAuthenticationFilter.shouldNotFilter

**Files:**
- Modify: `src/main/java/com/fitness/config/security/SecurityWhitelist.java`
- Modify: `src/main/java/com/fitness/config/SecurityConfig.java`
- Modify: `src/main/java/com/fitness/integration/security/JwtAuthenticationFilter.java`

**问题:** SecurityConfig 中有 2 行额外的白名单（`GET /api/v1/courses/**`、`/static/**`、`/uploads/**`），未收拢到 SecurityWhitelist。JwtAuthenticationFilter.shouldNotFilter 缺少 `/api/v1/announcements/published`、`/static/**`、`/uploads/**` 以及教练详情通配符匹配。

- [ ] **Step 1: 将 SecurityConfig 中额外白名单收拢到 SecurityWhitelist**

在 `SecurityWhitelist.URLS` 数组中追加：
```java
"/api/v1/courses/**",
"/static/**",
"/uploads/**"
```

- [ ] **Step 2: 移除 SecurityConfig 中的硬编码白名单**

删除 [第 42-43 行](file:///g:/ai-powered-fitness-system-v2/src/main/java/com/fitness/config/SecurityConfig.java#L42-L43)：
```java
// 删除这两行
.requestMatchers(HttpMethod.GET, "/api/v1/courses/**").permitAll()
.requestMatchers("/static/**", "/uploads/**").permitAll()
```

同时检查 `HttpMethod.GET` 限制需要保留——在 SecurityWhitelist 中 `"/api/v1/courses/**"` 会对所有 HTTP 方法放行，而原配置仅对 GET 放行。方案：SecurityWhitelist 只放行 ALL，SecurityConfig 对 GET 额外放行这一条保留但简化为引用常量。

简易方案：删除 SecurityConfig 中两行硬编码，在 SecurityWhitelist 追加 `"/static/**"`、`"/uploads/**"`。`GET /api/v1/courses/**` 改为所有方法（已有 `/courses/public/**` 覆盖公开接口，需要确认 courses 下其他接口的权限）。

检查 `CourseController` 和 `CourseSessionController` 的权限注解——发现它们已有 `@PreAuthorize`，所以 GET /api/v1/courses/** 全部放行是过度宽松的。将其从 SecurityConfig 移除，依赖各 Controller 自身权限注解。

- [ ] **Step 3: 补齐 JwtAuthenticationFilter.shouldNotFilter 缺失的白名单**

在 [exactMatchPaths](file:///g:/ai-powered-fitness-system-v2/src/main/java/com/fitness/integration/security/JwtAuthenticationFilter.java#L188-L201) 中添加：
```java
"/api/v1/announcements/published",
```

在 prefixMatchPaths 中添加：
```java
"/static/",
"/uploads/",
"/api/v1/coaches/",
```

注意：`/api/v1/coaches/` 前缀匹配会覆盖 `/api/v1/coaches/home` 和 `/api/v1/coaches/*/detail`。

- [ ] **Step 4: 移除 JwtAuthenticationFilter 对 Tomcat ClientAbortException 的硬依赖**

移除 [第 24 行](file:///g:/ai-powered-fitness-system-v2/src/main/java/com/fitness/integration/security/JwtAuthenticationFilter.java#L24) 的 import：
```java
// 删除
import org.apache.catalina.connector.ClientAbortException;
```

在 `isClientAbortException` 方法中以字符串匹配替换：
```java
private boolean isClientAbortException(Throwable throwable) {
    String className = throwable.getClass().getName();
    if (className.contains("ClientAbortException")) {
        return true;
    }
    Throwable cause = throwable.getCause();
    while (cause != null) {
        if (cause.getClass().getName().contains("ClientAbortException")) {
            return true;
        }
        cause = cause.getCause();
    }
    return false;
}
```

- [ ] **Step 5: 编译验证**

```bash
mvn compile -q
```

---

### Task 2.5: 补充 11 个 Controller 的 @PreAuthorize 权限注解

**Files (11 个 Controller):**
- Modify: `src/main/java/com/fitness/modules/product/controller/CoachProductController.java`
- Modify: `src/main/java/com/fitness/modules/user/controller/CoachDetailController.java`
- Modify: `src/main/java/com/fitness/modules/system/controller/SysDictController.java`
- Modify: `src/main/java/com/fitness/modules/product/controller/ProductOrderController.java`
- Modify: `src/main/java/com/fitness/modules/membership/controller/MembershipCardController.java`
- Modify: `src/main/java/com/fitness/modules/membership/controller/MembershipCardAdminController.java`
- Modify: `src/main/java/com/fitness/modules/knowledge/controller/RAGController.java`
- Modify: `src/main/java/com/fitness/integration/minio/controller/FileController.java`
- Modify: `src/main/java/com/fitness/modules/user/controller/UserController.java` (仅 checkUsernameExists)
- Modify: `src/main/java/com/fitness/modules/course/controller/CourseSessionController.java` (部分方法)

- [ ] **Step 1: CoachProductController — 类级 @PreAuthorize("hasRole('COACH')")**

全部方法为教练专用，类上加：
```java
@RestController
@RequestMapping("/api/v1/coach/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COACH')")
public class CoachProductController {
```

- [ ] **Step 2: CoachDetailController — 类级 @PreAuthorize("hasRole('COACH')")**

```java
@RestController
@RequestMapping("/api/v1/coach")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COACH')")
public class CoachDetailController {
```

- [ ] **Step 3: SysDictController — 类级 @PreAuthorize("hasRole('ADMIN')")**

```java
@RestController
@RequestMapping("/api/v1/admin/dict")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SysDictController {
```

除了 `getOptions` 可能需对前端公开——如果 `getOptions` 是下拉框接口需要公开，在方法上加 `@PreAuthorize("isAuthenticated()")` 覆盖类级。

- [ ] **Step 4: ProductOrderController — 类级 @PreAuthorize("isAuthenticated()")**

创建/支付/取消订单至少需登录：
```java
@PreAuthorize("isAuthenticated()")
public class ProductOrderController {
```

- [ ] **Step 5: MembershipCardController — 类级 @PreAuthorize("isAuthenticated()")**

- [ ] **Step 6: MembershipCardAdminController — 类级 @PreAuthorize("hasRole('ADMIN')")**

- [ ] **Step 7: RAGController — 类级 @PreAuthorize("isAuthenticated()")**

- [ ] **Step 8: FileController — 类级 @PreAuthorize("isAuthenticated()")**

- [ ] **Step 9: UserController.checkUsernameExists — 方法级 @PreAuthorize("isAuthenticated()")**

- [ ] **Step 10: CourseSessionController 公开接口补充**

`getSessionList`、`getSessionDetail`、`getUpcomingSessions` 如果是前端展示课表用的公开接口，保持 `permitAll`。否则加 `@PreAuthorize("isAuthenticated()")`。根据 SecurityWhitelist 分析，`/api/v1/courses/public/**` 已放行公开接口，Session 接口不在白名单中，应加认证。

- [ ] **Step 11: 编译验证**

```bash
mvn compile -q
```

---

### Task 2.6: 移除验证码明文日志

**Files:**
- Modify: `src/main/java/com/fitness/modules/user/service/impl/EmailCodeServiceImpl.java`
- Modify: `src/main/java/com/fitness/modules/user/service/impl/SmsCodeServiceImpl.java`

- [ ] **Step 1: EmailCodeServiceImpl — 移除 code 参数**

将 [第 54 行](file:///g:/ai-powered-fitness-system-v2/src/main/java/com/fitness/modules/user/service/impl/EmailCodeServiceImpl.java#L54) 改为：
```java
log.info("邮箱验证码发送成功: email={}", email);
```

- [ ] **Step 2: SmsCodeServiceImpl — 移除 code 参数**

将 [第 61 行](file:///g:/ai-powered-fitness-system-v2/src/main/java/com/fitness/modules/user/service/impl/SmsCodeServiceImpl.java#L61) 改为：
```java
log.error("短信验证码发送失败: phone={}, message={}", phone, result.message());
```

- [ ] **Step 3: 编译验证**

```bash
mvn compile -q
```

---

### Task 2.7: 替换 Random 为 SecureRandom

**Files:**
- Modify: `src/main/java/com/fitness/modules/user/service/impl/SmsCodeServiceImpl.java`
- Modify: `src/main/java/com/fitness/modules/user/service/impl/EmailCodeServiceImpl.java`
- Modify: `src/main/java/com/fitness/modules/product/service/impl/ProductOrderServiceImpl.java`
- Modify: `src/main/java/com/fitness/modules/membership/service/impl/MembershipOrderServiceImpl.java`
- Modify: `src/main/java/com/fitness/modules/user/service/impl/UserServiceImpl.java`

- [ ] **Step 1: SmsCodeServiceImpl.generateCode() — 替换为 SecureRandom**

```java
import java.security.SecureRandom;

private static final SecureRandom SECURE_RANDOM = new SecureRandom();

private String generateCode() {
    StringBuilder code = new StringBuilder(6);
    for (int i = 0; i < 6; i++) {
        code.append(SECURE_RANDOM.nextInt(10));
    }
    return code.toString();
}
```

- [ ] **Step 2: EmailCodeServiceImpl.generateCode() — 同上**

- [ ] **Step 3: ProductOrderServiceImpl.generateOrderNo() — 替换**

```java
private String generateOrderNo() {
    return "PO" + System.currentTimeMillis() + String.format("%04d", SECURE_RANDOM.nextInt(10000));
}
```

- [ ] **Step 4: MembershipOrderServiceImpl.generateOrderNo() — 替换**

- [ ] **Step 5: UserServiceImpl.generateUsernameFromPhone() — 替换**

- [ ] **Step 6: 编译验证**

```bash
mvn compile -q
```

---

## Phase 3: Entity 层规范化

### Task 3.1: 10 个 Entity 统一继承 BaseEntity

**Files (修改 9 个 Entity，MembershipOrder 时间字段已正确仅需添加继承):**
- Modify: `src/main/java/com/fitness/modules/product/model/entity/Product.java`
- Modify: `src/main/java/com/fitness/modules/product/model/entity/ProductOrder.java`
- Modify: `src/main/java/com/fitness/modules/membership/model/entity/MembershipCard.java`
- Modify: `src/main/java/com/fitness/modules/membership/model/entity/MembershipCardType.java`
- Modify: `src/main/java/com/fitness/modules/membership/model/entity/MembershipCardContent.java`
- Modify: `src/main/java/com/fitness/modules/membership/model/entity/MembershipOrder.java`
- Modify: `src/main/java/com/fitness/modules/membership/model/entity/UserMembership.java`
- Modify: `src/main/java/com/fitness/modules/system/model/entity/SysDict.java`
- Modify: `src/main/java/com/fitness/modules/system/model/entity/SysDictItem.java`
- Modify: `src/main/java/com/fitness/modules/user/model/entity/CoachDetail.java`

**说明:** 本次仅修改 Java Entity 层代码，数据库列名迁移脚本（`created_at` → `create_time`）需在 Phase 6 中通过 Flyway 进行，此处先标记 `@TableField` 保持与当前数据库兼容。

- [ ] **Step 1: Product.java — 继承 BaseEntity，统一字段命名**

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {
    // 删除以下字段（BaseEntity 已有）:
    // private Date createdAt;
    // private Date updatedAt;
}
```

**关键:** 数据库列目前是 `created_at`/`updated_at`，而 BaseEntity 映射 `create_time`/`update_time`。在 Flyway 迁移脚本执行前，需要使用 `@TableField` 做映射桥接。但这样做会让代码很冗余。更简单的方案是：**本次 Phase 3 暂不修改 8 个使用 createdAt/updatedAt 的 Entity**，等 Phase 6 统一执行 Flyway + 代码修改。

**调整方案:** Phase 3 仅处理以下 2 个可以直接改的 Entity：

- `MembershipOrder.java` — 已使用 `createTime/updateTime`，仅需 `extends BaseEntity` 并删除重复字段
- `CoachDetail.java` — 需要 `extends BaseEntity`，但使用了 `OffsetDateTime`，需改为 `LocalDateTime`

**推迟到 Phase 6 的 8 个 Entity：** Product, ProductOrder, MembershipCard, MembershipCardType, MembershipCardContent, UserMembership, SysDict, SysDictItem（需要 Flyway 配合迁移列名）。

- [ ] **Step 1: MembershipOrder.java — 继承 BaseEntity**

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("membership_order")
public class MembershipOrder extends BaseEntity {
    // 删除字段:
    // private LocalDateTime createTime;
    // private LocalDateTime updateTime;
    // 添加 deleted 的 @TableField("deleted")
}
```

- [ ] **Step 2: CoachDetail.java — 继承 BaseEntity + 日期类型修正**

```java
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coach_detail")
public class CoachDetail extends BaseEntity {
    // 删除字段:
    // private OffsetDateTime createTime;  // OffsetDateTime → BaseEntity 的 LocalDateTime
    // private OffsetDateTime updateTime;
    // private Boolean deleted; (以及 @TableLogic 注解，BaseEntity 已有)
}
```

注意：数据库 `coach_detail` 表的 `create_time`/`update_time` 列类型是 `TIMESTAMP WITH TIME ZONE` 还是 `TIMESTAMP WITHOUT TIME ZONE`？如果是前者，需确认 MyBatis-Plus TypeHandler 能正确处理。如果数据库也是 WITHOUT TIME ZONE，则无问题。

- [ ] **Step 3: 编译验证**

```bash
mvn compile -q
```

---

### Task 3.2: 移除 CoachDetail Entity 中的业务方法

**Files:**
- Modify: `src/main/java/com/fitness/modules/user/model/entity/CoachDetail.java`

**问题:** Entity 类中混入 8 个 JSON 转换方法（`getTagsJson`、`getSpecialtiesJson` 等）。

- [ ] **Step 1: 移除 8 个 getXxxJson 方法**

删除第 173-231 行的 8 个方法。

- [ ] **Step 2: 在调用处内联或抽取工具方法**

搜索这些方法的调用点，替换为直接的 JSON 转换逻辑。使用 `grep` 定位调用者：

```bash
grep -rn "getTagsJson\|getSpecialtiesJson\|getCertificationsJson" src/main/java/
```

根据调用情况决定：如果仅在 CoachDetailServiceImpl 中使用，在 Service 中直接使用 ObjectMapper 转换。如果多处使用，抽取 `CoachDetailJsonUtils` 工具类。

- [ ] **Step 3: 编译验证**

```bash
mvn compile -q
```

---

## Phase 4: Controller 层规范化

### Task 4.1: 修复 AnalysisReportController 分层架构问题

**Files:**
- Modify: `src/main/java/com/fitness/modules/analysis/controller/AnalysisReportController.java`

**问题:** Controller 直接注入 `UserMapper`，且 `getCurrentUserId` 手动从 SecurityContext 提取并查询数据库。

- [ ] **Step 1: 删除 UserMapper 注入，使用 SecurityUtils**

删除 `private final UserMapper userMapper;` 和对应 import。

- [ ] **Step 2: 替换 getCurrentUserId 实现**

```java
import com.fitness.integration.security.SecurityUtils;

private Long getCurrentUserId() {
    return SecurityUtils.getCurrentUserId();
}
```

- [ ] **Step 3: 同时修复硬编码字符串**

将 `Result.error("删除失败")` 改为 `Result.error(ErrorCode.OPERATION_FAILED)` 或对应的错误码。

- [ ] **Step 4: 编译验证**

```bash
mvn compile -q
```

---

### Task 4.2: 修复 AuthController — 移除多余 try-catch + 统一异常处理

**Files:**
- Modify: `src/main/java/com/fitness/modules/user/controller/AuthController.java`

- [ ] **Step 1: 移除 sendSmsCode 中的 try-catch**

删除 [第 107-137 行](file:///g:/ai-powered-fitness-system-v2/src/main/java/com/fitness/modules/user/controller/AuthController.java#L107-L137) 的 try-catch 块，让 `BusinessException` 直接抛给 `GlobalExceptionHandler`。

改造后：
```java
@PostMapping("/sms-code")
public Result<Map<String, Object>> sendSmsCode(@Valid @RequestBody SendSmsCodeDTO dto) {
    // ... 业务调用（不包裹 try-catch）
    smsCodeService.sendCode(dto.getPhone());
    Map<String, Object> data = new HashMap<>();
    data.put("message", "验证码发送成功");
    return Result.success(data);
}
```

- [ ] **Step 2: 编译验证**

```bash
mvn compile -q
```

---

### Task 4.3: 补充 6 个 Controller 缺失的 @Valid 注解

**Files:**
- Modify: `src/main/java/com/fitness/modules/course/controller/CourseController.java`
- Modify: `src/main/java/com/fitness/modules/course/controller/VideoCourseAdminController.java`
- Modify: `src/main/java/com/fitness/modules/course/controller/CourseSessionController.java`
- Modify: `src/main/java/com/fitness/modules/user/controller/UserAdminController.java`
- Modify: `src/main/java/com/fitness/modules/system/controller/SysDictController.java`
- Modify: `src/main/java/com/fitness/modules/booking/controller/BookingAdminController.java`

- [ ] **Step 1: 逐文件在 @RequestBody 参数前添加 @Valid**

在每个 Controller 的 `getXxxList(XxxQueryDTO query)` 和 `create/update(@RequestBody XxxDTO dto)` 参数前添加 `@Valid`。

- [ ] **Step 2: 编译验证**

```bash
mvn compile -q
```

---

## Phase 5: GlobalExceptionHandler 增强

### Task 5.1: 补充 7 种常见异常处理

**Files:**
- Modify: `src/main/java/com/fitness/common/exception/GlobalExceptionHandler.java`

- [ ] **Step 1: 添加 HttpMessageNotReadableException 处理（400）**

```java
@ExceptionHandler(HttpMessageNotReadableException.class)
public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
    log.warn("请求体解析失败: {}", e.getMessage());
    return Result.error(ErrorCode.PARAM_ERROR.getCode(), "请求体格式错误");
}
```

- [ ] **Step 2: 添加 MethodArgumentTypeMismatchException 处理（400）**

```java
@ExceptionHandler(MethodArgumentTypeMismatchException.class)
public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
    log.warn("参数类型不匹配: name={}, value={}", e.getName(), e.getValue());
    return Result.error(ErrorCode.PARAM_ERROR.getCode(), "参数类型错误: " + e.getName());
}
```

- [ ] **Step 3: 添加 ConstraintViolationException 处理（400）**

```java
@ExceptionHandler(ConstraintViolationException.class)
public Result<Void> handleConstraintViolationException(ConstraintViolationException e) {
    log.warn("参数校验失败: {}", e.getMessage());
    return Result.error(ErrorCode.PARAM_ERROR.getCode(), "参数校验失败: " + e.getMessage());
}
```

- [ ] **Step 4: 添加 MissingServletRequestParameterException 处理（400）**

```java
@ExceptionHandler(MissingServletRequestParameterException.class)
public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
    log.warn("缺少必要参数: {}", e.getParameterName());
    return Result.error(ErrorCode.PARAM_ERROR.getCode(), "缺少必要参数: " + e.getParameterName());
}
```

- [ ] **Step 5: 添加 HttpRequestMethodNotSupportedException 处理（405）**

```java
@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
public Result<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
    log.warn("不支持的请求方法: {}", e.getMethod());
    return Result.error(405, "不支持的请求方法: " + e.getMethod());
}
```

- [ ] **Step 6: 添加 MaxUploadSizeExceededException 处理（413）**

```java
@ExceptionHandler(MaxUploadSizeExceededException.class)
public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
    log.warn("文件大小超过限制: {}", e.getMessage());
    return Result.error(413, "文件大小超过限制");
}
```

- [ ] **Step 7: 添加 DataIntegrityViolationException 处理（409）**

```java
@ExceptionHandler(DataIntegrityViolationException.class)
public Result<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
    log.warn("数据完整性冲突: {}", e.getMessage());
    return Result.error(409, "数据冲突，请检查输入");
}
```

- [ ] **Step 8: 编译验证**

```bash
mvn compile -q
```

---

## Phase 6: 数据库列名迁移（可选，需谨慎执行并在测试环境验证）

### Task 6.1: Flyway 迁移 + 8 个 Entity 规范化

**说明:** 将 8 个使用 `created_at`/`updated_at` 的表重命名为 `create_time`/`update_time`，同步修改 Entity。

**Files (需先后操作):**
- Create: Flyway 迁移脚本
- Modify: 8 个 Entity 类
- Modify: 8 个 Mapper XML（如有 BaseResultMap）

> ⚠️ **此 Phase 需在有完整数据库备份的测试环境中执行。生产环境迁移需要停机窗口或使用在线 DDL 工具。**

- [ ] **Step 1: 创建 Flyway 迁移脚本**

为每张表生成 `ALTER TABLE xxx RENAME COLUMN created_at TO create_time` 等语句。

- [ ] **Step 2: 修改 8 个 Entity 继承 BaseEntity，删除重复字段**

Product, ProductOrder, MembershipCard, MembershipCardType, MembershipCardContent, UserMembership, SysDict, SysDictItem。

- [ ] **Step 3: 验证 Flyway 迁移 + 编译 + 集成测试**

```bash
mvn compile -q
mvn test
```

---

## 验证清单

每个 Phase 完成后执行：

```bash
# 编译验证
mvn compile -q

# 如果项目有测试
mvn test -q

# 启动应用验证（如果可以）
mvn spring-boot:run
```

## 执行顺序建议

| 顺序 | Phase | 原因 |
|------|-------|------|
| 1 | Phase 1 | 修复运行时错误，保障基础稳定性 |
| 2 | Phase 2 | 安全加固，消除最高风险 |
| 3 | Phase 3 | Entity 规范化（仅 2 个可直接改的） |
| 4 | Phase 4 | Controller 分层修复 |
| 5 | Phase 5 | 异常处理增强 |
| 6 | Phase 6 | 数据库列名迁移（需额外验证） |

Phase 1-5 之间无强依赖，可按序执行或选择性执行。Phase 6 需要数据库配合，建议在确认 Phase 1-5 无问题后独立执行。