# 短信验证码登录功能完善实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 完善登录模块的短信验证码登录功能，包括验证码生成、存储、验证和基于手机号的登录流程

**Architecture:** 基于现有的滑块验证流程，新增短信验证码服务层，使用Redis存储验证码，支持手机号登录/注册一体化流程。保持与现有用户名密码登录相同的JWT Token返回格式。

**Tech Stack:** Spring Boot, Redis (StringRedisTemplate), MyBatis-Plus, JWT, Validation

---

## 现状分析

当前已实现：
- 滑块验证流程（生成令牌、验证、失效）
- 短信验证码发送接口（模拟返回123456）
- 用户名密码登录

需要完善：
- 真实验证码生成与存储
- 验证码验证接口
- 手机号登录/注册接口

---

## 文件结构变更

| 类型 | 文件路径 | 说明 |
|------|----------|------|
| 新增 | `com.fitness.modules.user.service.SmsCodeService` | 短信验证码服务接口 |
| 新增 | `com.fitness.modules.user.service.impl.SmsCodeServiceImpl` | 短信验证码服务实现 |
| 新增 | `com.fitness.modules.user.model.dto.SmsCodeLoginDTO` | 短信验证码登录DTO |
| 新增 | `com.fitness.modules.user.model.dto.SmsCodeVerifyDTO` | 验证码校验DTO |
| 修改 | `com.fitness.common.constants.ErrorCode` | 新增短信相关错误码 |
| 修改 | `com.fitness.modules.user.mapper.UserMapper` | 新增根据手机号查询用户方法 |
| 修改 | `com.fitness.modules.user.service.UserService` | 新增短信登录方法 |
| 修改 | `com.fitness.modules.user.service.impl.UserServiceImpl` | 实现短信登录逻辑 |
| 修改 | `com.fitness.modules.user.controller.AuthController` | 新增短信登录接口 |

---

## Task 1: 新增短信相关错误码

**Files:**
- 修改: `src/main/java/com/fitness/common/constants/ErrorCode.java`

- [ ] **Step 1: 在ErrorCode枚举中添加短信相关错误码**

```java
// 在现有错误码后添加
SMS_CODE_ERROR(1004, "短信验证码错误"),
SMS_CODE_EXPIRED(1005, "短信验证码已过期"),
SMS_CODE_SEND_TOO_FREQUENT(1006, "短信验证码发送过于频繁"),
PHONE_NOT_BOUND(1007, "手机号未绑定用户");
```

- [ ] **Step 2: 验证编译通过**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

---

## Task 2: UserMapper添加手机号查询方法

**Files:**
- 修改: `src/main/java/com/fitness/modules/user/mapper/UserMapper.java`

- [ ] **Step 1: 添加根据手机号查询用户方法**

```java
/**
 * 根据手机号查询用户
 *
 * @param phone 手机号
 * @return 用户信息
 */
@Select("SELECT * FROM sys_user WHERE phone = #{phone} AND deleted = false")
User selectByPhone(@Param("phone") String phone);
```

- [ ] **Step 2: 验证编译通过**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

---

## Task 3: 创建短信验证码服务接口

**Files:**
- 创建: `src/main/java/com/fitness/modules/user/service/SmsCodeService.java`

- [ ] **Step 1: 创建SmsCodeService接口**

```java
package com.fitness.modules.user.service;

/**
 * 短信验证码服务接口
 */
public interface SmsCodeService {

    /**
     * 生成并发送短信验证码
     *
     * @param phone 手机号
     * @return 是否发送成功
     */
    boolean sendSmsCode(String phone);

    /**
     * 验证短信验证码
     *
     * @param phone 手机号
     * @param code  验证码
     * @return 是否验证通过
     */
    boolean verifySmsCode(String phone, String code);

    /**
     * 检查是否可以发送验证码（防频繁）
     *
     * @param phone 手机号
     * @return 是否可以发送
     */
    boolean canSend(String phone);

    /**
     * 获取剩余冷却时间（秒）
     *
     * @param phone 手机号
     * @return 剩余秒数，0表示可以发送
     */
    long getRemainingCooldown(String phone);
}
```

