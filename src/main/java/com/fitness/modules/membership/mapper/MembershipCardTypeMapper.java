package com.fitness.modules.membership.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.membership.model.entity.MembershipCardType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MembershipCardTypeMapper extends BaseMapper<MembershipCardType> {

    @Select("SELECT * FROM membership_card_type WHERE code = #{code} AND status = 'ACTIVE' LIMIT 1")
    MembershipCardType selectByCode(@Param("code") String code);
}
