package com.fitness.modules.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.result.Result;
import com.fitness.modules.course.model.dto.CourseQueryDTO;
import com.fitness.modules.course.model.vo.CourseCardVO;
import com.fitness.modules.course.model.vo.CourseCategoryVO;
import com.fitness.modules.course.model.vo.CourseVO;
import com.fitness.modules.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 课程查询控制器（公开接口）：首页展示接口
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
     * 获取课程列表（管理员）
     *
     * @param query 查询条件
     * @return 课程列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<Page<CourseVO>> getCourseList(CourseQueryDTO query) {
        log.info("获取课程列表请求: category={}, keyword={}, courseName={}, coachId={}, dayOfWeek={}",
                query.getCategory(), query.getKeyword(), query.getCourseName(), query.getCoachId(), query.getDayOfWeek());
        Page<CourseVO> page = courseService.getCourseList(query);
        return Result.success(page);
    }

    /**
     * 获取公开课程列表（游客可看）
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

    /**
     * 获取所有课程分类列表
     *
     * @return 分类列表
     */
    @GetMapping("/categories")
    public Result<java.util.List<String>> getCourseCategories() {
        log.info("获取课程分类列表请求");
        java.util.List<String> categories = courseService.getCourseCategories();
        return Result.success(categories);
    }

    /**
     * 获取首页课程体系数据（按分类分组）
     *
     * @return 课程体系分类列表
     */
    @GetMapping("/homepage/categories")
    public Result<java.util.List<CourseCategoryVO>> getHomePageCourses() {
        log.info("获取首页课程体系数据请求");
        java.util.List<CourseCategoryVO> categories = courseService.getHomePageCourses();
        return Result.success(categories);
    }

    /**
     * 获取首页课程卡片列表
     *
     * @param limit 限制数量（默认6）
     * @return 课程卡片列表
     */
    @GetMapping("/homepage/cards")
    public Result<java.util.List<CourseCardVO>> getHomePageCourseCards(
            @RequestParam(required = false, defaultValue = "6") Integer limit) {
        log.info("获取首页课程卡片列表请求, limit={}", limit);
        java.util.List<CourseCardVO> cards = courseService.getHomePageCourseCards(limit);
        return Result.success(cards);
    }
}