- [ ] **Step 2: 验证编译通过**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

---

## Task 4: 实现短信验证码服务

**Files:**
- 创建: `src/main/java/com/fitness/modules/user/service/impl/SmsCodeServiceImpl.java`

- [ ] **Step 1: 创建SmsCodeServiceImpl实现类**

```java
package com.fitness.modules.user.service.impl;

import com.fitness.modules.user.service.SmsCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信验证码服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsCodeServiceImpl implements SmsCodeService {

    private final StringRedisTemplate redisTemplate;

    // Redis键前缀
    private static final String SMS_CODE_KEY_PREFIX = "sms:code:";
    private static final String SMS_CODE_COOLDOWN_KEY_PREFIX = "sms:cooldown:";

    // 验证码过期时间（5分钟）
    private static final long CODE_EXPIRE_MINUTES = 5;
    // 发送冷却时间（60秒）
    private static final long COOLDOWN_SECONDS = 60;
    // 验证码长度
    private static final int CODE_LENGTH = 6;

    @Override
    public boolean sendSmsCode(String phone) {
        // 1. 检查冷却时间
        if (!canSend(phone)) {
            log.warn("短信验证码发送过于频繁: phone={}", phone);
            return false;
        }

        // 2. 生成6位数字验证码
        String code = generateCode();

        // 3. 存储验证码到Redis
        String codeKey = SMS_CODE_KEY_PREFIX + phone;
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 4. 设置冷却时间
        String cooldownKey = SMS_CODE_COOLDOWN_KEY_PREFIX + phone;
        redisTemplate.opsForValue().set(cooldownKey, "1", COOLDOWN_SECONDS, TimeUnit.SECONDS);

        // 5. 模拟发送短信（后续接入真实短信服务）
        log.info("短信验证码发送成功: phone={}, code={}", phone, code);

        return true;
    }

    @Override
    public boolean verifySmsCode(String phone, String code) {
        String codeKey = SMS_CODE_KEY_PREFIX + phone;
        String storedCode = redisTemplate.opsForValue().get(codeKey);

        if (storedCode == null) {
            log.warn("短信验证码验证失败: 验证码已过期或不存在, phone={}", phone);
            return false;
        }

        boolean matched = storedCode.equals(code);
        if (matched) {
            // 验证通过后删除验证码（一次性使用）
            redisTemplate.delete(codeKey);
            log.info("短信验证码验证成功: phone={}", phone);
        } else {
            log.warn("短信验证码验证失败: 验证码不匹配, phone={}", phone);
        }

        return matched;
    }

    @Override
    public boolean canSend(String phone) {
        String cooldownKey = SMS_CODE_COOLDOWN_KEY_PREFIX + phone;
        Boolean exists = redisTemplate.hasKey(cooldownKey);
        return !Boolean.TRUE.equals(exists);
    }

    @Override
    public long getRemainingCooldown(String phone) {
        String cooldownKey = SMS_CODE_COOLDOWN_KEY_PREFIX + phone;
        Long expire = redisTemplate.getExpire(cooldownKey, TimeUnit.SECONDS);
        return expire != null && expire > 0 ? expire : 0;
    }

    /**
     * 生成6位数字验证码
     *
     * @return 验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
}
```

- [ ] **Step 2: 验证编译通过**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

---

## Task 5: 创建短信验证码登录DTO

**Files:**
- 创建: `src/main/java/com/fitness/modules/user/model/dto/SmsCodeLoginDTO.java`

- [ ] **Step 1: 创建短信验证码登录DTO**

```java
package com.fitness.modules.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 短信验证码登录DTO
 */
@Data
public class SmsCodeLoginDTO {

    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 短信验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须是6位数字")
    private String smsCode;
}
```

- [ ] **Step 2: 验证编译通过**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

---

## Task 6: UserService添加短信登录方法

**Files:**
- 修改: `src/main/java/com/fitness/modules/user/service/UserService.java`

- [ ] **Step 1: 在UserService接口中添加短信登录方法**

