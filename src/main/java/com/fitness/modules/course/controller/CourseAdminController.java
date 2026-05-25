package com.fitness.modules.course.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.course.model.dto.CourseDTO;
import com.fitness.modules.course.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "课程管理（后台）", description = "后台课程管理接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/courses")
@RequiredArgsConstructor
public class CourseAdminController {

    private final CourseService courseService;

    @Operation(summary = "创建课程")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<Long> createCourse(@Valid @RequestBody CourseDTO dto) {
        log.info("创建课程请求: courseName={}", dto.getCourseName());
        Long courseId = courseService.createCourse(dto);
        return Result.success(courseId);
    }

    @Operation(summary = "更新课程")
    @PutMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<Void> updateCourse(@PathVariable Long courseId, @Valid @RequestBody CourseDTO dto) {
        log.info("更新课程请求: courseId={}", courseId);
        courseService.updateCourse(courseId, dto);
        return Result.success();
    }

    @Operation(summary = "删除课程")
    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<Void> deleteCourse(@PathVariable Long courseId) {
        log.info("删除课程请求: courseId={}", courseId);
        courseService.deleteCourse(courseId);
        return Result.success();
    }
}
