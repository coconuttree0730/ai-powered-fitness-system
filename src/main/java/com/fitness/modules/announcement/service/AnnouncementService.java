package com.fitness.modules.announcement.service;

import com.fitness.modules.announcement.model.dto.AnnouncementDTO;
import com.fitness.modules.announcement.model.vo.AnnouncementVO;

import java.util.List;

/**
 * 公告Service接口
 */
public interface AnnouncementService {

    /**
     * 获取所有已发布的公告（前台展示）
     *
     * @return 公告列表
     */
    List<AnnouncementVO> getPublishedAnnouncements();

    /**
     * 获取所有公告（管理后台使用）
     *
     * @return 公告列表
     */
    List<AnnouncementVO> getAllAnnouncements();

    /**
     * 根据ID获取公告详情
     *
     * @param id 公告ID
     * @return 公告详情
     */
    AnnouncementVO getAnnouncementById(Long id);

    /**
     * 创建公告
     *
     * @param announcementDTO 公告信息
     * @return 创建后的公告
     */
    AnnouncementVO createAnnouncement(AnnouncementDTO announcementDTO);

    /**
     * 更新公告
     *
     * @param id              公告ID
     * @param announcementDTO 公告信息
     * @return 更新后的公告
     */
    AnnouncementVO updateAnnouncement(Long id, AnnouncementDTO announcementDTO);

    /**
     * 删除公告
     *
     * @param id 公告ID
     */
    void deleteAnnouncement(Long id);

    /**
     * 批量删除公告
     *
     * @param ids 公告ID列表
     */
    void deleteAnnouncements(List<Long> ids);

    /**
     * 发布公告
     *
     * @param id 公告ID
     */
    void publishAnnouncement(Long id);

    /**
     * 下架公告
     *
     * @param id 公告ID
     */
    void unpublishAnnouncement(Long id);

    /**
     * 增加浏览量
     *
     * @param id 公告ID
     */
    void incrementViewCount(Long id);
}
