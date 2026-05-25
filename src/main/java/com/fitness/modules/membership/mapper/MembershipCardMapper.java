package com.fitness.modules.membership.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.membership.model.entity.MembershipCard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MembershipCardMapper extends BaseMapper<MembershipCard> {

    @Select("SELECT * FROM membership_card WHERE status = 'ACTIVE' ORDER BY sort_order ASC, id DESC")
    List<MembershipCard> selectActiveCards();

    @Select("SELECT * FROM membership_card WHERE is_recommend = true AND status = 'ACTIVE' ORDER BY sort_order ASC LIMIT #{limit}")
    List<MembershipCard> selectRecommendCards(@Param("limit") Integer limit);
}
