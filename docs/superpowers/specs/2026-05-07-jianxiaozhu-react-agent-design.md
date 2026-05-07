# 健小助 ReactAgent 重构设计

## 背景

当前会员端“健小助”聊天能力已经具备基础对话、知识库检索和消息存储能力，但 Spring AI Alibaba 的使用方式还不够规范，存在几个明显问题：

1. 聊天编排主要依赖手工拼接 prompt，缺少统一的 Agent Runtime。
2. RAG 由聊天服务直接调用并拼接上下文，检索增强能力没有以标准组件形式接入。
3. 短期上下文由 Redis List 手工维护，无法表达 Agent 推理状态、工具调用过程和多轮恢复。
4. 长期记忆缺失，用户稳定偏好、训练目标、限制条件无法跨会话复用。
5. 业务查询能力没有封装为 `@Tool`，模型不能基于真实业务方法进行可靠调度。

本次重构基于 `spring-ai-alibaba 1.1.2.0` 及其 Agent Framework 能力完成统一升级，但只覆盖查询类能力。写操作 Tool 与 Human-in-the-Loop 审批流放到下一轮接入。

## 目标

将会员端“健小助”重构为统一的 `ReactAgent` 聊天架构，完成以下能力：

1. 用 `ReactAgent` 统一替代当前手工 prompt 编排。
2. 将会员卡、课程、商品、教练、会员档案等查询能力封装成查询类 `@Tool`。
3. 将当前 PostgreSQL 知识库接入为标准化 RAG 能力，供 Agent 在说明性问题中调用。
4. 引入短期记忆、长期记忆和 Redis checkpoint。
5. 保留现有业务 Service、Controller、数据库主结构，尽量减少对既有业务的破坏。

## 本期范围

### 包含

1. 会员端“健小助”聊天接口切到新的 Agent Runtime。
2. 查询类 Tool：
   - 会员卡列表/详情/我的会员状态
   - 课程列表/详情/推荐课程
   - 商品列表/详情
   - 教练列表/详情/我的私教
   - 用户档案/训练目标/会籍概况
3. PostgreSQL 知识库接入 Agent 使用的 RAG。
4. Redis checkpoint 和短期记忆接入。
5. PostgreSQL 长期记忆存储与召回。
6. 系统提示词重构，明确工具优先、RAG 优先级和答复规范。

### 不包含

1. 购买会员卡、取消会员卡等写操作 Tool。
2. Human-in-the-Loop 审批和二次确认交互。
3. 健身计划生成能力并入 ReactAgent。
4. 全量重做现有知识库管理后台。

## 总体架构

本次改造后，聊天主链路改为“Chat API -> Agent Runtime -> Tool/RAG/Memory -> Response”模式。

### 1. Chat API 层

保留当前会员聊天控制器入口，继续负责：

1. 会员鉴权。
2. 会话归属校验。
3. 用户消息落库。
4. 调用 Agent Runtime。
5. 持久化 assistant 回复。

### 2. Agent Runtime 层

新增统一的 Agent 编排层，作为本次重构核心。它负责：

1. 构建 `ReactAgent`。
2. 注册系统提示词。
3. 注册查询类 Tool。
4. 装配 RAG 检索增强。
5. 读取和写入短期记忆、长期记忆。
6. 使用 Redis checkpoint 进行会话级状态恢复。

### 3. Tool 层

复用当前业务 Service，把只读查询能力封装为 `@Tool`，交给 Agent 调度。Tool 层不直接处理 HTTP，只负责业务查询和结果整形。

### 4. RAG 层

保留现有 PostgreSQL 知识库、向量检索和 chunk 表结构，但不再由聊天服务手工拼 prompt。RAG 层需要输出给 Agent 可消费的检索上下文和来源信息。

### 5. Memory 层

1. 短期记忆：依赖 Agent state + Redis checkpoint，以 `sessionId` 为线程标识。
2. 长期记忆：沉淀用户稳定事实，存 PostgreSQL，供新会话按需召回。

## 目录与职责拆分

建议新增和调整以下模块。

### `src/main/java/com/fitness/integration/ai/agent/config`

负责 Agent Framework 基础装配：

1. 模型与 Agent Runtime Bean。
2. `ReactAgent` 创建。
3. Tool 注册。
4. Redis checkpoint 配置。
5. RAG 与 Memory 装配。

### `src/main/java/com/fitness/modules/chat/agent`

负责“健小助”会员聊天 Agent 运行时：

1. `JianXiaoZhuAgentService`
2. `JianXiaoZhuAgentPrompts`
3. `JianXiaoZhuAgentState`
4. `JianXiaoZhuAgentResponseAssembler`

