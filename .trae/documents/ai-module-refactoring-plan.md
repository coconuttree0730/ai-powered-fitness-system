# AI 模块重构优化执行计划

> 基于 2026-05-15 对项目 AI 模块的全面代码审查，对照 Spring AI 官方文档和 Spring AI Alibaba 官方文档后制定。

---

## 阶段概览

| 阶段 | 优先级 | 内容 | 涉及文件数 | 预计风险 |
|------|--------|------|-----------|---------|
| Phase 1 | 🔴 安全修复 | API Key 泄露 + Redis 密码泄露 | 2 | 无 |
| Phase 2 | 🔴 架构修复 | SSE 流式架构重构 (SseEmitter→Flux) | 3 | 中 |
| Phase 3 | 🟠 API 迁移 | 迁移弃用 StreamingOutput→NodeOutput | 2 | 中 |
| Phase 4 | 🟠 异常处理 | 添加 AI 异常处理器 + 清理冗余配置 | 3 | 低 |
| Phase 5 | 🟡 代码拆分 | ChatAssistantServiceImpl 拆分解耦 | 5+ | 高 |
| Phase 6 | 🟡 清理优化 | 死代码清理 + Prompt 统一 + Bug 修复 | 5 | 低 |
| Phase 7 | 🟢 加固增强 | 限流 + 超时 + 配置化 | 3 | 低 |

---

## Phase 1: 安全修复（🔴 最高优先级，立即执行）

### 1.1 移除 API Key 日志泄露

- **文件**: `src/main/java/com/fitness/integration/ai/config/AIConfig.java`
- **问题**: `log.info("API Key: {}...", apiKey.substring(0, 10) + "***")` 泄露 API Key 前缀
- **操作**: 修改日志语句，只记录 API Key 是否已配置，不输出任何 Key 内容
- **修改内容**:
  ```java
  // 修改前:
  log.info("API Key: {}...", apiKey != null && apiKey.length() > 10 ? apiKey.substring(0, 10) + "***" : "N/A");
  
  // 修改后:
  log.info("API Key configured: {}", apiKey != null ? "YES" : "MISSING");
  ```

### 1.2 移除 Redis 密码泄露和 System.out.println

- **文件**: `src/main/java/com/fitness/integration/ai/agent/config/AgentRuntimeConfig.java`
- **问题 1**: `System.out.println("[RedissonConfig] Redis Password: '" + password + "'")` 打印明文密码
- **问题 2**: 全部使用 `System.out.println` 而非 Slf4j Logger
- **操作**:
  1. 给类添加 `@Slf4j` 注解
  2. 将所有 `System.out.println(...)` 替换为 `log.debug(...)`
  3. 删除打印密码的语句，密码验证改为仅记录是否配置
  4. 将 host/port 的调试日志级别设为 `debug`

### 1.3 验证

- 编译项目：`mvn compile -DskipTests`
- 检查日志输出确认无敏感信息泄露

---

## Phase 2: SSE 流式架构修复（🔴 架构问题）

### 2.1 重构 ChatAssistantController 流式端点

- **文件**: `src/main/java/com/fitness/modules/chat/controller/ChatAssistantController.java`
- **问题**: 使用 `SseEmitter` + `CachedThreadPool` + `blockLast()` 混合模式
- **目标**: 改为 Spring AI 标准的 `Flux<ServerSentEvent<String>>` 直接返回
- **操作**:
  1. 移除 `private final ExecutorService executorService` 字段
  2. 移除 `private final ObjectMapper objectMapper` 字段（如无其他用途）
  3. 将 `sendMessageStream()` 方法改为返回 `Flux<ServerSentEvent<String>>`：
     ```java
     @PostMapping(value = "/messages/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
     public Flux<ServerSentEvent<String>> sendMessageStream(@Valid @RequestBody ChatMessageDTO dto) {
         Long userId = SecurityUtils.getCurrentUserId();
         return chatAssistantService.sendMessageStream(userId, dto)
             .map(event -> ServerSentEvent.<String>builder()
                 .id(event.getType())
                 .event(event.getType())
                 .data(objectMapper.writeValueAsString(event))
                 .build())
             .concatWith(Flux.just(ServerSentEvent.<String>builder()
                 .event("done")
                 .data(objectMapper.writeValueAsString(ChatStreamEventVO.done()))
                 .build()))
             .onErrorResume(ex -> Flux.just(ServerSentEvent.<String>builder()
                 .event("error")
                 .data(objectMapper.writeValueAsString(ChatStreamEventVO.error("抱歉，健小助暂时无法完成本次回答")))
                 .build()));
     }
     ```
  4. 权限校验保留在方法开头，手动检查 SecurityUtils
  5. 移除 `sendEvent()` 私有方法

