# 前端代码优化重构计划

## 目标

在不改变 Web 界面（视觉/交互完全一致）的前提下，优化 `frontend/` 代码质量、性能和可维护性。

---

## 核心发现

| 问题 | 严重度 | 现状 |
|------|--------|------|
| 双 UI 框架全量引入 | **严重** | Element Plus (~1.2MB) + Naive UI (~350KB) 同时全量注册 |
| 超大单文件组件 | **严重** | Home.vue 6,494 行，15 个文件超 1,000 行 |
| 代码重复 | **高** | 3 个 Layout 中用户信息/退出登录逻辑完全相同 |
| 缺少生产构建优化 | **高** | vite.config.js 无代码分割、无压缩优化 |
| 死代码/未使用导入 | **中** | MemberLayout 暗色模式、未使用图标导入等 |
| 硬编码与不一致 | **中** | 公开路径在 2 处硬编码，/403/404 路由不存在 |

---

## 优化步骤

### 步骤 1：Vite 生产构建优化（不改 UI，只改配置）

**文件**: `vite.config.js`

具体改动：
- 添加 `build.rollupOptions.output.manualChunks` 代码分割，将 element-plus、naive-ui、echarts、vue 核心库分离为独立 chunk
- 添加 `build.terserOptions` 移除生产环境的 console/debugger
- 添加 `build.chunkSizeWarningLimit` 调高警告阈值

**验证**: `npm run build` 后对比优化前后各 chunk 体积大小

---

### 步骤 2：UI 框架按需引入（视觉不变，减少打包体积）

**文件**: `src/main.js`、`vite.config.js`

具体改动：
- 安装 `unplugin-auto-import` 和 `unplugin-vue-components`（Element Plus 和 Naive UI 解析器）
- 在 vite.config.js 中配置自动导入插件
- 移除 `src/main.js` 中的 `import ElementPlus from 'element-plus'`、`import 'element-plus/dist/index.css'`、`import naive from 'naive-ui'`
- 移除 `app.use(ElementPlus)` 和 `app.use(naive)`
- 各组件中已显式导入的组件保持不变（不改变视觉）

**验证**:
- `npm run build` 对比打包体积
- `npm run dev` 启动后逐一浏览页面，确认 UI 无差异
- 对比优化前后的 dist 文件夹大小

---

### 步骤 3：拆分超大组件 Home.vue（6,494 行 -> 多个子组件）

**文件**: `src/views/public/Home.vue` + 新建多个子组件

具体改动：
- 将 Home.vue 中的各板块抽取为独立组件，放在 `src/components/home/` 目录下：
  - `HomeNavbar.vue` — 导航栏
  - `HomeHero.vue` — Hero 轮播区域
  - `HomePhilosophy.vue` — 品牌理念板块
  - `HomeAIFeatures.vue` — AI 功能展示板块
  - `HomeCourses.vue` — 课程体系板块
  - `HomeEquipments.vue` — 器材展示板块
  - `HomeMembership.vue` — 会员卡体系板块
  - `HomeCoaches.vue` — 教练团队板块
  - `HomeBanner.vue` — 公告栏板块
- 原 Home.vue 变为只负责组合子组件和协调滚动监听逻辑
- 每个子组件只接收必要的 props，保持视觉输出不变

**验证**: 打开首页，逐板块对比优化前后的截图/视觉效果

---

### 步骤 4：拆分其他超大组件

按优先级依次处理：

| 文件 | 行数 | 拆分方案 |
|------|------|---------|
| `member/Assistant.vue` | 3,351 | 抽取 `ChatMessageList.vue`、`PlanGenerationAnimation.vue`，流式处理为 composable `useChatStream.js` |
| `member/Cards.vue` | 2,226 | 抽取 `PurchaseModal.vue`，支付逻辑为 composable `usePayment.js` |
| `admin/Dashboard.vue` | 1,689 | 抽取 8 个图表为独立组件，图表管理逻辑为 composable `useECharts.js` |
| `member/Profile.vue` | 1,659 | 拆分为 `ProfileAccount.vue`、`ProfileWallet.vue`、`ProfileOrders.vue`、`ProfileSecurity.vue` |
| `public/Courses.vue` + `public/Equipments.vue` | 1,605 + 1,550 | 抽取通用 `FilterableCardList.vue` 组件 |

