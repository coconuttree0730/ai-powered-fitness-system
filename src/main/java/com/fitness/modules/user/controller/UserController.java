package com.fitness.modules.user.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.user.model.vo.UserVO;
import com.fitness.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 * 处理用户信息相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * 获取当前登录用户信息
     * 需要用户已登录
     *
     * @return 用户信息
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public Result<UserVO> getCurrentUserInfo() {
        Long userId = getCurrentUserId();
        log.info("获取当前用户信息, userId: {}", userId);
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

    /**
     * 修改密码
     * 需要用户已登录
     *
     * @param request 包含旧密码和新密码的请求体
     * @return 操作结果
     */
    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> updatePassword(@RequestBody Map<String, String> request) {
        Long userId = getCurrentUserId();
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null || oldPassword.isEmpty()) {
            return Result.error("旧密码不能为空");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return Result.error("新密码不能为空");
        }

        log.info("修改密码请求, userId: {}", userId);
        boolean success = userService.updatePassword(userId, oldPassword, newPassword);
        if (success) {
            return Result.success();
        } else {
            return Result.error("修改密码失败");
        }
    }

    /**
     * 从SecurityContext获取当前用户ID
     *
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        // 从Principal中获取用户ID
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.fitness.integration.security.UserDetailsImpl) {
            return ((com.fitness.integration.security.UserDetailsImpl) principal).getId();
        }
        return null;
    }
}
