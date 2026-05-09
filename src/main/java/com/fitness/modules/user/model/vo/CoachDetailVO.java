package com.fitness.modules.user.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 教练详情VO
 * 用于返回教练详细信息给前端
 */
@Data
public class CoachDetailVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 性别
     */
    private String gender;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 个人展示图片URL（非头像）
     */
    private String personalImageUrl;

    /**
     * 教练标签列表
     */
    private List<String> tags;

    /**
     * 从业年限
     */
    private Integer workYears;

    /**
     * 专业领域列表
     */
    private List<String> specialties;

    /**
     * 教学风格
     */
    private String teachingStyle;

    /**
     * 教育背景
     */
    private String education;

    /**
     * 培训经历
     */
    private String training;

    /**
     * 语言能力列表
     */
    private List<String> languages;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 教学经验
     */
    private String experience;

    /**
     * 获得荣誉列表
     */
    private List<String> honors;

    /**
     * 学员数量
     */
    private Integer studentCount;

    /**
     * 好评率
     */
    private String rating;
}
