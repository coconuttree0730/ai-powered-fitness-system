package com.fitness.modules.chat.tools;

import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.membership.model.vo.MembershipCardVO;
import com.fitness.modules.membership.model.vo.UserMembershipVO;
import com.fitness.modules.membership.service.MembershipCardService;
import com.fitness.modules.membership.service.UserMembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MembershipQueryTools {

    private final MembershipCardService membershipCardService;
    private final UserMembershipService userMembershipService;

    @Tool(description = """
            查询当前可售的会员卡列表，包括月卡、季卡、年卡、次卡等类型。
            当用户询问"有什么会员卡"、"会员卡价格"、"办卡多少钱"、"会员套餐"等问题时，必须调用此工具。
            返回会员卡类型、价格、有效期、包含权益等信息。
            """)
    public List<MembershipCardVO> listActiveMembershipCards() {
        return membershipCardService.listActiveCards();
    }

    @Tool(description = """
            查询指定会员卡的详细信息。
            当用户询问某个具体会员卡的价格、权益、使用规则、有效期等问题时，必须调用此工具。
            例如"年卡多少钱"、"次卡有什么权益"、"这个卡怎么使用"等。
            """)
    public MembershipCardVO getMembershipCardDetail(Long cardId) {
        return membershipCardService.getCardDetail(cardId);
    }

    @Tool(description = """
            查询当前会员自己的会籍状态。
            当用户询问"我的会员什么时候到期"、"我的会籍状态"、"我还剩多少天"、"我的卡还有效吗"等问题时，必须调用此工具。
            返回会员类型、开始时间、到期时间、剩余次数、使用状态等信息。
            """)
    public UserMembershipVO getCurrentUserMembership() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userId == null ? null : userMembershipService.getUserMembership(userId);
    }
}
