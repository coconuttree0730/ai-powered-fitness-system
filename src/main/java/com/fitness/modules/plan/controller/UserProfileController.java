package com.fitness.modules.plan.controller;

import com.fitness.common.result.Result;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.plan.model.dto.UserProfileDTO;
import com.fitness.modules.plan.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户个人信息控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * 完善个人信息
     *
     * @param dto 个人信息
     * @return 操作结果
     */
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Void> saveProfile(@Valid @RequestBody UserProfileDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("完善个人信息: userId={}, height={}, weight={}, age={}",
                userId, dto.getHeight(), dto.getWeight(), dto.getAge());
        userProfileService.saveProfile(userId, dto);
        return Result.success();
    }

    /**
     * 获取个人信息
     *
     * @return 个人信息
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("获取个人信息: userId={}", userId);
        Map<String, Object> profile = userProfileService.getProfile(userId);
        return Result.success(profile);
    }
}
