package com.fitness.modules.user.service.impl;

import com.fitness.modules.user.mapper.PermissionMapper;
import com.fitness.modules.user.mapper.UserMapper;
import com.fitness.modules.user.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final UserMapper userMapper;
    private final PermissionMapper permissionMapper;

    /**
     * 缓存名称定义
     */
    private static final String CACHE_NAME_PERMISSIONS = "user:permissions";
    private static final String CACHE_NAME_ROLES = "user:roles";

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, String permission) {
        if (userId == null || permission == null || permission.isEmpty()) {
            return false;
        }

        // 获取用户所有权限
        List<String> permissions = getUserPermissions(userId);

        // 检查是否包含指定权限
        return permissions.contains(permission);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_NAME_PERMISSIONS, key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<String> getUserPermissions(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        log.debug("查询用户权限, userId: {}", userId);

        // 从数据库查询用户权限
        List<String> permissions = permissionMapper.selectPermissionsByUserId(userId);

        // 如果权限为空，返回空列表
        if (permissions == null) {
            return Collections.emptyList();
        }

        log.debug("用户 {} 的权限: {}", userId, permissions);
        return permissions;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CACHE_NAME_ROLES, key = "#userId", unless = "#result == null || #result.isEmpty()")
    public List<String> getUserRoles(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        log.debug("查询用户角色, userId: {}", userId);

        // 从数据库查询用户角色编码
        List<String> roles = userMapper.selectRoleCodesByUserId(userId);

        // 如果角色为空，默认赋予普通用户角色
        if (roles == null || roles.isEmpty()) {
            roles = Collections.singletonList("USER");
        }

        log.debug("用户 {} 的角色: {}", userId, roles);
        return roles;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasRole(Long userId, String roleCode) {
        if (userId == null || roleCode == null || roleCode.isEmpty()) {
            return false;
        }

        // 获取用户所有角色
        List<String> roles = getUserRoles(userId);

        // 标准化角色编码（移除ROLE_前缀进行比较）
        String normalizedRole = roleCode.startsWith("ROLE_") ? roleCode.substring(5) : roleCode;

        // 检查是否包含指定角色
        return roles.stream()
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .anyMatch(role -> role.equalsIgnoreCase(normalizedRole));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAnyRole(Long userId, String... roleCodes) {
        if (userId == null || roleCodes == null || roleCodes.length == 0) {
            return false;
        }

        // 获取用户所有角色
        Set<String> userRoles = new HashSet<>(getUserRoles(userId));

        // 标准化用户角色编码
        Set<String> normalizedUserRoles = new HashSet<>();
        for (String role : userRoles) {
            normalizedUserRoles.add(role.startsWith("ROLE_") ? role.substring(5) : role);
        }

        // 检查是否有任意一个角色匹配
        return Arrays.stream(roleCodes)
                .anyMatch(roleCode -> {
                    String normalizedRole = roleCode.startsWith("ROLE_") ? roleCode.substring(5) : roleCode;
                    return normalizedUserRoles.contains(normalizedRole);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAllRoles(Long userId, String... roleCodes) {
        if (userId == null || roleCodes == null || roleCodes.length == 0) {
            return false;
        }

        // 获取用户所有角色
        Set<String> userRoles = new HashSet<>(getUserRoles(userId));

        // 标准化用户角色编码
        Set<String> normalizedUserRoles = new HashSet<>();
        for (String role : userRoles) {
            normalizedUserRoles.add(role.startsWith("ROLE_") ? role.substring(5) : role);
        }

        // 检查是否拥有所有指定角色
        return Arrays.stream(roleCodes)
                .allMatch(roleCode -> {
                    String normalizedRole = roleCode.startsWith("ROLE_") ? roleCode.substring(5) : roleCode;
                    return normalizedUserRoles.contains(normalizedRole);
                });
    }
}
