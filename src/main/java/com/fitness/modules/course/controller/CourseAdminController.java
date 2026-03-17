package com.fitness.modules.course.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.course.model.dto.CourseDTO;
import com.fitness.modules.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 课程管理控制器（管理员/教练）
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/courses")
@RequiredArgsConstructor
public class CourseAdminController {

    private final CourseService courseService;

    /**
     * 创建课程
     *
     * @param dto 课程信息
     * @return 创建的课程ID
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<Long> createCourse(@Valid @RequestBody CourseDTO dto) {
        log.info("创建课程请求: courseName={}", dto.getCourseName());
        Long courseId = courseService.createCourse(dto);
        return Result.success(courseId);
    }

    /**
     * 更新课程
     *
     * @param courseId 课程ID
     * @param dto      课程信息
     * @return 操作结果
     */
    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<Void> updateCourse(@PathVariable Long courseId, @Valid @RequestBody CourseDTO dto) {
        log.info("更新课程请求: courseId={}", courseId);
        courseService.updateCourse(courseId, dto);
        return Result.success();
    }

    /**
     * 删除课程
     *
     * @param courseId 课程ID
     * @return 操作结果
     */
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<Void> deleteCourse(@PathVariable Long courseId) {
        log.info("删除课程请求: courseId={}", courseId);
        courseService.deleteCourse(courseId);
        return Result.success();
    }
}