**验证**: 每个拆分后逐一检查页面视觉效果不变

---

### 步骤 5：抽取 3 个 Layout 的公共逻辑

**文件**: `AdminLayout.vue`、`CoachLayout.vue`、`MemberLayout.vue`

具体改动：
- 新建 `src/components/UserInfoDropdown.vue` 通用组件，封装用户名显示（含首字母头像、下拉菜单、退出登录）
- 新建 `src/utils/renderIcon.js` 工具函数，将 Naive UI 的 `renderIcon` 逻辑集中
- 三个 Layout 改为引用 `UserInfoDropdown.vue`，移除重复的 computed 属性和方法

**验证**: 分别登录 admin/coach/member 账号，确认右上角用户信息区域显示一致

---

### 步骤 6：清理死代码和未使用导入

具体改动：
- **MemberLayout.vue**: 移除未使用的 `StarOutline`、`IdCardOutline` 图标导入
- **MemberLayout.vue**: 移除暗色模式相关的 `isDarkMode` ref 和 toggle 逻辑（无实际功能）
- **Auth store**: 移除 `hasRole` 函数（与 computed 属性功能重叠，标注为 deprecated 但保留兼容）
- **Auth store**: 合并 `login()` 和 `setLoginState()` 中的重复代码为私有函数 `_saveCredentials()`
- **Auth store**: 修复 `fetchUserInfo()` 同时写入 localStorage 和 sessionStorage 的问题
- **utils/auth.js**: 删除该文件（功能被 auth store 完全覆盖且无引用），或在 auth store 中统一引用它

**验证**: 编译无警告，lint 通过

---

### 步骤 7：修复路由和请求拦截器问题

**文件**: `src/router/index.js`、`src/utils/request.js`

具体改动：
- **request.js**: 将硬编码的 `publicPaths` 替换为从 router 获取当前路由 meta.public 判断
- **request.js**: 移除 `/403`、`/404` 路由跳转（路由表不存在），改为 `ElMessage.error()` 或 `useMessage().error()` 提示
- **router/index.js**: 移除路由守卫中的手动 localStorage 读取逻辑（第 292-301 行），改为复用 authStore 的方法
- **router/index.js**: 移除与 `meta.public` 重复的硬编码路径列表

**验证**: 
- 未登录访问需认证页面 → 重定向到首页
- Token 过期 → 弹错误提示而非跳转到不存在的 /401

---

### 步骤 8：清理文件结构

具体改动：
- 将 `src/views/member/bookings.utils.js` 和 `bookings.utils.test.js` 移动到 `src/utils/` 目录
- 将硬编码示例数据从 `src/stores/schedule.js` 的 `initSampleData` 移除
- 用 `crypto.randomUUID()` 替换 `Date.now()` 作为 ID 生成

**验证**: 编译通过，无导入路径错误

---

### 步骤 9：统一工具函数引用

具体改动：
- 确保 `getToken()` / `setToken()` / `removeToken()` 只通过一个入口调用（auth store 或 utils/auth.js 二选一）
- 将 Naive UI 的 `renderIcon` 抽取为公共工具函数 `src/utils/icon.js`

**验证**: 编译通过，所有页面图标正常显示

---

## 执行顺序

```
步骤1 (Vite构建优化) → 步骤2 (按需引入) → 步骤3-4 (拆分大组件) → 步骤5 (抽取公共逻辑) → 步骤6-7 (清理修复) → 步骤8-9 (结构整理)
```

## 预期收益

| 指标 | 优化前 | 优化后预期 |
|------|--------|-----------|
| 生产包体积 (gzip) | ~1.8MB | ~800KB |
| 最大单文件行数 | 6,494 | <500 |
| 超 1000 行文件数 | 17 | 0 |
| 布局代码重复 | 3 份 | 1 份 |
| 死代码/未使用导入 | 5+ | 0 |
| 构建时间 (dev) | 基准 | 减少 ~30% |

## 风险控制

- 每完成一个步骤，运行 `npm run build` 确保编译通过
- 每完成一个步骤，运行 `npm run dev` 手动抽查关键页面
- 拆分组件时严格保持 props/events 接口不变，确保父组件无需改动
- 不改动任何 CSS 样式值，只移动 CSS 到对应组件