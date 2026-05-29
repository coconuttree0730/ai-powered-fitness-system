package com.fitness.modules.coach.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.coach.mapper.CoachPackageMapper;
import com.fitness.modules.coach.model.dto.CoachPackageDTO;
import com.fitness.modules.coach.model.entity.CoachPackage;
import com.fitness.modules.coach.model.vo.CoachPackageVO;
import com.fitness.modules.coach.service.CoachPackageService;
import com.fitness.modules.user.mapper.UserMapper;
import com.fitness.modules.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 教练套餐服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoachPackageServiceImpl extends ServiceImpl<CoachPackageMapper, CoachPackage> implements CoachPackageService {

    private final CoachPackageMapper coachPackageMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CoachPackageVO createPackage(CoachPackageDTO dto, Long coachId) {
        log.debug("开始创建教练套餐: coachId={}, name={}", coachId, dto.getName());

        CoachPackage coachPackage = new CoachPackage();
        BeanUtil.copyProperties(dto, coachPackage);
        coachPackage.setCoachId(coachId);

        // 默认状态和排序
        if (coachPackage.getStatus() == null) {
            coachPackage.setStatus("ACTIVE");
        }
        if (coachPackage.getSortOrder() == null) {
            coachPackage.setSortOrder(0);
        }

        save(coachPackage);

        log.info("创建教练套餐成功: id={}, coachId={}, name={}", coachPackage.getId(), coachId, coachPackage.getName());
        return convertToVO(coachPackage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CoachPackageVO updatePackage(Long id, CoachPackageDTO dto, Long coachId) {
        log.debug("开始更新教练套餐: id={}, coachId={}", id, coachId);

        CoachPackage coachPackage = getById(id);
        if (coachPackage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "套餐不存在");
        }
        if (!coachPackage.getCoachId().equals(coachId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此套餐");
        }

        BeanUtil.copyProperties(dto, coachPackage, "id", "coachId", "createTime", "updateTime");
        updateById(coachPackage);

        log.info("更新教练套餐成功: id={}, coachId={}", id, coachId);
        return convertToVO(coachPackage);
    }

    @Override
    public CoachPackageVO getPackageDetail(Long id) {
        CoachPackage coachPackage = getById(id);
        if (coachPackage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "套餐不存在");
        }
        return convertToVO(coachPackage);
    }

    @Override
    public List<CoachPackageVO> getCoachPackages(Long coachId) {
        List<CoachPackage> packages = coachPackageMapper.selectByCoachId(coachId);
        return packages.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<CoachPackageVO> getAllActivePackages() {
        List<CoachPackage> packages = coachPackageMapper.selectAllActive();
        return packages.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status, Long coachId) {
        log.debug("开始更新套餐状态: id={}, status={}, coachId={}", id, status, coachId);

        CoachPackage coachPackage = getById(id);
        if (coachPackage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "套餐不存在");
        }
        if (!coachPackage.getCoachId().equals(coachId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此套餐");
        }

        coachPackage.setStatus(status);
        updateById(coachPackage);

        log.info("更新套餐状态成功: id={}, status={}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePackage(Long id, Long coachId) {
        log.debug("开始删除套餐: id={}, coachId={}", id, coachId);

        CoachPackage coachPackage = getById(id);
        if (coachPackage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "套餐不存在");
        }
        if (!coachPackage.getCoachId().equals(coachId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作此套餐");
        }

        // 逻辑删除：将状态设为 INACTIVE
        coachPackage.setStatus("INACTIVE");
        updateById(coachPackage);

        log.info("下架教练套餐成功: id={}, coachId={}", id, coachId);
    }

    /**
     * 将实体转换为视图对象
     */
    private CoachPackageVO convertToVO(CoachPackage coachPackage) {
        CoachPackageVO vo = new CoachPackageVO();
        BeanUtil.copyProperties(coachPackage, vo);

        // 填充教练名称
        if (coachPackage.getCoachId() != null) {
            User coach = userMapper.selectById(coachPackage.getCoachId());
            if (coach != null) {
                vo.setCoachName(coach.getNickname() != null ? coach.getNickname() : coach.getUsername());
            }
        }

        return vo;
    }
}