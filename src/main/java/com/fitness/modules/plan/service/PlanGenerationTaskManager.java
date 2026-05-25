package com.fitness.modules.plan.service;

import com.fitness.modules.plan.model.dto.PlanGenerationTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class PlanGenerationTaskManager {

    private final ConcurrentHashMap<String, PlanGenerationTask> tasks = new ConcurrentHashMap<>();

    private static final long TASK_EXPIRE_MINUTES = 30;

    public PlanGenerationTask createTask() {
        String taskId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        PlanGenerationTask task = new PlanGenerationTask();
        task.setTaskId(taskId);
        task.setStatus(PlanGenerationTask.Status.PENDING);
        task.setCreateTime(LocalDateTime.now());
        tasks.put(taskId, task);
        log.info("创建计划生成任务: taskId={}", taskId);
        return task;
    }

    public void markProcessing(String taskId) {
        PlanGenerationTask task = tasks.get(taskId);
        if (task != null) {
            task.setStatus(PlanGenerationTask.Status.PROCESSING);
        }
    }

    public void markCompleted(String taskId, Map<String, Object> result) {
        PlanGenerationTask task = tasks.get(taskId);
        if (task != null) {
            task.setStatus(PlanGenerationTask.Status.COMPLETED);
            task.setResult(result);
            task.setCompleteTime(LocalDateTime.now());
            log.info("计划生成任务完成: taskId={}", taskId);
        }
    }

    public void markFailed(String taskId, String errorMessage) {
        PlanGenerationTask task = tasks.get(taskId);
        if (task != null) {
            task.setStatus(PlanGenerationTask.Status.FAILED);
            task.setErrorMessage(errorMessage);
            task.setCompleteTime(LocalDateTime.now());
            log.error("计划生成任务失败: taskId={}, error={}", taskId, errorMessage);
        }
    }

    public PlanGenerationTask getTask(String taskId) {
        return tasks.get(taskId);
    }

    public void cleanupExpiredTasks() {
        LocalDateTime expireTime = LocalDateTime.now().minusMinutes(TASK_EXPIRE_MINUTES);
        tasks.entrySet().removeIf(entry ->
            entry.getValue().getCreateTime() != null &&
            entry.getValue().getCreateTime().isBefore(expireTime)
        );
    }
}
