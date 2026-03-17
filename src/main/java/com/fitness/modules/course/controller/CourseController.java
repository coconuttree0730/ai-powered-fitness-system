package com.fitness.modules.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.result.Result;
import com.fitness.modules.course.model.dto.CourseQueryDTO;
import com.fitness.modules.course.model.vo.CourseVO;
import com.fitness.modules.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 课程查询控制器（公开接口）
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * 获取课程详情
     *
     * @param courseId 课程ID
     * @return 课程详情
     */
    @GetMapping("/{courseId}")
    public Result<CourseVO> getCourseById(@PathVariable Long courseId) {
        log.info("获取课程详情请求: courseId={}", courseId);
        CourseVO courseVO = courseService.getCourseById(courseId);
        return Result.success(courseVO);
    }

    /**
     * 获取公开课程列表
     *
     * @param query 查询条件
     * @return 课程列表
     */
    @GetMapping("/public/list")
    public Result<Page<CourseVO>> getPublicCourseList(CourseQueryDTO query) {
        log.info("获取公开课程列表请求: category={}, keyword={}", query.getCategory(), query.getKeyword());
        Page<CourseVO> page = courseService.getPublicCourseList(query);
        return Result.success(page);
    }
}
