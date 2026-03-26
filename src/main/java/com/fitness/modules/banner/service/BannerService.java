package com.fitness.modules.banner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fitness.modules.banner.model.dto.BannerDTO;
import com.fitness.modules.banner.model.entity.Banner;
import com.fitness.modules.banner.model.vo.BannerVO;

import java.util.List;

/**
 * 轮播图Service接口
 */
public interface BannerService extends IService<Banner> {

    /**
     * 获取所有显示的轮播图
     *
     * @return 轮播图VO列表
     */
    List<BannerVO> getActiveBanners();

    /**
     * 获取所有轮播图（管理后台使用）
     *
     * @return 轮播图VO列表
     */
    List<BannerVO> getAllBanners();

    /**
     * 根据ID获取轮播图
     *
     * @param id 轮播图ID
     * @return 轮播图VO
     */
    BannerVO getBannerById(Long id);

    /**
     * 创建轮播图
     *
     * @param bannerDTO 轮播图DTO
     * @return 创建后的轮播图VO
     */
    BannerVO createBanner(BannerDTO bannerDTO);

    /**
     * 更新轮播图
     *
     * @param id        轮播图ID
     * @param bannerDTO 轮播图DTO
     * @return 更新后的轮播图VO
     */
    BannerVO updateBanner(Long id, BannerDTO bannerDTO);

    /**
     * 删除轮播图
     *
     * @param id 轮播图ID
     */
    void deleteBanner(Long id);

    /**
     * 批量删除轮播图
     *
     * @param ids 轮播图ID列表
     */
    void deleteBanners(List<Long> ids);

    /**
     * 更新轮播图状态
     *
     * @param id     轮播图ID
     * @param status 状态：0-隐藏，1-显示
     */
    void updateBannerStatus(Long id, Integer status);
}
