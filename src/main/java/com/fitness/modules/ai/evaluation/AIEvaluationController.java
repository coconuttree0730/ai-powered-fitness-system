package com.fitness.modules.ai.evaluation;

import com.fitness.common.result.Result;
import com.fitness.modules.ai.evaluation.model.dto.AIEvaluationRunDTO;
import com.fitness.modules.ai.evaluation.model.vo.AIEvaluationResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AI评测管理", description = "RAG与Agent评测接口")
@RestController
@RequestMapping("/api/v1/admin/ai/evaluation")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AIEvaluationController {

    private final AIEvaluationService evaluationService;

    @Operation(summary = "运行AI评测")
    @PostMapping("/run")
    public Result<AIEvaluationResultVO> run(@Valid @RequestBody AIEvaluationRunDTO runDTO) {
        return Result.success(evaluationService.run(runDTO));
    }
}
