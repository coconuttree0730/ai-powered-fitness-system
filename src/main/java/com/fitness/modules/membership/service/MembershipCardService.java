package com.fitness.modules.membership.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.modules.membership.model.dto.MembershipCardDTO;
import com.fitness.modules.membership.model.dto.MembershipCardQueryDTO;
import com.fitness.modules.membership.model.entity.MembershipCard;
import com.fitness.modules.membership.model.vo.MembershipCardVO;

import java.util.List;

public interface MembershipCardService extends IService<MembershipCard> {

    MembershipCardVO createCard(MembershipCardDTO dto);

    MembershipCardVO updateCard(Long id, MembershipCardDTO dto);

    void deleteCard(Long id);

    MembershipCardVO getCardById(Long id);

    MembershipCardVO getCardDetail(Long id);

    List<MembershipCardVO> listAllCards();

    List<MembershipCardVO> listActiveCards();

    List<MembershipCardVO> listRecommendCards(Integer limit);

    IPage<MembershipCardVO> getCardPage(MembershipCardQueryDTO queryDTO);
}
