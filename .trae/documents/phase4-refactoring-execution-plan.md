# Phase 4 重构执行计划（按步骤逐一验证）

## 当前状态

Phase 1 四个 ServiceImpl 的改造代码已写入，但存在编译错误需要修复：

| 文件 | 状态 | 问题 |
|------|------|------|
| `CourseServiceImpl.java` | ⚠️ 待修复 | 4处 `courseMapper.xxx` 需改为 `baseMapper.xxx`（`courseMapper` 字段已移除） |
| `VideoCourseServiceImpl.java` | ✅ 已完成 | 无问题 |
| `AnnouncementServiceImpl.java` | ⚠️ 待修复 | 1处 `announcementMapper.incrementViewCount` 需改为 `baseMapper.incrementViewCount` |
| `UserFitnessProfileServiceImpl.java` | ✅ 已完成 | 无问题 |

---

## 阶段 1：Service 层继承 ServiceImpl 改造（续）

### 步骤 1.0：修复编译错误
- **文件**：`CourseServiceImpl.java`
  - 行 107：`courseMapper.selectCourseDetail` → `baseMapper.selectCourseDetail`
  - 行 117：`courseMapper.selectCourseList` → `baseMapper.selectCourseList`
  - 行 124：`courseMapper.selectCourseList` → `baseMapper.selectCourseList`
  - 行 129：`courseMapper.selectDistinctCategories` → `baseMapper.selectDistinctCategories`
- **文件**：`AnnouncementServiceImpl.java`
  - 行 138：`announcementMapper.incrementViewCount` → `baseMapper.incrementViewCount`

### 验证门禁 1
- [ ] 运行 `mvn compile -DskipTests` → 编译通过
- [ ] 运行 `mvn test` → 60/67 通过（7个 BookingServiceTest 预存问题保持不变）
- [ ] **若不通过，修复问题后重试，不进入阶段 2**

---

## 阶段 2：ChatSession / ChatMessage 命名标准化

### 步骤 2.1：标准化 `ChatSession`
- **文件**：`src/main/java/com/fitness/modules/chat/model/entity/ChatSession.java`
- **操作**：
  1. 新增 `extends BaseEntity`
  2. 重命名字段 + 添加 `@TableField` 显式映射：
     - `createdAt` → `createTime` + `@TableField("created_at")`
     - `updatedAt` → `updateTime` + `@TableField("updated_at")`
     - `isDeleted` → `deleted` + `@TableField("is_deleted")`
  3. 保留原有 `@TableLogic(delval = "true")` 移到新字段上
- **同步修改引用文件**：
  - `ChatAssistantServiceImpl.java`：`setCreatedAt` → `setCreateTime`，`setUpdatedAt` → `setUpdateTime`，`setIsDeleted` → `setDeleted`
  - 其他引用处（lambda 排序 `ChatSession::getUpdatedAt` → `ChatSession::getUpdateTime`）

### 步骤 2.2：标准化 `ChatMessage`
- **文件**：`src/main/java/com/fitness/modules/chat/model/entity/ChatMessage.java`
- **操作**：
  - 重命名字段（ChatMessage 不继承 BaseEntity，因为没有 deleted 字段）：
    - `createdAt` → `createTime` + `@TableField("created_at")`
- **同步修改引用文件**：
  - `ChatAssistantServiceImpl.java` 中 `ChatMessage.setCreatedAt` 调用
  - `ChatContextServiceImpl.java` 中的调用

### 验证门禁 2
- [ ] 运行 `mvn compile -DskipTests` → 编译通过
- [ ] 运行 `mvn test` → 60/67 通过
- [ ] **若不通过，修复问题后重试，不进入阶段 3**

---

## 阶段 3：Controller @Valid 与 DTO 校验注解

### 步骤 3.1：给关键 DTO 添加校验注解
| DTO | 添加注解 |
|-----|----------|
| `LoginDTO` | `@NotBlank` on username, password |
| `UserDTO` | `@NotBlank` on username, `@Email` on email |
| `CoachDetailDTO` | `@NotBlank` on realName |
| `BannerDTO` | `@NotBlank` on title, `@NotNull` on sortOrder |
| `AnnouncementDTO` | `@NotBlank` on title, content |
| `BookingDTO` | `@NotNull` on sessionId, userId |
| `ProductDTO` | `@NotBlank` on name, `@NotNull` on price |

### 步骤 3.2：给关键 Controller 添加 `@Valid`
| Controller | 方法 | 参数 |
|------------|------|------|
| `AuthController` | `login` | `LoginDTO` |
| `UserAdminController` | `createUser` | `UserDTO` |
| `CoachDetailController` | `saveCoachDetail` | `CoachDetailDTO` |
| `BannerController` | create/update | `BannerDTO` |
| `AnnouncementController` | create/update | `AnnouncementDTO` |
| `BookingController` | create | `BookingDTO` |
| `ProductController` | create/update | `ProductDTO` |

### 验证门禁 3
- [ ] 运行 `mvn compile -DskipTests` → 编译通过
- [ ] 运行 `mvn test` → 60/67 通过
- [ ] **若不通过，修复问题后重试，不进入阶段 4**

---

## 阶段 4：工具类清理

### 步骤 4.1：删除 `DateUtils.java`
- **文件**：`src/main/java/com/fitness/common/utils/DateUtils.java`
- **原因**：全项目零引用

### 步骤 4.2：删除 `JsonUtils.java`
- **文件**：`src/main/java/com/fitness/common/utils/JsonUtils.java`
- **原因**：全项目零引用

### 验证门禁 4
- [ ] 运行 `mvn compile -DskipTests` → 编译通过
- [ ] 运行 `mvn test` → 60/67 通过
- [ ] **全部完成！**

---

## 执行原则

```
阶段 N → 编译 + 测试
  ├── 通过 ✅ → 进入阶段 N+1
  └── 失败 ❌ → 修复 → 重新验证 → 通过后再进入阶段 N+1
```

**每个阶段独立验证，绝不跳过验证门禁。任一阶段失败则暂停后续阶段，修复当前阶段问题后继续。**