package com.fitness.modules.course.job;

import com.fitness.modules.course.service.CourseSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseSessionScheduler {

    private final CourseSessionService sessionService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyMaintenance() {
        log.info("开始每日课程维护任务...");
        try {
            sessionService.closeExpiredSessions();
            sessionService.generateFutureSessions(1);
            log.info("每日课程维护任务完成");
        } catch (Exception e) {
            log.error("每日课程维护任务失败: {}", e.getMessage(), e);
        }
    }
}
