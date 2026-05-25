package com.fitness.modules.membership.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.constants.ErrorCode;
import com.fitness.modules.membership.mapper.MembershipCardTypeMapper;
import com.fitness.modules.membership.model.dto.MembershipCardTypeDTO;
import com.fitness.modules.membership.model.entity.MembershipCardType;
import com.fitness.modules.membership.model.vo.MembershipCardTypeVO;
import com.fitness.modules.membership.service.MembershipCardTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MembershipCardTypeServiceImpl extends ServiceImpl<MembershipCardTypeMapper, MembershipCardType> implements MembershipCardTypeService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MembershipCardTypeVO createType(MembershipCardTypeDTO dto) {
        // 检查编码是否已存在
        MembershipCardType existing = lambdaQuery()
                .eq(MembershipCardType::getCode, dto.getCode())
                .one();
        if (existing != null) {
            throw new BusinessException(ErrorCode.CODE_ALREADY_EXISTS, "类型编码已存在");
        }

        MembershipCardType type = new MembershipCardType();
        BeanUtil.copyProperties(dto, type);
        type.setStatus("ACTIVE");
        save(type);

        return convertToVO(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MembershipCardTypeVO updateType(Long id, MembershipCardTypeDTO dto) {
        MembershipCardType type = getById(id);
        if (type == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会员卡类型不存在");
        }

        // 检查编码是否被其他类型使用
        if (!type.getCode().equals(dto.getCode())) {
            MembershipCardType existing = lambdaQuery()
                    .eq(MembershipCardType::getCode, dto.getCode())
                    .ne(MembershipCardType::getId, id)
                    .one();
            if (existing != null) {
                throw new BusinessException(ErrorCode.CODE_ALREADY_EXISTS, "类型编码已存在");
            }
        }

        BeanUtil.copyProperties(dto, type);
        updateById(type);

        return convertToVO(type);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteType(Long id) {
        MembershipCardType type = getById(id);
        if (type == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会员卡类型不存在");
        }
        removeById(id);
    }

    @Override
    public MembershipCardTypeVO getTypeById(Long id) {
        MembershipCardType type = getById(id);
        if (type == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会员卡类型不存在");
        }
        return convertToVO(type);
    }

    @Override
    public List<MembershipCardTypeVO> listAllTypes() {
        List<MembershipCardType> list = lambdaQuery()
                .orderByAsc(MembershipCardType::getSortOrder)
                .list();
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<MembershipCardTypeVO> listActiveTypes() {
        List<MembershipCardType> list = lambdaQuery()
                .eq(MembershipCardType::getStatus, "ACTIVE")
                .orderByAsc(MembershipCardType::getSortOrder)
                .list();
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private MembershipCardTypeVO convertToVO(MembershipCardType type) {
        MembershipCardTypeVO vo = new MembershipCardTypeVO();
        BeanUtil.copyProperties(type, vo);
        vo.setStatusLabel(getStatusLabel(type.getStatus()));
        return vo;
    }

    private String getStatusLabel(String status) {
        return switch (status) {
            case "ACTIVE" -> "启用";
            case "INACTIVE" -> "禁用";
            default -> status;
        };
    }
}
