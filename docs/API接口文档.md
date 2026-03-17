# 智能健身系统 API 接口文档

## 目录

- [1. 概述](#1-概述)
- [2. 通用说明](#2-通用说明)
- [3. 认证模块](#3-认证模块)
- [4. 用户模块](#4-用户模块)
- [5. 权限模块](#5-权限模块)
- [6. 课程模块](#6-课程模块)
- [7. 器材模块](#7-器材模块)
- [8. 健身计划模块](#8-健身计划模块)
- [9. 仪表盘模块](#9-仪表盘模块)
- [10. AI服务模块](#10-ai服务模块)
- [11. 文件服务模块](#11-文件服务模块)
- [12. 错误码说明](#12-错误码说明)

---

## 1. 概述

本文档描述了智能健身系统后端 API 接口规范。系统基于 Spring Boot 3.5.x 开发，采用 RESTful 风格设计 API。

### 基础信息

| 项目 | 说明 |
|------|------|
| 基础路径 | `/api/v1` |
| 接口协议 | HTTP/HTTPS |
| 数据格式 | JSON |
| 字符编码 | UTF-8 |

---

## 2. 通用说明

### 2.1 统一响应格式

所有接口返回统一的响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2024-01-01T12:00:00"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 状态码，200表示成功 |
| message | String | 响应消息 |
| data | Object | 响应数据 |
| timestamp | LocalDateTime | 响应时间戳 |

### 2.2 认证方式

系统采用 JWT Token 认证，需要在请求头中携带 Token：

```
Authorization: Bearer <token>
```

### 2.3 分页参数

分页查询接口统一使用以下参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页大小 |

---

## 3. 认证模块

基础路径: `/api/v1/auth`

### 3.1 用户注册

**接口地址**: `POST /api/v1/auth/register`

**权限要求**: 无

**请求参数**:

```json
{
  "username": "testuser",
  "password": "123456",
  "phone": "13800138000",
  "email": "test@example.com",
  "roleCode": "MEMBER"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名，3-50字符，仅字母数字下划线 |
| password | String | 是 | 密码，6-20字符 |
| phone | String | 否 | 手机号，11位 |
| email | String | 否 | 邮箱地址 |
| roleCode | String | 否 | 角色编码 |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "phone": "13800138000",
    "email": "test@example.com",
    "avatar": null,
    "roles": ["MEMBER"],
    "createTime": "2024-01-01T12:00:00"
  }
}
```

### 3.2 用户登录

**接口地址**: `POST /api/v1/auth/login`

**权限要求**: 无

**请求参数**:

```json
{
  "username": "testuser",
  "password": "123456"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

---

## 4. 用户模块

基础路径: `/api/v1/users`

### 4.1 获取当前用户信息

**接口地址**: `GET /api/v1/users/me`

**权限要求**: 已登录用户

**请求参数**: 无

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "phone": "13800138000",
    "email": "test@example.com",
    "avatar": "http://example.com/avatar.jpg",
    "roles": ["MEMBER"],
    "createTime": "2024-01-01T12:00:00"
  }
}
```

### 4.2 修改密码

**接口地址**: `PUT /api/v1/users/password`

**权限要求**: 已登录用户

**请求参数**:

```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | String | 是 | 旧密码 |
| newPassword | String | 是 | 新密码 |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

## 5. 权限模块

基础路径: `/api/v1`

### 5.1 获取当前用户权限

**接口地址**: `GET /api/v1/permissions/me`

**权限要求**: 已登录用户

**请求参数**: 无

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "permissions": ["course:view", "equipment:view"],
    "roles": ["MEMBER"]
  }
}
```

### 5.2 获取所有角色列表

**接口地址**: `GET /api/v1/roles`

**权限要求**: 管理员 (ADMIN)

**请求参数**: 无

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "code": "ADMIN",
      "name": "系统管理员",
      "description": "拥有系统的所有权限"
    },
    {
      "code": "COACH",
      "name": "教练",
      "description": "可以管理课程、查看会员信息、制定训练计划"
    },
    {
      "code": "MEMBER",
      "name": "会员",
      "description": "可以预约课程、查看个人数据、使用AI健身计划"
    },
    {
      "code": "USER",
      "name": "普通用户",
      "description": "基础权限，可以浏览公开信息"
    }
  ]
}
```

### 5.3 获取当前用户角色信息

**接口地址**: `GET /api/v1/roles/me`

**权限要求**: 已登录用户

**请求参数**: 无

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": 1,
    "roles": ["MEMBER"],
    "isAdmin": false,
    "isCoach": false,
    "isMember": true
  }
}
```

---

## 6. 课程模块

### 6.1 公开接口

基础路径: `/api/v1/courses`

#### 6.1.1 获取课程详情

**接口地址**: `GET /api/v1/courses/{courseId}`

**权限要求**: 无

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| courseId | Long | 是 | 课程ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "courseName": "瑜伽入门课",
    "description": "适合初学者的瑜伽课程",
    "coachId": 2,
    "coachName": "张教练",
    "category": "YOGA",
    "startTime": "2024-01-15T10:00:00",
    "endTime": "2024-01-15T11:00:00",
    "capacity": 20,
    "bookedCount": 15,
    "remainingCount": 5,
    "status": 1,
    "imageUrl": "http://example.com/course.jpg"
  }
}
```

#### 6.1.2 获取公开课程列表

**接口地址**: `GET /api/v1/courses/public/list`

**权限要求**: 无

**请求参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| category | String | 否 | - | 课程分类 |
| startDate | LocalDate | 否 | - | 开始日期 |
| keyword | String | 否 | - | 关键字（课程名称） |
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页大小 |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "courseName": "瑜伽入门课",
        "description": "适合初学者的瑜伽课程",
        "coachId": 2,
        "coachName": "张教练",
        "category": "YOGA",
        "startTime": "2024-01-15T10:00:00",
        "endTime": "2024-01-15T11:00:00",
        "capacity": 20,
        "bookedCount": 15,
        "remainingCount": 5,
        "status": 1,
        "imageUrl": "http://example.com/course.jpg"
      }
    ],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

### 6.2 管理接口

基础路径: `/api/v1/admin/courses`

#### 6.2.1 创建课程

**接口地址**: `POST /api/v1/admin/courses`

**权限要求**: 管理员或教练 (ADMIN/COACH)

**请求参数**:

```json
{
  "courseName": "瑜伽入门课",
  "description": "适合初学者的瑜伽课程",
  "coachId": 2,
  "category": "YOGA",
  "startTime": "2024-01-15T10:00:00",
  "endTime": "2024-01-15T11:00:00",
  "capacity": 20,
  "status": 0,
  "imageUrl": "http://example.com/course.jpg"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| courseName | String | 是 | 课程名称 |
| description | String | 否 | 课程描述 |
| coachId | Long | 是 | 教练ID |
| category | String | 是 | 课程分类 |
| startTime | LocalDateTime | 是 | 开始时间（必须为未来时间） |
| endTime | LocalDateTime | 是 | 结束时间（必须为未来时间） |
| capacity | Integer | 是 | 容量（最大预约人数） |
| status | Integer | 否 | 状态：0-未开始, 1-进行中, 2-已结束, 3-已取消 |
| imageUrl | String | 否 | 课程图片URL |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

#### 6.2.2 更新课程

**接口地址**: `PUT /api/v1/admin/courses/{courseId}`

**权限要求**: 管理员或教练 (ADMIN/COACH)

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| courseId | Long | 是 | 课程ID |

**请求参数**: 同创建课程

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

#### 6.2.3 删除课程

**接口地址**: `DELETE /api/v1/admin/courses/{courseId}`

**权限要求**: 管理员或教练 (ADMIN/COACH)

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| courseId | Long | 是 | 课程ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

## 7. 器材模块

### 7.1 公开接口

基础路径: `/api/v1/equipment`

#### 7.1.1 获取器材详情

**接口地址**: `GET /api/v1/equipment/{id}`

**权限要求**: 无

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 器材ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "equipmentName": "跑步机",
    "location": "A区-01",
    "status": 1,
    "description": "专业跑步机",
    "imageUrl": "http://example.com/equipment.jpg",
    "purchaseDate": "2024-01-01",
    "createTime": "2024-01-01T12:00:00"
  }
}
```

#### 7.1.2 获取器材列表

**接口地址**: `GET /api/v1/equipment/list`

**权限要求**: 无

**请求参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| keyword | String | 否 | - | 关键字（器材名称） |
| status | Integer | 否 | - | 状态：0-维修中, 1-正常, 2-已报废 |
| pageNum | Integer | 否 | 1 | 页码 |
| pageSize | Integer | 否 | 10 | 每页大小 |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "equipmentName": "跑步机",
        "location": "A区-01",
        "status": 1,
        "description": "专业跑步机",
        "imageUrl": "http://example.com/equipment.jpg",
        "purchaseDate": "2024-01-01",
        "createTime": "2024-01-01T12:00:00"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1,
    "pages": 5
  }
}
```

### 7.2 管理接口

基础路径: `/api/v1/admin/equipment`

#### 7.2.1 创建器材

**接口地址**: `POST /api/v1/admin/equipment`

**权限要求**: 管理员 (ADMIN)

**请求参数**:

```json
{
  "equipmentName": "跑步机",
  "location": "A区-01",
  "status": 1,
  "description": "专业跑步机",
  "imageUrl": "http://example.com/equipment.jpg",
  "purchaseDate": "2024-01-01"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| equipmentName | String | 是 | 器材名称 |
| location | String | 是 | 器材位置 |
| status | Integer | 否 | 状态：0-维修中, 1-正常, 2-已报废 |
| description | String | 否 | 器材描述 |
| imageUrl | String | 否 | 器材图片URL |
| purchaseDate | LocalDate | 否 | 购买日期 |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

#### 7.2.2 更新器材

**接口地址**: `PUT /api/v1/admin/equipment/{id}`

**权限要求**: 管理员 (ADMIN)

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 器材ID |

**请求参数**: 同创建器材

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

#### 7.2.3 删除器材

**接口地址**: `DELETE /api/v1/admin/equipment/{id}`

**权限要求**: 管理员 (ADMIN)

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 器材ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

#### 7.2.4 获取所有报修记录

**接口地址**: `GET /api/v1/admin/equipment/repairs`

**权限要求**: 管理员 (ADMIN)

**请求参数**: 无

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "equipmentId": 1,
      "equipmentName": "跑步机",
      "userId": 5,
      "userName": "张三",
      "description": "跑步机无法启动",
      "imageUrl": "http://example.com/repair.jpg",
      "status": 0,
      "createTime": "2024-01-01T12:00:00",
      "handleTime": null
    }
  ]
}
```

#### 7.2.5 处理报修

**接口地址**: `PUT /api/v1/admin/equipment/repairs/{repairId}`

**权限要求**: 管理员 (ADMIN)

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| repairId | Long | 是 | 报修ID |

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 是 | 状态：0-待处理, 1-处理中, 2-已完成, 3-已关闭 |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

### 7.3 报修接口

基础路径: `/api/v1/repairs`

#### 7.3.1 提交报修申请

**接口地址**: `POST /api/v1/repairs`

**权限要求**: 已登录用户

**请求参数**:

```json
{
  "equipmentId": 1,
  "description": "跑步机无法启动",
  "imageUrl": "http://example.com/repair.jpg"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| equipmentId | Long | 是 | 器材ID |
| description | String | 是 | 问题描述 |
| imageUrl | String | 否 | 问题图片URL |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

#### 7.3.2 获取我的报修记录

**接口地址**: `GET /api/v1/repairs/my`

**权限要求**: 已登录用户

**请求参数**: 无

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "equipmentId": 1,
      "equipmentName": "跑步机",
      "description": "跑步机无法启动",
      "imageUrl": "http://example.com/repair.jpg",
      "status": 0,
      "createTime": "2024-01-01T12:00:00",
      "handleTime": null
    }
  ]
}
```

#### 7.3.3 取消报修

**接口地址**: `PUT /api/v1/repairs/{repairId}/cancel`

**权限要求**: 已登录用户

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| repairId | Long | 是 | 报修ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

## 8. 健身计划模块

### 8.1 健身计划接口

基础路径: `/api/v1/plans`

#### 8.1.1 生成健身计划

**接口地址**: `POST /api/v1/plans/generate`

**权限要求**: 会员 (MEMBER)

**请求参数**:

```json
{
  "goal": "增肌",
  "bodyPart": "胸部",
  "experience": "中级"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| goal | String | 是 | 健身目标 |
| bodyPart | String | 是 | 训练部位 |
| experience | String | 是 | 经验水平 |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1
}
```

#### 8.1.2 获取我的计划列表

**接口地址**: `GET /api/v1/plans/my`

**权限要求**: 已登录用户

**请求参数**: 无

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "id": 1,
      "planName": "增肌计划",
      "goal": "增肌",
      "duration": 30,
      "status": 1,
      "createTime": "2024-01-01T12:00:00",
      "details": []
    }
  ]
}
```

#### 8.1.3 获取计划详情

**接口地址**: `GET /api/v1/plans/{planId}`

**权限要求**: 已登录用户

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| planId | Long | 是 | 计划ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "planName": "增肌计划",
    "goal": "增肌",
    "duration": 30,
    "status": 1,
    "createTime": "2024-01-01T12:00:00",
    "details": [
      {
        "id": 1,
        "dayOfWeek": 1,
        "exerciseName": "卧推",
        "sets": 4,
        "reps": 12,
        "duration": 30,
        "notes": "注意呼吸节奏"
      }
    ]
  }
}
```

#### 8.1.4 删除计划

**接口地址**: `DELETE /api/v1/plans/{planId}`

**权限要求**: 已登录用户

**路径参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| planId | Long | 是 | 计划ID |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

### 8.2 个人信息接口

基础路径: `/api/v1/profile`

#### 8.2.1 完善个人信息

**接口地址**: `PUT /api/v1/profile`

**权限要求**: 已登录用户

**请求参数**:

```json
{
  "height": 175.5,
  "weight": 70.0,
  "age": 25,
  "experience": "中级",
  "fitnessGoal": "增肌"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| height | BigDecimal | 是 | 身高(cm)，范围50-250 |
| weight | BigDecimal | 是 | 体重(kg)，范围20-300 |
| age | Integer | 是 | 年龄，范围10-100 |
| experience | String | 是 | 健身经验 |
| fitnessGoal | String | 是 | 健身目标 |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

#### 8.2.2 获取个人信息

**接口地址**: `GET /api/v1/profile`

**权限要求**: 已登录用户

**请求参数**: 无

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "height": 175.5,
    "weight": 70.0,
    "age": 25,
    "experience": "中级",
    "fitnessGoal": "增肌"
  }
}
```

---

## 9. 仪表盘模块

基础路径: `/api/v1/dashboard`

### 9.1 获取仪表盘统计数据

**接口地址**: `GET /api/v1/dashboard/stats`

**权限要求**: 管理员 (ADMIN)

**请求参数**: 无

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalMembers": 1000,
    "totalCourses": 50,
    "totalBookings": 5000,
    "totalEquipment": 100,
    "activeMembers": 800
  }
}
```

### 9.2 获取会员卡销量统计

**接口地址**: `GET /api/v1/dashboard/member-cards`

**权限要求**: 管理员 (ADMIN)

**请求参数**: 无

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "monthCard": 100,
    "quarterCard": 50,
    "yearCard": 20
  }
}
```

### 9.3 获取到店高峰时间

**接口地址**: `GET /api/v1/dashboard/peak-hours`

**权限要求**: 管理员 (ADMIN)

**请求参数**: 无

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "hour": 18,
      "count": 150
    },
    {
      "hour": 19,
      "count": 200
    }
  ]
}
```

### 9.4 获取课程统计

**接口地址**: `GET /api/v1/dashboard/course-stats`

**权限要求**: 管理员 (ADMIN)

**请求参数**: 无

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "categoryName": "瑜伽",
      "courseCount": 10,
      "bookingCount": 500
    }
  ]
}
```

