package com.fitness.modules.knowledge.service;

import com.fitness.modules.knowledge.model.entity.KnowledgeChunk;
import com.fitness.modules.knowledge.model.vo.KnowledgeChunkVO;

import java.util.List;

public interface KnowledgeChunkService {

    List<KnowledgeChunk> listByDocumentId(Long documentId);

    void saveChunks(List<KnowledgeChunk> chunks);

    void deleteByDocumentId(Long documentId);

    void updateEmbedding(Long chunkId, float[] embedding);

    List<KnowledgeChunkVO> vectorSearch(float[] embedding, Integer topK, Long categoryId, Double similarityThreshold);

    List<KnowledgeChunkVO> keywordSearch(String keyword, Integer topK, Long categoryId);
}
