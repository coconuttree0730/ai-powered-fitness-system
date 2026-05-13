package com.fitness.modules.course.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

/**
 * 课程周实例实体类
 * 对应 fitness_course_session 表
 * 将周期性课程模板展开为每周具体的一次上课实例
 */
@Data
@TableName("fitness_course_session")
public class CourseSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("course_id")
    private Long courseId;

    @TableField("session_date")
    private LocalDate sessionDate;

    @TableField("day_of_week")
    private Integer dayOfWeek;

    @TableField("start_time")
    private LocalTime startTime;

    @TableField("end_time")
    private LocalTime endTime;

    @TableField("status")
    private Integer status;

    @TableField("capacity")
    private Integer capacity;

    @TableField("booked_count")
    private Integer bookedCount;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableLogic(value = "false", delval = "true")
    @TableField("deleted")
    private Boolean deleted;
}