```java
/**
 * 短信验证码登录
 * 如果手机号未注册，自动创建新用户
 *
 * @param phone   手机号
 * @param smsCode 短信验证码
 * @return Token信息
 */
Map<String, Object> loginBySmsCode(String phone, String smsCode);
```

- [ ] **Step 2: 验证编译通过**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

---

## Task 7: UserServiceImpl实现短信登录

**Files:**
- 修改: `src/main/java/com/fitness/modules/user/service/impl/UserServiceImpl.java`

- [ ] **Step 1: 注入SmsCodeService**

在类头部添加依赖：
```java
private final SmsCodeService smsCodeService;
```

- [ ] **Step 2: 添加短信登录实现方法**

在类末尾添加：
```java
@Override
@Transactional(rollbackFor = Exception.class)
public Map<String, Object> loginBySmsCode(String phone, String smsCode) {
    // 1. 验证短信验证码
    if (!smsCodeService.verifySmsCode(phone, smsCode)) {
        throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
    }

    // 2. 根据手机号查询用户
    User user = userMapper.selectByPhone(phone);

    // 3. 如果用户不存在，自动注册
    if (user == null) {
        user = new User();
        user.setUsername(generateUsernameFromPhone(phone));
        user.setPhone(phone);
        // 短信登录用户设置随机密码（用户后续可通过密码重置修改）
        user.setPassword(passwordEncoder.encode(generateRandomPassword()));
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);

        // 赋予默认角色 ROLE_USER
        Role role = roleMapper.selectByRoleCode("ROLE_USER");
        if (role != null) {
            userMapper.insertUserRole(user.getId(), role.getId());
        }

        log.info("短信登录自动注册新用户: phone={}, userId={}", phone, user.getId());
    }

    // 4. 检查用户状态
    if (user.getStatus() != 1) {
        throw new BusinessException(ErrorCode.FORBIDDEN);
    }

    // 5. 获取用户角色
    List<Role> roles = userMapper.selectRolesByUserId(user.getId());
    List<String> roleCodes = roles.stream()
            .map(Role::getRoleCode)
            .collect(Collectors.toList());

    // 6. 生成JWT Token
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());
    claims.put("roles", roleCodes);
    String token = jwtUtils.generateToken(user.getUsername(), claims);

    // 7. 构建返回结果
    Map<String, Object> result = new HashMap<>();
    result.put("token", token);
    result.put("tokenType", "Bearer");
    result.put("expiresIn", jwtUtils.getExpirationTime());

    UserVO userVO = convertToVO(user);
    userVO.setRoles(roleCodes);
    result.put("userInfo", userVO);

    log.info("短信验证码登录成功: phone={}, userId={}", phone, user.getId());
    return result;
}

/**
 * 根据手机号生成用户名
 * 格式：user_手机号后8位_随机4位
 *
 * @param phone 手机号
 * @return 用户名
 */
private String generateUsernameFromPhone(String phone) {
    String phoneSuffix = phone.substring(phone.length() - 8);
    Random random = new Random();
    int randomSuffix = random.nextInt(10000);
    return String.format("user_%s_%04d", phoneSuffix, randomSuffix);
}

/**
 * 生成随机密码
 *
 * @return 随机密码
 */
private String generateRandomPassword() {
    Random random = new Random();
    StringBuilder password = new StringBuilder();
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
    for (int i = 0; i < 16; i++) {
        password.append(chars.charAt(random.nextInt(chars.length())));
    }
    return password.toString();
}
```

- [ ] **Step 3: 添加Random类导入**

```java
import java.util.Random;
```

- [ ] **Step 4: 验证编译通过**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

---

## Task 8: 修改AuthController发送验证码接口

**Files:**
- 修改: `src/main/java/com/fitness/modules/user/controller/AuthController.java`

- [ ] **Step 1: 注入SmsCodeService**

```java
private final SmsCodeService smsCodeService;
```

- [ ] **Step 2: 修改sendSmsCode方法**

