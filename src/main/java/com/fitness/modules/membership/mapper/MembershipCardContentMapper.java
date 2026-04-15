package com.fitness.modules.membership.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fitness.modules.membership.model.entity.MembershipCardContent;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MembershipCardContentMapper extends BaseMapper<MembershipCardContent> {

    @Select("SELECT * FROM membership_card_content WHERE card_id = #{cardId} ORDER BY sort_order ASC")
    List<MembershipCardContent> selectByCardId(@Param("cardId") Long cardId);

    void deleteByCardId(@Param("cardId") Long cardId);
}
