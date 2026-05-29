package com.fitness.modules.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.modules.user.model.entity.CoachStudent;
import com.fitness.modules.user.model.vo.CoachStudentVO;

import java.util.List;

public interface CoachStudentService extends IService<CoachStudent> {

    CoachStudentVO bindStudent(Long memberId, Long coachId, Long coachPackageId, String packageCode, Integer validityDays);

    List<CoachStudentVO> getMyStudents(Long coachId);

    CoachStudentVO getBinding(Long memberId, Long coachId);

    boolean isBound(Long memberId, Long coachId);
}