### `src/main/java/com/fitness/modules/chat/tools`

按业务域拆分查询 Tool：

1. `MembershipQueryTools`
2. `CourseQueryTools`
3. `ProductQueryTools`
4. `CoachQueryTools`
5. `ProfileQueryTools`

### `src/main/java/com/fitness/modules/chat/rag`

作为 Agent 专用 RAG 适配层，封装：

1. 检索请求转换。
2. 多路召回结果整形。
3. 引用上下文构造。
4. 说明性问题的 RAG 接入规则。

### `src/main/java/com/fitness/modules/chat/memory`

长期记忆相关能力：

1. `LongTermMemoryService`
2. `LongTermMemoryRecallService`
3. `LongTermMemoryExtractor`
4. 记忆实体、Mapper、Repository

### `src/main/java/com/fitness/modules/chat/service/impl/ChatAssistantServiceImpl`

该类需要明显瘦身，只保留：

1. 会话与用户校验。
2. 消息持久化。
3. 调用 Agent Service。
4. 保存回复与返回 VO。

以下旧职责从该类迁出：

1. 手工拼接 RAG prompt。
2. 手工维护消息上下文。
3. 直接调用 `AIService.chat/fullPrompt` 作为主流程。

## Tool 设计

### Tool 分组

本期全部为只读 Tool，按业务域拆分，便于后续加审批写操作。

#### 1. MembershipQueryTools

能力：

1. 查询可购买会员卡列表。
2. 查询会员卡详情。
3. 查询当前用户持有的会员卡与状态。

适用问题：

1. “有哪些会员卡”
2. “月卡多少钱”
3. “我现在的会员卡什么时候到期”

#### 2. CourseQueryTools

能力：

1. 查询课程列表。
2. 查询课程详情。
3. 查询推荐课程。

适用问题：

1. “有哪些课程”
2. “瑜伽课有哪些”
3. “适合减脂的课程有哪些”

#### 3. ProductQueryTools

能力：

1. 查询商品列表。
2. 查询商品详情。

适用问题：

1. “有卖什么商品”
2. “蛋白粉多少钱”

#### 4. CoachQueryTools

能力：

1. 查询教练列表。
2. 查询教练详情。
3. 查询当前用户私教信息。

适用问题：

1. “有哪些教练”
2. “帮我看看我的私教是谁”

#### 5. ProfileQueryTools

能力：

1. 查询当前用户档案概况。
2. 查询训练目标、身高体重、偏好和会籍摘要。

适用问题：

1. “根据我的情况推荐课程”
2. “你还记得我的训练目标吗”

### Tool 设计原则

1. Tool 只包已有业务 Service，不重复造查询逻辑。
2. Tool 返回结构化结果，不返回长篇自然语言。
3. Agent 负责解释和整理 Tool 输出。
4. Tool 内仍然执行权限与边界校验，尤其是“我的会员状态”“我的私教”等用户敏感数据。

## RAG 设计

### RAG 的职责

RAG 只负责说明性、规则性、知识性问题，不负责动态业务实时数据。

RAG 优先覆盖：

1. 营业时间、馆内制度、退改规则、预约须知。
2. 课程注意事项。
3. 器械说明。
4. 基础健身知识、训练科普。

### Tool 与 RAG 的优先级

1. 涉及实时业务数据的问题优先走 Tool。
2. 涉及规则说明、制度说明、知识说明的问题优先走 RAG。
3. 一些混合问题允许先调用 Tool，再结合 RAG 组织答案。

示例：

1. “有哪些会员卡” -> 会员卡 Tool
2. “会员卡能不能退” -> RAG
3. “月卡多少钱，购买后多久生效” -> 先 Tool，后 RAG

### 现有知识库迁移策略

本次不推翻现有知识库表和向量检索实现，但要收敛为 Agent 友好的接口：

1. 复用现有 `knowledge_document / knowledge_chunk` 表。
2. 复用现有向量与关键词混合检索能力。
3. 由新的 `chat.rag` 适配层输出标准上下文对象，不再把“拼接好的整段 prompt”暴露给聊天服务。

## Memory 设计

### 短期记忆

短期记忆用于同一会话内保留上下文和 Agent 执行状态。

策略：

1. `sessionId` 作为 `threadId`。
2. Redis 作为 checkpoint 持久化介质。
3. 会话恢复时自动加载短期状态。
4. 代替当前手工 Redis List 上下文缓存作为主路径。

短期记忆关注：

1. 当前会话中的问题延续关系。
2. 刚刚调用过的 Tool 结果摘要。
3. 追问指代，如“那个课程”“刚才第二个会员卡”。

### 长期记忆

