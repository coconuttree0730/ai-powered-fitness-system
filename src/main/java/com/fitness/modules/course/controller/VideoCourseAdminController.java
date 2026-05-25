package com.fitness.modules.course.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.result.Result;
import com.fitness.modules.course.model.dto.VideoCourseDTO;
import com.fitness.modules.course.model.dto.VideoCourseQueryDTO;
import com.fitness.modules.course.model.vo.VideoCourseVO;
import com.fitness.modules.course.service.VideoCourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "视频课程管理", description = "视频课程后台管理接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/admin/video-courses")
@RequiredArgsConstructor
public class VideoCourseAdminController {

    private final VideoCourseService videoCourseService;

    @Operation(summary = "创建视频课程")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<Long> create(@Valid @RequestBody VideoCourseDTO dto) {
        log.info("创建视频课程请求: title={}", dto.getTitle());
        Long id = videoCourseService.createVideoCourse(dto);
        return Result.success(id);
    }

    @Operation(summary = "更新视频课程")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody VideoCourseDTO dto) {
        log.info("更新视频课程请求: id={}", id);
        videoCourseService.updateVideoCourse(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除视频课程")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<Void> delete(@PathVariable Long id) {
        log.info("删除视频课程请求: id={}", id);
        videoCourseService.deleteVideoCourse(id);
        return Result.success();
    }

    @Operation(summary = "获取视频课程详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<VideoCourseVO> getById(@PathVariable Long id) {
        return Result.success(videoCourseService.getVideoCourseById(id));
    }

    @Operation(summary = "获取视频课程列表")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<Page<VideoCourseVO>> list(@Valid VideoCourseQueryDTO query) {
        return Result.success(videoCourseService.getVideoCourseList(query));
    }

    @Operation(summary = "获取视频课程分类列表")
    @GetMapping("/categories")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<java.util.List<String>> getCategories() {
        return Result.success(videoCourseService.getCategories());
    }
}
