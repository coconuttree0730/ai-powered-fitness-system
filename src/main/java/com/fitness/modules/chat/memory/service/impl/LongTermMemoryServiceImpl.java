package com.fitness.modules.chat.memory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.modules.chat.memory.mapper.ChatLongTermMemoryMapper;
import com.fitness.modules.chat.memory.model.entity.ChatLongTermMemory;
import com.fitness.modules.chat.memory.service.LongTermMemoryService;
import com.fitness.modules.user.model.vo.UserFitnessProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LongTermMemoryServiceImpl implements LongTermMemoryService {

    private final ChatLongTermMemoryMapper mapper;

    @Override
    public List<ChatLongTermMemory> listByUserId(Long userId) {
        return mapper.selectList(new LambdaQueryWrapper<ChatLongTermMemory>()
                .eq(ChatLongTermMemory::getUserId, userId)
                .orderByAsc(ChatLongTermMemory::getMemoryType)
                .orderByAsc(ChatLongTermMemory::getMemoryKey));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void upsertMemory(Long userId, String memoryKey, String memoryType, String content, Map<String, Object> metadata) {
        ChatLongTermMemory existing = mapper.selectOne(new LambdaQueryWrapper<ChatLongTermMemory>()
                .eq(ChatLongTermMemory::getUserId, userId)
                .eq(ChatLongTermMemory::getMemoryKey, memoryKey)
                .last("LIMIT 1"));

        if (existing == null) {
            ChatLongTermMemory memory = new ChatLongTermMemory();
            memory.setUserId(userId);
            memory.setMemoryKey(memoryKey);
            memory.setMemoryType(memoryType);
            memory.setContent(content);
            memory.setMetadata(metadata);
            memory.setCreateTime(LocalDateTime.now());
            memory.setUpdateTime(LocalDateTime.now());
            mapper.insert(memory);
            return;
        }

        existing.setMemoryType(memoryType);
        existing.setContent(content);
        existing.setMetadata(metadata);
        existing.setUpdateTime(LocalDateTime.now());
        mapper.updateById(existing);
    }

    @Override
    public String buildMemoryContext(Long userId) {
        List<ChatLongTermMemory> memories = listByUserId(userId);
        if (memories.isEmpty()) {
            return "";
        }
        return memories.stream()
                .map(memory -> "- " + memory.getContent())
                .collect(Collectors.joining("\n"));
    }

    @Override
    public void refreshProfileMemories(Long userId, UserFitnessProfileVO profile) {
        if (profile == null) {
            return;
        }

        if (profile.getFitnessGoal() != null) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", "profile");
            upsertMemory(userId, "fitness-goal", "PROFILE", "用户健身目标是" + profile.getFitnessGoal(), metadata);
        }

        if (profile.getExperience() != null) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", "profile");
            upsertMemory(userId, "fitness-experience", "PROFILE", "用户健身经验等级是" + profile.getExperience(), metadata);
        }
    }
}