长期记忆用于跨会话复用用户稳定事实，不保存全量聊天历史。

建议存储内容：

1. 健身目标：减脂、增肌、塑形。
2. 训练限制：膝伤、腰伤、不能高冲击。
3. 课程偏好：喜欢团课、偏好晚间、有氧优先。
4. 关注方向：会员卡、私教、康复课程。

长期记忆要求：

1. 结构化或半结构化存储。
2. 每轮对话后提取候选记忆并去重写入。
3. 新会话开始时按需召回，而不是无脑全部注入。

## Checkpoint 设计

Redis checkpoint 专门用于 Agent 执行态持久化，与长期记忆分工明确：

1. Redis：线程状态、短期上下文、工具调用状态恢复。
2. PostgreSQL：长期记忆与知识库。

降级规则：

1. Redis 不可用时，对话允许降级为无短期记忆模式。
2. PostgreSQL 长期记忆不可用时，对话允许继续，但不提供跨会话偏好记忆。

## 提示词设计

系统提示词需要从“健身问答 prompt”升级为“Agent 调度规范 prompt”。

核心约束：

1. 对实时业务数据，优先调用 Tool，不凭空编造。
2. 对规则说明、场馆制度、健身知识，优先结合 RAG。
3. 当 Tool 与 RAG 都没有可靠依据时，必须明确说明信息不足。
4. 所有回答使用中文。
5. 回答应友好、专业、简洁，不输出内部推理过程。
6. 不应假装执行购买、取消等本期未开放能力。

## 请求处理数据流

会员聊天请求处理链路如下：

1. 前端将 `sessionId + userMessage` 发送到聊天接口。
2. 后端校验会员身份与会话归属。
3. 用户消息落库。
4. Agent Runtime 以 `sessionId` 为 `threadId` 恢复 Redis checkpoint。
5. Runtime 召回用户长期记忆。
6. `ReactAgent` 决定是否调用查询 Tool、是否调用 RAG。
7. Agent 汇总结果生成最终自然语言回复。
8. assistant 消息落库。
9. 对话后抽取长期记忆候选并写入 PostgreSQL。

## 异常与降级策略

### 1. Tool 失败

1. 单个 Tool 失败不应导致整个会话崩溃。
2. Tool 需要返回受控错误信息，供 Agent 判断是否改换路径或友好提示。

### 2. RAG 无结果

1. 视为“没有可靠知识命中”，不是系统异常。
2. Agent 可以改用 Tool 或通用建议，但必须标明无法确认具体馆内规则。

### 3. Redis checkpoint 不可用

1. 记录告警日志。
2. 降级为单轮对话或弱上下文对话。
3. 不中断主链路。

### 4. 长期记忆不可用

1. 不阻断当轮回答。
2. 只失去跨会话偏好召回能力。

### 5. 模型调用失败

1. 统一返回友好提示。
2. 保留完整错误日志、调用上下文和关联会话 ID。

## 测试策略

### 1. Tool 单元测试

验证：

1. Tool 能正确调用底层业务 Service。
2. Tool 输出结构符合 Agent 消费预期。
3. 用户相关 Tool 不会越权返回他人数据。

### 2. Agent 编排测试

验证典型问题的路由正确性：

1. “有哪些会员卡” -> 会员卡 Tool
2. “有哪些课程” -> 课程 Tool
3. “营业时间是什么” -> RAG
4. “适合我减脂的课程有哪些” -> Profile Tool + Course Tool

### 3. Memory 测试

验证：

1. 同一 `sessionId` 下短期记忆可跨轮使用。
2. 新会话可正确召回长期记忆。
3. Redis 故障时主流程可降级。

### 4. 接口集成测试

验证：

1. 聊天接口鉴权正确。
2. 会话校验正确。
3. 消息可落库。
4. Agent 回复链路可用。

## 迁移顺序

为降低风险，本次建议按以下顺序迁移：

1. 新增 Agent Runtime 与配置层。
2. 新增查询类 Tool。
3. 新增 RAG 适配层，复用现有知识库检索。
4. 新增长期记忆表与服务。
5. 接入 Redis checkpoint 与短期记忆。
6. 重写聊天服务主流程，切换到 Agent Runtime。
7. 清理旧的手工 prompt/RAG/Redis list 主路径。

## 明确不做的事

本次明确不做以下内容，避免范围失控：

1. 将健身计划生成重构为 Agent Tool 链。
2. 把所有 AI 相关能力一次性合并成一个万能 Agent。
3. 接入购买会员卡、取消会员卡写操作。
4. 接入 Human-in-the-Loop 确认流程。

这些内容在下一轮基于当前统一 Agent 骨架继续演进。
