# AI文本润色功能设计文档

**文档版本**: 1.0  
**创建日期**: 2026-04-02  
**作者**: AI Assistant

## 1. 功能概述

### 1.1 背景

在管理员端的课程管理、器械管理、商品管理等模块中，管理员需要编写描述文本。为了提升描述文本的质量和专业性，需要提供AI文本润色功能。

### 1.2 目标

- 提供一键式AI润色功能，快速提升描述文本质量
- 支持多个业务模块复用（课程、器械、商品等）
- 提供友好的用户体验，包括加载状态、取消操作、恢复原文等功能
- 确保数据安全和错误处理

### 1.3 适用范围

- 课程管理模块：新增/编辑课程的描述输入框
- 器械管理模块：新增/编辑器械的描述输入框
- 商品管理模块：新增/编辑商品的描述输入框
- 未来其他需要文本润色的业务模块

## 2. 系统架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────┐
│                      前端层 (Vue 3)                      │
│  ┌──────────────────────────────────────────────────┐  │
│  │  业务组件 (Courses.vue, Equipment.vue等)         │  │
│  │  - 描述输入框                                     │  │
│  │  - AI润色按钮                                     │  │
│  │  - 加载状态指示器                                 │  │
│  │  - 取消按钮                                       │  │
│  │  - 恢复原始文本按钮                               │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          ↓ HTTP POST
┌─────────────────────────────────────────────────────────┐
│                     后端层 (Spring Boot)                 │
│  ┌──────────────────────────────────────────────────┐  │
│  │  AITextPolishController                           │  │
│  │  POST /api/v1/ai/polish                           │  │
│  └──────────────────────────────────────────────────┘  │
│                          ↓                              │
│  ┌──────────────────────────────────────────────────┐  │
│  │  AIService (新增 polishText 方法)                 │  │
│  └──────────────────────────────────────────────────┘  │
│                          ↓                              │
│  ┌──────────────────────────────────────────────────┐  │
│  │  PromptTemplates (新增润色模板)                   │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│              Spring AI Alibaba (ChatClient)             │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│              阿里百炼大模型 (qwen-plus)                  │
└─────────────────────────────────────────────────────────┘
```

### 2.2 核心组件

1. **前端组件**：各业务模块中的AI润色功能模块
2. **后端Controller**：新建 `AITextPolishController`
3. **AI服务**：扩展 `AIService` 接口
4. **Prompt模板**：新增通用润色Prompt

## 3. API设计

### 3.1 接口规范

**接口路径**: `POST /api/v1/ai/polish`

**权限要求**: `ADMIN` 或 `COACH` 角色

**请求参数**:
```json
{
  "text": "原始描述文本"
}
```

**响应格式**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "polishedText": "润色后的描述文本",
    "originalText": "原始描述文本"
  }
}
```

### 3.2 输入验证

- 文本长度：3-100字
- 不能为空
- 不能包含非法字符

### 3.3 输出规格

- 润色后文本长度：50-150字
- 保持原有核心信息
- 专业严谨的语言风格

## 4. 数据模型

### 4.1 请求DTO

```java
public class TextPolishDTO {
    @NotBlank(message = "文本内容不能为空")
    @Size(min = 3, max = 100, message = "文本长度需在3-100字之间")
    private String text;
}
```

### 4.2 响应VO

```java
public class TextPolishVO {
    private String polishedText;
    private String originalText;
}
```

## 5. Prompt设计

### 5.1 通用润色Prompt模板

```
你是一位专业的文案编辑。请对以下文本进行润色：

{text}

润色要求：
1. 保持原有核心信息和事实不变
2. 优化语言表述，使其更加流畅、专业、有吸引力
3. 改善文本结构和逻辑，提升可读性
4. 适当补充细节描述，使内容更丰富完整
5. 使用专业、准确、易懂的语言风格
6. 控制在50-150字之间

请直接返回润色后的文本，不要添加任何解释或说明。
```

### 5.2 设计理念

- **通用性**：不依赖业务类型，适用于任何文本润色场景
- **专业性**：强调专业严谨的语言风格
- **可控性**：明确输出长度范围，避免过长或过短
- **简洁性**：要求AI直接返回结果，不添加额外解释

## 6. 前端UI设计

### 6.1 UI状态流转

```
状态1: 初始状态
┌─────────────────────────────────────────────────────────┐
│  [描述输入框]                                           │
│  placeholder: "请输入描述，支持ai润色..."               │
└─────────────────────────────────────────────────────────┘
[✨ AI润色]

                          ↓ 点击AI润色

状态2: 润色中
┌─────────────────────────────────────────────────────────┐
│  [描述输入框 - 文字闪烁动画]                            │
└─────────────────────────────────────────────────────────┘
[✨ AI润色中...] [✖️ 取消]

                          ↓ 润色完成

状态3: 润色完成
┌─────────────────────────────────────────────────────────┐
│  [润色后的描述内容]                                     │
└─────────────────────────────────────────────────────────┘
[↩️ 恢复原文]

                          ↓ 点击恢复原文

                          返回状态1
```

