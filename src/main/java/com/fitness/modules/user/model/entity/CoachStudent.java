package com.fitness.modules.user.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fitness.common.model.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("coach_student")
public class CoachStudent extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("member_id")
    private Long memberId;

    @TableField("coach_id")
    private Long coachId;

    @TableField("coach_package_id")
    private Long coachPackageId;

    @TableField("package_code")
    private String packageCode;

    @TableField("bind_time")
    private LocalDateTime bindTime;

    @TableField("expire_time")
    private LocalDateTime expireTime;

    @TableField("status")
    private String status;
}