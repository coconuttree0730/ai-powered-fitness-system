package com.fitness.modules.plan.service.impl;

import com.fitness.modules.plan.model.dto.UserProfileDTO;
import com.fitness.modules.plan.service.UserProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户个人信息服务实现类
 * 使用内存存储，实际项目中应该使用数据库存储
 */
@Slf4j
@Service
public class UserProfileServiceImpl implements UserProfileService {

    // 使用ConcurrentHashMap存储用户个人信息（实际项目中应使用数据库）
    private final Map<Long, Map<String, Object>> userProfiles = new ConcurrentHashMap<>();

    @Override
    public void saveProfile(Long userId, UserProfileDTO dto) {
        Map<String, Object> profile = new ConcurrentHashMap<>();
        profile.put("height", dto.getHeight());
        profile.put("weight", dto.getWeight());
        profile.put("age", dto.getAge());
        profile.put("experience", dto.getExperience());
        profile.put("fitnessGoal", dto.getFitnessGoal());
        profile.put("updatedAt", System.currentTimeMillis());

        userProfiles.put(userId, profile);
        log.info("用户个人信息保存成功: userId={}", userId);
    }

    @Override
    public Map<String, Object> getProfile(Long userId) {
        return userProfiles.get(userId);
    }

    @Override
    public boolean isProfileComplete(Long userId) {
        Map<String, Object> profile = userProfiles.get(userId);
        if (profile == null) {
            return false;
        }

        // 检查必要字段是否都存在
        return profile.get("height") != null
                && profile.get("weight") != null
                && profile.get("age") != null
                && profile.get("experience") != null
                && profile.get("fitnessGoal") != null;
    }
}
