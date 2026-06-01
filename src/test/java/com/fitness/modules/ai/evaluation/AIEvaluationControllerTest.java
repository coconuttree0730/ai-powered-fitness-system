package com.fitness.modules.ai.evaluation;

import com.fitness.common.result.Result;
import com.fitness.modules.ai.evaluation.model.dto.AIEvaluationRunDTO;
import com.fitness.modules.ai.evaluation.model.vo.AIEvaluationResultVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AIEvaluationControllerTest {

    @Test
    void runShouldReturnWrappedServiceResult() {
        AIEvaluationService evaluationService = mock(AIEvaluationService.class);
        AIEvaluationController controller = new AIEvaluationController(evaluationService);
        AIEvaluationRunDTO runDTO = new AIEvaluationRunDTO();
        AIEvaluationResultVO evaluationResult = new AIEvaluationResultVO();
        when(evaluationService.run(runDTO)).thenReturn(evaluationResult);

        Result<AIEvaluationResultVO> result = controller.run(runDTO);

        assertSame(evaluationResult, result.getData());
    }
}
