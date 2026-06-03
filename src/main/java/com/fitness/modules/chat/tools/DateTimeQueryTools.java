package com.fitness.modules.chat.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class DateTimeQueryTools implements ToolRiskAware {

    @Override
    public ToolRiskLevel getRiskLevel() {
        return ToolRiskLevel.LOW;
    }

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Tool(description = """
            获取当前系统时间。
            当用户询问"现在几点了"、"当前时间"、"现在日期"、"今天几号"等问题时，必须调用此工具。
            返回当前日期和时间。
            """)
    public String getCurrentTime() {
        String currentTime = LocalDateTime.now().format(FORMATTER);
        log.info("\n========== toolmessage ==========\n工具: 当前时间查询\n返回结果:\n{}\n========== toolmessage end ==========\n", currentTime);
        return currentTime;
    }
}