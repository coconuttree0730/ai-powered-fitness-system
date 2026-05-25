package com.fitness.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.user.model.entity.UserFitnessProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户健身档案Mapper接口
 */
@Mapper
public interface UserFitnessProfileMapper extends BaseMapper<UserFitnessProfile> {

    /**
     * 根据用户ID查询健身档案
     *
     * @param userId 用户ID
     * @return 健身档案
     */
    @Select("SELECT * FROM user_fitness_profile WHERE user_id = #{userId} AND deleted = false")
    UserFitnessProfile selectByUserId(@Param("userId") Long userId);
}
