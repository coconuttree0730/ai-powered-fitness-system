package com.fitness.modules.equipment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.equipment.model.entity.EquipmentType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 器材类型 Mapper 接口
 */
@Mapper
public interface EquipmentTypeMapper extends BaseMapper<EquipmentType> {

    /**
     * 根据类型编码查询类型
     *
     * @param typeCode 类型编码
     * @return 器材类型
     */
    @Select("SELECT * FROM fitness_equipment_type WHERE type_code = #{typeCode} AND deleted = false")
    EquipmentType selectByTypeCode(@Param("typeCode") String typeCode);

    /**
     * 查询所有有效的器材类型（按排序号排序）
     *
     * @return 器材类型列表
     */
    @Select("SELECT * FROM fitness_equipment_type WHERE deleted = false ORDER BY sort_order ASC, id ASC")
    List<EquipmentType> selectAllActiveTypes();
}