### 9.5 生成AI分析报告

**接口地址**: `POST /api/v1/dashboard/analysis`

**权限要求**: 管理员 (ADMIN)

**请求参数**:

```json
{
  "analysisType": "MEMBER"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| analysisType | String | 是 | 分析类型：MEMBER, COURSE, EQUIPMENT, OVERALL |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "analysisType": "MEMBER",
    "reportTitle": "会员数据分析报告",
    "reportContent": "根据数据分析...",
    "suggestions": [
      "建议增加高峰时段的课程数量",
      "建议优化会员卡定价策略"
    ],
    "generateTime": "2024-01-01T12:00:00"
  }
}
```

---

## 10. AI服务模块

基础路径: `/api/v1/ai`

### 10.1 通用对话接口

**接口地址**: `POST /api/v1/ai/chat`

**权限要求**: 管理员 (ADMIN)

**请求参数**:

```json
{
  "message": "你好，请介绍一下健身的好处"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| message | String | 是 | 消息内容 |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "健身有很多好处，包括..."
}
```

### 10.2 Prompt模板对话接口

**接口地址**: `POST /api/v1/ai/chat/prompt`

**权限要求**: 管理员 (ADMIN)

**请求参数**:

```json
{
  "prompt": "请根据以下信息生成健身计划：目标{goal}，部位{bodyPart}",
  "variables": {
    "goal": "增肌",
    "bodyPart": "胸部"
  }
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| prompt | String | 是 | Prompt模板 |
| variables | Map | 否 | 变量映射 |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "根据您的需求，为您生成以下健身计划..."
}
```

