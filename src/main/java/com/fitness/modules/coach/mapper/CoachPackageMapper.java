package com.fitness.modules.coach.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.coach.model.entity.CoachPackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 教练套餐 Mapper 接口
 */
@Mapper
public interface CoachPackageMapper extends BaseMapper<CoachPackage> {

    /**
     * 根据教练ID查询上架套餐列表
     *
     * @param coachId 教练ID
     * @return 套餐列表
     */
    @Select("SELECT * FROM coach_package WHERE coach_id = #{coachId} AND status = 'ACTIVE' ORDER BY sort_order")
    List<CoachPackage> selectByCoachId(@Param("coachId") Long coachId);

    /**
     * 查询所有上架套餐列表
     *
     * @return 套餐列表
     */
    @Select("SELECT * FROM coach_package WHERE status = 'ACTIVE' ORDER BY sort_order")
    List<CoachPackage> selectAllActive();
}