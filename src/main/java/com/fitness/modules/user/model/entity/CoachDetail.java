package com.fitness.modules.user.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fitness.common.mybatis.JsonbTypeHandler;
import lombok.Data;

import java.time.OffsetDateTime;

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
     * 真实姓名
     */
    @TableField("real_name")
    private String realName;

    /**
     * 性别
     */
    @TableField("gender")
    private String gender;

    /**
     * 年龄
     */
    @TableField("age")
    private Integer age;

    /**
     * 个人展示图片URL（非头像，用于首页展示）
     */
    @TableField("personal_image_url")
    private String personalImageUrl;

    /**
     * 教练标签（JSON数组存储）：
     * 标签数据存入数据库时，会自动转换为JSON数组存储在stgreSQL jsonb类型字段中
     * 使用Object类型接收PostgreSQL jsonb类型
     */
    @TableField(value = "tags", typeHandler = JsonbTypeHandler.class)
    private Object tags;

    /**
     * 从业年限
     */
    @TableField("work_years")
    private Integer workYears;

    /**
     * 专业领域（JSON数组）
     * 使用Object类型接收PostgreSQL jsonb类型
     */
    @TableField(value = "specialties", typeHandler = JsonbTypeHandler.class)
    private Object specialties;

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
     * 使用Object类型接收PostgreSQL jsonb类型
     */
    @TableField(value = "languages", typeHandler = JsonbTypeHandler.class)
    private Object languages;

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
     * 使用Object类型接收PostgreSQL jsonb类型
     */
    @TableField(value = "honors", typeHandler = JsonbTypeHandler.class)
    private Object honors;

    /**
     * 紧急联系人信息（JSON对象）
     * 使用Object类型接收PostgreSQL jsonb类型
     */
    @TableField(value = "emergency_contact", typeHandler = JsonbTypeHandler.class)
    private Object emergencyContact;

    /**
     * 资格认证（JSON数组）
     * 使用Object类型接收PostgreSQL jsonb类型
     */
    @TableField(value = "certifications", typeHandler = JsonbTypeHandler.class)
    private Object certifications;

    /**
     * 可用时间段（JSON对象）
     * 使用Object类型接收PostgreSQL jsonb类型
     */
    @TableField(value = "availability", typeHandler = JsonbTypeHandler.class)
    private Object availability;

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
    @TableLogic(value = "false", delval = "true")
    @TableField("deleted")
    private Boolean deleted;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private OffsetDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private OffsetDateTime updateTime;

    /**
     * 获取标签的JSON字符串
     */
    public String getTagsJson() {
        return convertToJsonString(tags);
    }

    /**
     * 获取专业领域的JSON字符串
     */
    public String getSpecialtiesJson() {
        return convertToJsonString(specialties);
    }

    /**
     * 获取语言能力的JSON字符串
     */
    public String getLanguagesJson() {
        return convertToJsonString(languages);
    }

    /**
     * 获取荣誉的JSON字符串
     */
    public String getHonorsJson() {
        return convertToJsonString(honors);
    }

    /**
     * 获取紧急联系人的JSON字符串
     */
    public String getEmergencyContactJson() {
        return convertToJsonString(emergencyContact);
    }

    /**
     * 获取资格认证的JSON字符串
     */
    public String getCertificationsJson() {
        return convertToJsonString(certifications);
    }

    /**
     * 获取可用时间段的JSON字符串
     */
    public String getAvailabilityJson() {
        return convertToJsonString(availability);
    }

    /**
     * 将Object转换为JSON字符串
     */
    private String convertToJsonString(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        // 如果是PGobject或其他类型，转换为字符串
        return obj.toString();
    }
}
