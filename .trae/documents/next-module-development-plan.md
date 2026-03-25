# AI-Powered Fitness System - 下一步开发计划

> **计划模式文档**: 本计划基于对现有代码库的全面分析，确定下一步应优先实现的功能模块。

***

## 一、项目现状分析

### 1.1 已实现的模块

| 模块           | 完成度     | 说明                  |
| ------------ | ------- | ------------------- |
| **用户管理**     | ✅ 已完成   | 用户注册、登录、滑块验证、角色权限管理 |
| **课程管理**     | ⚠️ 部分完成 | 课程CRUD、分类管理，但缺少预约功能 |
| **AI健身计划**   | ✅ 已完成   | AI生成健身计划、计划详情、用户画像  |
| **器材管理**     | ✅ 已完成   | 器材CRUD、器材类型、报修管理    |
| **仪表盘/数据分析** | ✅ 已完成   | 运营数据统计、AI智能分析报告     |
| **文件管理**     | ✅ 已完成   | MinIO文件上传、存储管理      |

### 1.2 数据库表结构现状

根据 `V1__init_schema.sql` 分析：

* ✅ `sys_user` - 用户表

* ✅ `sys_role` - 角色表

* ✅ `sys_user_role` - 用户角色关联表

* ✅ `sys_permission` - 权限表

* ✅ `sys_role_permission` - 角色权限关联表

* ✅ `sys_file` - 文件记录表

* ✅ `fitness_course` - 课程表

* ⚠️ `fitness_booking` - **预约表（仅DDL，无业务实现）**

* ✅ `fitness_equipment` - 器材表

* ✅ `fitness_equipment_repair` - 器材报修表

* ✅ `fitness_plan` - 健身计划表

* ✅ `fitness_plan_detail` - 健身计划详情表

***

## 二、下一步开发建议

### 🎯 推荐方案：课程预约模块 (Course Booking Module)

**推荐理由：**

1. **数据库已就绪** - `fitness_booking` 表结构已定义，包含完整字段
2. **业务逻辑清晰** - 预约是健身房核心业务流程
3. **与现有模块关联强** - 依赖课程模块、用户模块，可复用现有代码
4. **MVP关键功能** - 预约是MVP版本的核心功能之一

***

## 三、课程预约模块详细设计

### 3.1 功能需求

#### 3.1.1 用户端功能

| 功能     | 描述         | 接口权限   |
| ------ | ---------- | ------ |
| 预约课程   | 用户选择课程进行预约 | MEMBER |
| 取消预约   | 用户取消已预约的课程 | MEMBER |
| 我的预约列表 | 查看个人所有预约记录 | MEMBER |
| 预约详情   | 查看单个预约详情   | MEMBER |

#### 3.1.2 管理端功能

| 功能     | 描述         | 接口权限        |
| ------ | ---------- | ----------- |
| 查询所有预约 | 分页查询所有预约记录 | ADMIN/COACH |
| 确认预约   | 管理员确认用户预约  | ADMIN/COACH |
| 拒绝预约   | 管理员拒绝用户预约  | ADMIN/COACH |
| 课程预约统计 | 统计课程的预约情况  | ADMIN/COACH |

### 3.2 业务流程

```
用户预约流程:
1. 用户浏览课程列表 → 选择课程
2. 检查课程容量（booked_count < capacity）
3. 检查用户是否已预约该课程
4. 创建预约记录（status = 0-待确认）
5. 更新课程已预约数（booked_count + 1）
6. 预约成功

用户取消流程:
1. 用户查看我的预约
2. 选择要取消的预约
3. 更新预约状态（status = 2-已取消）
4. 更新课程已预约数（booked_count - 1）
5. 取消成功

管理员确认流程:
1. 管理员查看预约列表
2. 选择待确认的预约
3. 更新预约状态（status = 1-已确认）
4. 确认成功
```

### 3.3 数据库表结构

已存在 `fitness_booking` 表：

```sql
CREATE TABLE fitness_booking (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,           -- 用户ID
    course_id BIGINT NOT NULL,         -- 课程ID
    booking_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, -- 预约时间
    status SMALLINT DEFAULT 0,         -- 状态：0-待确认，1-已确认，2-已取消，3-已完成
    cancel_reason VARCHAR(255),        -- 取消原因
    deleted BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
```

### 3.4 API设计

#### 用户端接口

```
POST   /api/v1/bookings              # 创建预约
GET    /api/v1/bookings/my           # 获取我的预约列表
GET    /api/v1/bookings/{id}         # 获取预约详情
PUT    /api/v1/bookings/{id}/cancel  # 取消预约
```

#### 管理端接口

```
GET    /api/v1/admin/bookings        # 查询所有预约（分页）
PUT    /api/v1/admin/bookings/{id}/confirm  # 确认预约
PUT    /api/v1/admin/bookings/{id}/reject   # 拒绝预约
GET    /api/v1/admin/bookings/course/{courseId}/stats  # 课程预约统计
```

