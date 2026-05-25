# 敏感数据脱敏设计

## 背景

当前项目 VO 实体类中直接暴露了手机号、邮箱、真实姓名、收货地址等敏感字段，无任何保护。需要引入基于注解的声明式脱敏机制，在 Jackson 序列化阶段自动根据用户角色决定是否脱敏。

## 涉及的敏感字段

| 字段 | 类型 | 脱敏规则 | 出现的 VO |
|------|------|----------|-----------|
| phone | 手机号 | `138****1234` | UserVO, CoachDetailVO, CoachHomePageVO |
| email | 邮箱 | `zha***@qq.com` | UserVO, CoachDetailVO, CoachHomePageVO |
| realName | 真实姓名 | `张*明` | CoachDetailVO, CoachHomePageVO |
| address | 收货地址 | 前6字符 + `***` | ProductOrderVO |

## 角色权限策略

- **ADMIN / COACH**：返回原始完整数据
- **MEMBER / 未认证访客**：返回脱敏数据

判断逻辑在 `SensitiveSerializer` 中通过 `SecurityContextHolder.getContext().getAuthentication()` 获取当前认证信息。

## 架构设计

### 新增文件

全部位于 `src/main/java/com/fitness/common/sensitive/` 目录下：

#### 1. `SensitiveType.java` — 敏感类型枚举

```java
public enum SensitiveType {
    PHONE,
    EMAIL,
    NAME,
    ADDRESS
}
```

#### 2. `Sensitive.java` — 自定义注解

```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@JacksonAnnotationsInside
@JsonSerialize(using = SensitiveSerializer.class)
public @interface Sensitive {
    SensitiveType value();
}
```

使用 `@JacksonAnnotationsInside` 元注解使其可与其他 Jackson 注解组合。

#### 3. `MaskUtil.java` — 脱敏工具类

静态方法，各类型脱敏规则：

- `maskPhone(String)`: 保留前3后4，中间替换为 `****`
- `maskEmail(String)`: @ 前保留前3字符，@ 后保留完整域名
- `maskName(String)`: 两字保留首尾，三字以上保留首尾，中间替换为 `*`
- `maskAddress(String)`: 保留前6字符 + `***`
- `mask(String, SensitiveType)`: 分发方法

空值和空字符串直接返回原值，不做处理。

#### 4. `SensitiveSerializer.java` — Jackson 序列化器

实现 `JsonSerializer<String>` 和 `ContextualSerializer`：

- `createContextual()`: 从 `@Sensitive` 注解读取 `SensitiveType`，创建带类型的序列化器实例
- `serialize()`: 调用 `shouldMask()` 判断是否需要脱敏，是则调用 `MaskUtil.mask()`，否则原样输出
- `shouldMask()`: 从 `SecurityContextHolder` 获取认证信息，检查是否具有 `ROLE_ADMIN` 或 `ROLE_COACH` 权限

#### 5. `SensitiveModule.java` — Jackson Module

注册 `SensitiveSerializer` 到 Jackson 的 `ObjectMapper`。

### 修改文件

#### `config/JacksonConfig.java`

在现有配置基础上注册 `SensitiveModule`。

#### `user/model/vo/UserVO.java`

```java
@Sensitive(SensitiveType.PHONE)
private String phone;

@Sensitive(SensitiveType.EMAIL)
private String email;
```

#### `user/model/vo/CoachDetailVO.java`

```java
@Sensitive(SensitiveType.PHONE)
private String phone;

@Sensitive(SensitiveType.EMAIL)
private String email;

@Sensitive(SensitiveType.NAME)
private String realName;
```

#### `user/model/vo/CoachHomePageVO.java`

```java
@Sensitive(SensitiveType.PHONE)
private String phone;

@Sensitive(SensitiveType.EMAIL)
private String email;

@Sensitive(SensitiveType.NAME)
private String realName;
```

#### `product/model/vo/ProductOrderVO.java`

```java
@Sensitive(SensitiveType.ADDRESS)
private String address;
```

## 不改动的部分

- Service 层的 VO 转换逻辑（`BeanUtil.copyProperties` / 手动赋值）无需修改，脱敏在序列化阶段自动完成
- Controller 层无需修改
- `emergencyContact` 字段为 Object 类型（JSONB），本次不处理（非 String 类型需额外设计）

## 验证方式

1. 以 MEMBER 角色调用 `/api/v1/user/profile`，确认 phone 返回 `138****1234` 格式
2. 以 ADMIN 角色调用同一接口，确认 phone 返回完整号码
3. 以未认证状态调用公开接口（如教练列表），确认返回脱敏数据
4. 运行 `./mvnw verify` 确保 Checkstyle 通过
