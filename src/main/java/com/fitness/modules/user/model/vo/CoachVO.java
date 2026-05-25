package com.fitness.modules.user.model.vo;

import lombok.Data;

/**
 * 教练信息VO
 * 用于下拉列表等简单场景
 */
@Data
public class CoachVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 教练姓名（用户名）
     */
    private String name;
}
