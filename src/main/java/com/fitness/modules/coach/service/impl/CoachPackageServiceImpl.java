package com.fitness.modules.coach.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.PageResult;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        coachPackage.setStatus("INACTIVE");
        updateById(coachPackage);

        log.info("下架教练套餐成功: id={}, coachId={}", id, coachId);
    }

    @Override
    public PageResult<CoachPackageVO> getAdminPage(int page, int pageSize, String keyword, String status, Long coachId) {
        Page<CoachPackage> mpPage = new Page<>(page, pageSize);
        IPage<CoachPackage> result = coachPackageMapper.selectAdminPage(mpPage, keyword, status, coachId);

        List<CoachPackage> records = result.getRecords();
        if (records.isEmpty()) {
            return PageResult.of(List.of(), 0L);
        }

        Set<Long> coachIds = records.stream()
                .map(CoachPackage::getCoachId)
                .collect(Collectors.toSet());
        Map<Long, User> coachMap = coachIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(coachIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        List<CoachPackageVO> voList = records.stream()
                .map(pkg -> convertToVO(pkg, coachMap))
                .collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CoachPackageVO createPackageAsAdmin(CoachPackageDTO dto, Long coachId) {
        log.info("管理员创建套餐: coachId={}, name={}", coachId, dto.getName());
        return createPackage(dto, coachId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CoachPackageVO updatePackageAsAdmin(Long id, CoachPackageDTO dto) {
        log.info("管理员更新套餐: id={}", id);

        CoachPackage coachPackage = getById(id);
        if (coachPackage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "套餐不存在");
        }

        BeanUtil.copyProperties(dto, coachPackage, "id", "coachId", "createTime", "updateTime");
        if (dto.getCoachId() != null) {
            coachPackage.setCoachId(dto.getCoachId());
        }
        updateById(coachPackage);

        return convertToVO(coachPackage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatusAsAdmin(Long id, String status) {
        log.info("管理员更新套餐状态: id={}, status={}", id, status);

        CoachPackage coachPackage = getById(id);
        if (coachPackage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "套餐不存在");
        }

        coachPackage.setStatus(status);
        updateById(coachPackage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePackageAsAdmin(Long id) {
        log.info("管理员删除套餐: id={}", id);

        CoachPackage coachPackage = getById(id);
        if (coachPackage == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "套餐不存在");
        }

        coachPackage.setStatus("INACTIVE");
        updateById(coachPackage);
    }

    private CoachPackageVO convertToVO(CoachPackage coachPackage) {
        CoachPackageVO vo = new CoachPackageVO();
        BeanUtil.copyProperties(coachPackage, vo);

        if (coachPackage.getCoachId() != null) {
            User coach = userMapper.selectById(coachPackage.getCoachId());
            if (coach != null) {
                vo.setCoachName(coach.getNickname() != null ? coach.getNickname() : coach.getUsername());
            }
        }

        return vo;
    }

    private CoachPackageVO convertToVO(CoachPackage coachPackage, Map<Long, User> coachMap) {
        CoachPackageVO vo = new CoachPackageVO();
        BeanUtil.copyProperties(coachPackage, vo);

        User coach = coachMap.get(coachPackage.getCoachId());
        if (coach != null) {
            vo.setCoachName(coach.getNickname() != null ? coach.getNickname() : coach.getUsername());
        }

        return vo;
    }
}