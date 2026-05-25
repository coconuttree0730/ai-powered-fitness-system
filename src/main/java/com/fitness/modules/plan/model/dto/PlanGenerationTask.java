package com.fitness.modules.plan.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class PlanGenerationTask {

    public enum Status {
        PENDING, PROCESSING, COMPLETED, FAILED
    }

    private String taskId;
    private Status status;
    private Map<String, Object> result;
    private String errorMessage;
    private LocalDateTime createTime;
    private LocalDateTime completeTime;
}
