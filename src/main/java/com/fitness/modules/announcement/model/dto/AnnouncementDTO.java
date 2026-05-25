package com.fitness.modules.announcement.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告DTO
 */
@Data
public class AnnouncementDTO {

    /**
     * 公告标题
     */
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 200, message = "公告标题长度不能超过200字符")
    private String title;

    /**
     * 公告内容
     */
    @NotBlank(message = "公告内容不能为空")
    private String content;

    /**
     * 公告类型：SYSTEM-系统公告, ACTIVITY-活动通知, IMPORTANT-重要提醒
     */
    @NotBlank(message = "公告类型不能为空")
    private String type;

    /**
     * 状态：0-草稿，1-已发布
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
}
