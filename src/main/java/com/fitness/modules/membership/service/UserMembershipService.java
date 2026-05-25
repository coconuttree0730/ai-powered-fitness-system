package com.fitness.modules.membership.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.modules.membership.model.entity.MembershipOrder;
import com.fitness.modules.membership.model.entity.UserMembership;
import com.fitness.modules.membership.model.vo.UserMembershipVO;

public interface UserMembershipService extends IService<UserMembership> {

    UserMembershipVO getUserMembership(Long userId);

    void activateMembership(MembershipOrder order);

    void extendMembership(Long userId, Integer durationDays, String membershipType);

    boolean checkMembershipValid(Long userId);
}
