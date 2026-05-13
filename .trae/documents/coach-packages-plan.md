# 教练端私教套餐管理 - 实现计划

> **目标：** 让教练可以在自己的后台编辑和管理私教套餐，会员购买后自动成为该教练的学员。

> **架构：** 复用现有 `product` 表（已含 `coach_id` 字段），教练端新增「我的套餐」页面，提供 CRUD 接口；会员端购买流程保持不变。

***

## 一、文件结构

### 后端新增/修改

| 文件                                                                                 | 操作 | 说明                           |
| ---------------------------------------------------------------------------------- | -- | ---------------------------- |
| `src/main/java/com/fitness/modules/product/controller/CoachProductController.java` | 新增 | 教练端套餐管理 API（查询/创建/更新/删除/上下架） |
| `src/main/java/com/fitness/modules/product/service/ProductService.java`            | 修改 | 新增教练套餐相关接口方法                 |
| `src/main/java/com/fitness/modules/product/service/impl/ProductServiceImpl.java`   | 修改 | 实现教练套餐 CRUD                  |
| `src/main/java/com/fitness/modules/product/mapper/ProductMapper.java`              | 修改 | 新增按 coach\_id 查询方法           |

### 前端新增/修改

| 文件                                      | 操作 | 说明                      |
| --------------------------------------- | -- | ----------------------- |
| `frontend/src/views/coach/Packages.vue` | 新增 | 教练端「我的套餐」页面             |
| `frontend/src/api/coachProduct.js`      | 新增 | 教练端套餐 API 封装            |
| `frontend/src/router/index.js`          | 修改 | 新增 `/coach/packages` 路由 |
| `frontend/src/layouts/CoachLayout.vue`  | 修改 | 左侧菜单新增「我的套餐」入口          |

***

## 二、详细任务步骤

### Task 1: 后端 - 新增教练套餐管理 Controller

**文件：** `src/main/java/com/fitness/modules/product/controller/CoachProductController.java`（新建）

* [ ] **Step 1.1: 创建 Controller**

```java
package com.fitness.modules.product.controller;

import com.fitness.common.result.Result;
import com.fitness.common.util.SecurityUtils;
import com.fitness.modules.product.model.dto.ProductDTO;
import com.fitness.modules.product.model.vo.ProductVO;
import com.fitness.modules.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coach/products")
@RequiredArgsConstructor
public class CoachProductController {

    private final ProductService productService;

    @GetMapping
    public Result<List<ProductVO>> listMyProducts() {
        Long coachId = SecurityUtils.getCurrentUserId();
        return Result.success(productService.getProductsByCoachId(coachId));
    }

    @PostMapping
    public Result<ProductVO> create(@Valid @RequestBody ProductDTO dto) {
        Long coachId = SecurityUtils.getCurrentUserId();
        dto.setCoachId(coachId);
        dto.setCategory("COURSE");
        return Result.success(productService.createProduct(dto));
    }

    @PutMapping("/{id}")
    public Result<ProductVO> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        Long coachId = SecurityUtils.getCurrentUserId();
        dto.setId(id);
        dto.setCoachId(coachId);
        return Result.success(productService.updateProduct(dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long coachId = SecurityUtils.getCurrentUserId();
        productService.deleteCoachProduct(id, coachId);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Long coachId = SecurityUtils.getCurrentUserId();
        productService.updateCoachProductStatus(id, coachId, status);
        return Result.success();
    }
}
```

***

### Task 2: 后端 - 扩展 ProductService

**文件：** `src/main/java/com/fitness/modules/product/service/ProductService.java`

* [ ] **Step 2.1: 新增接口方法**

在接口中新增：

```java
List<ProductVO> getProductsByCoachId(Long coachId);
void deleteCoachProduct(Long productId, Long coachId);
void updateCoachProductStatus(Long productId, Long coachId, String status);
```

**文件：** `src/main/java/com/fitness/modules/product/service/impl/ProductServiceImpl.java`

* [ ] **Step 2.2: 实现方法**

