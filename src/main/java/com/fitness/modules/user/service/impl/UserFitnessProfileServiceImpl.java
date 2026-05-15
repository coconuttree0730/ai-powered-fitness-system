package com.fitness.modules.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.modules.user.mapper.UserFitnessProfileMapper;
import com.fitness.modules.user.model.dto.UserFitnessProfileDTO;
import com.fitness.modules.user.model.entity.UserFitnessProfile;
import com.fitness.modules.user.model.vo.UserFitnessProfileVO;
import com.fitness.modules.user.service.UserFitnessProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserFitnessProfileServiceImpl extends ServiceImpl<UserFitnessProfileMapper, UserFitnessProfile> implements UserFitnessProfileService {

    @Override
    public UserFitnessProfileVO getProfile(Long userId) {
        UserFitnessProfile profile = baseMapper.selectByUserId(userId);
        if (profile == null) {
            return null;
        }
        return convertToVO(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserFitnessProfileVO saveOrUpdateProfile(Long userId, UserFitnessProfileDTO dto) {
        // 查询是否已存在档案
        UserFitnessProfile existingProfile = baseMapper.selectByUserId(userId);

        UserFitnessProfile profile;
        if (existingProfile == null) {
            // 创建新档案
            profile = new UserFitnessProfile();
            profile.setUserId(userId);
            BeanUtil.copyProperties(dto, profile);
            this.save(profile);
            log.info("创建用户健身档案成功, userId: {}", userId);
        } else {
            // 更新现有档案
            profile = existingProfile;
            BeanUtil.copyProperties(dto, profile);
            this.updateById(profile);
            log.info("更新用户健身档案成功, userId: {}", userId);
        }

        return convertToVO(profile);
    }

    @Override
    public boolean isProfileComplete(Long userId) {
        UserFitnessProfile profile = baseMapper.selectByUserId(userId);
        if (profile == null) {
            return false;
        }
        // 检查必填字段是否都已填写
        return profile.getHeight() != null
                && profile.getWeight() != null
                && profile.getAge() != null
                && profile.getExperience() != null
                && profile.getFitnessGoal() != null
                && profile.getGender() != null;
    }

    /**
     * 转换为VO对象
     *
     * @param profile 实体对象
     * @return VO对象
     */
    private UserFitnessProfileVO convertToVO(UserFitnessProfile profile) {
        UserFitnessProfileVO vo = new UserFitnessProfileVO();
        BeanUtil.copyProperties(profile, vo);
        return vo;
    }
}
