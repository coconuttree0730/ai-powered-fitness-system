package com.fitness.modules.knowledge.service;

import com.fitness.modules.knowledge.model.dto.RAGQueryDTO;
import com.fitness.modules.knowledge.model.vo.RAGSearchResultVO;

import java.util.List;

public interface RAGService {

    RAGSearchResultVO search(RAGQueryDTO queryDTO);

    String chatWithRAG(String query, Long categoryId);

    List<RAGSearchResultVO.RetrievedChunk> hybridSearch(String query, Integer topK, Long categoryId, Double similarityThreshold);
}
