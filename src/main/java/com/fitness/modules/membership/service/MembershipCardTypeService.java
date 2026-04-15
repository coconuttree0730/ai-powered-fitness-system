package com.fitness.modules.membership.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.modules.membership.model.dto.MembershipCardTypeDTO;
import com.fitness.modules.membership.model.entity.MembershipCardType;
import com.fitness.modules.membership.model.vo.MembershipCardTypeVO;

import java.util.List;

public interface MembershipCardTypeService extends IService<MembershipCardType> {

    MembershipCardTypeVO createType(MembershipCardTypeDTO dto);

    MembershipCardTypeVO updateType(Long id, MembershipCardTypeDTO dto);

    void deleteType(Long id);

    MembershipCardTypeVO getTypeById(Long id);

    List<MembershipCardTypeVO> listAllTypes();

    List<MembershipCardTypeVO> listActiveTypes();
}