### 10.3 流式对话接口

**接口地址**: `POST /api/v1/ai/chat/stream`

**权限要求**: 管理员 (ADMIN)

**Content-Type**: `text/event-stream`

**请求参数**:

```json
{
  "message": "请详细介绍健身计划"
}
```

**响应**: SSE流式数据

### 10.4 生成健身计划

**接口地址**: `POST /api/v1/ai/fitness-plan`

**权限要求**: 管理员/教练/会员 (ADMIN/COACH/MEMBER)

**请求参数**:

```json
{
  "userId": 1,
  "goal": "增肌",
  "bodyPart": "胸部",
  "experience": "中级",
  "height": 175,
  "weight": 70,
  "age": 25
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 否 | 用户ID |
| goal | String | 是 | 健身目标 |
| bodyPart | String | 否 | 训练部位 |
| experience | String | 是 | 健身经验 |
| height | Integer | 否 | 身高(cm) |
| weight | Integer | 否 | 体重(kg) |
| age | Integer | 否 | 年龄 |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "# 健身计划\n\n## 周一：胸部训练\n..."
}
```

### 10.5 分析健身数据

**接口地址**: `POST /api/v1/ai/analyze`

**权限要求**: 管理员/教练/会员 (ADMIN/COACH/MEMBER)

**请求参数**:

