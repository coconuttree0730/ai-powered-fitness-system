package com.fitness.modules.plan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.plan.model.entity.FitnessPlanDetail;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 健身计划详情数据访问层
 */
public interface FitnessPlanDetailMapper extends BaseMapper<FitnessPlanDetail> {

    /**
     * 查询计划的所有详情
     *
     * @param planId 计划ID
     * @return 详情列表
     */
    @Select("SELECT * FROM fitness_plan_detail WHERE plan_id = #{planId} AND deleted = 0 ORDER BY day_of_week, sort_order")
    List<FitnessPlanDetail> selectByPlanId(@Param("planId") Long planId);
}
