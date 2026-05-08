package com.fitness.modules.course.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("fitness_video_course")
public class VideoCourse {

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

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic(value = "false", delval = "true")
    @TableField("deleted")
    private Boolean deleted;
}
