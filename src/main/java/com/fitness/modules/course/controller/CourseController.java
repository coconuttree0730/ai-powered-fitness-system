package com.fitness.modules.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.result.Result;
import com.fitness.modules.course.model.dto.CourseQueryDTO;
import com.fitness.modules.course.model.vo.CourseCardVO;
import com.fitness.modules.course.model.vo.CourseCategoryVO;
import com.fitness.modules.course.model.vo.CourseVO;
import com.fitness.modules.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "课程浏览", description = "会员端课程浏览接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @Operation(summary = "获取课程详情")
    @GetMapping("/{courseId}")
    public Result<CourseVO> getCourseById(@PathVariable Long courseId) {
        log.info("获取课程详情请求: courseId={}", courseId);
        CourseVO courseVO = courseService.getCourseById(courseId);
        return Result.success(courseVO);
    }

    @Operation(summary = "获取课程管理列表")
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<Page<CourseVO>> getCourseList(@Valid CourseQueryDTO query) {
        log.info("获取课程列表请求: category={}, keyword={}, courseName={}, coachId={}, dayOfWeek={}",
                query.getCategory(), query.getKeyword(), query.getCourseName(), query.getCoachId(),
                query.getDayOfWeek());
        Page<CourseVO> page = courseService.getCourseList(query);
        return Result.success(page);
    }

    @Operation(summary = "获取公开课程列表")
    @GetMapping("/public/list")
    public Result<Page<CourseVO>> getPublicCourseList(@Valid CourseQueryDTO query) {
        log.info("获取公开课程列表请求: category={}, keyword={}", query.getCategory(), query.getKeyword());
        Page<CourseVO> page = courseService.getPublicCourseList(query);
        return Result.success(page);
    }

    @Operation(summary = "获取课程分类列表")
    @GetMapping("/categories")
    public Result<java.util.List<String>> getCourseCategories() {
        log.info("获取课程分类列表请求");
        java.util.List<String> categories = courseService.getCourseCategories();
        return Result.success(categories);
    }

    @Operation(summary = "获取首页课程体系数据")
    @GetMapping("/homepage/categories")
    public Result<java.util.List<CourseCategoryVO>> getHomePageCourses() {
        log.info("获取首页课程体系数据请求");
        java.util.List<CourseCategoryVO> categories = courseService.getHomePageCourses();
        return Result.success(categories);
    }

    @Operation(summary = "获取首页课程卡片列表")
    @GetMapping("/homepage/cards")
    public Result<java.util.List<CourseCardVO>> getHomePageCourseCards(
            @RequestParam(required = false, defaultValue = "6") Integer limit) {
        log.info("获取首页课程卡片列表请求, limit={}", limit);
        java.util.List<CourseCardVO> cards = courseService.getHomePageCourseCards(limit);
        return Result.success(cards);
    }
}