```json
{
  "userId": 1,
  "variables": {
    "totalWorkouts": 30,
    "averageDuration": 45,
    "goals": "增肌"
  }
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 否 | 用户ID |
| variables | Map | 否 | 分析变量 |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "根据您的健身数据分析..."
}
```

### 10.6 获取营养建议

**接口地址**: `POST /api/v1/ai/nutrition`

**权限要求**: 管理员/教练/会员 (ADMIN/COACH/MEMBER)

**请求参数**:

```json
{
  "userId": 1,
  "variables": {
    "goal": "增肌",
    "weight": 70,
    "activityLevel": "高"
  }
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | Long | 否 | 用户ID |
| variables | Map | 否 | 营养变量 |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "根据您的目标，建议每日摄入..."
}
```

### 10.7 获取运动动作指导

**接口地址**: `POST /api/v1/ai/exercise-guide`

**权限要求**: 管理员/教练/会员 (ADMIN/COACH/MEMBER)

**请求参数**:

```json
{
  "exerciseName": "卧推",
  "variables": {
    "experience": "中级"
  }
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| exerciseName | String | 是 | 动作名称 |
| variables | Map | 否 | 额外变量 |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": "卧推动作指导：\n1. 平躺在卧推凳上..."
}
```

---

## 11. 文件服务模块

