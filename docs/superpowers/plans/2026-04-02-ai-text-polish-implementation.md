# AI文本润色功能实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现通用的AI文本润色功能，支持课程、器械、商品等模块的描述文本润色

**Architecture:** 扩展现有AI模块，在AIController中添加润色接口，前端通过统一的API调用实现润色功能

**Tech Stack:** Spring Boot 3.5.x, Spring AI Alibaba 1.1.2.0, Vue 3, Element Plus

---

## 文件结构

### 后端文件
- **修改**: `src/main/java/com/fitness/integration/ai/prompt/PromptTemplates.java` - 添加润色Prompt模板
- **修改**: `src/main/java/com/fitness/integration/ai/service/AIService.java` - 添加polishText方法接口
- **修改**: `src/main/java/com/fitness/integration/ai/service/impl/AIServiceImpl.java` - 实现polishText方法
- **修改**: `src/main/java/com/fitness/integration/ai/controller/AIController.java` - 添加润色接口
- **创建**: `src/main/java/com/fitness/integration/ai/model/dto/TextPolishDTO.java` - 请求DTO
- **创建**: `src/main/java/com/fitness/integration/ai/model/vo/TextPolishVO.java` - 响应VO

### 前端文件
- **创建**: `frontend/src/api/ai.js` - AI相关API接口
- **修改**: `frontend/src/views/admin/Courses.vue` - 添加AI润色功能
- **修改**: `frontend/src/views/admin/Equipment.vue` - 添加AI润色功能
- **修改**: `frontend/src/views/admin/Products.vue` - 添加AI润色功能

---

## Task 1: 后端 - 创建数据模型

**Files:**
- Create: `src/main/java/com/fitness/integration/ai/model/dto/TextPolishDTO.java`
- Create: `src/main/java/com/fitness/integration/ai/model/vo/TextPolishVO.java`

- [ ] **Step 1: 创建TextPolishDTO.java**

```java
package com.fitness.integration.ai.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TextPolishDTO {
    @NotBlank(message = "文本内容不能为空")
    @Size(min = 3, max = 100, message = "文本长度需在3-100字之间")
    private String text;
}
```

- [ ] **Step 2: 创建TextPolishVO.java**

```java
package com.fitness.integration.ai.model.vo;

import lombok.Data;

@Data
public class TextPolishVO {
    private String polishedText;
    private String originalText;
}
```

- [ ] **Step 3: 提交数据模型**

```bash
git add src/main/java/com/fitness/integration/ai/model/dto/TextPolishDTO.java
git add src/main/java/com/fitness/integration/ai/model/vo/TextPolishVO.java
git commit -m "feat: 添加AI文本润色数据模型"
```

---

## Task 2: 后端 - 扩展Prompt模板

**Files:**
- Modify: `src/main/java/com/fitness/integration/ai/prompt/PromptTemplates.java`

- [ ] **Step 1: 在PromptTemplates类中添加润色Prompt模板**

在 `PromptTemplates.java` 文件的第116行后添加以下内容：

```java
    /**
     * 文本润色 Prompt 模板
     */
    private static final String TEXT_POLISH_TEMPLATE = """
            你是一位专业的文案编辑。请对以下文本进行润色：

            %s

            润色要求：
            1. 保持原有核心信息和事实不变
            2. 优化语言表述，使其更加流畅、专业、有吸引力
            3. 改善文本结构和逻辑，提升可读性
            4. 适当补充细节描述，使内容更丰富完整
            5. 使用专业、准确、易懂的语言风格
            6. 控制在50-150字之间

            请直接返回润色后的文本，不要添加任何解释或说明。
            """;

    /**
     * 生成文本润色Prompt
     *
     * @param text 原始文本
     * @return 生成的Prompt
     */
    public String generateTextPolish(String text) {
        return String.format(TEXT_POLISH_TEMPLATE, text);
    }
```

- [ ] **Step 2: 提交Prompt模板扩展**

```bash
git add src/main/java/com/fitness/integration/ai/prompt/PromptTemplates.java
git commit -m "feat: 添加文本润色Prompt模板"
```

