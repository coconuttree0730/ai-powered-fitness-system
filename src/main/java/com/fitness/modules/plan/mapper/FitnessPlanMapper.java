package com.fitness.modules.plan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.plan.model.entity.FitnessPlan;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface FitnessPlanMapper extends BaseMapper<FitnessPlan> {

    @Select("SELECT * FROM fitness_plan WHERE user_id = #{userId} AND deleted = false ORDER BY create_time DESC")
    @ResultMap("BaseResultMap")
    List<FitnessPlan> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM fitness_plan WHERE user_id = #{userId} AND deleted = false ORDER BY create_time DESC LIMIT 1")
    @ResultMap("BaseResultMap")
    FitnessPlan selectLatestByUserId(@Param("userId") Long userId);
}
