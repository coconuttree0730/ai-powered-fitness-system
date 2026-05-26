package com.fitness.modules.course.component;

import com.fitness.modules.course.mapper.CourseSessionMapper;
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
    private final CourseSessionMapper sessionMapper;

    @Override
    public void run(ApplicationArguments args) {
        long count = sessionMapper.selectCount(null);
        if (count > 0) {
            log.info("课程排期已存在({}条), 跳过初始化", count);
            return;
        }
        log.info("首次部署, 自动生成未来2周课程排期...");
        try {
            sessionService.generateFutureSessions(2);
            log.info("课程排期初始化完成");
        } catch (Exception e) {
            log.error("课程排期初始化失败: {}", e.getMessage(), e);
        }
    }
}