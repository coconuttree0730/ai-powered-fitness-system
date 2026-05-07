# 健小助 ReactAgent 基础能力 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将会员端“健小助”聊天主链路切换到 Spring AI Alibaba `ReactAgent`，接入查询类 Tool、PostgreSQL RAG、Redis checkpoint、短期记忆和 PostgreSQL 长期记忆。

**Architecture:** 保留现有会话、消息、知识库和业务 Service，新增 `chat.agent / chat.tools / chat.memory / chat.rag` 四层，将查询能力封装为 `@Tool` 交给 `ReactAgent` 调度。短期记忆与 checkpoint 走 Redis，长期记忆走 PostgreSQL，自定义 RAG 上下文组装器复用现有知识库检索。

**Tech Stack:** Spring Boot 3.5, Spring AI Alibaba 1.1.2.0, Spring AI 1.1.2, PostgreSQL/pgvector, Redis, MyBatis-Plus, JUnit 5, Mockito

---

### Task 1: 接入 Agent Framework 依赖与长期记忆表

**Files:**
- Modify: `pom.xml`
- Create: `src/main/resources/db/migration/V42__create_chat_long_term_memory_table.sql`
- Test: `src/test/java/com/fitness/modules/chat/memory/LongTermMemoryEntitySmokeTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.fitness.modules.chat.memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LongTermMemoryEntitySmokeTest {

    @Test
    void memoryTableVersionShouldBeDefined() {
        assertEquals("V42__create_chat_long_term_memory_table.sql", "V42__create_chat_long_term_memory_table.sql");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -Dtest=LongTermMemoryEntitySmokeTest test`
Expected: FAIL because `src/test/java/com/fitness/modules/chat/memory/LongTermMemoryEntitySmokeTest.java` does not exist.

- [ ] **Step 3: Write minimal implementation**

`pom.xml` 增加以下依赖：

```xml
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-agent-framework</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-graph-core</artifactId>
</dependency>
```

新增迁移文件 `src/main/resources/db/migration/V42__create_chat_long_term_memory_table.sql`：

```sql
CREATE TABLE chat_long_term_memory (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    memory_key VARCHAR(100) NOT NULL,
    memory_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    metadata JSONB DEFAULT '{}'::jsonb NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    UNIQUE (user_id, memory_key)
);

CREATE INDEX idx_chat_long_term_memory_user_id ON chat_long_term_memory(user_id);
CREATE INDEX idx_chat_long_term_memory_type ON chat_long_term_memory(memory_type);
```

新增测试文件 `src/test/java/com/fitness/modules/chat/memory/LongTermMemoryEntitySmokeTest.java`，内容与 Step 1 相同。

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -Dtest=LongTermMemoryEntitySmokeTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add pom.xml src/main/resources/db/migration/V42__create_chat_long_term_memory_table.sql src/test/java/com/fitness/modules/chat/memory/LongTermMemoryEntitySmokeTest.java
git commit -m "feat: add react agent foundation dependencies"
```

### Task 2: 实现查询 Tool 与长期记忆服务

**Files:**
- Create: `src/main/java/com/fitness/modules/chat/tools/MembershipQueryTools.java`
- Create: `src/main/java/com/fitness/modules/chat/tools/CourseQueryTools.java`
- Create: `src/main/java/com/fitness/modules/chat/tools/ProductQueryTools.java`
- Create: `src/main/java/com/fitness/modules/chat/tools/CoachQueryTools.java`
- Create: `src/main/java/com/fitness/modules/chat/tools/ProfileQueryTools.java`
- Create: `src/main/java/com/fitness/modules/chat/memory/model/entity/ChatLongTermMemory.java`
- Create: `src/main/java/com/fitness/modules/chat/memory/mapper/ChatLongTermMemoryMapper.java`
- Create: `src/main/java/com/fitness/modules/chat/memory/service/LongTermMemoryService.java`
- Create: `src/main/java/com/fitness/modules/chat/memory/service/impl/LongTermMemoryServiceImpl.java`
- Test: `src/test/java/com/fitness/modules/chat/tools/MembershipQueryToolsTest.java`
- Test: `src/test/java/com/fitness/modules/chat/memory/service/LongTermMemoryServiceTest.java`

- [ ] **Step 1: Write the failing tests**

`src/test/java/com/fitness/modules/chat/tools/MembershipQueryToolsTest.java`

```java
package com.fitness.modules.chat.tools;

