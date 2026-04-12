package com.fitness.modules.user.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息响应VO
 * 不包含敏感信息（如密码）
 */
@Data
public class UserVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名（登录账号）
     */
    private String username;

    /**
     * 昵称/姓名（用于展示）
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 角色列表
     */
    private List<String> roles;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
