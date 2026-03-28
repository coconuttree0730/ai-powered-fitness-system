package com.fitness.modules.user.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.user.model.dto.UserFitnessProfileDTO;
import com.fitness.modules.user.model.vo.UserFitnessProfileVO;
import com.fitness.modules.user.service.UserFitnessProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户健身档案控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Validated
public class UserFitnessProfileController {

    private final UserFitnessProfileService userFitnessProfileService;

    /**
     * 获取当前用户的健身档案
     *
     * @return 健身档案信息
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Result<UserFitnessProfileVO> getProfile() {
        Long userId = getCurrentUserId();
        log.info("获取用户健身档案, userId: {}", userId);
        UserFitnessProfileVO profile = userFitnessProfileService.getProfile(userId);
        return Result.success(profile);
    }

    /**
     * 创建或更新当前用户的健身档案
     *
     * @param dto 健身档案DTO
     * @return 更新后的健身档案
     */
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public Result<UserFitnessProfileVO> updateProfile(@Valid @RequestBody UserFitnessProfileDTO dto) {
        Long userId = getCurrentUserId();
        log.info("更新用户健身档案, userId: {}, height: {}, weight: {}, age: {}, experience: {}, fitnessGoal: {}",
                userId, dto.getHeight(), dto.getWeight(), dto.getAge(), dto.getExperience(), dto.getFitnessGoal());
        UserFitnessProfileVO profile = userFitnessProfileService.saveOrUpdateProfile(userId, dto);
        return Result.success(profile);
    }

    /**
     * 检查当前用户的健身档案是否已完善
     *
     * @return true-已完善，false-未完善
     */
    @GetMapping("/complete")
    @PreAuthorize("isAuthenticated()")
    public Result<Boolean> isProfileComplete() {
        Long userId = getCurrentUserId();
        log.info("检查用户健身档案是否完善, userId: {}", userId);
        boolean complete = userFitnessProfileService.isProfileComplete(userId);
        return Result.success(complete);
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.fitness.integration.security.UserDetailsImpl) {
            com.fitness.integration.security.UserDetailsImpl userDetails =
                    (com.fitness.integration.security.UserDetailsImpl) authentication.getPrincipal();
            return userDetails.getId();
        }
        throw new IllegalStateException("无法获取当前用户ID");
    }
}
