package com.fitness.modules.equipment.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报修记录展示VO
 */
@Data
public class RepairVO {

    /**
     * 报修ID
     */
    private Long id;

    /**
     * 报修用户ID
     */
    private Long userId;

    /**
     * 关联的器材ID
     */
    private Long equipmentId;

    /**
     * 器材编号
     */
    private String equipmentNo;

    /**
     * 器材名称
     */
    private String equipmentName;

    /**
     * 报修用户名称
     */
    private String userName;

    /**
     * 报修描述
     */
    private String description;

    /**
     * 报修图片URL列表
     */
    private List<String> imageUrls;

    /**
     * 状态：0-待处理, 1-处理中, 2-已完成, 3-已关闭
     */
    private Integer status;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理记录列表
     */
    private List<RepairRecordVO> records;
}
