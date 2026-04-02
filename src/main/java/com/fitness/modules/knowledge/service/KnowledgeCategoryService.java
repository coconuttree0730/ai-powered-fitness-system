package com.fitness.modules.knowledge.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.knowledge.model.dto.KnowledgeCategoryDTO;
import com.fitness.modules.knowledge.model.entity.KnowledgeCategory;
import com.fitness.modules.knowledge.model.vo.KnowledgeCategoryVO;

import java.util.List;

public interface KnowledgeCategoryService {

    List<KnowledgeCategoryVO> listAll();

    KnowledgeCategoryVO getById(Long id);

    Long create(KnowledgeCategoryDTO dto);

    void update(KnowledgeCategoryDTO dto);

    void delete(Long id);

    KnowledgeCategory getEntityById(Long id);
}
