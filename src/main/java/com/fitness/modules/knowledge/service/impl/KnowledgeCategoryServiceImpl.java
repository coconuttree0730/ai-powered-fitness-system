package com.fitness.modules.knowledge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.constants.ErrorCode;
import com.fitness.modules.knowledge.mapper.KnowledgeCategoryMapper;
import com.fitness.modules.knowledge.model.dto.KnowledgeCategoryDTO;
import com.fitness.modules.knowledge.model.entity.KnowledgeCategory;
import com.fitness.modules.knowledge.model.vo.KnowledgeCategoryVO;
import com.fitness.modules.knowledge.service.KnowledgeCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeCategoryServiceImpl implements KnowledgeCategoryService {

    private final KnowledgeCategoryMapper categoryMapper;

    @Override
    public List<KnowledgeCategoryVO> listAll() {
        LambdaQueryWrapper<KnowledgeCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeCategory::getDeleted, false)
               .orderByAsc(KnowledgeCategory::getSortOrder);
        return categoryMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public KnowledgeCategoryVO getById(Long id) {
        KnowledgeCategory category = getEntityById(id);
        return toVO(category);
    }

    @Override
    public Long create(KnowledgeCategoryDTO dto) {
        LambdaQueryWrapper<KnowledgeCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeCategory::getCode, dto.getCode())
               .eq(KnowledgeCategory::getDeleted, false);
        if (categoryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), "分类编码已存在");
        }

        KnowledgeCategory category = new KnowledgeCategory();
        BeanUtil.copyProperties(dto, category);
        categoryMapper.insert(category);
        return category.getId();
    }

    @Override
    public void update(KnowledgeCategoryDTO dto) {
        KnowledgeCategory category = getEntityById(dto.getId());

        if (!category.getCode().equals(dto.getCode())) {
            LambdaQueryWrapper<KnowledgeCategory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(KnowledgeCategory::getCode, dto.getCode())
                   .eq(KnowledgeCategory::getDeleted, false)
                   .ne(KnowledgeCategory::getId, dto.getId());
            if (categoryMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(ErrorCode.DATA_ALREADY_EXISTS.getCode(), "分类编码已存在");
            }
        }

        BeanUtil.copyProperties(dto, category);
        categoryMapper.updateById(category);
    }

    @Override
    public void delete(Long id) {
        KnowledgeCategory category = getEntityById(id);
        categoryMapper.deleteById(id);
    }

    @Override
    public KnowledgeCategory getEntityById(Long id) {
        KnowledgeCategory category = categoryMapper.selectById(id);
        if (category == null || category.getDeleted()) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND.getCode(), "分类不存在");
        }
        return category;
    }

    private KnowledgeCategoryVO toVO(KnowledgeCategory category) {
        KnowledgeCategoryVO vo = new KnowledgeCategoryVO();
        BeanUtil.copyProperties(category, vo);
        return vo;
    }
}
