package com.fitness.modules.user.model.vo;

import com.fitness.common.sensitive.Sensitive;
import com.fitness.common.sensitive.SensitiveType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息响应VO
 * 不包含敏感信息（如密码）
 */
@Data
@Schema(description = "用户信息响应")
public class UserVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户 ID", example = "1001")
    private Long id;

    /**
     * 用户名（登录账号）
     */
    @Schema(description = "用户名", example = "fitness_user")
    private String username;

    /**
     * 昵称/姓名（用于展示）
     */
    @Schema(description = "展示昵称", example = "燃脂达人")
    private String nickname;

    /**
     * 手机号
     */
    @Sensitive(SensitiveType.PHONE)
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    /**
     * 邮箱
     */
    @Sensitive(SensitiveType.EMAIL)
    @Schema(description = "邮箱地址", example = "user@example.com")
    private String email;

    /**
     * 头像URL
     */
    @Schema(description = "头像 URL", example = "https://cdn.example.com/avatar.png")
    private String avatar;

    /**
     * 状态: 0-禁用, 1-启用
     */
    @Schema(description = "账号状态，0 表示禁用，1 表示启用", example = "1")
    private Integer status;

    /**
     * 角色列表
     */
    @Schema(description = "角色编码列表", example = "[\"USER\"]")
    private List<String> roles;

    /**
     * 积分
     */
    @Schema(description = "当前积分", example = "120")
    private Integer points;

    /**
     * 余额
     */
    @Schema(description = "账户余额", example = "199.00")
    private java.math.BigDecimal balance;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间，格式为 yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
