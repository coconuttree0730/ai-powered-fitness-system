package com.fitness.modules.course.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VideoCourseDTO {

    @NotBlank(message = "视频标题不能为空")
    private String title;

    private String description;

    private String category;

    private String coverUrl;

    @NotBlank(message = "视频URL不能为空")
    private String videoUrl;

    private Integer durationSeconds;

    private Long fileSize;

    private String difficultyLevel;

    private Long coachId;

    private Integer status;

    private Integer sortOrder;
}
