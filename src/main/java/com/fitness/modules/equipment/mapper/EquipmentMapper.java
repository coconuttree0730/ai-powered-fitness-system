package com.fitness.modules.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.equipment.model.dto.EquipmentQueryDTO;
import com.fitness.modules.equipment.model.entity.Equipment;
import com.fitness.modules.equipment.model.vo.EquipmentVO;
import org.apache.ibatis.annotations.Param;

/**
 * 器材数据访问层
 */
public interface EquipmentMapper extends BaseMapper<Equipment> {

    /**
     * 分页查询器材列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    Page<EquipmentVO> selectEquipmentList(Page<Equipment> page, @Param("query") EquipmentQueryDTO query);

    /**
     * 查询器材详情
     *
     * @param equipmentId 器材ID
     * @return 器材详情
     */
    EquipmentVO selectEquipmentDetail(@Param("equipmentId") Long equipmentId);
}