***

## 四、实现任务清单

### Task 1: 创建预约实体类 (Entity)

**文件**: `src/main/java/com/fitness/modules/booking/model/entity/Booking.java`

**说明**: 对应 `fitness_booking` 表的实体类

***

### Task 2: 创建DTO和VO类

**文件**:

* `src/main/java/com/fitness/modules/booking/model/dto/BookingDTO.java` - 创建预约请求

* `src/main/java/com/fitness/modules/booking/model/dto/BookingQueryDTO.java` - 预约查询条件

* `src/main/java/com/fitness/modules/booking/model/dto/BookingCancelDTO.java` - 取消预约请求

* `src/main/java/com/fitness/modules/booking/model/vo/BookingVO.java` - 预约详情VO

* `src/main/java/com/fitness/modules/booking/model/vo/BookingListVO.java` - 预约列表VO

***

### Task 3: 创建Mapper接口

**文件**: `src/main/java/com/fitness/modules/booking/mapper/BookingMapper.java`

**方法**:

* 基础CRUD（继承BaseMapper）

* 自定义查询：根据用户ID查询预约列表

* 自定义查询：根据课程ID查询预约列表

* 自定义查询：检查用户是否已预约课程

***

### Task 4: 创建Service接口和实现

**文件**:

* `src/main/java/com/fitness/modules/booking/service/BookingService.java`

* `src/main/java/com/fitness/modules/booking/service/impl/BookingServiceImpl.java`

**方法**:

* `createBooking(Long userId, BookingDTO dto)` - 创建预约

* `cancelBooking(Long userId, Long bookingId, BookingCancelDTO dto)` - 取消预约

* `getMyBookings(Long userId)` - 获取我的预约列表

* `getBookingDetail(Long userId, Long bookingId)` - 获取预约详情

* `getBookingList(BookingQueryDTO query)` - 管理端查询预约列表

* `confirmBooking(Long bookingId)` - 确认预约

* `rejectBooking(Long bookingId)` - 拒绝预约

***

### Task 5: 创建Controller

**文件**:

* `src/main/java/com/fitness/modules/booking/controller/BookingController.java` - 用户端接口

* `src/main/java/com/fitness/modules/booking/controller/BookingAdminController.java` - 管理端接口

***

### Task 6: 添加错误码

**文件**: `src/main/java/com/fitness/common/constants/ErrorCode.java`

**新增错误码**:

* `BOOKING_NOT_FOUND` - 预约记录不存在

* `BOOKING_ALREADY_EXISTS` - 已预约该课程

* `COURSE_FULL` - 课程已满员

* `BOOKING_CANNOT_CANCEL` - 预约无法取消（课程已开始或已取消）

***

### Task 7: 编写单元测试

**文件**: `src/test/java/com/fitness/modules/booking/service/BookingServiceTest.java`

**测试用例**:

* 正常预约课程

* 重复预约（应失败）

* 课程已满预约（应失败）

* 正常取消预约

* 取消不存在的预约（应失败）

* 取消已取消的预约（应失败）

***

## 五、备选方案

如果课程预约模块不符合当前需求，以下是其他可选模块：

### 备选1: 会员卡/会员套餐模块

* **功能**: 会员卡类型管理、会员套餐购买、会员有效期管理

* **复杂度**: 中

* **优先级**: 高（涉及核心业务）

### 备选2: 通知消息模块

* **功能**: 系统通知、预约提醒、站内消息

* **复杂度**: 低

* **优先级**: 中

### 备选3: 健身记录/打卡模块

* **功能**: 用户健身打卡、运动记录、消耗卡路里统计

* **复杂度**: 中

* **优先级**: 中

### 备选4: 支付模块

* **功能**: 课程预约支付、会员卡购买支付

* **复杂度**: 高

* **优先级**: 低（MVP可后期添加）

***

## 六、技术实现要点

### 6.1 并发控制

预约操作涉及库存扣减（课程容量），需要考虑并发问题：

方案：Redis  + lua 脚本实现 并发问题！


### 6.2 事务管理

预约操作需要保证原子性：

```java
@Transactional(rollbackFor = Exception.class)
public Long createBooking(Long userId, BookingDTO dto) {
    // 1. 检查课程是否存在且可预约
    // 2. 检查是否已预约
    // 3. 扣减课程容量
    // 4. 创建预约记录
}
```

### 6.3 状态流转

预约状态流转图：

```
待确认(0) → 已确认(1) → 已完成(3)
    ↓
已取消(2)
```

***

## 七、总结

**推荐立即开始**: 课程预约模块

**理由**:

1. 数据库表已就绪，无需额外DDL
2. 业务逻辑清晰，与现有模块耦合度适中
3. 是健身房系统的核心功能
4. 实现难度适中，可在1-2个迭代周期内完成

**预期产出**:

* 完整的预约CRUD功能

* 用户端和管理端分离的API设计

* 完善的单元测试覆盖

* 符合项目编码规范的代码实现