替换原有sendSmsCode方法：
```java
/**
 * 发送短信验证码
 * 需要先完成滑块验证
 *
 * @param dto 请求数据
 * @return 发送结果
 */
@PostMapping("/sms-code")
public Result<Map<String, Object>> sendSmsCode(@Valid @RequestBody SmsCodeDTO dto) {
    log.info("发送短信验证码请求: phone={}", dto.getPhone());

    // 1. 检查滑块验证是否通过
    if (!sliderVerifyService.isVerified(dto.getVerifyToken())) {
        log.warn("发送短信验证码失败: 滑块验证未通过或已过期, phone={}", dto.getPhone());
        return Result.error(400, "请先完成滑块验证");
    }

    // 2. 检查发送频率
    if (!smsCodeService.canSend(dto.getPhone())) {
        long remainingSeconds = smsCodeService.getRemainingCooldown(dto.getPhone());
        log.warn("发送短信验证码失败: 发送过于频繁, phone={}, remaining={}s", dto.getPhone(), remainingSeconds);
        return Result.error(429, "发送过于频繁，请" + remainingSeconds + "秒后再试");
    }

    // 3. 使验证令牌失效（一次性使用）
    sliderVerifyService.invalidateToken(dto.getVerifyToken());

    // 4. 发送短信验证码
    boolean sent = smsCodeService.sendSmsCode(dto.getPhone());

    if (!sent) {
        return Result.error(500, "验证码发送失败，请稍后重试");
    }

    Map<String, Object> result = new HashMap<>();
    result.put("sent", true);
    result.put("message", "验证码已发送");

    return Result.success(result);
}
```

- [ ] **Step 3: 添加短信验证码登录接口**

```java
/**
 * 短信验证码登录
 * 如果手机号未注册，自动创建新用户
 *
 * @param dto 登录信息
 * @return Token信息
 */
@PostMapping("/login/sms")
public Result<Map<String, Object>> loginBySmsCode(@Valid @RequestBody SmsCodeLoginDTO dto) {
    log.info("短信验证码登录请求: phone={}", dto.getPhone());
    Map<String, Object> tokenInfo = userService.loginBySmsCode(dto.getPhone(), dto.getSmsCode());
    return Result.success(tokenInfo);
}
```

- [ ] **Step 4: 验证编译通过**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

---

## Task 9: 运行测试验证

**Files:**
- 运行测试

- [ ] **Step 1: 运行单元测试**

Run: `./mvnw test -q`
Expected: Tests run: X, Failures: 0, Errors: 0

- [ ] **Step 2: 启动应用验证**

Run: `./mvnw spring-boot:run &`
然后测试接口：

1. 获取滑块验证令牌：`GET /api/v1/auth/slider-verify/token`
2. 验证滑块：`POST /api/v1/auth/slider-verify/verify`
3. 发送短信验证码：`POST /api/v1/auth/sms-code`
4. 短信验证码登录：`POST /api/v1/auth/login/sms`

---

## API接口文档

### 1. 发送短信验证码

**接口：** `POST /api/v1/auth/sms-code`

**请求体：**
```json
{
  "phone": "13800138000",
  "verifyToken": "滑块验证通过的令牌"
}
```

**成功响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "sent": true,
    "message": "验证码已发送"
  }
}
```

**频率限制响应：**
```json
{
  "code": 429,
  "message": "发送过于频繁，请45秒后再试"
}
```

### 2. 短信验证码登录

**接口：** `POST /api/v1/auth/login/sms`

**请求体：**
```json
{
  "phone": "13800138000",
  "smsCode": "123456"
}
```

**成功响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userInfo": {
      "id": 1,
      "username": "user_01380001_1234",
      "phone": "13800138000",
      "email": null,
      "avatar": null,
      "createTime": "2024-01-01T10:00:00",
      "roles": ["ROLE_USER"]
    }
  }
}
```

**验证码错误响应：**
```json
{
  "code": 1004,
  "message": "短信验证码错误"
}
```

---

## 注意事项

1. **验证码有效期：** 5分钟
2. **发送冷却时间：** 60秒
3. **自动注册：** 未注册手机号会自动创建用户，用户名为`user_手机号后8位_随机4位`
4. **安全：** 验证码验证通过后立即失效（一次性使用）
5. **后续优化：** 当前为模拟发送，生产环境需要接入真实短信服务（阿里云、腾讯云等）
