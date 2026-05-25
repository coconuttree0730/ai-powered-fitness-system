package com.fitness.modules.chat.memory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.chat.memory.model.entity.ChatLongTermMemory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatLongTermMemoryMapper extends BaseMapper<ChatLongTermMemory> {
}
