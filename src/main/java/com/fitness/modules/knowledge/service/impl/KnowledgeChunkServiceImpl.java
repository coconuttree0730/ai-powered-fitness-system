package com.fitness.modules.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.modules.knowledge.mapper.KnowledgeChunkMapper;
import com.fitness.modules.knowledge.model.entity.KnowledgeChunk;
import com.fitness.modules.knowledge.model.vo.KnowledgeChunkVO;
import com.fitness.modules.knowledge.service.KnowledgeChunkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeChunkServiceImpl implements KnowledgeChunkService {

    private final KnowledgeChunkMapper chunkMapper;

    private String toVectorString(float[] embedding) {
        if (embedding == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public List<KnowledgeChunk> listByDocumentId(Long documentId) {
        LambdaQueryWrapper<KnowledgeChunk> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeChunk::getDocumentId, documentId)
               .orderByAsc(KnowledgeChunk::getChunkIndex);
        return chunkMapper.selectList(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveChunks(List<KnowledgeChunk> chunks) {
        for (KnowledgeChunk chunk : chunks) {
            chunkMapper.insert(chunk);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByDocumentId(Long documentId) {
        chunkMapper.deleteByDocumentId(documentId);
    }

    @Override
    public void updateEmbedding(Long chunkId, float[] embedding) {
        String embeddingStr = toVectorString(embedding);
        chunkMapper.updateEmbedding(chunkId, embeddingStr);
    }

    @Override
    public List<KnowledgeChunkVO> vectorSearch(float[] embedding, Integer topK, Long categoryId, Double similarityThreshold) {
        String embeddingStr = toVectorString(embedding);
        return chunkMapper.vectorSearch(embeddingStr, topK, categoryId, similarityThreshold);
    }

    @Override
    public List<KnowledgeChunkVO> keywordSearch(String keyword, Integer topK, Long categoryId) {
        return chunkMapper.keywordSearch(keyword, topK, categoryId);
    }
}
