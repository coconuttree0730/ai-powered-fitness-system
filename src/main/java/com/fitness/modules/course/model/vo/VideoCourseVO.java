package com.fitness.modules.course.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoCourseVO {

    private Long id;

    private String title;

    private String description;

    private String category;

    private String coverUrl;

    private String videoUrl;

    private Integer durationSeconds;

    private Long fileSize;

    private String difficultyLevel;

    private Long coachId;

    private String coachName;

    private String coachAvatar;

    private Integer status;

    private Integer viewCount;

    private Integer sortOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
