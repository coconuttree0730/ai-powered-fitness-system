package com.fitness.modules.user.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 首页教练展示VO
 * 用于首页教练团队区域展示
 */
@Data
public class HomePageCoachVO {

    /**
     * 教练ID
     */
    private Long id;

    /**
     * 教练姓名
     */
    private String name;

    /**
     * 教练职称/头衔
     */
    private String title;

    /**
     * 展示图片URL（优先使用个人展示图片）
     */
    private String image;

    /**
     * 从业年限（如：8+）
     */
    private String experience;

    /**
     * 学员数量（如：2000+）
     */
    private String students;

    /**
     * 好评率（如：99%）
     */
    private String rating;

    /**
     * 评分（如：4.5）
     */
    private Double ratingScore;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 教练标签列表（首页最多显示3个）
     */
    private List<String> tags;
}
