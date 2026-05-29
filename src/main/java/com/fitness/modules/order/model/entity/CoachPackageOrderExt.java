package com.fitness.modules.order.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("coach_package_order_ext")
public class CoachPackageOrderExt {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long coachPackageId;
    private Long coachId;
    private String packageName;
    private LocalDateTime expireTime;

    @TableField(exist = false)
    private String coachName;

    @TableField(exist = false)
    private String coachAvatar;
}