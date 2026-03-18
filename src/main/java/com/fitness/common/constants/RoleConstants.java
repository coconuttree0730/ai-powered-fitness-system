package com.fitness.common.constants;

/**
 * 角色常量定义类
 * 定义系统中所有角色编码常量
 */
public final class RoleConstants {

    private RoleConstants() {
        // 私有构造函数，防止实例化
    }

    /**
     * 系统管理员
     * 拥有系统的所有权限
     */
    public static final String ROLE_ADMIN = "ADMIN";

    /**
     * 教练
     * 可以管理课程、查看会员信息、制定训练计划
     */
    public static final String ROLE_COACH = "COACH";

    /**
     * 会员
     * 可以预约课程、查看个人数据、使用AI健身计划
     */
    public static final String ROLE_MEMBER = "MEMBER";

    /**
     * 普通用户/游客
     * 基础权限，可以浏览公开信息
     */
    public static final String ROLE_USER = "USER";

//---------------------------------

    /**
     * 带前缀的角色常量（用于Spring Security）
     */
    public static final String ROLE_PREFIX = "ROLE_";

    /**
     * 带前缀的系统管理员
     */
    public static final String ROLE_ADMIN_PREFIXED = ROLE_PREFIX + ROLE_ADMIN;

    /**
     * 带前缀的教练
     */
    public static final String ROLE_COACH_PREFIXED = ROLE_PREFIX + ROLE_COACH;

    /**
     * 带前缀的会员
     */
    public static final String ROLE_MEMBER_PREFIXED = ROLE_PREFIX + ROLE_MEMBER;

    /**
     * 带前缀的普通用户
     */
    public static final String ROLE_USER_PREFIXED = ROLE_PREFIX + ROLE_USER;

    /**
     * 获取带前缀的角色编码
     *
     * @param roleCode 角色编码
     * @return 带前缀的角色编码
     */
    public static String withPrefix(String roleCode) {
        if (roleCode == null || roleCode.isEmpty()) {
            return null;
        }
        if (roleCode.startsWith(ROLE_PREFIX)) {
            return roleCode;
        }
        //拼接返回带前缀
        return ROLE_PREFIX + roleCode;
    }

    /**
     * 移除角色前缀
     *
     * @param roleCode 带前缀的角色编码
     * @return 不带前缀的角色编码
     */
    public static String removePrefix(String roleCode) {
        if (roleCode == null || roleCode.isEmpty()) {
            return null;
        }
        if (roleCode.startsWith(ROLE_PREFIX)) {
            return roleCode.substring(ROLE_PREFIX.length());
        }
        return roleCode;
    }
}
