package com.fitness.modules.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.equipment.model.entity.EquipmentRepair;
import com.fitness.modules.equipment.model.vo.MyRepairVO;
import com.fitness.modules.equipment.model.vo.RepairVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 器材报修数据访问层
 */
public interface EquipmentRepairMapper extends BaseMapper<EquipmentRepair> {

    /**
     * 查询器材的报修记录
     *
     * @param equipmentId 器材ID
     * @return 报修记录列表
     */
    List<RepairVO> selectRepairList(@Param("equipmentId") Long equipmentId);

    /**
     * 查询所有报修记录
     *
     * @return 报修记录列表
     */
    List<RepairVO> selectAllRepairs();

    /**
     * 查询用户的报修记录
     *
     * @param userId 用户ID
     * @return 报修记录列表
     */
    List<MyRepairVO> selectMyRepairs(@Param("userId") Long userId);
}
