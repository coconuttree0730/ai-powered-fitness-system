# 会员卡管理模块配置说明

## 📋 模块概述

会员卡管理模块实现了以下功能：
- **管理端**：会员卡类型管理、会员卡内容管理（价格、描述、权益等）
- **会员端**：浏览会员卡、在线购卡、支付宝沙箱支付
- **支付流程**：浏览 → 选择 → 下单 → 支付 → 激活会员

## 🗄️ 数据库表

模块创建了以下表：
- `membership_card_type` - 会员卡类型表
- `membership_card` - 会员卡定义表
- `membership_card_content` - 会员卡内容项表（动态多条）
- `membership_order` - 会员卡订单表
- `user_membership` - 用户会员信息表

## ⚙️ 支付宝沙箱配置

### 1. 申请支付宝沙箱账号

1. 访问 [支付宝开放平台](https://open.alipay.com/)
2. 登录后进入"开发者中心" → "沙箱环境"
3. 获取以下信息：
   - **APPID**
   - **支付宝公钥**
   - **应用私钥**

### 2. 配置 application.yml

在 `application.yml` 中配置以下参数（或使用环境变量）：

```yaml
alipay:
  # 应用ID（从沙箱环境获取）
  app-id: ${ALIPAY_APP_ID:your_app_id_here}
  
  # 应用私钥（从沙箱环境获取）
  private-key: ${ALIPAY_PRIVATE_KEY:your_private_key_here}
  
  # 支付宝公钥（从沙箱环境获取）
  alipay-public-key: ${ALIPAY_PUBLIC_KEY:your_alipay_public_key_here}
  
  # 支付宝网关（沙箱环境固定地址）
  server-url: ${ALIPAY_SERVER_URL:https://openapi-sandbox.dl.alipaydev.com/gateway.do}
  
  # 异步通知回调地址（需要外网可访问）
  notify-url: ${ALIPAY_NOTIFY_URL:http://your-domain.com/api/v1/payment/alipay/notify}
  
  # 同步跳转地址（支付成功后跳转的页面）
  return-url: ${ALIPAY_RETURN_URL:http://localhost:5173/membership/pay/success}
```

### 3. 使用环境变量（推荐）

在 Windows PowerShell 中设置：
```powershell
$env:ALIPAY_APP_ID="你的APPID"
$env:ALIPAY_PRIVATE_KEY="你的应用私钥"
$env:ALIPAY_PUBLIC_KEY="你的支付宝公钥"
$env:ALIPAY_NOTIFY_URL="http://你的域名/api/v1/payment/alipay/notify"
```

在 Linux/Mac 中设置：
```bash
export ALIPAY_APP_ID="你的APPID"
export ALIPAY_PRIVATE_KEY="你的应用私钥"
export ALIPAY_PUBLIC_KEY="你的支付宝公钥"
export ALIPAY_NOTIFY_URL="http://你的域名/api/v1/payment/alipay/notify"
```

### 4. 沙箱测试账号

在支付宝沙箱环境中，使用提供的测试账号进行支付测试：
- 沙箱买家账号
- 沙箱支付密码

## 🔌 API 接口列表

### 管理端接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/admin/membership/types` | POST | 创建会员卡类型 |
| `/api/v1/admin/membership/types/{id}` | PUT | 更新会员卡类型 |
| `/api/v1/admin/membership/types/{id}` | DELETE | 删除会员卡类型 |
| `/api/v1/admin/membership/types` | GET | 获取所有会员卡类型 |
| `/api/v1/admin/membership/cards` | POST | 创建会员卡 |
| `/api/v1/admin/membership/cards/{id}` | PUT | 更新会员卡 |
| `/api/v1/admin/membership/cards/{id}` | DELETE | 删除会员卡 |
| `/api/v1/admin/membership/cards` | GET | 获取所有会员卡 |
| `/api/v1/admin/membership/cards/active` | GET | 获取上架的会员卡 |

### 会员端接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/membership/cards` | GET | 获取会员卡列表 |
| `/api/v1/membership/cards/recommend` | GET | 获取推荐会员卡 |
| `/api/v1/membership/cards/{id}` | GET | 获取会员卡详情 |
| `/api/v1/membership/orders` | POST | 创建订单 |
| `/api/v1/membership/orders/pay` | POST | 支付订单 |
| `/api/v1/membership/orders/{orderNo}` | GET | 获取订单详情 |
| `/api/v1/membership/orders/my` | GET | 获取我的订单 |
| `/api/v1/membership/orders/{orderNo}/cancel` | POST | 取消订单 |
| `/api/v1/membership/my` | GET | 获取我的会员信息 |
| `/api/v1/membership/check` | GET | 检查会员是否有效 |

### 支付回调接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/v1/payment/alipay/notify` | POST | 支付宝异步通知回调 |

## 📦 会员卡内容类型

会员卡内容支持以下类型（管理端动态添加）：
- `BENEFIT` - 权益说明
- `RULE` - 使用规则
- `PRIVILEGE` - 特权列表
- `OTHER` - 其他

## 💳 支付方式

支持以下支付方式：
- `ALIPAY` - 支付宝支付
- `BALANCE` - 余额支付

## ⏰ 定时任务

- **订单超时检查**：每5分钟检查一次，超过30分钟未支付的订单自动取消

## 🔒 安全说明

1. **支付宝私钥**和**公钥**请妥善保管，不要提交到代码仓库
2. 生产环境请使用正式的支付宝网关地址
3. 异步通知回调地址需要配置HTTPS
4. 建议在生产环境使用环境变量或配置中心管理敏感信息

## 🚀 启动步骤

1. 运行 Flyway 迁移脚本创建表
2. 配置支付宝沙箱参数
3. 启动应用
4. 使用管理端创建会员卡类型和会员卡
5. 使用会员端测试购买流程

## 📝 注意事项

1. 沙箱环境的支付不会真实扣款
2. 回调地址需要外网可访问，本地开发可以使用内网穿透工具（如 ngrok）
3. 会员卡购买成功后，用户会自动获得相应天数的会员有效期
4. 购买会员卡会赠送积分（根据会员卡配置的 points_reward）