```java
@Override
public List<ProductVO> getProductsByCoachId(Long coachId) {
    List<Product> products = productMapper.selectList(
        new LambdaQueryWrapper<Product>()
            .eq(Product::getCoachId, coachId)
            .eq(Product::getCategory, "COURSE")
            .orderByAsc(Product::getSortOrder)
    );
    return products.stream().map(this::convertToVO).collect(Collectors.toList());
}

@Override
public void deleteCoachProduct(Long productId, Long coachId) {
    Product product = productMapper.selectById(productId);
    if (product == null || !coachId.equals(product.getCoachId())) {
        throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
    }
    productMapper.deleteById(productId);
}

@Override
public void updateCoachProductStatus(Long productId, Long coachId, String status) {
    Product product = productMapper.selectById(productId);
    if (product == null || !coachId.equals(product.getCoachId())) {
        throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
    }
    product.setStatus(status);
    productMapper.updateById(product);
}
```

* [ ] **Step 2.3: 修改 createProduct，自动设置 coachId**

在 `createProduct` 方法中，如果 `dto.getCoachId() != null`，则自动设置 `product.setCoachId(dto.getCoachId())`。

***

### Task 3: 后端 - 扩展 ProductMapper

**文件：** `src/main/java/com/fitness/modules/product/mapper/ProductMapper.java`

* [ ] **Step 3.1: 确认现有方法足够**

`ProductMapper` 继承 `BaseMapper<Product>`，已有 `selectList`、`selectById`、`deleteById`、`updateById` 等方法，Task 2 中使用的都是 BaseMapper 自带方法，无需新增 XML。

***

### Task 4: 前端 - 新增教练套餐 API

**文件：** `frontend/src/api/coachProduct.js`（新建）

* [ ] **Step 4.1: 创建 API 文件**

```javascript
import request from '@/utils/request'

export function getMyPackages() {
  return request({ url: '/coach/products', method: 'get' })
}

export function createPackage(data) {
  return request({ url: '/coach/products', method: 'post', data })
}

export function updatePackage(id, data) {
  return request({ url: `/coach/products/${id}`, method: 'put', data })
}

export function deletePackage(id) {
  return request({ url: `/coach/products/${id}`, method: 'delete' })
}

export function updatePackageStatus(id, status) {
  return request({ url: `/coach/products/${id}/status`, method: 'put', params: { status } })
}
```

***

### Task 5: 前端 - 新增「我的套餐」页面

**文件：** `frontend/src/views/coach/Packages.vue`（新建）

* [ ] **Step 5.1: 创建页面组件**

页面结构：

1. 页面头部：标题「我的套餐」+ 副标题 + 新增套餐按钮
2. 统计卡片：套餐总数、上架中、已下架
3. 套餐列表卡片：每个卡片展示套餐名、编码、价格、库存、状态、描述
4. 每个卡片操作：编辑、上架/下架、删除
5. 新增/编辑弹窗：表单包含 name、code、description、originalPrice、stock、sortOrder、status

参考 `frontend/src/views/coach/Courses.vue` 的卡片风格，使用 `n-card` 布局。

