package com.fitness.modules.ai.evaluation;

import com.fitness.modules.ai.evaluation.model.dto.AIEvaluationRunDTO;
import com.fitness.modules.ai.evaluation.model.vo.AIEvaluationResultVO;

public interface AIEvaluationService {

    AIEvaluationResultVO run(AIEvaluationRunDTO runDTO);
}
