package com.fitness.modules.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.knowledge.model.entity.KnowledgeChunk;
import com.fitness.modules.knowledge.model.vo.KnowledgeChunkVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KnowledgeChunkMapper extends BaseMapper<KnowledgeChunk> {

    List<KnowledgeChunkVO> vectorSearch(
            @Param("embedding") String embedding,
            @Param("topK") Integer topK,
            @Param("categoryId") Long categoryId,
            @Param("similarityThreshold") Double similarityThreshold
    );

    List<KnowledgeChunkVO> keywordSearch(
            @Param("keyword") String keyword,
            @Param("topK") Integer topK,
            @Param("categoryId") Long categoryId
    );

    void deleteByDocumentId(@Param("documentId") Long documentId);

    void updateEmbedding(
            @Param("id") Long id,
            @Param("embedding") String embedding
    );
}
