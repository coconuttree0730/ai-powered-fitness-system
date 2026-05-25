package com.fitness.modules.user.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fitness.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 * 对应 sys_user 表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（登录账号，唯一）
     */
    @TableField("username")
    private String username;

    /**
     * 昵称/姓名（用于展示）
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 密码
     */
    @TableField("password")
    private String password;

    /**
     * 手机号
     */
    @TableField("phone")
    private String phone;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;

    /**
     * 头像URL
     */
    @TableField("avatar")
    private String avatar;

    /**
     * 状态: 0-禁用, 1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 积分余额
     */
    @TableField("points")
    private Integer points;

    /**
     * 账户余额
     */
    @TableField("balance")
    private java.math.BigDecimal balance;
}
