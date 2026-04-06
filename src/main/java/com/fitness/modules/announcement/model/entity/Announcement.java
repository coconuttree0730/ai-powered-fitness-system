package com.fitness.modules.announcement.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告实体类
 */
@Data
@TableName("sys_announcement")
public class Announcement {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容
     */
    private String content;

    /**
     * 公告类型：SYSTEM-系统公告, ACTIVITY-活动通知, IMPORTANT-重要提醒
     */
    private String type;

    /**
     * 状态：0-草稿，1-已发布
     */
    private Integer status;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 软删除标识
     */
    @TableLogic(value = "false", delval = "true")
    private Boolean deleted;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
