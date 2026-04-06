package com.fitness.modules.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.equipment.model.entity.RepairRecord;
import com.fitness.modules.equipment.model.vo.RepairRecordVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 报修处理记录数据访问层
 */
public interface RepairRecordMapper extends BaseMapper<RepairRecord> {

    /**
     * 查询报修的处理记录列表
     *
     * @param repairId 报修ID
     * @return 处理记录列表
     */
    List<RepairRecordVO> selectRecordsByRepairId(@Param("repairId") Long repairId);

    /**
     * 根据报修ID删除处理记录（逻辑删除）
     *
     * @param repairId 报修ID
     */
    void deleteByRepairId(@Param("repairId") Long repairId);
}
