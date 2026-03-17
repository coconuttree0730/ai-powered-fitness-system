package com.fitness.modules.course.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课程实体类
 * 对应 fitness_course 表
 */
@Data
@TableName("fitness_course")
public class Course {

    /**
     * 课程ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课程名称
     */
    @TableField("course_name")
    private String courseName;

    /**
     * 课程描述
     */
    @TableField("description")
    private String description;

    /**
     * 教练ID
     */
    @TableField("coach_id")
    private Long coachId;

    /**
     * 课程分类
     */
    @TableField("category")
    private String category;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private LocalDateTime endTime;

    /**
     * 容量（可预约人数）
     */
    @TableField("capacity")
    private Integer capacity;

    /**
     * 已预约数
     */
    @TableField("booked_count")
    private Integer bookedCount;

    /**
     * 状态：0-未开始, 1-进行中, 2-已结束, 3-已取消
     */
    @TableField("status")
    private Integer status;

    /**
     * 课程图片URL
     */
    @TableField("image_url")
    private String imageUrl;

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
     * 逻辑删除标志
     */
    @TableLogic
    @TableField("deleted")
    private Boolean deleted;
}
