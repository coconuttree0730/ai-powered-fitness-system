# AI健身系统 API接口文档

## 目录

1. [概述](#概述)
2. [认证模块](#认证模块)
3. [用户模块](#用户模块)
4. [教练模块](#教练模块)
5. [课程模块](#课程模块)
6. [预约模块](#预约模块)
7. [器材模块](#器材模块)
8. [商品模块](#商品模块)
9. [健身计划模块](#健身计划模块)
10. [AI助手模块](#ai助手模块)
11. [知识库模块](#知识库模块)
12. [仪表盘模块](#仪表盘模块)
13. [内容管理模块](#内容管理模块)
14. [文件管理模块](#文件管理模块)

---

## 概述

### 基础信息

- **基础URL**: `http://localhost:8080`
- **API版本**: v1
- **认证方式**: JWT Token (Bearer Token)

### 通用响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 权限说明

| 角色 | 说明 |
|------|------|
| ADMIN | 系统管理员，拥有所有权限 |
| COACH | 教练，可管理课程、查看会员信息 |
| MEMBER | 会员，可预约课程、使用AI功能 |
| USER | 普通用户，基础浏览权限 |

---

## 认证模块

### 1. 用户注册

- **URL**: `POST /api/v1/auth/register`
- **权限**: 公开
- **描述**: 用户注册新账号

**请求体**:
```json
{
  "username": "string",
  "password": "string",
  "phone": "string",
  "email": "string"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "string",
    "phone": "string",
    "email": "string"
  }
}
```

### 2. 用户登录

- **URL**: `POST /api/v1/auth/login`
- **权限**: 公开
- **描述**: 用户登录获取Token

**请求体**:
```json
{
  "username": "string",
  "password": "string"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "string",
    "refreshToken": "string",
    "expiresIn": 3600
  }
}
```

### 3. 获取滑块验证令牌

- **URL**: `GET /api/v1/auth/slider-verify/token`
- **权限**: 公开
- **描述**: 获取滑块验证所需的令牌

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "string",
    "backgroundImage": "string",
    "sliderImage": "string"
  }
}
```

### 4. 验证滑块结果

- **URL**: `POST /api/v1/auth/slider-verify/verify`
- **权限**: 公开
- **描述**: 验证滑块验证结果

**请求体**:
```json
{
  "token": "string",
  "sliderValue": 100,
  "timestamp": 1234567890
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "verified": true,
    "message": "验证成功"
  }
}
```

### 5. 发送短信验证码

- **URL**: `POST /api/v1/auth/sms-code`
- **权限**: 公开（需先完成滑块验证）
- **描述**: 发送短信验证码

**请求体**:
```json
{
  "phone": "string",
  "verifyToken": "string"
}
```

**响应**:
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

### 6. 短信验证码登录

- **URL**: `POST /api/v1/auth/login/sms`
- **权限**: 公开
- **描述**: 使用短信验证码登录

**请求体**:
```json
{
  "phone": "string",
  "smsCode": "string"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "string",
    "refreshToken": "string",
    "expiresIn": 3600
  }
}
```

---

## 用户模块

### 用户信息管理

#### 1. 获取当前用户信息

- **URL**: `GET /api/v1/users/me`
- **权限**: 已登录用户
- **描述**: 获取当前登录用户的详细信息

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "string",
    "phone": "string",
    "email": "string",
    "avatar": "string",
    "status": 1
  }
}
```

#### 2. 修改密码

- **URL**: `PUT /api/v1/users/password`
- **权限**: 已登录用户
- **描述**: 修改当前用户密码

**请求体**:
```json
{
  "oldPassword": "string",
  "newPassword": "string"
}
```

#### 3. 更新用户名

- **URL**: `PUT /api/v1/users/username`
- **权限**: 已登录用户
- **描述**: 更新当前用户用户名

**请求体**:
```json
{
  "username": "string"
}
```

#### 4. 发送旧手机验证码

- **URL**: `POST /api/v1/users/phone/code/old`
- **权限**: 已登录用户
- **描述**: 发送验证码到当前绑定的手机号

#### 5. 发送新手机验证码

- **URL**: `POST /api/v1/users/phone/code/new`
- **权限**: 已登录用户
- **描述**: 发送验证码到新手机号

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| phone | string | 是 | 新手机号 |

#### 6. 更新手机号

- **URL**: `PUT /api/v1/users/phone`
- **权限**: 已登录用户
- **描述**: 更新当前用户手机号

**请求体**:
```json
{
  "phone": "string",
  "code": "string",
  "oldCode": "string"
}
```

#### 7. 发送邮箱验证码

- **URL**: `POST /api/v1/users/email/code`
- **权限**: 已登录用户
- **描述**: 发送邮箱验证码

**请求体**:
```json
{
  "email": "string"
}
```

#### 8. 更新邮箱

- **URL**: `PUT /api/v1/users/email`
- **权限**: 已登录用户
- **描述**: 更新当前用户邮箱

**请求体**:
```json
{
  "email": "string",
  "code": "string"
}
```

#### 9. 通过短信修改密码

- **URL**: `POST /api/v1/users/password/code`
- **权限**: 已登录用户
- **描述**: 发送验证码用于修改密码

#### 10. 通过短信验证码修改密码

- **URL**: `PUT /api/v1/users/password/by-sms`
- **权限**: 已登录用户
- **描述**: 使用短信验证码修改密码

**请求体**:
```json
{
  "smsCode": "string",
  "newPassword": "string"
}
```

#### 11. 上传头像

- **URL**: `POST /api/v1/users/avatar`
- **权限**: 已登录用户
- **描述**: 上传用户头像

**请求**: multipart/form-data
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | file | 是 | 头像图片文件 |

### 用户管理（管理员）

#### 1. 分页查询用户列表

- **URL**: `GET /api/v1/admin/users`
- **权限**: ADMIN
- **描述**: 分页查询用户列表

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | int | 否 | 页码，默认1 |
| pageSize | int | 否 | 每页数量，默认10 |
| username | string | 否 | 用户名 |
| phone | string | 否 | 手机号 |
| status | int | 否 | 状态 |

#### 2. 创建用户

- **URL**: `POST /api/v1/admin/users`
- **权限**: ADMIN
- **描述**: 管理员创建新用户

**请求体**:
```json
{
  "username": "string",
  "password": "string",
  "phone": "string",
  "email": "string"
}
```

#### 3. 更新用户

- **URL**: `PUT /api/v1/admin/users/{id}`
- **权限**: ADMIN
- **描述**: 更新用户信息

**请求体**:
```json
{
  "username": "string",
  "phone": "string",
  "email": "string",
  "status": 1
}
```

#### 4. 重置用户密码

- **URL**: `PUT /api/v1/admin/users/{id}/password`
- **权限**: ADMIN
- **描述**: 重置用户密码

**请求体**:
```json
{
  "newPassword": "string"
}
```

#### 5. 删除用户

- **URL**: `DELETE /api/v1/admin/users/{id}`
- **权限**: ADMIN
- **描述**: 删除用户

#### 6. 更新用户状态

- **URL**: `PUT /api/v1/admin/users/{id}/status`
- **权限**: ADMIN
- **描述**: 更新用户状态

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | int | 是 | 状态：0-禁用，1-启用 |

### 权限管理

#### 1. 获取当前用户权限

- **URL**: `GET /api/v1/permissions/me`
- **权限**: 已登录用户
- **描述**: 获取当前用户的权限列表

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "permissions": ["string"],
    "roles": ["string"]
  }
}
```

#### 2. 获取所有角色列表

- **URL**: `GET /api/v1/roles`
- **权限**: ADMIN
- **描述**: 获取系统所有角色

#### 3. 获取当前用户角色

- **URL**: `GET /api/v1/roles/me`
- **权限**: 已登录用户
- **描述**: 获取当前用户的角色信息

### 健身档案

#### 1. 获取健身档案

- **URL**: `GET /api/v1/profile`
- **权限**: 已登录用户
- **描述**: 获取当前用户的健身档案

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "height": 175,
    "weight": 70,
    "age": 25,
    "gender": "male",
    "experience": "beginner",
    "fitnessGoal": "lose_weight"
  }
}
```

#### 2. 更新健身档案

- **URL**: `PUT /api/v1/profile`
- **权限**: 已登录用户
- **描述**: 创建或更新健身档案

**请求体**:
```json
{
  "height": 175,
  "weight": 70,
  "age": 25,
  "gender": "male",
  "experience": "beginner",
  "fitnessGoal": "lose_weight"
}
```

#### 3. 检查档案是否完善

- **URL**: `GET /api/v1/profile/complete`
- **权限**: 已登录用户
- **描述**: 检查健身档案是否已完善

---

## 教练模块

### 1. 获取教练列表

- **URL**: `GET /api/v1/coaches/list`
- **权限**: ADMIN, COACH
- **描述**: 获取所有教练列表

### 2. 获取当前教练详情

- **URL**: `GET /api/v1/coaches/detail`
- **权限**: 教练
- **描述**: 获取当前登录教练的详情

### 3. 获取指定教练详情

- **URL**: `GET /api/v1/coaches/{id}/detail`
- **权限**: 公开
- **描述**: 获取指定教练的详情

### 4. 更新教练详情

- **URL**: `PUT /api/v1/coaches/detail`
- **权限**: 教练
- **描述**: 更新教练个人信息

**请求体**:
```json
{
  "specialty": "string",
  "bio": "string",
  "certifications": ["string"]
}
```

### 5. 上传个人展示图片

- **URL**: `POST /api/v1/coaches/detail/image`
- **权限**: 教练
- **描述**: 上传教练个人展示图片

**请求**: multipart/form-data

### 6. 删除个人展示图片

- **URL**: `DELETE /api/v1/coaches/detail/image`
- **权限**: 教练
- **描述**: 删除教练个人展示图片

### 7. 更新教练标签

- **URL**: `PUT /api/v1/coaches/detail/tags`
- **权限**: 教练
- **描述**: 更新教练标签

**请求体**:
```json
["标签1", "标签2"]
```

### 8. 获取首页教练列表

- **URL**: `GET /api/v1/coaches/home`
- **权限**: 公开
- **描述**: 获取首页展示的教练列表

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| limit | int | 否 | 限制数量，默认4 |

### 9. 获取我的专属教练

- **URL**: `GET /api/v1/coaches/my-private-coach`
- **权限**: 已登录用户
- **描述**: 获取当前会员的专属教练

---

## 课程模块

### 课程查询（公开）

#### 1. 获取课程详情

- **URL**: `GET /api/v1/courses/{courseId}`
- **权限**: 公开
- **描述**: 获取课程详细信息

#### 2. 获取课程列表

- **URL**: `GET /api/v1/courses/list`
- **权限**: ADMIN, COACH
- **描述**: 获取课程列表（管理端）

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| category | string | 否 | 课程分类 |
| keyword | string | 否 | 关键词 |
| courseName | string | 否 | 课程名称 |
| coachId | long | 否 | 教练ID |
| startDate | string | 否 | 开始日期 |
| endDate | string | 否 | 结束日期 |

#### 3. 获取公开课程列表

- **URL**: `GET /api/v1/courses/public/list`
- **权限**: 公开
- **描述**: 获取公开课程列表（游客可看）

#### 4. 获取课程分类

- **URL**: `GET /api/v1/courses/categories`
- **权限**: 公开
- **描述**: 获取所有课程分类

#### 5. 获取首页课程体系

- **URL**: `GET /api/v1/courses/homepage/categories`
- **权限**: 公开
- **描述**: 获取首页课程体系数据（按分类分组）

#### 6. 获取首页课程卡片

- **URL**: `GET /api/v1/courses/homepage/cards`
- **权限**: 公开
- **描述**: 获取首页课程卡片列表

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| limit | int | 否 | 限制数量，默认6 |

### 课程管理（管理员/教练）

#### 1. 创建课程

- **URL**: `POST /api/v1/admin/courses`
- **权限**: ADMIN, COACH
- **描述**: 创建新课程

**请求体**:
```json
{
  "courseName": "string",
  "category": "string",
  "description": "string",
  "duration": 60,
  "capacity": 20,
  "coachId": 1
}
```

#### 2. 更新课程

- **URL**: `PUT /api/v1/admin/courses/{courseId}`
- **权限**: ADMIN, COACH
- **描述**: 更新课程信息

#### 3. 删除课程

- **URL**: `DELETE /api/v1/admin/courses/{courseId}`
- **权限**: ADMIN, COACH
- **描述**: 删除课程

---

## 预约模块

### 预约管理（用户端）

#### 1. 创建预约

- **URL**: `POST /api/v1/bookings`
- **权限**: MEMBER
- **描述**: 创建课程预约

**请求体**:
```json
{
  "courseId": 1,
  "bookingDate": "2024-01-01",
  "bookingTime": "10:00"
}
```

#### 2. 取消预约

- **URL**: `PUT /api/v1/bookings/{bookingId}/cancel`
- **权限**: 已登录用户
- **描述**: 取消预约

**请求体**:
```json
{
  "reason": "string"
}
```

#### 3. 获取我的预约列表

- **URL**: `GET /api/v1/bookings/my`
- **权限**: 已登录用户
- **描述**: 获取当前用户的预约列表

#### 4. 获取预约详情

- **URL**: `GET /api/v1/bookings/{bookingId}`
- **权限**: 已登录用户
- **描述**: 获取预约详细信息

### 预约管理（管理端）

#### 1. 分页查询预约列表

- **URL**: `GET /api/v1/admin/bookings`
- **权限**: ADMIN, COACH
- **描述**: 分页查询预约列表

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | long | 否 | 用户ID |
| courseId | long | 否 | 课程ID |
| status | string | 否 | 预约状态 |

#### 2. 确认预约

- **URL**: `PUT /api/v1/admin/bookings/{bookingId}/confirm`
- **权限**: ADMIN, COACH
- **描述**: 确认预约

#### 3. 拒绝预约

- **URL**: `PUT /api/v1/admin/bookings/{bookingId}/reject`
- **权限**: ADMIN, COACH
- **描述**: 拒绝预约

#### 4. 完成预约

- **URL**: `PUT /api/v1/admin/bookings/{bookingId}/complete`
- **权限**: ADMIN, COACH
- **描述**: 标记预约完成

---

## 器材模块

### 器材查询（公开）

#### 1. 获取器材详情

- **URL**: `GET /api/v1/equipment/{id}`
- **权限**: 公开
- **描述**: 获取器材详细信息

#### 2. 获取器材列表

- **URL**: `GET /api/v1/equipment/list`
- **权限**: 公开
- **描述**: 获取器材列表

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | string | 否 | 关键词 |
| status | int | 否 | 状态 |
| pageNum | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |

#### 3. 获取器材报修记录

- **URL**: `GET /api/v1/equipment/{id}/repairs`
- **权限**: 公开
- **描述**: 获取指定器材的报修记录

#### 4. 获取首页器材数据

- **URL**: `GET /api/v1/equipment/homepage`
- **权限**: 公开
- **描述**: 获取首页展示的器材数据

### 器材管理（管理员）

#### 1. 创建器材

- **URL**: `POST /api/v1/admin/equipment`
- **权限**: ADMIN
- **描述**: 创建新器材

**请求体**:
```json
{
  "equipmentName": "string",
  "typeCode": "string",
  "description": "string",
  "status": 1
}
```

#### 2. 更新器材

- **URL**: `PUT /api/v1/admin/equipment/{id}`
- **权限**: ADMIN
- **描述**: 更新器材信息

#### 3. 删除器材

- **URL**: `DELETE /api/v1/admin/equipment/{id}`
- **权限**: ADMIN
- **描述**: 删除器材

#### 4. 获取所有报修记录

- **URL**: `GET /api/v1/admin/equipment/repairs`
- **权限**: ADMIN
- **描述**: 获取所有报修记录

#### 5. 获取报修详情

- **URL**: `GET /api/v1/admin/equipment/repairs/{repairId}`
- **权限**: ADMIN
- **描述**: 获取报修详细信息

#### 6. 处理报修

- **URL**: `PUT /api/v1/admin/equipment/repairs/{repairId}`
- **权限**: ADMIN
- **描述**: 处理报修申请

**请求体**:
```json
{
  "status": "string",
  "remark": "string"
}
```

#### 7. 添加处理记录

- **URL**: `POST /api/v1/admin/equipment/repairs/{repairId}/records`
- **权限**: ADMIN
- **描述**: 添加报修处理记录

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| content | string | 是 | 处理内容 |

#### 8. 获取报修处理记录

- **URL**: `GET /api/v1/admin/equipment/repairs/{repairId}/records`
- **权限**: ADMIN
- **描述**: 获取报修处理记录列表

#### 9. 删除报修记录

- **URL**: `DELETE /api/v1/admin/equipment/repairs/{repairId}`
- **权限**: ADMIN
- **描述**: 删除报修记录

#### 10. 获取所有器材类型

- **URL**: `GET /api/v1/admin/equipment/types`
- **权限**: ADMIN
- **描述**: 获取所有器材类型

### 报修管理（会员端）

#### 1. 提交报修申请

- **URL**: `POST /api/v1/repairs`
- **权限**: 已登录用户
- **描述**: 提交器材报修申请

**请求体**:
```json
{
  "equipmentId": 1,
  "description": "string",
  "images": ["string"]
}
```

#### 2. 获取我的报修记录

- **URL**: `GET /api/v1/repairs/my`
- **权限**: 已登录用户
- **描述**: 获取当前用户的报修记录

#### 3. 获取报修详情

- **URL**: `GET /api/v1/repairs/{repairId}`
- **权限**: 已登录用户
- **描述**: 获取报修详细信息

#### 4. 取消报修

- **URL**: `PUT /api/v1/repairs/{repairId}/cancel`
- **权限**: 已登录用户
- **描述**: 取消报修申请

---

## 商品模块

### 商品查询（公开）

#### 1. 获取商品列表

- **URL**: `GET /api/v1/products`
- **权限**: 公开
- **描述**: 获取商品列表

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| category | string | 否 | 商品分类 |

#### 2. 获取商品详情

- **URL**: `GET /api/v1/products/{id}`
- **权限**: 公开
- **描述**: 获取商品详细信息

### 商品管理（管理员）

#### 1. 获取商品列表（管理端）

- **URL**: `GET /api/v1/admin/products`
- **权限**: ADMIN
- **描述**: 获取所有商品列表

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| category | string | 否 | 分类 |
| status | string | 否 | 状态 |
| keyword | string | 否 | 关键词 |

#### 2. 创建商品

- **URL**: `POST /api/v1/admin/products`
- **权限**: ADMIN
- **描述**: 创建新商品

**请求体**:
```json
{
  "name": "string",
  "category": "string",
  "price": 100.00,
  "stock": 100,
  "description": "string"
}
```

#### 3. 更新商品

- **URL**: `PUT /api/v1/admin/products/{id}`
- **权限**: ADMIN
- **描述**: 更新商品信息

#### 4. 删除商品

- **URL**: `DELETE /api/v1/admin/products/{id}`
- **权限**: ADMIN
- **描述**: 删除商品

#### 5. 更新商品状态

- **URL**: `PUT /api/v1/admin/products/{id}/status`
- **权限**: ADMIN
- **描述**: 更新商品状态

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | string | 是 | 状态 |

#### 6. 更新商品库存

- **URL**: `PUT /api/v1/admin/products/{id}/stock`
- **权限**: ADMIN
- **描述**: 更新商品库存

**请求体**:
```json
{
  "type": "add",
  "quantity": 10
}
```

### 商品订单

#### 1. 计算价格

- **URL**: `POST /api/v1/product-orders/calculate`
- **权限**: 已登录用户
- **描述**: 计算订单价格

**请求体**:
```json
{
  "productId": 1,
  "quantity": 1
}
```

#### 2. 创建订单

- **URL**: `POST /api/v1/product-orders`
- **权限**: 已登录用户
- **描述**: 创建商品订单

**请求体**:
```json
{
  "productId": 1,
  "quantity": 1,
  "address": "string"
}
```

#### 3. 获取订单列表

- **URL**: `GET /api/v1/product-orders`
- **权限**: 已登录用户
- **描述**: 获取当前用户的订单列表

#### 4. 获取订单详情

- **URL**: `GET /api/v1/product-orders/{orderNo}`
- **权限**: 已登录用户
- **描述**: 获取订单详细信息

#### 5. 支付订单

- **URL**: `POST /api/v1/product-orders/{orderNo}/pay`
- **权限**: 已登录用户
- **描述**: 支付订单

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| payMethod | string | 是 | 支付方式 |

#### 6. 取消订单

- **URL**: `POST /api/v1/product-orders/{orderNo}/cancel`
- **权限**: 已登录用户
- **描述**: 取消订单

---

## 健身计划模块

### 1. 从个人档案生成计划

- **URL**: `POST /api/v1/plans/generate-from-profile`
- **权限**: MEMBER
- **描述**: 从个人档案自动生成健身计划

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "planName": "string",
    "weeklyPlan": {}
  }
}
```

### 2. 手动生成计划

- **URL**: `POST /api/v1/plans/generate`
- **权限**: MEMBER
- **描述**: 手动选择参数生成健身计划

**请求体**:
```json
{
  "goal": "lose_weight",
  "bodyPart": "full_body",
  "experience": "beginner"
}
```

### 3. 获取我的计划列表

- **URL**: `GET /api/v1/plans/my`
- **权限**: 已登录用户
- **描述**: 获取当前用户的健身计划列表

### 4. 获取计划详情

- **URL**: `GET /api/v1/plans/{planId}`
- **权限**: 已登录用户
- **描述**: 获取健身计划详细信息

### 5. 删除计划

- **URL**: `DELETE /api/v1/plans/{planId}`
- **权限**: 已登录用户
- **描述**: 删除健身计划

---

## AI助手模块

### 聊天会话

#### 1. 创建会话

- **URL**: `POST /api/v1/chat/sessions`
- **权限**: MEMBER
- **描述**: 创建新的聊天会话

#### 2. 发送消息

- **URL**: `POST /api/v1/chat/messages`
- **权限**: MEMBER
- **描述**: 发送聊天消息

**请求体**:
```json
{
  "sessionId": 1,
  "content": "string"
}
```

#### 3. 流式发送消息

- **URL**: `POST /api/v1/chat/messages/stream`
- **权限**: MEMBER
- **描述**: 流式发送聊天消息（SSE）

**请求体**:
```json
{
  "sessionId": 1,
  "content": "string"
}
```

#### 4. 获取会话列表

- **URL**: `GET /api/v1/chat/sessions`
- **权限**: MEMBER
- **描述**: 获取当前用户的会话列表

#### 5. 获取会话详情

- **URL**: `GET /api/v1/chat/sessions/{sessionId}`
- **权限**: MEMBER
- **描述**: 获取会话详细信息

#### 6. 删除会话

- **URL**: `DELETE /api/v1/chat/sessions/{sessionId}`
- **权限**: MEMBER
- **描述**: 删除聊天会话

#### 7. 获取会话消息

- **URL**: `GET /api/v1/chat/sessions/{sessionId}/messages`
- **权限**: MEMBER
- **描述**: 获取会话的消息列表

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| lastMessageId | long | 否 | 最后消息ID（用于分页） |
| limit | int | 否 | 限制数量，默认10 |

### AI健身计划

#### 1. 生成健身计划卡片

- **URL**: `POST /api/v1/chat/fitness-plan/generate`
- **权限**: MEMBER
- **描述**: 生成健身计划卡片

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| goal | string | 是 | 健身目标 |
| bodyPart | string | 是 | 训练部位 |
| experience | string | 是 | 经验水平 |

#### 2. 保存健身计划

- **URL**: `POST /api/v1/chat/fitness-plan/save`
- **权限**: MEMBER
- **描述**: 保存健身计划

**请求体**:
```json
{
  "planName": "string",
  "weeklyPlan": {}
}
```

#### 3. 获取我的健身计划

- **URL**: `GET /api/v1/chat/fitness-plan/my`
- **权限**: MEMBER
- **描述**: 获取当前用户的健身计划列表

### AI通用接口

#### 1. 通用对话

- **URL**: `POST /api/v1/ai/chat`
- **权限**: 已登录用户
- **描述**: AI通用对话接口

**请求体**:
```json
{
  "message": "string"
}
```

#### 2. Prompt模板对话

- **URL**: `POST /api/v1/ai/chat/prompt`
- **权限**: 已登录用户
- **描述**: 使用Prompt模板对话

**请求体**:
```json
{
  "prompt": "string",
  "variables": {}
}
```

#### 3. 流式对话

- **URL**: `POST /api/v1/ai/chat/stream`
- **权限**: 已登录用户
- **描述**: AI流式对话接口（SSE）

**请求体**:
```json
{
  "message": "string"
}
```

#### 4. 生成健身计划

- **URL**: `POST /api/v1/ai/fitness-plan`
- **权限**: ADMIN, COACH, MEMBER
- **描述**: AI生成健身计划

**请求体**:
```json
{
  "goal": "string",
  "bodyPart": "string",
  "experience": "string",
  "height": 175,
  "weight": 70,
  "age": 25
}
```

#### 5. 分析健身数据

- **URL**: `POST /api/v1/ai/analyze`
- **权限**: ADMIN, COACH, MEMBER
- **描述**: AI分析健身数据

**请求体**:
```json
{
  "variables": {}
}
```

#### 6. 获取营养建议

- **URL**: `POST /api/v1/ai/nutrition`
- **权限**: ADMIN, COACH, MEMBER
- **描述**: AI获取营养建议

**请求体**:
```json
{
  "variables": {}
}
```

#### 7. 获取运动动作指导

- **URL**: `POST /api/v1/ai/exercise-guide`
- **权限**: ADMIN, COACH, MEMBER
- **描述**: AI获取运动动作指导

**请求体**:
```json
{
  "exerciseName": "string",
  "variables": {}
}
```

#### 8. 文本润色

- **URL**: `POST /api/v1/ai/polish`
- **权限**: 已登录用户
- **描述**: AI文本润色

**请求体**:
```json
{
  "text": "string"
}
```

---

## 知识库模块

### 知识分类

#### 1. 获取分类列表

- **URL**: `GET /api/v1/knowledge/categories`
- **权限**: ADMIN
- **描述**: 获取所有知识分类

#### 2. 获取分类详情

- **URL**: `GET /api/v1/knowledge/categories/{id}`
- **权限**: ADMIN
- **描述**: 获取分类详细信息

#### 3. 创建分类

- **URL**: `POST /api/v1/knowledge/categories`
- **权限**: ADMIN
- **描述**: 创建知识分类

**请求体**:
```json
{
  "name": "string",
  "description": "string"
}
```

#### 4. 更新分类

- **URL**: `PUT /api/v1/knowledge/categories/{id}`
- **权限**: ADMIN
- **描述**: 更新分类信息

#### 5. 删除分类

- **URL**: `DELETE /api/v1/knowledge/categories/{id}`
- **权限**: ADMIN
- **描述**: 删除知识分类

### 知识文档

#### 1. 分页查询文档

- **URL**: `GET /api/v1/knowledge/documents`
- **权限**: ADMIN
- **描述**: 分页查询知识文档

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | int | 否 | 页码 |
| pageSize | int | 否 | 每页数量 |
| categoryId | long | 否 | 分类ID |
| status | string | 否 | 状态 |

#### 2. 获取文档详情

- **URL**: `GET /api/v1/knowledge/documents/{id}`
- **权限**: ADMIN
- **描述**: 获取文档详细信息

#### 3. 创建文档

- **URL**: `POST /api/v1/knowledge/documents`
- **权限**: ADMIN
- **描述**: 创建知识文档

**请求体**:
```json
{
  "title": "string",
  "content": "string",
  "categoryId": 1
}
```

#### 4. 上传文档

- **URL**: `POST /api/v1/knowledge/documents/upload`
- **权限**: ADMIN
- **描述**: 上传知识文档

**请求**: multipart/form-data
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | file | 是 | 文档文件 |
| title | string | 否 | 文档标题 |

#### 5. 更新文档

- **URL**: `PUT /api/v1/knowledge/documents/{id}`
- **权限**: ADMIN
- **描述**: 更新文档信息

#### 6. 删除文档

- **URL**: `DELETE /api/v1/knowledge/documents/{id}`
- **权限**: ADMIN
- **描述**: 删除知识文档

#### 7. 发布文档

- **URL**: `PATCH /api/v1/knowledge/documents/{id}/publish`
- **权限**: ADMIN
- **描述**: 发布知识文档

#### 8. 归档文档

- **URL**: `PATCH /api/v1/knowledge/documents/{id}/archive`
- **权限**: ADMIN
- **描述**: 归档知识文档

#### 9. 重建索引

- **URL**: `POST /api/v1/knowledge/documents/{id}/reindex`
- **权限**: ADMIN
- **描述**: 重建文档向量索引

### RAG检索

#### 1. 知识检索

- **URL**: `POST /api/v1/knowledge/rag/search`
- **权限**: 公开
- **描述**: RAG知识检索

**请求体**:
```json
{
  "query": "string",
  "categoryId": 1,
  "topK": 5
}
```

#### 2. RAG对话

- **URL**: `POST /api/v1/knowledge/rag/chat`
- **权限**: 公开
- **描述**: 基于RAG的对话

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| query | string | 是 | 查询内容 |
| categoryId | long | 否 | 分类ID |

---

## 仪表盘模块

### 1. 获取统计数据

- **URL**: `GET /api/v1/dashboard/stats`
- **权限**: ADMIN
- **描述**: 获取仪表盘统计数据

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalUsers": 1000,
    "totalCourses": 50,
    "totalBookings": 500,
    "totalRevenue": 100000
  }
}
```

### 2. 获取会员卡销量统计

- **URL**: `GET /api/v1/dashboard/member-cards`
- **权限**: ADMIN
- **描述**: 获取会员卡销量统计

### 3. 获取到店高峰时间

- **URL**: `GET /api/v1/dashboard/peak-hours`
- **权限**: ADMIN
- **描述**: 获取到店高峰时间统计

### 4. 获取课程统计

- **URL**: `GET /api/v1/dashboard/course-stats`
- **权限**: ADMIN
- **描述**: 获取课程统计数据

### 5. 生成AI分析报告

- **URL**: `POST /api/v1/dashboard/analysis`
- **权限**: ADMIN
- **描述**: 生成AI分析报告

**请求体**:
```json
{
  "analysisType": "member"
}
```

---

## 内容管理模块

### 轮播图管理

#### 1. 获取显示的轮播图

- **URL**: `GET /api/v1/banners/active`
- **权限**: 公开
- **描述**: 获取所有显示的轮播图

#### 2. 获取所有轮播图

- **URL**: `GET /api/v1/banners`
- **权限**: ADMIN
- **描述**: 获取所有轮播图（管理端）

#### 3. 获取轮播图详情

- **URL**: `GET /api/v1/banners/{id}`
- **权限**: ADMIN
- **描述**: 获取轮播图详细信息

#### 4. 创建轮播图

- **URL**: `POST /api/v1/banners`
- **权限**: ADMIN
- **描述**: 创建轮播图

**请求体**:
```json
{
  "title": "string",
  "imageUrl": "string",
  "linkUrl": "string",
  "sort": 1,
  "status": 1
}
```

#### 5. 更新轮播图

- **URL**: `PUT /api/v1/banners/{id}`
- **权限**: ADMIN
- **描述**: 更新轮播图

#### 6. 删除轮播图

- **URL**: `DELETE /api/v1/banners/{id}`
- **权限**: ADMIN
- **描述**: 删除轮播图

#### 7. 批量删除轮播图

- **URL**: `DELETE /api/v1/banners`
- **权限**: ADMIN
- **描述**: 批量删除轮播图

**请求体**:
```json
[1, 2, 3]
```

#### 8. 更新轮播图状态

- **URL**: `PATCH /api/v1/banners/{id}/status`
- **权限**: ADMIN
- **描述**: 更新轮播图状态

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | int | 是 | 状态：0-隐藏，1-显示 |

### 公告管理

#### 1. 获取已发布公告

- **URL**: `GET /api/v1/announcements/published`
- **权限**: 公开
- **描述**: 获取所有已发布的公告

#### 2. 获取所有公告

- **URL**: `GET /api/v1/announcements`
- **权限**: ADMIN
- **描述**: 获取所有公告（管理端）

#### 3. 获取公告详情

- **URL**: `GET /api/v1/announcements/{id}`
- **权限**: 公开
- **描述**: 获取公告详细信息

#### 4. 创建公告

- **URL**: `POST /api/v1/announcements`
- **权限**: ADMIN
- **描述**: 创建公告

**请求体**:
```json
{
  "title": "string",
  "content": "string",
  "type": "notice"
}
```

#### 5. 更新公告

- **URL**: `PUT /api/v1/announcements/{id}`
- **权限**: ADMIN
- **描述**: 更新公告

#### 6. 删除公告

- **URL**: `DELETE /api/v1/announcements/{id}`
- **权限**: ADMIN
- **描述**: 删除公告

#### 7. 批量删除公告

- **URL**: `DELETE /api/v1/announcements`
- **权限**: ADMIN
- **描述**: 批量删除公告

**请求体**:
```json
[1, 2, 3]
```

#### 8. 发布公告

- **URL**: `PATCH /api/v1/announcements/{id}/publish`
- **权限**: ADMIN
- **描述**: 发布公告

#### 9. 下架公告

- **URL**: `PATCH /api/v1/announcements/{id}/unpublish`
- **权限**: ADMIN
- **描述**: 下架公告

#### 10. 增加浏览量

- **URL**: `PATCH /api/v1/announcements/{id}/view`
- **权限**: 公开
- **描述**: 增加公告浏览量

---

## 文件管理模块

### 1. 上传文件

- **URL**: `POST /api/v1/files/upload`
- **权限**: 已登录用户
- **描述**: 上传文件

**请求**: multipart/form-data
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | file | 是 | 文件 |
| folder | string | 否 | 存储文件夹，默认files |

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "fileUrl": "string",
    "fileName": "string",
    "fileSize": 1024
  }
}
```

### 2. 上传图片

- **URL**: `POST /api/v1/files/upload/image`
- **权限**: 已登录用户
- **描述**: 上传图片

**请求**: multipart/form-data
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | file | 是 | 图片文件 |

### 3. 删除文件

- **URL**: `DELETE /api/v1/files`
- **权限**: 已登录用户
- **描述**: 删除文件

**参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileUrl | string | 是 | 文件URL |

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权，请先登录 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 429 | 请求过于频繁 |
| 500 | 服务器内部错误 |

---

## 附录

### 健身目标枚举

| 值 | 说明 |
|----|------|
| lose_weight | 减脂 |
| build_muscle | 增肌 |
| keep_fit | 塑形 |
| improve_endurance | 提高耐力 |

### 训练部位枚举

| 值 | 说明 |
|----|------|
| full_body | 全身 |
| upper_body | 上肢 |
| lower_body | 下肢 |
| core | 核心 |
| chest | 胸部 |
| back | 背部 |
| legs | 腿部 |
| arms | 手臂 |
| shoulders | 肩部 |

### 经验水平枚举

| 值 | 说明 |
|----|------|
| beginner | 初学者 |
| intermediate | 中级 |
| advanced | 高级 |

### 课程分类枚举

| 值 | 说明 |
|----|------|
| YOGA | 瑜伽 |
| PILATES | 普拉提 |
| STRENGTH | 力量训练 |
| CARDIO | 有氧运动 |
| DANCE | 舞蹈 |
| BOXING | 搏击 |
