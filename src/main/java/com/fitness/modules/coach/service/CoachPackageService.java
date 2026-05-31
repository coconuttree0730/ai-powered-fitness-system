package com.fitness.modules.coach.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.common.result.PageResult;
import com.fitness.modules.coach.model.dto.CoachPackageDTO;
import com.fitness.modules.coach.model.entity.CoachPackage;
import com.fitness.modules.coach.model.vo.CoachPackageVO;

import java.util.List;

public interface CoachPackageService extends IService<CoachPackage> {

    CoachPackageVO createPackage(CoachPackageDTO dto, Long coachId);

    CoachPackageVO updatePackage(Long id, CoachPackageDTO dto, Long coachId);

    CoachPackageVO getPackageDetail(Long id);

    List<CoachPackageVO> getCoachPackages(Long coachId);

    List<CoachPackageVO> getAllActivePackages();

    void updateStatus(Long id, String status, Long coachId);

    void deletePackage(Long id, Long coachId);

    PageResult<CoachPackageVO> getAdminPage(int page, int pageSize, String keyword, String status, Long coachId);

    CoachPackageVO createPackageAsAdmin(CoachPackageDTO dto, Long coachId);

    CoachPackageVO updatePackageAsAdmin(Long id, CoachPackageDTO dto);

    void updateStatusAsAdmin(Long id, String status);

    void deletePackageAsAdmin(Long id);
}