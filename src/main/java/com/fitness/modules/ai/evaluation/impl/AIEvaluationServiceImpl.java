package com.fitness.modules.ai.evaluation.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.ai.evaluation.AIEvaluationService;
import com.fitness.modules.ai.evaluation.model.EvaluationCase;
import com.fitness.modules.ai.evaluation.model.dto.AIEvaluationRunDTO;
import com.fitness.modules.ai.evaluation.model.vo.AIEvaluationCaseResultVO;
import com.fitness.modules.ai.evaluation.model.vo.AIEvaluationResultVO;
import com.fitness.modules.knowledge.model.dto.RAGDebugQueryDTO;
import com.fitness.modules.knowledge.model.vo.RAGDebugChunkVO;
import com.fitness.modules.knowledge.model.vo.RAGDebugResultVO;
import com.fitness.modules.knowledge.service.RAGService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIEvaluationServiceImpl implements AIEvaluationService {

    private static final String CASES_PATH = "evaluation/rag-agent-eval-cases.json";

    private final RAGService ragService;
    private final ObjectMapper objectMapper;

    @Override
    public AIEvaluationResultVO run(AIEvaluationRunDTO runDTO) {
        List<EvaluationCase> cases = loadCases();
        log.info("AI evaluation started, cases={}, selectedCaseIds={}", cases.size(), runDTO.getCaseIds());
        Set<String> selectedCaseIds = runDTO.getCaseIds() == null
                ? Collections.emptySet()
                : Set.copyOf(runDTO.getCaseIds());
        if (!selectedCaseIds.isEmpty()) {
            cases = cases.stream()
                    .filter(evaluationCase -> selectedCaseIds.contains(evaluationCase.getId()))
                    .collect(Collectors.toList());
        }

        List<AIEvaluationCaseResultVO> results = cases.stream()
                .map(evaluationCase -> runCase(evaluationCase, runDTO))
                .collect(Collectors.toList());

        int passed = (int) results.stream().filter(AIEvaluationCaseResultVO::getPassed).count();
        long averageRetrievalTime = Math.round(results.stream()
                .mapToLong(result -> result.getRetrievalTimeMs() == null ? 0L : result.getRetrievalTimeMs())
                .average()
                .orElse(0));
        long averageTotalTime = Math.round(results.stream()
                .mapToLong(result -> result.getTotalTimeMs() == null ? 0L : result.getTotalTimeMs())
                .average()
                .orElse(0));

        AIEvaluationResultVO result = new AIEvaluationResultVO();
        result.setTotal(results.size());
        result.setPassed(passed);
        result.setHitRate(results.isEmpty() ? 0 : (double) passed / results.size());
        result.setAverageRetrievalTimeMs(averageRetrievalTime);
        result.setAverageTotalTimeMs(averageTotalTime);
        result.setCases(results);
        log.info("AI evaluation finished, total={}, passed={}, hitRate={}",
                result.getTotal(), result.getPassed(), result.getHitRate());
        return result;
    }

    private AIEvaluationCaseResultVO runCase(EvaluationCase evaluationCase, AIEvaluationRunDTO runDTO) {
        long startTime = System.currentTimeMillis();
        RAGDebugQueryDTO queryDTO = new RAGDebugQueryDTO();
        queryDTO.setQuery(evaluationCase.getQuestion());
        queryDTO.setTopK(runDTO.getTopK());
        queryDTO.setSimilarityThreshold(runDTO.getSimilarityThreshold());
        queryDTO.setCategoryCode(evaluationCase.getCategoryCode());
        RAGDebugResultVO debugResult = ragService.debugSearch(queryDTO);

        List<RAGDebugChunkVO> chunks = debugResult.getMergedChunks() == null
                ? Collections.emptyList()
                : debugResult.getMergedChunks();
        String topDocumentTitle = chunks.isEmpty() ? null : chunks.get(0).getDocumentTitle();
        boolean matchedDocument = matchesDocument(chunks, evaluationCase.getExpectedDocumentTitle());
        List<String> matchedKeywords = matchedKeywords(chunks, evaluationCase.getExpectedKeywords());
        boolean matchedKeyword = !matchedKeywords.isEmpty();

        AIEvaluationCaseResultVO result = new AIEvaluationCaseResultVO();
        result.setId(evaluationCase.getId());
        result.setQuestion(evaluationCase.getQuestion());
        result.setMatchedDocument(matchedDocument);
        result.setMatchedKeyword(matchedKeyword);
        result.setPassed(matchedDocument || matchedKeyword);
        result.setTopDocumentTitle(topDocumentTitle);
        result.setMatchedKeywords(matchedKeywords);
        result.setRetrievalTimeMs(debugResult.getRetrievalTimeMs());
        result.setTotalTimeMs(System.currentTimeMillis() - startTime);
        return result;
    }

    private List<EvaluationCase> loadCases() {
        try {
            return objectMapper.readValue(
                    new ClassPathResource(CASES_PATH).getInputStream(),
                    new TypeReference<>() {
                    });
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "读取AI评测用例失败");
        }
    }

    private boolean matchesDocument(List<RAGDebugChunkVO> chunks, String expectedDocumentTitle) {
        if (expectedDocumentTitle == null || expectedDocumentTitle.isBlank()) {
            return false;
        }
        return chunks.stream()
                .map(RAGDebugChunkVO::getDocumentTitle)
                .filter(title -> title != null)
                .anyMatch(title -> title.contains(expectedDocumentTitle));
    }

    private List<String> matchedKeywords(List<RAGDebugChunkVO> chunks, List<String> expectedKeywords) {
        if (expectedKeywords == null || expectedKeywords.isEmpty()) {
            return Collections.emptyList();
        }
        String combinedContent = chunks.stream()
                .map(RAGDebugChunkVO::getContent)
                .filter(content -> content != null)
                .collect(Collectors.joining("\n"));
        return expectedKeywords.stream()
                .filter(keyword -> combinedContent.contains(keyword))
                .collect(Collectors.toList());
    }
}
