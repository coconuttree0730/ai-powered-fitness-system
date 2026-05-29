package com.fitness.modules.membership.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.modules.membership.mapper.MembershipCardMapper;
import com.fitness.modules.membership.mapper.MembershipCardTypeMapper;
import com.fitness.modules.membership.mapper.UserMembershipMapper;
import com.fitness.modules.membership.model.entity.MembershipCard;
import com.fitness.modules.membership.model.entity.MembershipCardType;
import com.fitness.modules.membership.model.entity.MembershipOrder;
import com.fitness.modules.membership.model.entity.UserMembership;
import com.fitness.modules.membership.model.vo.PurchaseCheckVO;
import com.fitness.modules.membership.model.vo.UserMembershipVO;
import com.fitness.modules.membership.service.UserMembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserMembershipServiceImpl extends ServiceImpl<UserMembershipMapper, UserMembership> implements UserMembershipService {

    private final UserMembershipMapper userMembershipMapper;
    private final MembershipCardMapper cardMapper;
    private final MembershipCardTypeMapper typeMapper;

    @Override
    public UserMembershipVO getUserMembership(Long userId) {
        log.debug("查询用户会员信息: userId={}", userId);
        UserMembership membership = userMembershipMapper.selectByUserId(userId);
        log.debug("查询会员结果: userId={}, membership={}", userId, membership);
        
        if (membership == null) {
            log.debug("用户无会员信息: userId={}", userId);
            // 返回空会员信息
            UserMembershipVO vo = new UserMembershipVO();
            vo.setUserId(userId);
            vo.setIsActive(false);
            vo.setRemainingDays(0);
            return vo;
        }

        UserMembershipVO vo = convertToVO(membership);

        // 计算剩余天数
        if (membership.getExpireTime() != null && membership.getIsActive()) {
            long remainingDays = ChronoUnit.DAYS.between(LocalDateTime.now(), membership.getExpireTime());
            vo.setRemainingDays((int) Math.max(0, remainingDays));
            log.debug("计算会员剩余天数: userId={}, expireTime={}, remainingDays={}", 
                    userId, membership.getExpireTime(), remainingDays);

            // 检查是否过期
            if (remainingDays <= 0) {
                log.info("会员已过期，更新状态: userId={}, expireTime={}", userId, membership.getExpireTime());
                membership.setIsActive(false);
                updateById(membership);
                vo.setIsActive(false);
            }
        }

        log.debug("返回用户会员信息: userId={}, isActive={}, remainingDays={}", 
                userId, vo.getIsActive(), vo.getRemainingDays());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void activateMembership(MembershipOrder order) {
        log.debug("开始激活会员: userId={}, cardId={}", order.getUserId(), order.getCardId());
        
        UserMembership membership = userMembershipMapper.selectByUserId(order.getUserId());
        log.debug("查询现有会员信息: userId={}, membership={}", order.getUserId(), membership);

        MembershipCard card = cardMapper.selectById(order.getCardId());
        log.debug("查询会员卡信息: cardId={}, card={}", order.getCardId(), card);
        
        if (card == null) {
            log.error("会员卡不存在: cardId={}", order.getCardId());
            return;
        }

        MembershipCardType cardType = typeMapper.selectById(card.getTypeId());
        String typeName = cardType != null ? cardType.getName() : "会员";
        log.debug("会员卡类型: typeId={}, typeName={}", card.getTypeId(), typeName);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expireTime;

        if (membership == null) {
            // 首次购买会员
            log.debug("首次购买会员: userId={}", order.getUserId());
            membership = new UserMembership();
            membership.setUserId(order.getUserId());
            membership.setMembershipType(typeName);
            membership.setStartTime(now);
            membership.setExpireTime(now.plusDays(card.getDurationDays()));
            membership.setIsActive(true);
            membership.setTotalOrders(1);
            save(membership);
            log.debug("首次会员信息保存成功: userId={}, expireTime={}", order.getUserId(), membership.getExpireTime());
        } else {
            // 续费或新购
            log.debug("续费或重新购买会员: userId={}, existingExpireTime={}, isActive={}", 
                    order.getUserId(), membership.getExpireTime(), membership.getIsActive());
            
            if (membership.getIsActive() && membership.getExpireTime() != null && membership.getExpireTime().isAfter(now)) {
                // 在原有基础上延长
                expireTime = membership.getExpireTime().plusDays(card.getDurationDays());
                log.debug("在原有基础上延长: oldExpireTime={}, newExpireTime={}", membership.getExpireTime(), expireTime);
            } else {
                // 已过期或从未激活，重新计算
                membership.setStartTime(now);
                expireTime = now.plusDays(card.getDurationDays());
                log.debug("重新计算会员时间: startTime={}, expireTime={}", now, expireTime);
            }

            membership.setMembershipType(typeName);
            membership.setExpireTime(expireTime);
            membership.setIsActive(true);
            membership.setTotalOrders(membership.getTotalOrders() + 1);
            updateById(membership);
            log.debug("会员信息更新成功: userId={}, totalOrders={}", order.getUserId(), membership.getTotalOrders());
        }

        log.info("会员激活成功: userId={}, cardId={}, typeName={}, expireTime={}",
                order.getUserId(), order.getCardId(), typeName, membership.getExpireTime());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void extendMembership(Long userId, Integer durationDays, String membershipType) {
        UserMembership membership = userMembershipMapper.selectByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        if (membership == null) {
            membership = new UserMembership();
            membership.setUserId(userId);
            membership.setMembershipType(membershipType);
            membership.setStartTime(now);
            membership.setExpireTime(now.plusDays(durationDays));
            membership.setIsActive(true);
            membership.setTotalOrders(1);
            save(membership);
        } else {
            if (membership.getIsActive() && membership.getExpireTime() != null && membership.getExpireTime().isAfter(now)) {
                membership.setExpireTime(membership.getExpireTime().plusDays(durationDays));
            } else {
                membership.setStartTime(now);
                membership.setExpireTime(now.plusDays(durationDays));
            }
            membership.setMembershipType(membershipType);
            membership.setIsActive(true);
            membership.setTotalOrders(membership.getTotalOrders() + 1);
            updateById(membership);
        }
    }

    @Override
    public boolean checkMembershipValid(Long userId) {
        UserMembership membership = userMembershipMapper.selectByUserId(userId);
        if (membership == null || !membership.getIsActive()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (membership.getExpireTime() == null || membership.getExpireTime().isBefore(now)) {
            // 更新过期状态
            membership.setIsActive(false);
            updateById(membership);
            return false;
        }

        return true;
    }

    @Override
    public PurchaseCheckVO checkPurchaseEligibility(Long userId, Long cardId) {
        log.debug("购卡前校验: userId={}, cardId={}", userId, cardId);

        PurchaseCheckVO vo = new PurchaseCheckVO();
        vo.setHasExistingMembership(false);

        UserMembership membership = userMembershipMapper.selectByUserId(userId);
        if (membership == null) {
            log.debug("用户无会员记录: userId={}", userId);
            return vo;
        }

        if (!Boolean.TRUE.equals(membership.getIsActive())) {
            log.debug("会员未激活: userId={}", userId);
            return vo;
        }

        LocalDateTime now = LocalDateTime.now();
        if (membership.getExpireTime() == null || membership.getExpireTime().isBefore(now)) {
            log.info("会员已过期，更新状态: userId={}, expireTime={}", userId, membership.getExpireTime());
            membership.setIsActive(false);
            updateById(membership);
            return vo;
        }

        long remainingDays = ChronoUnit.DAYS.between(now, membership.getExpireTime());
        int days = (int) Math.max(0, remainingDays);

        vo.setHasExistingMembership(true);
        vo.setMembershipType(membership.getMembershipType());
        vo.setRemainingDays(days);
        vo.setWarningMessage(String.format("您当前已有%s会员（剩余%d天），购买新卡后将自动续期延长有效期",
                membership.getMembershipType() != null ? membership.getMembershipType() : "会员", days));

        log.debug("购卡校验结果: userId={}, hasExistingMembership=true, membershipType={}, remainingDays={}",
                userId, vo.getMembershipType(), vo.getRemainingDays());
        return vo;
    }

    private UserMembershipVO convertToVO(UserMembership membership) {
        UserMembershipVO vo = new UserMembershipVO();
        BeanUtil.copyProperties(membership, vo);
        return vo;
    }
}
