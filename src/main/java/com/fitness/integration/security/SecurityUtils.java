package com.fitness.integration.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Security 工具类
 * 提供获取当前登录用户信息的方法
 */
@Slf4j
public class SecurityUtils {

    /**
     * 获取当前登录用户的Authentication对象
     *
     * @return Optional包装的Authentication对象
     */
    public static Optional<Authentication> getAuthentication() {
        // 获取当前登录用户（security-线程数据）的Authentication对象
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * 获取当前登录用户详情
     *
     * @return Optional包装的UserDetailsImpl对象
     */
    public static Optional<UserDetailsImpl> getCurrentUser() {
        return getAuthentication()
                .map(Authentication::getPrincipal)
                .filter(principal -> principal instanceof UserDetailsImpl)
                .map(principal -> (UserDetailsImpl) principal);
    }

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID，如果未登录则返回null
     */
    public static Long getCurrentUserId() {
        return getCurrentUser()
                .map(UserDetailsImpl::getId)
                .orElse(null);
    }

    /**
     * 获取当前登录用户名
     *
     * @return 用户名，如果未登录则返回null
     */
    public static String getCurrentUsername() {
        return getAuthentication()
                .map(Authentication::getName)
                .orElse(null);
    }

    /**
     * 获取当前登录用户角色列表
     *
     * @return 角色列表，如果未登录则返回空列表
     */
    public static Collection<String> getCurrentUserRoles() {
        return getCurrentUser()
                .map(userDetails -> userDetails.getAuthorities().stream()
                        .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                        .collect(Collectors.toList()))
                .orElse(java.util.List.of());
    }

    /**
     * 判断当前用户是否已登录
     *
     * @return 是否已登录
     */
    public static boolean isAuthenticated() {
        return getAuthentication()
                .map(Authentication::isAuthenticated)
                .orElse(false);
    }

    /**
     * 判断当前用户是否具有指定角色
     *
     * @param role 角色编码（不需要ROLE_前缀） hasRole("role_admin")
     * @return 是否具有该角色
     */
    public static boolean hasRole(String role) {
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return getCurrentUser()
                .map(userDetails -> userDetails.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals(roleWithPrefix)))
                .orElse(false);
    }

    /**
     * 判断当前用户是否具有__任意 any __指定角色
     *
     * @param roles 角色编码数组（不需要ROLE_前缀）
     * @return 是否具有任意角色
     */
    public static boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断当前用户是否具有__所有 all __指定角色
     *
     * @param roles 角色编码数组（不需要ROLE_前缀）
     * @return 是否具有所有角色
     */
    public static boolean hasAllRoles(String... roles) {
        for (String role : roles) {
            if (!hasRole(role)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断当前用户是否为管理员
     *
     * @return 是否为管理员
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * 获取当前登录用户详情（强制）
     *
     * @return UserDetailsImpl对象
     * @throws IllegalStateException 如果未登录
     */
    public static UserDetailsImpl requireCurrentUser() {
        return getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("当前用户未登录"));
    }

    /**
     * 获取当前登录用户ID（强制）
     *
     * @return 用户ID
     * @throws IllegalStateException 如果未登录
     */
    public static Long requireCurrentUserId() {
        return getCurrentUserId();
    }

    /**
     * 清除当前安全上下文
     */
    public static void clearContext() {
        SecurityContextHolder.clearContext();
        log.debug("Security上下文已清除");
    }
}
