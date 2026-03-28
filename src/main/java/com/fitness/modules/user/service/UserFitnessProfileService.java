package com.fitness.modules.user.service;

import com.fitness.modules.user.model.dto.UserFitnessProfileDTO;
import com.fitness.modules.user.model.vo.UserFitnessProfileVO;

/**
 * 用户健身档案服务接口
 */
public interface UserFitnessProfileService {

    /**
     * 获取用户健身档案
     *
     * @param userId 用户ID
     * @return 健身档案VO
     */
    UserFitnessProfileVO getProfile(Long userId);

    /**
     * 创建或更新用户健身档案
     *
     * @param userId 用户ID
     * @param dto    健身档案DTO
     * @return 更新后的健身档案VO
     */
    UserFitnessProfileVO saveOrUpdateProfile(Long userId, UserFitnessProfileDTO dto);

    /**
     * 检查用户是否已完善健身档案
     *
     * @param userId 用户ID
     * @return true-已完善，false-未完善
     */
    boolean isProfileComplete(Long userId);
}
