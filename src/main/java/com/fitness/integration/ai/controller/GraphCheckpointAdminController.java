package com.fitness.integration.ai.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.ai.model.vo.GraphCheckpointDetailVO;
import com.fitness.integration.ai.service.GraphCheckpointDebugService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/ai/checkpoints")
@RequiredArgsConstructor
public class GraphCheckpointAdminController {

    private final GraphCheckpointDebugService graphCheckpointDebugService;

    @GetMapping("/{threadId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<GraphCheckpointDetailVO> getCheckpointDetail(@PathVariable String threadId) {
        log.info("查看 graph checkpoint: threadId={}", threadId);
        return Result.success(graphCheckpointDebugService.getThreadCheckpointDetail(threadId));
    }

    @PostMapping("/decode")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<GraphCheckpointDetailVO> decodeCheckpointContent(
            @Valid @RequestBody DecodeCheckpointRequest request) {
        log.info("解码 graph checkpoint 原始内容");
        return Result.success(graphCheckpointDebugService.decodeCheckpointContent(request.getContent()));
    }

    @Data
    public static class DecodeCheckpointRequest {

        @NotBlank(message = "content 不能为空")
        private String content;
    }
}
