package com.fitness.modules.coach.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.modules.coach.model.dto.CoachPackageDTO;
import com.fitness.modules.coach.model.entity.CoachPackage;
import com.fitness.modules.coach.model.vo.CoachPackageVO;

import java.util.List;

/**
 * 教练套餐服务接口
 */
public interface CoachPackageService extends IService<CoachPackage> {

    /**
     * 创建套餐
     *
     * @param dto     套餐信息
     * @param coachId 教练ID
     * @return 套餐视图对象
     */
    CoachPackageVO createPackage(CoachPackageDTO dto, Long coachId);

    /**
     * 更新套餐
     *
     * @param id      套餐ID
     * @param dto     套餐信息
     * @param coachId 教练ID
     * @return 套餐视图对象
     */
    CoachPackageVO updatePackage(Long id, CoachPackageDTO dto, Long coachId);

    /**
     * 获取套餐详情
     *
     * @param id 套餐ID
     * @return 套餐视图对象
     */
    CoachPackageVO getPackageDetail(Long id);

    /**
     * 获取教练的套餐列表
     *
     * @param coachId 教练ID
     * @return 套餐视图对象列表
     */
    List<CoachPackageVO> getCoachPackages(Long coachId);

    /**
     * 获取所有上架套餐
     *
     * @return 套餐视图对象列表
     */
    List<CoachPackageVO> getAllActivePackages();

    /**
     * 更新套餐状态
     *
     * @param id      套餐ID
     * @param status  状态
     * @param coachId 教练ID
     */
    void updateStatus(Long id, String status, Long coachId);

    /**
     * 删除（下架）套餐
     *
     * @param id      套餐ID
     * @param coachId 教练ID
     */
    void deletePackage(Long id, Long coachId);
}