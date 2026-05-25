package com.fitness.modules.chat.tools;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.course.model.dto.CourseQueryDTO;
import com.fitness.modules.course.model.vo.CourseVO;
import com.fitness.modules.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CourseQueryTools {

    private final CourseService courseService;

    @Tool(description = """
            查询健身场馆的公开课程列表，包括瑜伽、普拉提、有氧操、力量训练等团课。
            当用户询问"有哪些课程"、"有什么课"、"课程表"、"团课安排"、"查看课程"等问题时，必须调用此工具。
            支持按分类过滤，如"瑜伽"、"有氧"、"力量"等。
            返回课程名称、时间、教练、剩余名额等信息。
            """)
    public List<CourseVO> listCourses(String category) {
        CourseQueryDTO queryDTO = new CourseQueryDTO();
        queryDTO.setCategory(category);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(10);
        Page<CourseVO> page = courseService.getPublicCourseList(queryDTO);
        return page.getRecords();
    }

    @Tool(description = """
            查询指定课程的详细信息。
            当用户询问某个具体课程的内容、时间、教练、难度、适合人群等问题时，必须调用此工具。
            例如"瑜伽课什么时候上"、"这个课程适合初学者吗"、"普拉提课的内容是什么"等。
            """)
    public CourseVO getCourseDetail(Long courseId) {
        return courseService.getCourseById(courseId);
    }
}
