package com.fitness.modules.chat.tools;

import com.fitness.modules.knowledge.model.dto.RAGQueryDTO;
import com.fitness.modules.knowledge.model.vo.RAGSearchResultVO;
import com.fitness.modules.knowledge.service.RAGService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RagQueryTool implements ToolRiskAware {

    @Override
    public ToolRiskLevel getRiskLevel() {
        return ToolRiskLevel.LOW;
    }

    private final RAGService ragService;

    @Tool(description = """
            从健身房知识库中检索相关信息。
            
            【何时调用此工具】
            当用户询问以下类型的问题时，必须调用此工具：
            - 健身房地址、位置、营业时间、联系方式
            - 健身知识、训练方法、器械使用技巧
            - 饮食建议、营养搭配、补剂使用
            - 健身计划制定、训练频率安排
            - 减脂/增肌方法、体态矫正
            - 运动损伤预防、恢复建议
            - 任何关于健身房设施、规则、服务的问题
            
            【如何使用】
            将用户的问题作为 query 参数传入，工具会返回知识库中最相关的信息。
            如果返回"未找到相关知识库内容"，则基于你的训练数据回答。
            """)
    public String searchKnowledge(String query) {
        RAGQueryDTO queryDTO = new RAGQueryDTO();
        queryDTO.setQuery(query);
        queryDTO.setTopK(5);
        queryDTO.setSimilarityThreshold(0.3);

        RAGSearchResultVO result = ragService.search(queryDTO);
        if (result == null || result.getChunks() == null || result.getChunks().isEmpty()) {
            String noResult = "未找到相关知识库内容";
            log.info("\n========== toolmessage ==========\n工具: RAG检索\n查询: {}\n返回结果:\n{}\n========== toolmessage end ==========\n", query, noResult);
            return noResult;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < result.getChunks().size(); i++) {
            RAGSearchResultVO.RetrievedChunk chunk = result.getChunks().get(i);
            builder.append("来源: ").append(chunk.getDocumentTitle()).append("\n");
            builder.append("内容: ").append(chunk.getContent()).append("\n\n");
        }
        String resultStr = builder.toString().trim();

        log.info("\n========== toolmessage ==========\n工具: RAG检索\n查询: {}\n检索到 {} 个相关切片\n返回结果:\n{}\n========== toolmessage end ==========\n", 
                query, result.getChunks().size(), resultStr);
        return resultStr;
    }
}
