package com.fitness.modules.chat.memory.service;

import com.fitness.modules.chat.memory.model.entity.ChatLongTermMemory;
import com.fitness.modules.chat.memory.service.impl.LongTermMemoryServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LongTermMemoryServiceTest {

    @Test
    void listByUserIdShouldReturnOrderedMemories() {
        LongTermMemoryServiceImpl service = mock(LongTermMemoryServiceImpl.class);
        ChatLongTermMemory memory = new ChatLongTermMemory();
        memory.setMemoryKey("fitness-goal");
        when(service.listByUserId(1L)).thenReturn(List.of(memory));

        List<ChatLongTermMemory> result = service.listByUserId(1L);

        assertEquals(1, result.size());
        assertEquals("fitness-goal", result.get(0).getMemoryKey());
    }
}
