package com.fitness.modules.chat.tools;

import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.user.model.vo.UserFitnessProfileVO;
import com.fitness.modules.user.service.UserFitnessProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfileQueryTools implements ToolRiskAware {

    @Override
    public ToolRiskLevel getRiskLevel() {
        return ToolRiskLevel.LOW;
    }

    private final UserFitnessProfileService userFitnessProfileService;

    @Tool(description = """
            查询当前会员的健身档案信息。
            当用户询问"我的健身档案"、"我的体测数据"、"我的BMI"、"我的健身目标"、"我的身体状况"等问题时，必须调用此工具。
            返回身高、体重、BMI、体脂率、健身目标、运动偏好、身体状况等信息。
            """)
    public UserFitnessProfileVO getCurrentUserProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userId == null ? null : userFitnessProfileService.getProfile(userId);
    }
}
