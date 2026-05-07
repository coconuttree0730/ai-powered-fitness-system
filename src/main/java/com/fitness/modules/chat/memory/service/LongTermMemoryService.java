package com.fitness.modules.chat.memory.service;

import com.fitness.modules.chat.memory.model.entity.ChatLongTermMemory;
import com.fitness.modules.user.model.vo.UserFitnessProfileVO;

import java.util.List;
import java.util.Map;

public interface LongTermMemoryService {

    List<ChatLongTermMemory> listByUserId(Long userId);

    void upsertMemory(Long userId, String memoryKey, String memoryType, String content, Map<String, Object> metadata);

    String buildMemoryContext(Long userId);

    void refreshProfileMemories(Long userId, UserFitnessProfileVO profile);
}
