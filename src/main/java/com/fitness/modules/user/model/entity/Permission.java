package com.fitness.modules.user.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 权限实体类
 * 对应 sys_permission 表
 */
@Data
@TableName("sys_permission")
public class Permission {

    /**
     * 权限ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 权限名称
     */
    @TableField("permission_name")
    private String permissionName;

    /**
     * 权限编码
     */
    @TableField("permission_code")
    private String permissionCode;

    /**
     * 权限描述
     */
    @TableField("description")
    private String description;

    /**
     * 权限类型: MENU-菜单, BUTTON-按钮, API-接口
     */
    @TableField("type")
    private String type;

    /**
     * 父权限ID
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;

    /**
     * 软删除标志
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
