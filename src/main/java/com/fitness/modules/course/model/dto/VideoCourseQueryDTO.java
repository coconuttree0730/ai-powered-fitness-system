package com.fitness.modules.course.model.dto;

import lombok.Data;

@Data
public class VideoCourseQueryDTO {

    private String title;

    private String category;

    private Long coachId;

    private Integer status;

    private String difficultyLevel;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