### 2.2 修复 ChatAssistantServiceImpl 流式事务问题

- **文件**: `src/main/java/com/fitness/modules/chat/service/impl/ChatAssistantServiceImpl.java`
- **问题**: `doOnComplete` 中保存 assistant 消息时事务上下文已丢失
- **操作**:
  1. 将 `doOnComplete` 中的数据库保存逻辑提取为独立方法
  2. 在该方法上添加 `@Transactional(propagation = Propagation.REQUIRES_NEW)`
  3. 或使用 `TransactionTemplate` 手动管理事务
  4. 移除 `.subscribeOn(Schedulers.boundedElastic())`（应由 WebFlux 调度）
  5. 简化 `SecurityContext` 传递逻辑（Spring Security 在 WebFlux 中通过 `ReactiveSecurityContextHolder` 自动传递）

### 2.3 检查 AIController 流式端点

- **文件**: `src/main/java/com/fitness/integration/ai/controller/AIController.java`
- **当前状态**: `streamChat()` 已正确返回 `Flux<String>`，但权限检查使用了手动 `SecurityUtils.isAuthenticated()` 而非 `@PreAuthorize`
- **操作**: 将 `SecurityUtils.isAuthenticated()` 改为 `@PreAuthorize("isAuthenticated()")`，与其他端点保持一致

### 2.4 验证

- 编译项目：`mvn compile -DskipTests`
- 检查 `ChatAssistantServiceImplTest` 中相关测试是否需要更新
- 运行相关单元测试

---

## Phase 3: 弃用 API 迁移（🟠 API 现代化）

### 3.1 迁移 JianXiaoZhuAgentServiceImpl 流式输出

- **文件**: `src/main/java/com/fitness/modules/chat/agent/JianXiaoZhuAgentServiceImpl.java`
- **问题**: 使用已弃用的 `StreamingOutput`、`OutputType.AGENT_MODEL_STREAMING` 等类型
- **目标**: 迁移到 `NodeOutput` 模式
- **操作**:
  1. 查询当前 `spring-ai-alibaba-agent-framework` 版本中 `ReactAgent.stream()` 返回类型
  2. 使用 `reactAgent.stream(finalPrompt, runnableConfig)` 返回的 `Flux<NodeOutput>`
  3. 通过 `NodeOutput` 的方法判断事件类型（`isSTART()`, `isEND()`, `node()`, `state()` 等）
  4. 重构 `streamChat()` 方法：
     ```java
     return reactAgent.stream(finalPrompt, runnableConfig)
         .filter(output -> !output.isSTART() && !output.isEND())
         .flatMap(output -> {
             // 根据 NodeOutput 属性判断事件类型
             // 映射到 ChatStreamEventVO
         });
     ```
  5. 移除 `@SuppressWarnings("deprecation")` 注解

### 3.2 检查 AgentRuntimeConfig 兼容性

- **文件**: `src/main/java/com/fitness/integration/ai/agent/config/AgentRuntimeConfig.java`
- **操作**: 确认 `SummarizationHook`、`RedisSaver`、`DatabaseStore` 等类在 1.1.2.0 版本中非弃用状态
- 如已弃用，查阅对应新版 API 并迁移

### 3.3 验证

- 编译项目并运行相关单元测试
- 确认 Agent 流式对话功能正常

---

## Phase 4: 异常处理与配置清理（🟠 中优先级）

### 4.1 添加 AI 异常处理器

- **文件**: `src/main/java/com/fitness/common/exception/GlobalExceptionHandler.java`
- **问题**: `AiIntegrationException` 无专门处理器，落入通用 Exception 处理器
- **操作**: 添加新方法：
  ```java
  @ExceptionHandler(AiIntegrationException.class)
  public Result<Void> handleAiIntegrationException(AiIntegrationException e) {
      log.error("AI 服务调用异常: {}", e.getMessage(), e);
      return Result.error(ErrorCode.AI_GENERATE_ERROR);
  }
  ```

### 4.2 清理冗余 Redisson 配置

