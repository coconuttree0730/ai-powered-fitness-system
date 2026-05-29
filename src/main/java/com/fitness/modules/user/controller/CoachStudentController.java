package com.fitness.modules.user.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.user.model.vo.CoachStudentVO;
import com.fitness.modules.user.service.CoachStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "教练学员", description = "教练学员管理接口")
@RestController
@RequestMapping("/api/v1/coach/students")
@RequiredArgsConstructor
@PreAuthorize("hasRole('COACH')")
public class CoachStudentController {

    private final CoachStudentService coachStudentService;

    @Operation(summary = "获取我的学员列表")
    @GetMapping("/my")
    public Result<List<CoachStudentVO>> listMyStudents() {
        Long coachId = SecurityUtils.getCurrentUserId();
        return Result.success(coachStudentService.getMyStudents(coachId));
    }

    @Operation(summary = "获取指定学员的绑定信息")
    @GetMapping("/binding")
    public Result<CoachStudentVO> getStudentBinding(@RequestParam Long memberId) {
        Long coachId = SecurityUtils.getCurrentUserId();
        return Result.success(coachStudentService.getBinding(memberId, coachId));
    }
}