---

## Task 3: 后端 - 扩展AIService接口

**Files:**
- Modify: `src/main/java/com/fitness/integration/ai/service/AIService.java`

- [ ] **Step 1: 在AIService接口中添加polishText方法**

在 `AIService.java` 文件的第83行后添加以下内容：

```java
    /**
     * 文本润色
     *
     * @param text 原始文本
     * @return 润色后的文本
     */
    String polishText(String text);
```

- [ ] **Step 2: 提交接口扩展**

```bash
git add src/main/java/com/fitness/integration/ai/service/AIService.java
git commit -m "feat: 在AIService接口中添加polishText方法"
```

---

## Task 4: 后端 - 实现polishText方法

**Files:**
- Modify: `src/main/java/com/fitness/integration/ai/service/impl/AIServiceImpl.java`

- [ ] **Step 1: 在AIServiceImpl类中实现polishText方法**

在 `AIServiceImpl.java` 文件的第280行后添加以下内容：

```java
    @Override
    public String polishText(String text) {
        log.info("文本润色请求，文本长度: {}", text.length());
        String traceId = langSmithService.startTrace("polishText", Map.of("type", "text_polish"));
        long startTime = System.currentTimeMillis();

        try {
            String prompt = promptTemplates.generateTextPolish(text);
            langSmithService.logInput(traceId, prompt, model);

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            long duration = System.currentTimeMillis() - startTime;
            langSmithService.logOutput(traceId, response, duration);
            log.info("文本润色成功，响应长度: {}, 耗时: {}ms", response != null ? response.length() : 0, duration);
            return response;
        } catch (Exception e) {
            langSmithService.logError(traceId, e.getMessage());
            log.error("文本润色失败", e);
            throw new RuntimeException("文本润色失败: " + e.getMessage(), e);
        } finally {
            langSmithService.endTrace(traceId);
        }
    }
```

- [ ] **Step 2: 提交实现**

```bash
git add src/main/java/com/fitness/integration/ai/service/impl/AIServiceImpl.java
git commit -m "feat: 实现polishText方法"
```

---

## Task 5: 后端 - 添加润色接口

**Files:**
- Modify: `src/main/java/com/fitness/integration/ai/controller/AIController.java`

- [ ] **Step 1: 在AIController中添加必要的import**

在 `AIController.java` 文件的import区域添加：

```java
import com.fitness.integration.ai.model.dto.TextPolishDTO;
import com.fitness.integration.ai.model.vo.TextPolishVO;
```

- [ ] **Step 2: 在AIController类中添加润色接口**

在 `AIController.java` 文件的第131行后添加以下内容：

```java
    /**
     * 文本润色接口
     *
     * @param request 润色请求
     * @return 润色结果
     */
    @PostMapping("/polish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<TextPolishVO> polishText(@Valid @RequestBody TextPolishDTO request) {
        log.info("文本润色请求，文本长度: {}", request.getText().length());
        String polishedText = aiService.polishText(request.getText());
        
        TextPolishVO vo = new TextPolishVO();
        vo.setPolishedText(polishedText);
        vo.setOriginalText(request.getText());
        
        return Result.success(vo);
    }
```

- [ ] **Step 3: 提交接口**

```bash
git add src/main/java/com/fitness/integration/ai/controller/AIController.java
git commit -m "feat: 添加文本润色接口"
```

---

## Task 6: 后端 - 测试润色功能

**Files:**
- Test: 启动后端服务并测试接口

- [ ] **Step 1: 启动后端服务**

```bash
./mvnw spring-boot:run
```

- [ ] **Step 2: 使用curl测试润色接口**

```bash
curl -X POST http://localhost:8080/api/v1/ai/polish \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"text":"瑜伽课程，适合初学者"}'
```

预期响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "polishedText": "本瑜伽课程专为初学者设计...",
    "originalText": "瑜伽课程，适合初学者"
  }
}
```

- [ ] **Step 3: 验证输入验证**

测试空文本：
```bash
curl -X POST http://localhost:8080/api/v1/ai/polish \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"text":""}'
```

预期响应：400错误，提示"文本内容不能为空"

---

## Task 7: 前端 - 创建AI API接口

**Files:**
- Create: `frontend/src/api/ai.js`

- [ ] **Step 1: 创建ai.js文件**

```javascript
import request from '@/utils/request'

