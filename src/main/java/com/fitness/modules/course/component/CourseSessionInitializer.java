package com.fitness.modules.course.component;

import com.fitness.modules.course.service.CourseSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CourseSessionInitializer implements ApplicationRunner {

    private final CourseSessionService sessionService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("启动时自动生成未来4周课程排期...");
        try {
            sessionService.generateFutureSessions(4);
            log.info("课程排期初始化完成");
        } catch (Exception e) {
            log.error("课程排期初始化失败: {}", e.getMessage(), e);
        }
    }
}