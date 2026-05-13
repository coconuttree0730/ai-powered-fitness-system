package com.fitness.modules.user.service;

import com.fitness.modules.user.model.dto.CoachDetailDTO;
import com.fitness.modules.user.model.vo.CoachDetailVO;
import com.fitness.modules.user.model.vo.HomePageCoachVO;
import com.fitness.modules.user.model.vo.MyPrivateCoachVO;
import com.fitness.modules.product.model.vo.ProductVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 教练详情Service接口
 */
public interface CoachDetailService {

    /**
     * 获取教练详情
     *
     * @param userId 用户ID
     * @return 教练详情VO
     */
    CoachDetailVO getCoachDetail(Long userId);

    /**
     * 获取当前登录教练的详情
     *
     * @return 教练详情VO
     */
    CoachDetailVO getCurrentCoachDetail();

    /**
     * 更新教练详情
     *
     * @param dto 教练详情DTO
     * @return 更新后的教练详情VO
     */
    CoachDetailVO updateCoachDetail(CoachDetailDTO dto);

    /**
     * 上传个人展示图片
     *
     * @param file 图片文件
     * @return 图片URL
     */
    String uploadPersonalImage(MultipartFile file);

    /**
     * 删除个人展示图片
     */
    void deletePersonalImage();

    /**
     * 更新教练标签
     *
     * @param tags 标签列表
     * @return 更新后的教练详情VO
     */
    CoachDetailVO updateTags(List<String> tags);

    /**
     * 获取首页展示的教练列表
     *
     * @param limit 限制数量
     * @return 首页教练VO列表
     */
    List<HomePageCoachVO> getHomePageCoaches(int limit);

    /**
     * 初始化教练详情（用于新教练注册时）
     *
     * @param userId 用户ID
     */
    void initCoachDetail(Long userId);

    /**
     * 获取当前登录会员的专属教练
     *
     * @return 专属教练信息，如果没有则返回null
     */
    MyPrivateCoachVO getMyPrivateCoach();

    /**
     * 获取指定教练的可购买私教套餐列表
     *
     * @param coachId 教练用户ID
     * @return 套餐商品列表
     */
    List<ProductVO> getCoachPackages(Long coachId);
}