基础路径: `/api/v1/files`

### 11.1 上传文件

**接口地址**: `POST /api/v1/files/upload`

**权限要求**: 无

**Content-Type**: `multipart/form-data`

**请求参数**:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| file | File | 是 | - | 上传的文件 |
| folder | String | 否 | files | 存储文件夹 |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "fileName": "abc123.jpg",
    "fileUrl": "http://minio.example.com/bucket/files/abc123.jpg",
    "fileType": "image/jpeg",
    "fileSize": 102400
  }
}
```

### 11.2 上传图片

**接口地址**: `POST /api/v1/files/upload/image`

**权限要求**: 无

**Content-Type**: `multipart/form-data`

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 上传的图片文件 |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "fileName": "abc123.jpg",
    "fileUrl": "http://minio.example.com/bucket/images/abc123.jpg",
    "fileType": "image/jpeg",
    "fileSize": 102400
  }
}
```

### 11.3 删除文件

**接口地址**: `DELETE /api/v1/files`

**权限要求**: 无

**请求参数**:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| fileUrl | String | 是 | 文件访问URL |

**响应示例**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

## 12. 错误码说明

### 12.1 通用错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 参数错误 |
| 401 | 未登录或Token已过期 |
| 403 | 没有权限访问 |
| 404 | 资源不存在 |
| 500 | 系统内部错误 |