- **文件**: `src/main/java/com/fitness/integration/ai/agent/config/AgentRuntimeConfig.java`
- **问题**: 手动创建 `RedissonClient`，与已有的 `spring-boot-starter-data-redis` 配置重复
- **操作**:
  1. 检查 `spring-ai-alibaba-graph-core` 的 `RedisSaver` 是否支持 Lettuce（项目已引入 Lettuce 作为 Redis 客户端）
  2. 如果 `RedisSaver` **必须**使用 Redisson，则：保留 `redissonClient()` Bean，但复用 `RedisProperties` 配置，移除手动拼接 URL 逻辑
  3. 如果 `RedisSaver` 支持其他客户端，则：移除 `redissonClient()` Bean，改用项目已有的 Redis 连接
  4. 简化 `redissonClient()` 方法，使用 `Redisson.create()` 的 Config 方式通过 `redisProperties` 自动注入

### 4.3 验证

- 编译项目
- 验证 Redis 连接正常

---

## Phase 5: ChatAssistantServiceImpl 拆分（🟡 代码结构优化）

> ⚠️ 此阶段变更较大，需要特别注意测试覆盖

### 5.1 提取健身计划解析器

- **新建文件**: `src/main/java/com/fitness/modules/chat/service/impl/FitnessPlanParser.java`
- **职责**: 解析 AI 返回的健身计划文本
- **迁移方法**:
  - `generateWeeklyPlanForCard()` → 保留在 ChatAssistantServiceImpl（编排逻辑）
  - `parseWeeklyPlan()` → 移到 FitnessPlanParser
  - `extractDayOfWeek()` → 移到 FitnessPlanParser
  - `extractTrainingType()` → 移到 FitnessPlanParser
  - `extractFocus()` → 移到 FitnessPlanParser
  - `parseExerciseLine()` → 移到 FitnessPlanParser
  - `finalizeDayPlan()` → 移到 FitnessPlanParser
  - `generateDefaultWeeklyPlan()` → 移到 FitnessPlanParser
  - `getDefaultExercisesForDay()` → 移到 FitnessPlanParser
  - `createExercise()` → 移到 FitnessPlanParser
  - `isTipsLine()` → 移到 FitnessPlanParser
  - `stripLinePrefix()` → 移到 FitnessPlanParser
  - `extractExerciseName()` → 移到 FitnessPlanParser
  - `inferTrainingType()` → 移到 FitnessPlanParser
  - `defaultFocus()` → 移到 FitnessPlanParser
  - `containsAny()` → 移到 FitnessPlanParser（或公共工具类）

### 5.2 提取推荐服务

- **新建文件**: `src/main/java/com/fitness/modules/chat/service/RecommendationService.java`
- **职责**: 课程推荐、器材推荐、周度建议
- **迁移方法**:
  - `generateWeeklyAdvice()` → 移到 RecommendationService
  - `recommendCourses()` → 移到 RecommendationService
  - `recommendEquipment()` → 移到 RecommendationService

### 5.3 提取 JSON 序列化工具类

- **新建文件**: `src/main/java/com/fitness/modules/chat/util/ExerciseJsonSerializer.java`
- **职责**: ExerciseVO 的 JSON 序列化/反序列化
- **迁移方法**:
  - `parseExercises()` → 移到 ExerciseJsonSerializer
  - `serializeExercises()` → 移到 ExerciseJsonSerializer
  - `asInteger()` → 移到 ExerciseJsonSerializer
  - `toInteger()` → 移到 ExerciseJsonSerializer

### 5.4 重构 ChatAssistantServiceImpl

- **文件**: `src/main/java/com/fitness/modules/chat/service/impl/ChatAssistantServiceImpl.java`
- **操作**:
  1. 注入新的 `FitnessPlanParser`、`RecommendationService`、`ExerciseJsonSerializer`
  2. 将已迁移的方法替换为委托调用
  3. 移除已迁移到新类的常量（`DAY_NAMES`, `SETS_REPS_PATTERN` 等）
  4. 移除已迁移的 `ObjectMapper` 字段
  5. 目标：将原有 990 行缩减到 ~400 行以下

### 5.5 验证

- 运行全部单元测试：`mvn test`
- 特别关注 `ChatAssistantServiceImplTest`
- 编译通过且所有测试通过后方可进入下一阶段

---

## Phase 6: 清理与统一（🟡 代码卫生）

### 6.1 清理死代码：ChatRagContextService

- **文件**: `src/main/java/com/fitness/modules/chat/rag/ChatRagContextService.java`
- **问题**: 未被任何 Bean 使用，RAG 功能已由 `RagQueryTool` 实现
- **操作**:
  1. 确认无任何地方注入 `ChatRagContextService`
  2. 删除该文件

