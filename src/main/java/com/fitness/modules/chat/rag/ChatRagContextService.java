package com.fitness.modules.chat.rag;

import com.fitness.modules.knowledge.model.dto.RAGQueryDTO;
import com.fitness.modules.knowledge.model.vo.RAGSearchResultVO;
import com.fitness.modules.knowledge.service.RAGService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatRagContextService {

    private final RAGService ragService;

    public String buildContext(String query) {
        RAGQueryDTO queryDTO = new RAGQueryDTO();
        queryDTO.setQuery(query);
        queryDTO.setTopK(5);
        queryDTO.setSimilarityThreshold(0.3);

        RAGSearchResultVO result = ragService.search(queryDTO);
        if (result == null || result.getChunks() == null || result.getChunks().isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (RAGSearchResultVO.RetrievedChunk chunk : result.getChunks()) {
            builder.append("来源: ")
                    .append(chunk.getDocumentTitle())
                    .append("\n内容: ")
                    .append(chunk.getContent())
                    .append("\n\n");
        }
        return builder.toString().trim();
    }
}
