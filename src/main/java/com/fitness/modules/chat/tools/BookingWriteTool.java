package com.fitness.modules.chat.tools;

import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.booking.model.dto.BookingDTO;
import com.fitness.modules.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * 课程预约写工具
 * 需要用户确认后才能执行（HITL）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingWriteTool implements ToolRiskAware {

    private final BookingService bookingService;

    @Override
    public ToolRiskLevel getRiskLevel() {
        return ToolRiskLevel.MEDIUM;
    }

    @Tool(description = """
            登记用户参加课程。当用户明确表示想参加某个课程时，调用此工具。
            参数 sessionId 来自 CourseSessionQueryTools 查询结果中的 id 字段。
            """)
    public String bookCourseSession(Long sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return "预约失败：用户未登录";
        }

        try {
            BookingDTO dto = new BookingDTO();
            dto.setSessionId(sessionId);

            bookingService.createBooking(userId, dto);
            log.info("Booking created: userId={}, sessionId={}", userId, sessionId);
            return "预约成功！请准时参加课程。";
        } catch (Exception e) {
            log.warn("Booking failed: userId={}, sessionId={}, error={}", userId, sessionId, e.getMessage());
            return "预约失败：" + e.getMessage();
        }
    }
}
