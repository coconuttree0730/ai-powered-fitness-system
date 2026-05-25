package com.fitness.integration.security;

import com.fitness.modules.user.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;

/**
 * 自定义权限评估器
 * 实现 Spring Security 的 PermissionEvaluator 接口
 * 支持 hasPermission 表达式
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final PermissionService permissionService;

    /**
     * 评估权限（目标对象版本）
     *
     * @param authentication     认证信息
     * @param targetDomainObject 目标对象
     * @param permission         权限
     * @return true-有权限，false-无权限
     */
    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // 如果目标是null，检查用户是否有指定权限
        if (targetDomainObject == null) {
            return hasPermission(authentication, permission);
        }

        // 获取当前用户ID
        Long userId = getCurrentUserId(authentication);
        if (userId == null) {
            return false;
        }

        // 转换权限为字符串
        String permissionStr = permission != null ? permission.toString() : null;
        if (permissionStr == null || permissionStr.isEmpty()) {
            return false;
        }

        log.debug("检查用户 {} 对目标 {} 的权限: {}", userId, targetDomainObject, permissionStr);

        // 调用PermissionService检查权限
        return permissionService.hasPermission(userId, permissionStr);
    }

    /**
     * 评估权限（目标ID版本）
     *
     * @param authentication 认证信息
     * @param targetId       目标ID
     * @param targetType     目标类型
     * @param permission     权限
     * @return true-有权限，false-无权限
     */
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // 获取当前用户ID
        Long userId = getCurrentUserId(authentication);
        if (userId == null) {
            return false;
        }

        // 转换权限为字符串
        String permissionStr = permission != null ? permission.toString() : null;
        if (permissionStr == null || permissionStr.isEmpty()) {
            return false;
        }

        log.debug("检查用户 {} 对目标 {}({}) 的权限: {}", userId, targetType, targetId, permissionStr);

        // 调用PermissionService检查权限
        return permissionService.hasPermission(userId, permissionStr);
    }

    /**
     * 检查用户是否有指定权限
     *
     * @param authentication 认证信息
     * @param permission     权限
     * @return true-有权限，false-无权限
     */
    private boolean hasPermission(Authentication authentication, Object permission) {
        if (permission == null) {
            return false;
        }

        String permissionStr = permission.toString();

        // 从Authentication中获取权限
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // 检查是否有指定权限
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals(permissionStr)) {
                return true;
            }
        }

        // 获取用户ID，使用PermissionService检查
        Long userId = getCurrentUserId(authentication);
        if (userId != null) {
            return permissionService.hasPermission(userId, permissionStr);
        }

        return false;
    }

    /**
     * 从Authentication中获取当前用户ID
     *
     * @param authentication 认证信息
     * @return 用户ID
     */
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        // 如果Principal是UserDetailsImpl类型
        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getId();
        }

        // 如果Principal是字符串（用户名），需要额外处理
        // 这里简化处理，实际可能需要从Token中解析
        if (principal instanceof String) {
            // 尝试从SecurityUtils获取
            return SecurityUtils.getCurrentUserId();
        }

        return null;
    }
}
