package com.fitness.modules.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.modules.user.mapper.CoachStudentMapper;
import com.fitness.modules.user.mapper.UserFitnessProfileMapper;
import com.fitness.modules.user.model.entity.CoachStudent;
import com.fitness.modules.user.model.entity.UserFitnessProfile;
import com.fitness.modules.user.model.vo.CoachStudentVO;
import com.fitness.modules.user.service.CoachStudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoachStudentServiceImpl extends ServiceImpl<CoachStudentMapper, CoachStudent>
        implements CoachStudentService {

    private final CoachStudentMapper coachStudentMapper;
    private final UserFitnessProfileMapper userFitnessProfileMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CoachStudentVO bindStudent(Long memberId, Long coachId, Long coachPackageId,
                                       String packageCode, Integer validityDays) {
        log.info("绑定教练学员关系: memberId={}, coachId={}, coachPackageId={}, packageCode={}, validityDays={}",
                memberId, coachId, coachPackageId, packageCode, validityDays);

        CoachStudent existing = coachStudentMapper.selectByMemberAndCoach(memberId, coachId);
        if (existing != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime baseExpireTime = existing.getExpireTime() != null && existing.getExpireTime().isAfter(now)
                    ? existing.getExpireTime()
                    : now;
            existing.setCoachPackageId(coachPackageId);
            existing.setPackageCode(packageCode);
            existing.setExpireTime(baseExpireTime.plusDays(validityDays != null ? validityDays : 365));
            existing.setStatus("ACTIVE");
            coachStudentMapper.updateById(existing);
            updatePrivateCoachProfile(memberId, coachId);
            log.info("教练-学员绑定续期成功: bindingId={}", existing.getId());
            return toVO(existing, null, null);
        }

        CoachStudent binding = new CoachStudent();
        binding.setMemberId(memberId);
        binding.setCoachId(coachId);
        binding.setCoachPackageId(coachPackageId);
        binding.setPackageCode(packageCode);
        binding.setBindTime(LocalDateTime.now());
        binding.setExpireTime(LocalDateTime.now().plusDays(validityDays != null ? validityDays : 365));
        binding.setStatus("ACTIVE");

        coachStudentMapper.insert(binding);

        updatePrivateCoachProfile(memberId, coachId);

        log.info("教练-学员绑定成功: bindingId={}", binding.getId());
        return toVO(binding, null, null);
    }

    private void updatePrivateCoachProfile(Long memberId, Long coachId) {
        UserFitnessProfile profile = userFitnessProfileMapper.selectByUserId(memberId);
        if (profile == null) {
            profile = new UserFitnessProfile();
            profile.setUserId(memberId);
            profile.setPrivateCoachId(coachId);
            userFitnessProfileMapper.insert(profile);
        } else {
            profile.setPrivateCoachId(coachId);
            userFitnessProfileMapper.updateById(profile);
        }
    }

    @Override
    public List<CoachStudentVO> getMyStudents(Long coachId) {
        log.info("查询教练的学员列表: coachId={}", coachId);
        return coachStudentMapper.selectByCoachId(coachId).stream()
                .peek(this::normalizeStudent)
                .toList();
    }

    @Override
    public CoachStudentVO getBinding(Long memberId, Long coachId) {
        log.info("查询学员-教练绑定: memberId={}, coachId={}", memberId, coachId);
        CoachStudent binding = coachStudentMapper.selectByMemberAndCoach(memberId, coachId);
        if (binding == null) {
            return null;
        }
        return toVO(binding, null, null);
    }

    @Override
    public boolean isBound(Long memberId, Long coachId) {
        return getBinding(memberId, coachId) != null;
    }

    private CoachStudentVO toVO(CoachStudent entity, String studentName, String studentAvatar) {
        CoachStudentVO vo = new CoachStudentVO();
        vo.setId(entity.getId());
        vo.setMemberId(entity.getMemberId());
        vo.setCoachId(entity.getCoachId());
        vo.setCoachPackageId(entity.getCoachPackageId());
        vo.setPackageCode(entity.getPackageCode());
        vo.setBindTime(entity.getBindTime());
        vo.setExpireTime(entity.getExpireTime());
        vo.setStatus(entity.getStatus());
        vo.setStudentName(studentName);
        vo.setStudentAvatar(studentAvatar);
        return vo;
    }

    private void normalizeStudent(CoachStudentVO student) {
        if (student.getStudentName() == null || student.getStudentName().isBlank()) {
            student.setStudentName("学员" + Objects.toString(student.getMemberId(), ""));
        }
        if (student.getStudentPhone() == null) {
            student.setStudentPhone("");
        }
    }
}
