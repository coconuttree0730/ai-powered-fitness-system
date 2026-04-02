package com.fitness.modules.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.knowledge.model.entity.KnowledgeDocument;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KnowledgeDocumentMapper extends BaseMapper<KnowledgeDocument> {
}
