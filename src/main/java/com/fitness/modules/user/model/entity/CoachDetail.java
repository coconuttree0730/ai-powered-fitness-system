package com.fitness.modules.user.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fitness.common.model.entity.BaseEntity;
import com.fitness.common.mybatis.JsonbTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 教练详情实体类
 * 对应 coach_detail 表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coach_detail")
public class CoachDetail extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String realName;

    private String gender;

    private Integer age;

    private String personalImageUrl;

    @TableField(value = "tags", typeHandler = JsonbTypeHandler.class)
    private Object tags;

    private Integer workYears;

    @TableField(value = "specialties", typeHandler = JsonbTypeHandler.class)
    private Object specialties;

    private String teachingStyle;

    private String education;

    private String training;

    @TableField(value = "languages", typeHandler = JsonbTypeHandler.class)
    private Object languages;

    private String bio;

    private String experience;

    @TableField(value = "honors", typeHandler = JsonbTypeHandler.class)
    private Object honors;

    @TableField(value = "emergency_contact", typeHandler = JsonbTypeHandler.class)
    private Object emergencyContact;

    @TableField(value = "certifications", typeHandler = JsonbTypeHandler.class)
    private Object certifications;

    @TableField(value = "availability", typeHandler = JsonbTypeHandler.class)
    private Object availability;

    private Integer studentCount;

    private String rating;
}