### 6.2 统一 System Prompt 语言

- **问题**: Prompt 模板和 System Prompt 混用中英文
- **操作**:
  1. 将 [PromptTemplates.java](file:///g:/ai-powered-fitness-system-v2/src/main/java/com/fitness/integration/ai/prompt/PromptTemplates.java) 中所有 System Prompt 翻译为中文（因为输出要求是中文）
  2. 将 [ChatPromptTemplates.java](file:///g:/ai-powered-fitness-system-v2/src/main/java/com/fitness/modules/chat/prompt/ChatPromptTemplates.java) 中的 `FITNESS_ASSISTANT_SYSTEM_PROMPT` 翻译为中文
  3. 保持 `JianXiaoZhuAgentPrompts.java` 的中文不变

### 6.3 修复 Unicode 转义为可读中文

- **文件**: `src/main/java/com/fitness/modules/chat/prompt/ChatPromptTemplates.java`
- **问题**: `ragContext.contains("\u65e0\u6cd5\u627e\u5230\u76f8\u5173\u4fe1\u606f")` 不可读
- **操作**: 替换为可读的中文字符串常量：
  ```java
  private static final String RAG_NO_RESULT_KEYWORD = "无法找到相关信息";
  private static final String RAG_TEMP_UNAVAILABLE_KEYWORD = "暂时无法";
  ```

### 6.4 验证

- 编译项目
- 确认 Prompt 文本在代码审查中可读

---

## Phase 7: 加固增强（🟢 锦上添花）

### 7.1 添加外部 API 超时配置

- **文件**: `src/main/java/com/fitness/modules/chat/tools/WeatherQueryTools.java`
- **操作**: 为 RestClient 添加 connectTimeout(5s) 和 readTimeout(10s)

- **文件**: `src/main/java/com/fitness/modules/chat/tools/LocationQueryTools.java`
- **操作**: 为 RestClient 添加 connectTimeout(5s) 和 readTimeout(10s)

### 7.2 硬编码 fallback 配置化

- **文件**: `src/main/java/com/fitness/modules/chat/tools/LocationQueryTools.java`
- **问题**: `String defaultCity = "北京"` 硬编码
- **操作**: 改为从 `application.yml` 读取默认城市：
  ```java
  @Value("${chat.tools.location.default-city:北京}")
  private String defaultCity;
  ```

### 7.3 添加 AI 接口限流

- **涉及文件**: `AIController.java`, `ChatAssistantController.java`
- **操作**: 
  1. 为 AI 端点添加 `@RateLimiter` 或自定义限流注解
  2. 建议引入 Bucket4j 或使用 Spring Cloud Gateway
  3. 至少为 `/api/v1/ai/chat/stream` 和 `/api/v1/chat/messages/stream` 添加限制

### 7.4 验证

- 编译并运行全量测试
- 确认限流配置生效

---

## 执行约定

1. **每个 Phase 完成后必须执行测试**，确认无回归
2. **Phase 5 属于大重构**，建议在前 4 个 Phase 都通过后再进行
3. **每个文件修改前先阅读完整文件内容**，确保理解全部上下文
4. **每完成一个 Phase 记录完成状态**，便于跟踪进度
5. 测试命令：`mvn test` 或 `mvn test -pl -Dtest=具体测试类`

---

## 文件修改清单

| 文件 | Phase | 操作类型 |
|------|-------|---------|
| `config/AIConfig.java` | 1 | 修改 |
| `agent/config/AgentRuntimeConfig.java` | 1, 4, 7 | 修改 |
| `controller/ChatAssistantController.java` | 2 | 重构 |
| `service/impl/ChatAssistantServiceImpl.java` | 2, 5 | 重构 |
| `controller/AIController.java` | 2 | 修改 |
| `agent/JianXiaoZhuAgentServiceImpl.java` | 3 | 重构 |
| `common/exception/GlobalExceptionHandler.java` | 4 | 新增方法 |
| `service/impl/FitnessPlanParser.java` | 5 | 新建 |
| `service/RecommendationService.java` | 5 | 新建 |
| `util/ExerciseJsonSerializer.java` | 5 | 新建 |
| `rag/ChatRagContextService.java` | 6 | 删除 |
| `prompt/PromptTemplates.java` | 6 | 修改 |
| `prompt/ChatPromptTemplates.java` | 6 | 修改 |
| `tools/WeatherQueryTools.java` | 7 | 修改 |
| `tools/LocationQueryTools.java` | 7 | 修改 |