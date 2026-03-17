package com.fitness.modules.user.controller;

import com.fitness.common.constants.RoleConstants;
import com.fitness.common.result.Result;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.user.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 权限控制器
 * 提供权限查询相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 获取当前用户的权限列表
     * 需要用户已登录
     *
     * @return 权限列表
     */
    @GetMapping("/permissions/me")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> getCurrentUserPermissions() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("获取当前用户权限, userId: {}", userId);

        // 获取用户权限
        List<String> permissions = permissionService.getUserPermissions(userId);

        // 获取用户角色
        List<String> roles = permissionService.getUserRoles(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("permissions", permissions);
        result.put("roles", roles);

        return Result.success(result);
    }

    /**
     * 获取所有角色列表
     * 仅管理员可访问
     *
     * @return 角色列表
     */
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Map<String, String>>> getAllRoles() {
        log.info("获取所有角色列表");

        // 定义系统角色列表
        List<Map<String, String>> roles = Arrays.asList(
                createRoleInfo(RoleConstants.ROLE_ADMIN, "系统管理员", "拥有系统的所有权限"),
                createRoleInfo(RoleConstants.ROLE_COACH, "教练", "可以管理课程、查看会员信息、制定训练计划"),
                createRoleInfo(RoleConstants.ROLE_MEMBER, "会员", "可以预约课程、查看个人数据、使用AI健身计划"),
                createRoleInfo(RoleConstants.ROLE_USER, "普通用户", "基础权限，可以浏览公开信息")
        );

        return Result.success(roles);
    }

    /**
     * 获取当前用户的角色信息
     * 需要用户已登录
     *
     * @return 角色信息
     */
    @GetMapping("/roles/me")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> getCurrentUserRoles() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("获取当前用户角色, userId: {}", userId);

        // 获取用户角色
        List<String> roles = permissionService.getUserRoles(userId);

        // 检查是否拥有特定角色
        boolean isAdmin = permissionService.hasRole(userId, RoleConstants.ROLE_ADMIN);
        boolean isCoach = permissionService.hasRole(userId, RoleConstants.ROLE_COACH);
        boolean isMember = permissionService.hasRole(userId, RoleConstants.ROLE_MEMBER);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("roles", roles);
        result.put("isAdmin", isAdmin);
        result.put("isCoach", isCoach);
        result.put("isMember", isMember);

        return Result.success(result);
    }

    /**
     * 创建角色信息Map
     *
     * @param code        角色编码
     * @param name        角色名称
     * @param description 角色描述
     * @return 角色信息Map
     */
    private Map<String, String> createRoleInfo(String code, String name, String description) {
        Map<String, String> role = new HashMap<>();
        role.put("code", code);
        role.put("name", name);
        role.put("description", description);
        return role;
    }
}
