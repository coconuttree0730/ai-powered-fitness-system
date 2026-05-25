package com.fitness.modules.chat.tools;

import com.fitness.modules.user.model.vo.HomePageCoachVO;
import com.fitness.modules.user.model.vo.MyPrivateCoachVO;
import com.fitness.modules.user.service.CoachDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CoachQueryTools {

    private final CoachDetailService coachDetailService;

    @Tool(description = """
            查询健身场馆的教练列表，包括私教、团课教练等。
            当用户询问"有哪些教练"、"教练团队"、"推荐教练"、"查看教练"等问题时，必须调用此工具。
            返回教练姓名、专长领域、资质认证、教学经验等信息。
            """)
    public List<HomePageCoachVO> listCoaches(Integer limit) {
        int size = limit == null || limit <= 0 ? 6 : Math.min(limit, 10);
        return coachDetailService.getHomePageCoaches(size);
    }

    @Tool(description = """
            查询当前会员绑定的专属私教信息。
            当用户询问"我的私教是谁"、"我的教练"、"分配给我的教练"等问题时，必须调用此工具。
            返回私教姓名、联系方式、专长、上课时间等信息。
            """)
    public MyPrivateCoachVO getMyPrivateCoach() {
        return coachDetailService.getMyPrivateCoach();
    }
}
