package com.fitness.modules.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 教练详情DTO
 * 用于接收前端传入的教练信息
 */
@Data
public class CoachDetailDTO {

    /**
     * 用户名
     * 6-20个字符，只允许字母、数字、下划线
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 6, max = 20, message = "用户名长度必须在6-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

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
     * 资格认证列表
     */
    private List<CertificationDTO> certifications;

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
}
