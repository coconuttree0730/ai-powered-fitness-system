package com.fitness.modules.membership.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.cache.RedisCacheNames;
import com.fitness.common.cache.RedisTemplateCacheSupport;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.constants.ErrorCode;
import com.fitness.modules.membership.mapper.MembershipCardContentMapper;
import com.fitness.modules.membership.mapper.MembershipCardMapper;
import com.fitness.modules.membership.mapper.MembershipCardTypeMapper;
import com.fitness.modules.membership.model.dto.MembershipCardContentDTO;
import com.fitness.modules.membership.model.dto.MembershipCardDTO;
import com.fitness.modules.membership.model.dto.MembershipCardQueryDTO;
import com.fitness.modules.membership.model.entity.MembershipCard;
import com.fitness.modules.membership.model.entity.MembershipCardContent;
import com.fitness.modules.membership.model.entity.MembershipCardType;
import com.fitness.modules.membership.model.vo.MembershipCardContentVO;
import com.fitness.modules.membership.model.vo.MembershipCardVO;
import com.fitness.modules.membership.service.MembershipCardService;
import com.fitness.modules.order.mapper.MembershipOrderExtMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipCardServiceImpl extends ServiceImpl<MembershipCardMapper, MembershipCard> implements MembershipCardService {

    private final MembershipCardContentMapper contentMapper;
    private final MembershipCardTypeMapper typeMapper;
    private final MembershipOrderExtMapper membershipOrderExtMapper;
    private final RedisTemplateCacheSupport redisTemplateCacheSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(
            value = {
                    RedisCacheNames.MEMBERSHIP_ACTIVE_CARDS,
                    RedisCacheNames.MEMBERSHIP_RECOMMEND_CARDS
            },
            allEntries = true
    )
    public MembershipCardVO createCard(MembershipCardDTO dto) {
        log.debug("开始创建会员卡: name={}, typeCode={}", dto.getName(), dto.getTypeCode());

        // 根据 typeCode 查询类型信息
        MembershipCardType type = typeMapper.selectByCode(dto.getTypeCode());
        log.debug("查询会员卡类型: typeCode={}, type={}", dto.getTypeCode(), type);

        if (type == null) {
            log.warn("会员卡类型不存在: typeCode={}", dto.getTypeCode());
            throw new BusinessException(ErrorCode.NOT_FOUND, "会员卡类型不存在");
        }

        MembershipCard card = new MembershipCard();
        BeanUtil.copyProperties(dto, card);
        card.setTypeId(type.getId());  // 设置类型ID
        card.setStatus("ACTIVE");
        // 计算日均价格
        if (card.getDailyPrice() == null && card.getPrice() != null && card.getDurationDays() != null) {
            card.setDailyPrice(card.getPrice().divide(new BigDecimal(card.getDurationDays()), 2, RoundingMode.HALF_UP));
            log.debug("计算日均价格: price={}, durationDays={}, dailyPrice={}",
                    card.getPrice(), card.getDurationDays(), card.getDailyPrice());
        }

        save(card);
        log.debug("会员卡保存成功: cardId={}", card.getId());

        // 保存内容项
        if (!CollectionUtils.isEmpty(dto.getContents())) {
            log.info("保存会员卡内容项: cardId={}, contentCount={}", card.getId(), dto.getContents().size());
            saveContents(card.getId(), dto.getContents());
        } else {
            log.info("会员卡无内容项: cardId={}", card.getId());
        }

        log.info("创建会员卡成功: cardId={}, name={}", card.getId(), card.getName());
        clearMembershipCaches();
        return getCardDetail(card.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(
            value = {
                    RedisCacheNames.MEMBERSHIP_ACTIVE_CARDS,
                    RedisCacheNames.MEMBERSHIP_RECOMMEND_CARDS
            },
            allEntries = true
    )
    public MembershipCardVO updateCard(Long id, MembershipCardDTO dto) {
        MembershipCard card = getById(id);
        if (card == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会员卡不存在");
        }

        // 验证类型是否存在
        MembershipCardType type = typeMapper.selectByCode(dto.getTypeCode());
        if (type == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会员卡类型不存在");
        }

        BeanUtil.copyProperties(dto, card);
        card.setId(id);
        card.setTypeId(type.getId());  // 更新类型ID

        // 重新计算日均价格
        if (card.getPrice() != null && card.getDurationDays() != null) {
            card.setDailyPrice(card.getPrice().divide(new BigDecimal(card.getDurationDays()), 2, RoundingMode.HALF_UP));
        }

        updateById(card);

        // 更新内容项：先删除再重新添加
        contentMapper.deleteByCardId(id);
        if (!CollectionUtils.isEmpty(dto.getContents())) {
            saveContents(id, dto.getContents());
        }

        clearMembershipCaches();
        return getCardDetail(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(
            value = {
                    RedisCacheNames.MEMBERSHIP_ACTIVE_CARDS,
                    RedisCacheNames.MEMBERSHIP_RECOMMEND_CARDS
            },
            allEntries = true
    )
    public void deleteCard(Long id) {
        MembershipCard card = getById(id);
        if (card == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会员卡不存在");
        }

        if (membershipOrderExtMapper.countByCardId(id) > 0) {
            throw new BusinessException(409, "会员卡已有订单记录，无法删除，请先下架该会员卡");
        }

        // 删除内容项
        contentMapper.deleteByCardId(id);

        // 删除会员卡
        removeById(id);
        clearMembershipCaches();
    }

    @Override
    public MembershipCardVO getCardById(Long id) {
        MembershipCard card = getById(id);
        if (card == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会员卡不存在");
        }
        return convertToVO(card);
    }

    @Override
    public MembershipCardVO getCardDetail(Long id) {
        MembershipCard card = getById(id);
        if (card == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "会员卡不存在");
        }

        MembershipCardVO vo = convertToVO(card);

        // 获取类型名称
        MembershipCardType type = typeMapper.selectById(card.getTypeId());
        if (type != null) {
            vo.setTypeName(type.getName());
        }

        // 获取内容项
        List<MembershipCardContent> contents = contentMapper.selectByCardId(id);
        vo.setContents(contents.stream().map(this::convertContentToVO).collect(Collectors.toList()));

        return vo;
    }

    @Override
    public List<MembershipCardVO> listAllCards() {
        List<MembershipCard> list = lambdaQuery()
                .orderByAsc(MembershipCard::getSortOrder)
                .orderByDesc(MembershipCard::getId)
                .list();
        return list.stream().map(card -> {
            MembershipCardVO vo = convertToVO(card);
            // 查询每个会员卡的内容项
            List<MembershipCardContent> contents = contentMapper.selectByCardId(card.getId());
            vo.setContents(contents.stream().map(this::convertContentToVO).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<MembershipCardVO> listActiveCards() {
        return redisTemplateCacheSupport.getOrLoad(RedisCacheNames.MEMBERSHIP_ACTIVE_CARDS, "all", () -> {
            List<MembershipCard> list = baseMapper.selectActiveCards();
            List<MembershipCardVO> cards = convertToVOListWithContents(list);
            log.debug("[DB LOAD] active membership cards, count={}", cards.size());
            return cards;
        });
    }

    @Override
    public List<MembershipCardVO> listRecommendCards(Integer limit) {
        Integer safeLimit = limit == null ? 4 : limit;
        return redisTemplateCacheSupport.getOrLoad(RedisCacheNames.MEMBERSHIP_RECOMMEND_CARDS, String.valueOf(safeLimit), () -> {
            List<MembershipCard> list = baseMapper.selectRecommendCards(safeLimit);
            List<MembershipCardVO> cards = convertToVOListWithContents(list);
            log.debug("[DB LOAD] recommend membership cards, limit={}, count={}", safeLimit, cards.size());
            return cards;
        });
    }

    @Override
    public IPage<MembershipCardVO> getCardPage(MembershipCardQueryDTO queryDTO) {
        log.debug("开始分页查询会员卡: queryDTO={}", queryDTO);

        Page<MembershipCard> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        LambdaQueryWrapper<MembershipCard> wrapper = new LambdaQueryWrapper<>();

        // 构建查询条件
        if (StringUtils.hasText(queryDTO.getName())) {
            wrapper.like(MembershipCard::getName, queryDTO.getName());
        }
        if (StringUtils.hasText(queryDTO.getTypeCode())) {
            // 需要根据typeCode查询typeId
            MembershipCardType type = typeMapper.selectByCode(queryDTO.getTypeCode());
            if (type != null) {
                wrapper.eq(MembershipCard::getTypeId, type.getId());
            }
        }
        if (StringUtils.hasText(queryDTO.getStatus())) {
            wrapper.eq(MembershipCard::getStatus, queryDTO.getStatus());
        }

        wrapper.orderByAsc(MembershipCard::getSortOrder)
               .orderByDesc(MembershipCard::getId);

        IPage<MembershipCard> cardPage = page(page, wrapper);

        // 转换为VO（包含内容项）
        List<MembershipCardVO> voList = cardPage.getRecords().stream().map(card -> {
            MembershipCardVO vo = convertToVO(card);
            // 查询每个会员卡的内容项
            List<MembershipCardContent> contents = contentMapper.selectByCardId(card.getId());
            if (!CollectionUtils.isEmpty(contents)) {
                vo.setContents(contents.stream().map(this::convertContentToVO).collect(Collectors.toList()));
            }
            return vo;
        }).collect(Collectors.toList());

        Page<MembershipCardVO> resultPage = new Page<>(cardPage.getCurrent(), cardPage.getSize(), cardPage.getTotal());
        resultPage.setRecords(voList);

        log.debug("分页查询会员卡完成: total={}, records={}", resultPage.getTotal(), voList.size());
        return resultPage;
    }

    private List<MembershipCardVO> convertToVOListWithContents(List<MembershipCard> cards) {
        if (CollectionUtils.isEmpty(cards)) {
            return List.of();
        }
        return cards.stream().map(card -> {
            MembershipCardVO vo = convertToVO(card);
            // 补充类型名称
            if (vo.getTypeName() == null && card.getTypeId() != null) {
                MembershipCardType type = typeMapper.selectById(card.getTypeId());
                if (type != null) {
                    vo.setTypeName(type.getName());
                }
            }
            // 查询内容项
            List<MembershipCardContent> contents = contentMapper.selectByCardId(card.getId());
            vo.setContents(contents.stream().map(this::convertContentToVO).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
    }

    private void saveContents(Long cardId, List<MembershipCardContentDTO> contents) {
        log.debug("开始保存会员卡内容项: cardId={}, contentCount={}", cardId, contents.size());
        for (int i = 0; i < contents.size(); i++) {
            MembershipCardContentDTO contentDTO = contents.get(i);
            MembershipCardContent content = new MembershipCardContent();
            BeanUtil.copyProperties(contentDTO, content);
            content.setCardId(cardId);
            content.setSortOrder(contentDTO.getSortOrder() != null ? contentDTO.getSortOrder() : i);
            contentMapper.insert(content);
            log.debug("保存内容项成功: cardId={}, contentId={}, title={}", cardId, content.getId(), content.getTitle());
        }
        log.debug("会员卡内容项保存完成: cardId={}, totalCount={}", cardId, contents.size());
    }

    private MembershipCardVO convertToVO(MembershipCard card) {
        MembershipCardVO vo = new MembershipCardVO();
        BeanUtil.copyProperties(card, vo);
        vo.setStatusLabel(getStatusLabel(card.getStatus()));

        // 获取类型信息
        if (card.getTypeId() != null) {
            MembershipCardType type = typeMapper.selectById(card.getTypeId());
            if (type != null) {
                vo.setTypeCode(type.getCode());
                vo.setTypeName(type.getName());
            }
        }

        return vo;
    }

    private MembershipCardContentVO convertContentToVO(MembershipCardContent content) {
        MembershipCardContentVO vo = new MembershipCardContentVO();
        BeanUtil.copyProperties(content, vo);
        vo.setContentTypeLabel(getContentTypeLabel(content.getContentType()));
        return vo;
    }

    private String getStatusLabel(String status) {
        return switch (status) {
            case "ACTIVE" -> "上架";
            case "INACTIVE" -> "下架";
            default -> status;
        };
    }

    private String getContentTypeLabel(String contentType) {
        return switch (contentType) {
            case "BENEFIT" -> "权益说明";
            case "RULE" -> "使用规则";
            case "PRIVILEGE" -> "特权列表";
            case "OTHER" -> "其他";
            default -> contentType;
        };
    }

    private void clearMembershipCaches() {
        redisTemplateCacheSupport.evictAll(RedisCacheNames.MEMBERSHIP_ACTIVE_CARDS);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.MEMBERSHIP_RECOMMEND_CARDS);
    }
}
