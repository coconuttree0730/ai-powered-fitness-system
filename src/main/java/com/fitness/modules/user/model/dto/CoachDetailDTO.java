package com.fitness.modules.user.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 教练详情DTO
 * 用于接收前端传入的教练信息
 */
@Data
public class CoachDetailDTO {

    /**
     * 从业年限
     */
    private Integer workYears;

    /**
     * 专业领域列表
     */
    private List<String> specialties;

    /**
     * 教学风格
     */
    private String teachingStyle;

    /**
     * 教育背景
     */
    private String education;

    /**
     * 培训经历
     */
    private String training;

    /**
     * 语言能力列表
     */
    private List<String> languages;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 教学经验
     */
    private String experience;

    /**
     * 获得荣誉列表
     */
    private List<String> honors;

    /**
     * 紧急联系人信息
     */
    private EmergencyContactDTO emergencyContact;

    /**
     * 资格认证列表
     */
    private List<CertificationDTO> certifications;

    /**
     * 可用时间段
     */
    private AvailabilityDTO availability;

    /**
     * 紧急联系人DTO
     */
    @Data
    public static class EmergencyContactDTO {
        private String name;
        private String relation;
        private String phone;
    }

    /**
     * 资格认证DTO
     */
    @Data
    public static class CertificationDTO {
        private String name;
        private String number;
        private String validDate;
        private String status;
    }

    /**
     * 可用时间段DTO
     */
    @Data
    public static class AvailabilityDTO {
        private List<TimeSlotDTO> monday;
        private List<TimeSlotDTO> tuesday;
        private List<TimeSlotDTO> wednesday;
        private List<TimeSlotDTO> thursday;
        private List<TimeSlotDTO> friday;
        private List<TimeSlotDTO> saturday;
        private List<TimeSlotDTO> sunday;
    }

    /**
     * 时间段DTO
     */
    @Data
    public static class TimeSlotDTO {
        private String start;
        private String end;
    }
}
