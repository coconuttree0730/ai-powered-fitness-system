package com.fitness.modules.user.model.vo;

import lombok.Data;

/**
 * 首页教练展示VO - 用于Mapper查询结果
 */
@Data
public class CoachHomePageVO {

    /**
     * 教练详情ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 个人展示图片URL
     */
    private String personalImageUrl;

    /**
     * 教练标签JSON
     * 使用Object类型接收PostgreSQL jsonb类型
     */
    private Object tags;

    /**
     * 从业年限
     */
    private Integer workYears;

    /**
     * 专业领域JSON
     * 使用Object类型接收PostgreSQL jsonb类型
     */
    private Object specialties;

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
     * 语言能力JSON
     * 使用Object类型接收PostgreSQL jsonb类型
     */
    private Object languages;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 教学经验
     */
    private String experience;

    /**
     * 获得荣誉JSON
     * 使用Object类型接收PostgreSQL jsonb类型
     */
    private Object honors;

    /**
     * 紧急联系人信息JSON
     * 使用Object类型接收PostgreSQL jsonb类型
     */
    private Object emergencyContact;

    /**
     * 资格认证JSON
     * 使用Object类型接收PostgreSQL jsonb类型
     */
    private Object certifications;

    /**
     * 可用时间段JSON
     * 使用Object类型接收PostgreSQL jsonb类型
     */
    private Object availability;

    /**
     * 学员数量
     */
    private Integer studentCount;

    /**
     * 好评率
     */
    private String rating;

    /**
     * 软删除标志
     */
    private Boolean deleted;

    // 关联sys_user表的字段
    /**
     * 用户名
     */
    private String username;

    /**
     * 头像
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