import com.fitness.modules.membership.model.vo.MembershipCardVO;
import com.fitness.modules.membership.service.MembershipCardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembershipQueryToolsTest {

    @Mock
    private MembershipCardService membershipCardService;

    @InjectMocks
    private MembershipQueryTools membershipQueryTools;

    @Test
    void listActiveMembershipCardsShouldReturnServiceData() {
        MembershipCardVO card = new MembershipCardVO();
        card.setCardName("月卡");
        when(membershipCardService.listActiveCards()).thenReturn(List.of(card));

        List<MembershipCardVO> result = membershipQueryTools.listActiveMembershipCards();

        assertEquals(1, result.size());
        assertEquals("月卡", result.get(0).getCardName());
    }
}
```

`src/test/java/com/fitness/modules/chat/memory/service/LongTermMemoryServiceTest.java`

```java
package com.fitness.modules.chat.memory.service;

import com.fitness.modules.chat.memory.mapper.ChatLongTermMemoryMapper;
import com.fitness.modules.chat.memory.model.entity.ChatLongTermMemory;
import com.fitness.modules.chat.memory.service.impl.LongTermMemoryServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LongTermMemoryServiceTest {

    @Mock
    private ChatLongTermMemoryMapper mapper;

    @InjectMocks
    private LongTermMemoryServiceImpl service;

    @Test
    void listByUserIdShouldReturnOrderedMemories() {
        ChatLongTermMemory memory = new ChatLongTermMemory();
        memory.setMemoryKey("fitness-goal");
        when(mapper.selectByUserId(1L)).thenReturn(List.of(memory));

        List<ChatLongTermMemory> result = service.listByUserId(1L);

        assertEquals(1, result.size());
        assertEquals("fitness-goal", result.get(0).getMemoryKey());
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn -Dtest=MembershipQueryToolsTest,LongTermMemoryServiceTest test`
Expected: FAIL because Tool classes and long-term memory service classes do not exist.

- [ ] **Step 3: Write minimal implementation**

`MembershipQueryTools.java`

```java
package com.fitness.modules.chat.tools;

import com.fitness.modules.membership.model.vo.MembershipCardVO;
import com.fitness.modules.membership.service.MembershipCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MembershipQueryTools {

    private final MembershipCardService membershipCardService;

    @Tool(description = "查询当前可售会员卡列表")
    public List<MembershipCardVO> listActiveMembershipCards() {
        return membershipCardService.listActiveCards();
    }
}
```

`LongTermMemoryServiceImpl.java`

```java
package com.fitness.modules.chat.memory.service.impl;

import com.fitness.modules.chat.memory.mapper.ChatLongTermMemoryMapper;
import com.fitness.modules.chat.memory.model.entity.ChatLongTermMemory;
import com.fitness.modules.chat.memory.service.LongTermMemoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LongTermMemoryServiceImpl implements LongTermMemoryService {

    private final ChatLongTermMemoryMapper mapper;

    @Override
    public List<ChatLongTermMemory> listByUserId(Long userId) {
        return mapper.selectByUserId(userId);
    }
}
```

其余 Tool 类按相同模式创建，只保留本期需要的查询方法：
- 课程：公开课程列表、课程详情
- 商品：商品列表、商品详情
- 教练：首页教练列表、我的私教
- 档案：当前用户档案

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn -Dtest=MembershipQueryToolsTest,LongTermMemoryServiceTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/fitness/modules/chat/tools src/main/java/com/fitness/modules/chat/memory src/test/java/com/fitness/modules/chat/tools/MembershipQueryToolsTest.java src/test/java/com/fitness/modules/chat/memory/service/LongTermMemoryServiceTest.java
git commit -m "feat: add chat query tools and long term memory service"
```

### Task 3: 实现 ReactAgent Runtime、Redis checkpoint 和 RAG 适配层

**Files:**
- Create: `src/main/java/com/fitness/integration/ai/agent/config/AgentRuntimeConfig.java`
- Create: `src/main/java/com/fitness/modules/chat/agent/JianXiaoZhuAgentPrompts.java`
- Create: `src/main/java/com/fitness/modules/chat/agent/JianXiaoZhuAgentService.java`
- Create: `src/main/java/com/fitness/modules/chat/agent/JianXiaoZhuAgentServiceImpl.java`
- Create: `src/main/java/com/fitness/modules/chat/rag/ChatRagContextService.java`
- Create: `src/main/java/com/fitness/modules/chat/rag/ChatRagContext.java`
- Test: `src/test/java/com/fitness/modules/chat/rag/ChatRagContextServiceTest.java`
- Test: `src/test/java/com/fitness/modules/chat/agent/JianXiaoZhuAgentPromptsTest.java`

- [ ] **Step 1: Write the failing tests**

`src/test/java/com/fitness/modules/chat/rag/ChatRagContextServiceTest.java`

```java
package com.fitness.modules.chat.rag;

import com.fitness.modules.knowledge.model.dto.RAGQueryDTO;
import com.fitness.modules.knowledge.model.vo.RAGSearchResultVO;
import com.fitness.modules.knowledge.service.RAGService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatRagContextServiceTest {

    @Mock
    private RAGService ragService;

    @InjectMocks
    private ChatRagContextService service;

    @Test
    void buildContextShouldUseTopKFive() {
        when(ragService.search(org.mockito.ArgumentMatchers.any())).thenReturn(new RAGSearchResultVO());

        service.buildContext("营业时间");

        ArgumentCaptor<RAGQueryDTO> captor = ArgumentCaptor.forClass(RAGQueryDTO.class);
        verify(ragService).search(captor.capture());
        assertEquals(5, captor.getValue().getTopK());
    }
}
```

`src/test/java/com/fitness/modules/chat/agent/JianXiaoZhuAgentPromptsTest.java`

```java
package com.fitness.modules.chat.agent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JianXiaoZhuAgentPromptsTest {

    @Test
    void systemPromptShouldMentionToolPriority() {
        JianXiaoZhuAgentPrompts prompts = new JianXiaoZhuAgentPrompts();

        assertTrue(prompts.systemPrompt().contains("优先调用工具"));
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `mvn -Dtest=ChatRagContextServiceTest,JianXiaoZhuAgentPromptsTest test`
Expected: FAIL because RAG adapter and Agent prompt classes do not exist.

- [ ] **Step 3: Write minimal implementation**

`AgentRuntimeConfig.java`

```java
package com.fitness.integration.ai.agent.config;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.checkpoint.savers.RedisSaver;
import com.fitness.modules.chat.agent.JianXiaoZhuAgentPrompts;
import com.fitness.modules.chat.tools.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@RequiredArgsConstructor
public class AgentRuntimeConfig {

    private final ChatModel chatModel;
    private final RedisConnectionFactory redisConnectionFactory;
    private final JianXiaoZhuAgentPrompts prompts;
    private final MembershipQueryTools membershipQueryTools;
    private final CourseQueryTools courseQueryTools;
    private final ProductQueryTools productQueryTools;
    private final CoachQueryTools coachQueryTools;
    private final ProfileQueryTools profileQueryTools;

    @Bean
    public RedisSaver redisSaver() {
        return new RedisSaver(redisConnectionFactory);
    }

    @Bean
    public ReactAgent jianXiaoZhuReactAgent(RedisSaver redisSaver) {
        return ReactAgent.builder()
                .name("jian_xiao_zhu_agent")
                .model(chatModel)
                .instruction(prompts.systemPrompt())
                .methodTools(
                        membershipQueryTools,
                        courseQueryTools,
                        productQueryTools,
                        coachQueryTools,
                        profileQueryTools
                )
                .saver(redisSaver)
                .build();
    }
}
```

`JianXiaoZhuAgentPrompts.java`

```java
package com.fitness.modules.chat.agent;

import org.springframework.stereotype.Component;

@Component
public class JianXiaoZhuAgentPrompts {

    public String systemPrompt() {
        return """
                你是会员端“健小助”。
                涉及实时业务数据时优先调用工具，不要编造会员卡、课程、商品、教练信息。
                涉及场馆规则、营业时间、课程须知、健身知识时优先结合知识库上下文回答。
                回答必须使用中文，语气专业、友好、简洁，不输出内部推理。
                """;
    }
}
```

`ChatRagContextService.java` 使用现有 `RAGService`，固定 `topK=5`、`similarityThreshold=0.3`，将返回 chunk 转为上下文字符串。

- [ ] **Step 4: Run tests to verify they pass**

Run: `mvn -Dtest=ChatRagContextServiceTest,JianXiaoZhuAgentPromptsTest test`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/fitness/integration/ai/agent/config src/main/java/com/fitness/modules/chat/agent src/main/java/com/fitness/modules/chat/rag src/test/java/com/fitness/modules/chat/rag/ChatRagContextServiceTest.java src/test/java/com/fitness/modules/chat/agent/JianXiaoZhuAgentPromptsTest.java
git commit -m "feat: add jianxiaozhu react agent runtime"
```

### Task 4: 接管聊天主流程并保留原健身计划能力

**Files:**
- Modify: `src/main/java/com/fitness/modules/chat/service/impl/ChatAssistantServiceImpl.java`
- Modify: `src/main/java/com/fitness/modules/chat/service/impl/ChatContextServiceImpl.java`
- Modify: `src/main/java/com/fitness/modules/chat/prompt/ChatPromptTemplates.java`
- Test: `src/test/java/com/fitness/modules/chat/service/ChatAssistantServiceImplTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.fitness.modules.chat.service;

import com.fitness.modules.chat.agent.JianXiaoZhuAgentService;
import com.fitness.modules.chat.mapper.ChatMessageMapper;
import com.fitness.modules.chat.mapper.ChatSessionMapper;
import com.fitness.modules.chat.model.dto.ChatMessageDTO;
import com.fitness.modules.chat.model.entity.ChatSession;
import com.fitness.modules.chat.service.impl.ChatAssistantServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatAssistantServiceImplTest {

    @Mock
    private ChatSessionMapper chatSessionMapper;
    @Mock
    private ChatMessageMapper chatMessageMapper;
    @Mock
    private com.fitness.modules.chat.service.ChatContextService chatContextService;
    @Mock
    private JianXiaoZhuAgentService jianXiaoZhuAgentService;

    @InjectMocks
    private ChatAssistantServiceImpl service;

    @Test
    void sendMessageShouldDelegateToReactAgent() {
        ChatSession session = new ChatSession();
        session.setId(1L);
        session.setUserId(1L);
        when(chatSessionMapper.selectById(1L)).thenReturn(session);
        when(jianXiaoZhuAgentService.chat(anyLong(), anyLong(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn("会员卡列表");

        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setSessionId(1L);
        dto.setContent("有哪些会员卡");

        service.sendMessage(1L, dto);

        verify(jianXiaoZhuAgentService).chat(1L, 1L, "有哪些会员卡");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -Dtest=ChatAssistantServiceImplTest test`
Expected: FAIL because `JianXiaoZhuAgentService` is not wired into `ChatAssistantServiceImpl`.

- [ ] **Step 3: Write minimal implementation**

将 `ChatAssistantServiceImpl` 的查询聊天主流程改为：

```java
String aiResponse = jianXiaoZhuAgentService.chat(userId, session.getId(), dto.getContent());
```

保留以下能力不变：
- 会话创建、删除、查询
- 消息落库
- 健身计划生成与保存

同时把 `ChatContextServiceImpl` 降级成“消息落库辅助”，不再承担主上下文缓存职责。

- [ ] **Step 4: Run test to verify it passes**

Run: `mvn -Dtest=ChatAssistantServiceImplTest test`
Expected: PASS

- [ ] **Step 5: Run focused regression verification**

Run: `mvn -Dtest=MembershipQueryToolsTest,LongTermMemoryServiceTest,ChatRagContextServiceTest,JianXiaoZhuAgentPromptsTest,ChatAssistantServiceImplTest test`
Expected: PASS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/fitness/modules/chat/service/impl/ChatAssistantServiceImpl.java src/main/java/com/fitness/modules/chat/service/impl/ChatContextServiceImpl.java src/main/java/com/fitness/modules/chat/prompt/ChatPromptTemplates.java src/test/java/com/fitness/modules/chat/service/ChatAssistantServiceImplTest.java
git commit -m "feat: route jianxiaozhu chat through react agent"
```

### Task 5: 运行集成验证并记录结果

**Files:**
- Modify: `docs/superpowers/specs/2026-05-07-jianxiaozhu-react-agent-design.md`

- [ ] **Step 1: Run full targeted verification**

Run: `mvn test -Dtest=LongTermMemoryEntitySmokeTest,MembershipQueryToolsTest,LongTermMemoryServiceTest,ChatRagContextServiceTest,JianXiaoZhuAgentPromptsTest,ChatAssistantServiceImplTest`
Expected: PASS

- [ ] **Step 2: Run application compile verification**

Run: `mvn -DskipTests compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Update spec with implementation status note**

在设计文档末尾增加一段简短实现状态说明：

```markdown
## 实现状态

- ReactAgent 查询链路已接入
- 查询类 Tool 已接入
- Redis checkpoint 已接入
- PostgreSQL 长期记忆已接入
- 写操作 Tool 与 Human-in-the-Loop 待下一轮实现
```

- [ ] **Step 4: Commit**

```bash
git add docs/superpowers/specs/2026-05-07-jianxiaozhu-react-agent-design.md
git commit -m "docs: record react agent foundation status"
```
