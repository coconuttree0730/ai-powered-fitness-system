# Phase 4 重构计划

## 现状分析

| 指标 | 数据 |
|------|------|
| 已继承 ServiceImpl 的 Service | 10 个 |
| 未继承 ServiceImpl 的 Service | 24 个 |
| Chat 模块命名不一致实体 | 2 个（ChatSession, ChatMessage） |
| 缺少 @Valid 的 Controller 方法 | 22+ 个 |
| 完全未使用的工具类 | 2 个（DateUtils, JsonUtils） |

---

## 阶段 1：Service 层继承 ServiceImpl 改造

### 目标
将单 Mapper 的 CRUD 密集型 Service 改为继承 MyBatis-Plus `ServiceImpl<M, T>`，消除手动 `xxxMapper.insert/selectById/updateById/deleteById` 调用，统一使用 `this.save/getById/updateById/removeById` 等内置方法。

### 实施步骤

#### 步骤 1.1：改造 `CourseServiceImpl`
- 文件：`src/main/java/com/fitness/modules/course/service/impl/CourseServiceImpl.java`
- 改为：`extends ServiceImpl<CourseMapper, Course>`
- 替换：
  - `courseMapper.insert(course)` → `this.save(course)`
  - `courseMapper.selectById(id)` → `this.getById(id)`
  - `courseMapper.deleteById(id)` → `this.removeById(id)`
  - 分页查询保留自定义 `courseMapper.selectCourseList`（含 JOIN 逻辑）
- 验证：编译 + `CourseServiceImpl` 相关测试

#### 步骤 1.2：改造 `VideoCourseServiceImpl`
- 文件：`src/main/java/com/fitness/modules/course/service/impl/VideoCourseServiceImpl.java`
- 改为：`extends ServiceImpl<VideoCourseMapper, VideoCourse>`
- 替换：
  - `videoCourseMapper.insert(course)` → `this.save(course)`
  - `videoCourseMapper.updateById(existing)` → `this.updateById(existing)`
  - 分页查询保留自定义 `videoCourseMapper.selectVideoCoursePage`
- 验证：编译 + 测试

#### 步骤 1.3：改造 `AnnouncementServiceImpl`
- 文件：`src/main/java/com/fitness/modules/announcement/service/impl/AnnouncementServiceImpl.java`
- 改为：`extends ServiceImpl<AnnouncementMapper, Announcement>`
- 替换：
  - `announcementMapper.selectById(id)` → `this.getById(id)`
  - `announcementMapper.insert(entity)` → `this.save(entity)`
  - `announcementMapper.updateById(entity)` → `this.updateById(entity)`
  - `announcementMapper.deleteById(id)` → `this.removeById(id)`
  - 需要移除 `private final AnnouncementMapper announcementMapper` 并改用 `this.baseMapper`
- 验证：编译 + 测试

#### 步骤 1.4：改造 `UserFitnessProfileServiceImpl`
- 文件：`src/main/java/com/fitness/modules/user/service/impl/UserFitnessProfileServiceImpl.java`
- 改为：`extends ServiceImpl<UserFitnessProfileMapper, UserFitnessProfile>`
- 替换：
  - `userFitnessProfileMapper.insert(profile)` → `this.save(profile)`
  - `userFitnessProfileMapper.updateById(profile)` → `this.updateById(profile)`
  - `userFitnessProfileMapper.selectOne(queryWrapper)` → `this.getOne(queryWrapper)`
- 验证：编译 + 测试

#### 最终验证
- 运行 `mvn compile -DskipTests` 确认编译通过
- 运行 `mvn test` 确认 60/67 通过（7个 BookingServiceTest 预存问题保持不变）

---

## 阶段 2：ChatSession / ChatMessage 命名标准化

### 背景
- 项目规范：`create_time`, `update_time`, `deleted`（见 AGENTS.md）
- `ChatLongTermMemory` 已使用规范命名：`createTime` / `updateTime`
- `ChatSession` 使用：`createdAt` / `updatedAt` / `isDeleted`
- `ChatMessage` 使用：`createdAt`（无 delete/update）
- **关键约束**：DB 列名是 `created_at` / `updated_at` / `is_deleted`，不能改列名

### 实施步骤