### 12.2 用户模块错误码

| 错误码 | 说明 |
|--------|------|
| 1001 | 用户不存在 |
| 1002 | 用户已存在 |
| 1003 | 密码错误 |

### 12.3 课程模块错误码

| 错误码 | 说明 |
|--------|------|
| 2001 | 课程不存在 |
| 2002 | 课程已满 |

### 12.4 器材模块错误码

| 错误码 | 说明 |
|--------|------|
| 3001 | 器材不存在 |
| 3002 | 器材正在使用中，无法删除 |
| 3003 | 报修记录不存在 |
| 3004 | 报修已处理，无法取消 |

### 12.5 健身计划模块错误码

| 错误码 | 说明 |
|--------|------|
| 4001 | 健身计划不存在 |
| 4002 | 请先完善个人信息 |
| 4003 | AI生成计划失败 |

### 12.6 文件模块错误码

| 错误码 | 说明 |
|--------|------|
| 5001 | 文件上传失败 |
| 5002 | 文件类型不允许 |
| 5003 | 文件大小超过限制 |
| 5004 | 文件不存在 |

### 12.7 分析模块错误码

| 错误码 | 说明 |
|--------|------|
| 6001 | 数据分析失败 |

---

## 附录

### A. 角色权限说明

| 角色 | 编码 | 说明 |
|------|------|------|
| 系统管理员 | ADMIN | 拥有系统的所有权限 |
| 教练 | COACH | 可以管理课程、查看会员信息、制定训练计划 |
| 会员 | MEMBER | 可以预约课程、查看个人数据、使用AI健身计划 |
| 普通用户 | USER | 基础权限，可以浏览公开信息 |

### B. 课程状态说明

| 状态值 | 说明 |
|--------|------|
| 0 | 未开始 |
| 1 | 进行中 |
| 2 | 已结束 |
| 3 | 已取消 |

### C. 器材状态说明

| 状态值 | 说明 |
|--------|------|
| 0 | 维修中 |
| 1 | 正常 |
| 2 | 已报废 |

### D. 报修状态说明

| 状态值 | 说明 |
|--------|------|
| 0 | 待处理 |
| 1 | 处理中 |
| 2 | 已完成 |
| 3 | 已关闭 |

### E. 健身计划状态说明

| 状态值 | 说明 |
|--------|------|
| 0 | 已停用 |
| 1 | 进行中 |
