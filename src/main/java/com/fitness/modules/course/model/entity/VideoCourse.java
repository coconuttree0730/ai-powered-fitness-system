package com.fitness.modules.course.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fitness.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("fitness_video_course")
public class VideoCourse extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("title")
    private String title;

    @TableField("description")
    private String description;

    @TableField("category")
    private String category;

    @TableField("cover_url")
    private String coverUrl;

    @TableField("video_url")
    private String videoUrl;

    @TableField("duration_seconds")
    private Integer durationSeconds;

    @TableField("file_size")
    private Long fileSize;

    @TableField("difficulty_level")
    private String difficultyLevel;

    @TableField("coach_id")
    private Long coachId;

    @TableField("status")
    private Integer status;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("sort_order")
    private Integer sortOrder;
}