export function polishDescription(data) {
  return request({
    url: '/api/v1/ai/polish',
    method: 'post',
    data,
    timeout: 30000
  })
}
```

- [ ] **Step 2: 提交API文件**

```bash
git add frontend/src/api/ai.js
git commit -m "feat: 创建AI API接口文件"
```

---

## Task 8: 前端 - 修改Courses.vue添加AI润色功能

**Files:**
- Modify: `frontend/src/views/admin/Courses.vue`

- [ ] **Step 1: 在script setup中添加必要的import**

在 `Courses.vue` 文件的第361行后添加：

```javascript
import { polishDescription } from '@/api/ai'
import { MagicStick, RefreshLeft } from '@element-plus/icons-vue'
```

- [ ] **Step 2: 在script setup中添加润色相关状态**

在 `Courses.vue` 文件的第377行后添加：

```javascript
const polishing = ref(false)
const originalText = ref('')
const hasPolished = ref(false)
```

- [ ] **Step 3: 在script setup中添加润色相关方法**

在 `Courses.vue` 文件的第717行后添加：

```javascript
async function handlePolish() {
  const textLength = form.description.trim().length
  
  if (textLength === 0) {
    message.warning('请先输入描述内容')
    return
  }
  
  if (textLength < 3) {
    message.warning('文本内容至少需要3个字')
    return
  }
  
  if (textLength > 100) {
    message.warning('文本长度不能超过100字')
    return
  }
  
  originalText.value = form.description
  polishing.value = true
  hasPolished.value = false
  
  try {
    const response = await polishDescription({ text: form.description })
    form.description = response.polishedText
    hasPolished.value = true
    message.success('润色成功')
  } catch (error) {
    message.error(error.message || '润色失败，请重试')
  } finally {
    polishing.value = false
  }
}

function handleCancelPolish() {
  polishing.value = false
  message.info('已取消润色')
}

function handleRestore() {
  form.description = originalText.value
  hasPolished.value = false
  message.success('已恢复原始文本')
}
```

- [ ] **Step 4: 修改描述输入框部分**

将 `Courses.vue` 文件的第249-251行替换为：

```vue
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入描述，支持ai润色..."
            maxlength="100"
            :class="{ 'loading-textarea': polishing }"
          />
          
          <div class="polish-button-group">
            <el-button 
              v-if="!hasPolished && !polishing"
              type="primary" 
              @click="handlePolish"
            >
              <el-icon><MagicStick /></el-icon>
              AI润色
            </el-button>
            
            <el-button 
              v-if="polishing"
              type="primary" 
              loading
            >
              AI润色中...
            </el-button>
            <el-button 
              v-if="polishing"
              @click="handleCancelPolish"
            >
              取消
            </el-button>
            
            <el-button 
              v-if="hasPolished && !polishing"
              @click="handleRestore"
            >
              <el-icon><RefreshLeft /></el-icon>
              恢复原文
            </el-button>
          </div>
        </el-form-item>
```

- [ ] **Step 5: 在style中添加润色相关样式**

在 `Courses.vue` 文件的第905行后添加：

```css
.polish-button-group {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}