### 6.2 组件代码结构

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
    <!-- AI润色按钮（初始状态显示） -->
    <el-button 
      v-if="!hasPolished && !polishing"
      type="primary" 
      @click="handlePolish"
    >
      <el-icon><MagicStick /></el-icon>
      AI润色
    </el-button>
    
    <!-- 润色中状态 -->
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
    
    <!-- 恢复原文按钮 -->
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

### 6.3 状态管理

```javascript
const polishing = ref(false)           // 是否正在润色
const originalText = ref('')           // 保存原始文本用于恢复
const hasPolished = ref(false)         // 是否已润色完成
```

### 6.4 样式设计

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

## 7. 错误处理

### 7.1 前端错误处理

```javascript
const ErrorTypes = {
  NETWORK_ERROR: '网络连接失败，请检查网络后重试',
  TIMEOUT_ERROR: '请求超时，请稍后重试',
  AI_SERVICE_ERROR: 'AI服务暂时不可用，请稍后重试',
  INPUT_EMPTY: '请先输入描述内容',
  INPUT_TOO_SHORT: '文本内容至少需要3个字',
  INPUT_TOO_LONG: '文本长度不能超过100字',
  UNKNOWN_ERROR: '润色失败，请重试'
}

const handlePolishError = (error) => {
  let errorMessage = ErrorTypes.UNKNOWN_ERROR
  
  if (error.response) {
    switch (error.response.status) {
      case 400:
        errorMessage = error.response.data.message || '输入内容不合法'
        break
      case 500:
        errorMessage = ErrorTypes.AI_SERVICE_ERROR
        break
      case 503:
        errorMessage = 'AI服务正在维护，请稍后重试'
        break
    }
  } else if (error.request) {
    errorMessage = ErrorTypes.NETWORK_ERROR
  }
  
  ElMessage.error(errorMessage)
}
```

### 7.2 后端异常处理

```java
@RestControllerAdvice
public class AIExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        log.error("AI服务异常", e);
        return Result.error("AI服务调用失败: " + e.getMessage());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(message);
    }
}
```

## 8. 数据安全

### 8.1 安全措施

1. **HTTPS加密传输**：确保数据传输安全
2. **JWT Token认证**：验证用户身份
3. **接口权限控制**：使用 `@PreAuthorize` 注解
4. **输入验证**：防止SQL注入和XSS攻击
5. **请求频率限制**：防止滥用AI服务

### 8.2 隐私保护

- 不记录用户输入的原始文本
- 不存储润色结果
- 仅用于临时处理，处理完成后立即丢弃

## 9. 性能优化

### 9.1 请求超时设置

```javascript
// 前端设置30秒超时
const polishDescription = (data) => {
  return request({
    url: '/api/v1/ai/polish',
    method: 'post',
    data,
    timeout: 30000
  })
}
```

### 9.2 文本长度优化

- 前端预检查文本长度，避免无效请求
- 后端验证文本长度，确保符合要求

### 9.3 加载状态优化

- 使用文字闪烁动画提示用户正在处理
- 提供取消功能，避免用户长时间等待

## 10. 测试策略

### 10.1 单元测试

- 测试 `AIService.polishText()` 方法
- 测试输入验证逻辑
- 测试Prompt生成逻辑

### 10.2 集成测试

- 测试完整的润色流程
- 测试错误处理场景
- 测试权限控制

### 10.3 前端测试

- 测试UI状态流转
- 测试用户交互
- 测试错误提示

## 11. 部署说明

### 11.1 环境要求

- Spring Boot 3.5.x
- Spring AI Alibaba 1.1.2.0
- 阿里百炼API Key
- Vue 3 + Element Plus

### 11.2 配置项

```yaml
spring:
  ai:
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY}
      chat:
        enabled: true
        options:
          model: qwen-plus
          temperature: 0.7
```

## 12. 未来扩展

### 12.1 功能扩展

- 支持多种润色风格（专业、活泼、简洁等）
- 支持批量润色
- 支持润色历史记录

### 12.2 性能优化

- 实现流式响应，实时显示润色过程
- 添加缓存机制，避免重复润色相同文本
- 优化Prompt，提升润色质量

## 13. 总结

本设计文档描述了一个通用的AI文本润色功能，具有以下特点：

1. **通用性**：适用于多个业务模块，无需修改后端代码
2. **易用性**：一键润色，操作简单直观
3. **安全性**：完善的权限控制和数据保护
4. **可靠性**：全面的错误处理和异常恢复
5. **可扩展性**：为未来功能扩展预留空间

该功能将显著提升管理员编写描述文本的效率和质量，为用户提供更好的内容体验。
