package com.fitness.modules.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.knowledge.model.entity.KnowledgeCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KnowledgeCategoryMapper extends BaseMapper<KnowledgeCategory> {
}