.loading-textarea textarea {
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
```

- [ ] **Step 6: 提交Courses.vue修改**

```bash
git add frontend/src/views/admin/Courses.vue
git commit -m "feat: 在课程管理中添加AI润色功能"
```

---

## Task 9: 前端 - 修改Equipment.vue添加AI润色功能

**Files:**
- Modify: `frontend/src/views/admin/Equipment.vue`

- [ ] **Step 1: 读取Equipment.vue文件结构**

首先读取文件，找到描述输入框的位置和script setup区域。

- [ ] **Step 2: 添加必要的import和状态**

参考Task 8的步骤，在Equipment.vue中添加相同的import和状态。

- [ ] **Step 3: 添加润色方法**

参考Task 8的步骤，在Equipment.vue中添加相同的润色方法。

- [ ] **Step 4: 修改描述输入框**

找到描述输入框，添加AI润色按钮组。

- [ ] **Step 5: 添加样式**

添加润色相关的CSS样式。

- [ ] **Step 6: 提交Equipment.vue修改**

```bash
git add frontend/src/views/admin/Equipment.vue
git commit -m "feat: 在器械管理中添加AI润色功能"
```

---

## Task 10: 前端 - 修改Products.vue添加AI润色功能

**Files:**
- Modify: `frontend/src/views/admin/Products.vue`

- [ ] **Step 1: 读取Products.vue文件结构**

首先读取文件，找到描述输入框的位置和script setup区域。

- [ ] **Step 2: 添加必要的import和状态**

参考Task 8的步骤，在Products.vue中添加相同的import和状态。

- [ ] **Step 3: 添加润色方法**

参考Task 8的步骤，在Products.vue中添加相同的润色方法。

- [ ] **Step 4: 修改描述输入框**

找到描述输入框，添加AI润色按钮组。

- [ ] **Step 5: 添加样式**

添加润色相关的CSS样式。

- [ ] **Step 6: 提交Products.vue修改**

```bash
git add frontend/src/views/admin/Products.vue
git commit -m "feat: 在商品管理中添加AI润色功能"
```

---

## Task 11: 前端 - 测试润色功能

**Files:**
- Test: 启动前端服务并测试功能

- [ ] **Step 1: 启动前端服务**

```bash
cd frontend
npm run dev
```

- [ ] **Step 2: 测试课程管理润色功能**

1. 打开浏览器访问 http://localhost:5173
2. 登录管理员账号
3. 进入课程管理页面
4. 点击"新增课程"
5. 在描述输入框中输入简短描述（如"瑜伽课程"）
6. 点击"AI润色"按钮
7. 验证加载状态显示
8. 验证润色结果填充到输入框
9. 验证"恢复原文"按钮显示
10. 点击"恢复原文"验证恢复功能

- [ ] **Step 3: 测试器械管理润色功能**

重复Step 2的步骤，测试器械管理模块。

- [ ] **Step 4: 测试商品管理润色功能**

重复Step 2的步骤，测试商品管理模块。

- [ ] **Step 5: 测试错误处理**

1. 测试空输入
2. 测试过短文本（少于3字）
3. 测试过长文本（超过100字）
4. 测试网络错误（断开网络）
5. 验证错误提示正确显示

---

## Task 12: 最终提交和文档更新

**Files:**
- Update: 文档和README

- [ ] **Step 1: 更新项目文档**

如果需要，更新项目README或相关文档，说明新增的AI润色功能。

- [ ] **Step 2: 最终提交**

```bash
git add .
git commit -m "feat: 完成AI文本润色功能"
```

- [ ] **Step 3: 推送到远程仓库**

```bash
git push origin master
```

---

## 测试清单

### 后端测试
- [ ] 文本润色接口正常工作
- [ ] 输入验证正确（空文本、过短、过长）
- [ ] 权限控制正确（需要ADMIN或COACH角色）
- [ ] 错误处理正确
- [ ] 日志记录正确

### 前端测试
- [ ] 课程管理润色功能正常
- [ ] 器械管理润色功能正常
- [ ] 商品管理润色功能正常
- [ ] UI状态流转正确
- [ ] 加载状态显示正确
- [ ] 取消功能正常
- [ ] 恢复原文功能正常
- [ ] 错误提示正确显示

### 集成测试
- [ ] 前后端联调正常
- [ ] 数据传输正确
- [ ] 超时处理正确
- [ ] 并发请求处理正确

---

## 注意事项

1. **安全性**：确保JWT Token正确传递，接口权限控制正确
2. **性能**：注意AI响应时间，前端设置30秒超时
3. **用户体验**：加载状态清晰，错误提示友好
4. **代码规范**：遵循项目现有代码风格和规范
5. **测试覆盖**：确保关键功能都有测试覆盖