```vue
<template>
  <div class="coach-packages">
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">我的套餐</h2>
        <span class="page-subtitle">管理您的私教课程套餐，供学员购买</span>
      </div>
      <n-button type="primary" @click="openAddModal">
        <template #icon><n-icon :component="AddOutline" /></template>
        新增套餐
      </n-button>
    </div>

    <!-- 套餐列表 -->
    <div v-if="packages.length > 0" class="package-grid">
      <div v-for="pkg in packages" :key="pkg.id" class="package-card">
        <div class="package-header">
          <h3 class="package-name">{{ pkg.name }}</h3>
          <n-tag :type="pkg.status === 'ACTIVE' ? 'success' : 'default'">
            {{ pkg.status === 'ACTIVE' ? '上架中' : '已下架' }}
          </n-tag>
        </div>
        <div class="package-body">
          <div class="package-meta">
            <span class="meta-item">编码: {{ pkg.code }}</span>
            <span class="meta-item price">¥{{ pkg.originalPrice }}</span>
            <span class="meta-item">库存: {{ pkg.stock }}</span>
          </div>
          <p class="package-desc">{{ pkg.description || '暂无描述' }}</p>
        </div>
        <div class="package-actions">
          <n-button text type="primary" @click="openEditModal(pkg)">编辑</n-button>
          <n-button text :type="pkg.status === 'ACTIVE' ? 'warning' : 'success'" 
            @click="toggleStatus(pkg)">
            {{ pkg.status === 'ACTIVE' ? '下架' : '上架' }}
          </n-button>
          <n-button text type="error" @click="handleDelete(pkg)">删除</n-button>
        </div>
      </div>
    </div>
    <n-empty v-else description="暂无套餐，点击右上角新增" />

    <!-- 新增/编辑弹窗 -->
    <n-modal v-model:show="showModal" preset="card" :title="modalTitle" style="width: 520px">
      <n-form ref="formRef" :model="form" :rules="rules" label-width="100">
        <n-form-item label="套餐名称" path="name">
          <n-input v-model:value="form.name" placeholder="例如：单次体验课" />
        </n-form-item>
        <n-form-item label="套餐编码" path="code">
          <n-input v-model:value="form.code" placeholder="例如：COACH_TRIAL" />
        </n-form-item>
        <n-form-item label="价格" path="originalPrice">
          <n-input-number v-model:value="form.originalPrice" :min="0" :precision="2" style="width: 100%">
            <template #prefix>¥</template>
          </n-input-number>
        </n-form-item>
        <n-form-item label="库存" path="stock">
          <n-input-number v-model:value="form.stock" :min="0" :precision="0" style="width: 100%" />
        </n-form-item>
        <n-form-item label="排序" path="sortOrder">
          <n-input-number v-model:value="form.sortOrder" :min="0" style="width: 100%" />
        </n-form-item>
        <n-form-item label="状态" path="status">
          <n-radio-group v-model:value="form.status">
            <n-radio-button value="ACTIVE">上架</n-radio-button>
            <n-radio-button value="INACTIVE">下架</n-radio-button>
          </n-radio-group>
        </n-form-item>
        <n-form-item label="套餐说明" path="description">
          <n-input v-model:value="form.description" type="textarea" :rows="3" placeholder="请输入套餐说明..." />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">保存</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>
```

***

### Task 6: 前端 - 新增路由

**文件：** `frontend/src/router/index.js`

* [ ] **Step 6.1: 在 coach 路由 children 中新增**

```javascript
{
  path: 'packages',
  name: 'CoachPackages',
  component: () => import('@/views/coach/Packages.vue'),
  meta: { title: '我的套餐' }
}
```

***

### Task 7: 前端 - 新增教练菜单入口

**文件：** `frontend/src/layouts/CoachLayout.vue`

* [ ] **Step 7.1: 在 menuOptions 数组中新增菜单项**

在「课程管理」之后、「日程安排」之前插入：

```javascript
{
  label: '我的套餐',
  key: '/coach/packages',
  icon: renderIcon(PricetagsOutline)
}
```

需要导入 `PricetagsOutline` 图标。

***

### Task 8: 验证构建

* [ ] **Step 8.1: 后端编译**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

* [ ] **Step 8.2: 前端编译**

Run: `cd frontend && npm run build -- --mode development`
Expected: build completed without errors

***

## 三、业务操作流程（人为操作）

### 教练操作流程

1. 教练登录教练端
2. 左侧菜单 → **我的套餐**
3. 点击「新增套餐」
4. 填写表单：

   * 套餐名称：如「单次体验课」

   * 套餐编码：如 `COACH_TRIAL`

   * 价格：如 `99.00`

   * 库存：如 `999`

   * 排序：如 `1`

   * 状态：上架

   * 套餐说明：如「按次付费，不绑定长期套餐」
5. 点击保存
6. 重复以上步骤创建其余 3 个套餐（小课程包、中课程包、大课程包）

### 会员购买流程（保持不变）

1. 会员进入「我的教练」
2. 浏览推荐教练，点击卡片查看详情
3. 点击「成为学员」
4. 选择套餐 → 确认购买 → 支付
5. 支付成功后自动绑定为该教练学员

***

## 四、数据权限说明

* 教练只能看到自己创建的套餐（`coach_id = 当前用户ID`）

* 教练只能编辑/删除/上下架自己的套餐

* 创建套餐时 `category` 自动固定为 `COURSE`

* 会员端 `getCoachPackages` 接口只返回 `ACTIVE` 状态的套餐

