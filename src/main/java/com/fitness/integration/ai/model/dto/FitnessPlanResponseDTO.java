package com.fitness.integration.ai.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * AI健身计划响应DTO
 * 用于接收AI返回的结构化JSON数据
 */
@Data
public class FitnessPlanResponseDTO {

    private String subtitle;

    private UserInfo userInfo;

    private List<DayPlanDTO> weeklyPlan;

    @Data
    public static class UserInfo {
        private String height;
        private String weight;
        private String bmi;
        private String goal;
        private String intensity;
    }

    @Data
    public static class DayPlanDTO {
        private String dayName;
        private String focus;
        private CourseDTO course;
        private List<EquipmentDTO> equipment;
        private List<ExerciseDTO> exercises;
        private List<String> tips;
    }

    @Data
    public static class CourseDTO {
        private String name;
        private String description;
        private String coverImage;
        private Integer duration;
        @JsonProperty("id")
        private Long courseId;
    }

    @Data
    public static class EquipmentDTO {
        private String name;
        private String image;
    }

    @Data
    public static class ExerciseDTO {
        private String name;
        private Integer sets;
        private Integer reps;
        private Integer restSeconds;
    }
}
