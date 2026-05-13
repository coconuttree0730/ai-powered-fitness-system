package com.fitness.modules.course.model.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 课程周实例展示VO（会员端/管理端通用）
 */
@Data
public class CourseSessionVO {
    
    private Long id;
    private Long courseId;
    private String courseName;
    private String description;
    private String category;
    private String imageUrl;
    private String difficultyLevel;
    private Integer durationMinutes;
    private Integer caloriesMin;
    private Integer caloriesMax;

    private Long coachId;
    private String coachName;
    private String coachAvatar;

    private Integer dayOfWeek;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer capacity;
    private Integer bookedCount;
    private Integer remainingCount;
    private Integer status;

    private LocalDateTime createTime;
}
