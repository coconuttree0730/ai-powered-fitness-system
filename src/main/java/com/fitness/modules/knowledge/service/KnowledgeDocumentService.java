package com.fitness.modules.knowledge.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.knowledge.model.dto.KnowledgeDocumentDTO;
import com.fitness.modules.knowledge.model.dto.KnowledgeDocumentQueryDTO;
import com.fitness.modules.knowledge.model.entity.KnowledgeDocument;
import com.fitness.modules.knowledge.model.vo.KnowledgeDocumentDetailVO;
import com.fitness.modules.knowledge.model.vo.KnowledgeDocumentVO;
import org.springframework.web.multipart.MultipartFile;

public interface KnowledgeDocumentService {

    Page<KnowledgeDocumentVO> page(KnowledgeDocumentQueryDTO queryDTO);

    KnowledgeDocumentDetailVO getDetailById(Long id);

    Long create(KnowledgeDocumentDTO dto);

    Long uploadDocument(MultipartFile file, String title, String categoryCode);

    void update(KnowledgeDocumentDTO dto);

    void delete(Long id);

    void publish(Long id);

    void archive(Long id);

    void reindex(Long id);

    KnowledgeDocument getEntityById(Long id);

    void updateChunkCount(Long documentId, Integer chunkCount);
}
