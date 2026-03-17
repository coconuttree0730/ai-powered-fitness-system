package com.fitness.modules.plan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.plan.model.entity.FitnessPlan;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 健身计划数据访问层
 */
public interface FitnessPlanMapper extends BaseMapper<FitnessPlan> {

    /**
     * 查询用户的所有计划
     *
     * @param userId 用户ID
     * @return 计划列表
     */
    @Select("SELECT * FROM fitness_plan WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<FitnessPlan> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询用户最新的计划
     *
     * @param userId 用户ID
     * @return 最新计划
     */
    @Select("SELECT * FROM fitness_plan WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC LIMIT 1")
    FitnessPlan selectLatestByUserId(@Param("userId") Long userId);
}
