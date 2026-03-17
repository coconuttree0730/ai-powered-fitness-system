package com.fitness.modules.plan.service;

import com.fitness.modules.plan.model.dto.UserProfileDTO;

import java.util.Map;

/**
 * 用户个人信息服务接口
 */
public interface UserProfileService {

    /**
     * 保存用户个人信息
     *
     * @param userId 用户ID
     * @param dto    个人信息
     */
    void saveProfile(Long userId, UserProfileDTO dto);

    /**
     * 获取用户个人信息
     *
     * @param userId 用户ID
     * @return 个人信息
     */
    Map<String, Object> getProfile(Long userId);

    /**
     * 检查用户是否已完善个人信息
     *
     * @param userId 用户ID
     * @return true-已完善, false-未完善
     */
    boolean isProfileComplete(Long userId);
}
