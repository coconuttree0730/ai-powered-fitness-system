package com.fitness.modules.user.service;

import java.util.List;

/**
 * 权限服务接口
 * 提供用户权限和角色查询功能
 */
public interface PermissionService {

    /**
     * 检查用户是否有特定权限
     *
     * @param userId     用户ID
     * @param permission 权限编码
     * @return true-有权限，false-无权限
     */
    boolean hasPermission(Long userId, String permission);

    /**
     * 获取用户的所有权限列表
     *
     * @param userId 用户ID
     * @return 权限编码列表
     */
    List<String> getUserPermissions(Long userId);

    /**
     * 获取用户的所有角色列表
     *
     * @param userId 用户ID
     * @return 角色编码列表
     */
    List<String> getUserRoles(Long userId);

    /**
     * 检查用户是否有特定角色
     *
     * @param userId   用户ID
     * @param roleCode 角色编码
     * @return true-有角色，false-无角色
     */
    boolean hasRole(Long userId, String roleCode);

    /**
     * 检查用户是否有任意一个指定角色
     *
     * @param userId    用户ID
     * @param roleCodes 角色编码列表
     * @return true-有任意一个角色，false-都没有
     */
    boolean hasAnyRole(Long userId, String... roleCodes);

    /**
     * 检查用户是否拥有所有指定角色
     *
     * @param userId    用户ID
     * @param roleCodes 角色编码列表
     * @return true-拥有所有角色，false-缺少某个角色
     */
    boolean hasAllRoles(Long userId, String... roleCodes);
}
