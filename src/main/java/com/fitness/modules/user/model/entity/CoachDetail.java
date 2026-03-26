package com.fitness.modules.user.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 教练详情实体类
 * 对应 coach_detail 表
 */
@Data
@TableName("coach_detail")
public class CoachDetail {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（关联sys_user）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 个人展示图片URL（非头像，用于首页展示）
     */
    @TableField("personal_image_url")
    private String personalImageUrl;

    /**
     * 教练标签（JSON数组存储）
     */
    @TableField("tags")
    private String tags;

    /**
     * 从业年限
     */
    @TableField("work_years")
    private Integer workYears;

    /**
     * 专业领域（JSON数组）
     */
    @TableField("specialties")
    private String specialties;

    /**
     * 教学风格
     */
    @TableField("teaching_style")
    private String teachingStyle;

    /**
     * 教育背景
     */
    @TableField("education")
    private String education;

    /**
     * 培训经历
     */
    @TableField("training")
    private String training;

    /**
     * 语言能力（JSON数组）
     */
    @TableField("languages")
    private String languages;

    /**
     * 个人简介
     */
    @TableField("bio")
    private String bio;

    /**
     * 教学经验
     */
    @TableField("experience")
    private String experience;

    /**
     * 获得荣誉（JSON数组）
     */
    @TableField("honors")
    private String honors;

    /**
     * 紧急联系人信息（JSON对象）
     */
    @TableField("emergency_contact")
    private String emergencyContact;

    /**
     * 资格认证（JSON数组）
     */
    @TableField("certifications")
    private String certifications;

    /**
     * 可用时间段（JSON对象）
     */
    @TableField("availability")
    private String availability;

    /**
     * 学员数量（统计）
     */
    @TableField("student_count")
    private Integer studentCount;

    /**
     * 好评率
     */
    @TableField("rating")
    private String rating;

    /**
     * 软删除标志
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;

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
}
