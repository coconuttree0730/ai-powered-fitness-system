package com.fitness.modules.membership.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.membership.model.entity.UserMembership;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMembershipMapper extends BaseMapper<UserMembership> {

    @Select("SELECT * FROM user_membership WHERE user_id = #{userId}")
    UserMembership selectByUserId(@Param("userId") Long userId);
}