#### 步骤 2.1：标准化 `ChatSession`
- 文件：`src/main/java/com/fitness/modules/chat/model/entity/ChatSession.java`
- 操作：
  1. 新增 `extends BaseEntity`
  2. 重命名字段 + 添加 `@TableField` 显式映射到旧列名：
     - `createdAt` → `createTime` + `@TableField("created_at")`
     - `updatedAt` → `updateTime` + `@TableField("updated_at")`
     - `isDeleted` → `deleted` + `@TableField("is_deleted")`
  3. 保留原有 `@TableLogic(delval = "true")`
- 同步修改所有引用 `ChatSession` 的 Java 文件（setter/getter 调用）：
  - `ChatAssistantServiceImpl`：`setCreatedAt` → `setCreateTime`，`setUpdatedAt` → `setUpdateTime`，`setIsDeleted` → `setDeleted`
  - 其他引用处（lambda 排序 `ChatSession::getUpdatedAt` → `ChatSession::getUpdateTime`）
- 验证：编译 + 测试

#### 步骤 2.2：标准化 `ChatMessage`
- 文件：`src/main/java/com/fitness/modules/chat/model/entity/ChatMessage.java`
- 操作：
  1. 重命名字段（ChatMessage 不继承 BaseEntity，因为没有 deleted 字段）：
     - `createdAt` → `createTime` + `@TableField("created_at")`
  2. 同步修改 `ChatAssistantServiceImpl` 中 `ChatMessage.setCreatedAt` 调用
  3. 同步修改 `ChatContextServiceImpl` 中的调用
- 验证：编译 + 测试

#### 最终验证
- 运行 `mvn compile -DskipTests` 确认编译通过
- 运行 `mvn test` 确认无新增失败

---

## 阶段 3：Controller @Valid 与 DTO 校验注解

### 范围
由于涉及面较广（22+ 个 Controller），本阶段聚焦核心业务入口的 create/update 方法。

### 实施步骤

#### 步骤 3.1：给关键 DTO 添加校验注解
| DTO | 添加注解 |
|-----|----------|
| `LoginDTO` | `@NotBlank` on username, password |
| `UserDTO` | `@NotBlank` on username, `@Email` on email |
| `CoachDetailDTO` | `@NotBlank` on realName |
| `BannerDTO` | `@NotBlank` on title, `@NotNull` on sortOrder |
| `AnnouncementDTO` | `@NotBlank` on title, content |
| `BookingDTO` | `@NotNull` on sessionId, userId |
| `ProductDTO` | `@NotBlank` on name, `@NotNull` on price |

#### 步骤 3.2：给关键 Controller 添加 `@Valid`
| Controller | 方法 | 参数 |
|------------|------|------|
| `AuthController` | `login` | `LoginDTO` |
| `UserAdminController` | `createUser` | `UserDTO` |
| `CoachDetailController` | `saveCoachDetail` | `CoachDetailDTO` |
| `BannerController` | create/update | `BannerDTO` |
| `AnnouncementController` | create/update | `AnnouncementDTO` |
| `BookingController` | create | `BookingDTO` |
| `ProductController` | create/update | `ProductDTO` |

#### 最终验证
- 运行 `mvn compile -DskipTests` 确认编译通过
- 运行 `mvn test` 确认无新增失败

---

## 阶段 4：工具类清理

### 实施步骤

#### 步骤 4.1：删除 `DateUtils.java`
- 文件：`src/main/java/com/fitness/common/utils/DateUtils.java`
- 原因：全项目零引用

#### 步骤 4.2：删除 `JsonUtils.java`
- 文件：`src/main/java/com/fitness/common/utils/JsonUtils.java`
- 原因：全项目零引用（项目使用 Jackson `ObjectMapper` 或 Hutool 处理 JSON）

#### 最终验证
- 运行 `mvn compile -DskipTests` 确认编译通过（无 import 引用这些类即可）
- 运行 `mvn test` 确认无新增失败

---

## 执行顺序与验证门禁

```
阶段 1: Service → ServiceImpl 改造
  ├── 编译通过 ✅
  ├── 测试 60/67 ✅
  └── 进入阶段 2

阶段 2: ChatSession / ChatMessage 命名标准化
  ├── 编译通过 ✅
  ├── 测试 60/67 ✅
  └── 进入阶段 3

阶段 3: Controller @Valid 与 DTO 校验注解
  ├── 编译通过 ✅
  ├── 测试 60/67 ✅
  └── 进入阶段 4

阶段 4: 工具类清理
  ├── 编译通过 ✅
  └── 测试 60/67 ✅ → 全部完成
```

每个阶段独立验证，任一阶段失败则暂停后续阶段，修复当前阶段问题后继续。