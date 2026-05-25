package com.fitness.modules.user.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.user.model.vo.CoachVO;
import com.fitness.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/coaches")
@RequiredArgsConstructor
@Tag(name = "教练管理", description = "教练列表查询接口")
public class CoachController {

    private final UserService userService;

    @Operation(summary = "获取教练列表")
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('COACH')")
    public Result<List<CoachVO>> getCoachList() {
        log.info("获取教练列表请求");
        List<CoachVO> coachList = userService.getCoachList();
        return Result.success(coachList);
    }
}
