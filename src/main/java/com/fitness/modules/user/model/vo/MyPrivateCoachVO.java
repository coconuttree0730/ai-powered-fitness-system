package com.fitness.modules.user.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 我的专属教练VO
 * 用于会员端展示专属教练信息
 */
@Data
public class MyPrivateCoachVO {

    /**
     * 教练ID
     */
    private Long id;

    /**
     * 教练姓名
     */
    private String name;

    /**
     * 教练头像
     */
    private String avatar;

    /**
     * 个人展示图片URL
     */
    private String personalImageUrl;

    /**
     * 教练标签列表
     */
    private List<String> tags;

    /**
     * 专业领域
     */
    private List<String> specialties;

    /**
     * 从业年限
     */
    private Integer workYears;

    /**
     * 学员数量
     */
    private Integer studentCount;

    /**
     * 好评率/评分
     */
    private String rating;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 教学风格
     */
    private String teachingStyle;
}
