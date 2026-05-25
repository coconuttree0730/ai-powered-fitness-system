package com.fitness.modules.knowledge.service;

import com.fitness.modules.knowledge.model.entity.KnowledgeChunk;
import com.fitness.modules.knowledge.model.entity.KnowledgeDocument;

import java.util.List;

public interface DocumentProcessorService {

    String parseFile(String fileUrl, String fileType);

    List<KnowledgeChunk> chunkDocument(KnowledgeDocument document, String content);

    String generateSummary(String content);

    String extractTitle(String content, String fileName);
}
