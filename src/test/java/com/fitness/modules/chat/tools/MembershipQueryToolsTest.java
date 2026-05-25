package com.fitness.modules.chat.tools;

import com.fitness.modules.membership.model.vo.MembershipCardVO;
import com.fitness.modules.membership.service.MembershipCardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembershipQueryToolsTest {

    @Mock
    private MembershipCardService membershipCardService;

    @InjectMocks
    private MembershipQueryTools membershipQueryTools;

    @Test
    void listActiveMembershipCardsShouldReturnServiceData() {
        MembershipCardVO card = new MembershipCardVO();
        card.setName("月卡");
        when(membershipCardService.listActiveCards()).thenReturn(List.of(card));

        List<MembershipCardVO> result = membershipQueryTools.listActiveMembershipCards();

        assertEquals(1, result.size());
        assertEquals("月卡", result.get(0).getName());
    }
}
