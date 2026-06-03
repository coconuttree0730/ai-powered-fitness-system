package com.fitness.modules.chat.tools;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.course.model.dto.CourseQueryDTO;
import com.fitness.modules.course.model.vo.CourseSessionVO;
import com.fitness.modules.course.service.CourseSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CourseSessionQueryTools implements ToolRiskAware {

    @Override
    public ToolRiskLevel getRiskLevel() {
        return ToolRiskLevel.LOW;
    }

    private final CourseSessionService courseSessionService;

    @Tool(description = """
            查询未来可预约的课程场次列表，包括瑜伽、普拉提、动感单车等课程的具体上课时间。
            当用户询问"明天有什么课"、"下周有什么课"、"这周的课程安排"、"什么时候有瑜伽课"等问题时，必须调用此工具。
            返回课程名称、上课日期、时间、教练、剩余名额等信息。
            支持按课程分类过滤，如"瑜伽"、"有氧"、"力量"等。
            """)
    public List<CourseSessionVO> listUpcomingSessions(String category, Integer days) {
        CourseQueryDTO query = new CourseQueryDTO();
        query.setCategory(category);
        query.setStartDate(LocalDate.now());
        query.setEndDate(LocalDate.now().plusDays(days != null ? days : 7));
        query.setPageNum(1);
        query.setPageSize(10);
        Page<CourseSessionVO> page = courseSessionService.getSessionList(query);
        return page.getRecords();
    }

    @Tool(description = """
            查询指定课程的未来场次安排。
            当用户询问"瑜伽课什么时候上"、"这个课还能约吗"、"瑜伽课最近一场是什么时候"等问题时，必须调用此工具。
            返回该课程未来所有可预约的场次，包括日期、时间、剩余名额。
            """)
    public List<CourseSessionVO> listSessionsByCourse(Long courseId) {
        CourseQueryDTO query = new CourseQueryDTO();
        query.setCourseId(courseId);
        query.setStartDate(LocalDate.now());
        query.setEndDate(LocalDate.now().plusWeeks(2));
        query.setPageNum(1);
        query.setPageSize(10);
        Page<CourseSessionVO> page = courseSessionService.getSessionList(query);
        return page.getRecords();
    }

    @Tool(description = """
            查询指定日期的课程场次安排。
            当用户询问"6月3号有什么课"、"周二有什么课"、"下午有什么课"等问题时，必须调用此工具。
            返回该日期所有课程的场次信息。
            """)
    public List<CourseSessionVO> listSessionsByDate(LocalDate date) {
        CourseQueryDTO query = new CourseQueryDTO();
        query.setStartDate(date);
        query.setEndDate(date);
        query.setPageNum(1);
        query.setPageSize(10);
        Page<CourseSessionVO> page = courseSessionService.getSessionList(query);
        return page.getRecords();
    }
}
