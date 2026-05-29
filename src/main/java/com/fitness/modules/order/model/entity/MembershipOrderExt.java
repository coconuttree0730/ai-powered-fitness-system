package com.fitness.modules.order.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("membership_order_ext")
public class MembershipOrderExt {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;
    private Long cardId;
    private String cardName;
    private LocalDateTime expireTime;
}