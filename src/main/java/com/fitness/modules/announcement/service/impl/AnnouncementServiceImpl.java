package com.fitness.modules.announcement.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.constants.ErrorCode;
import com.fitness.modules.announcement.mapper.AnnouncementMapper;
import com.fitness.modules.announcement.model.dto.AnnouncementDTO;
import com.fitness.modules.announcement.model.entity.Announcement;
import com.fitness.modules.announcement.model.vo.AnnouncementVO;
import com.fitness.modules.announcement.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告Service实现类
 */
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;

    @Override
    public List<AnnouncementVO> getPublishedAnnouncements() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        //条件匹配
        wrapper.eq(Announcement::getStatus, 1)
               .eq(Announcement::getDeleted, false)
               .orderByDesc(Announcement::getPublishTime);
        //获取公告列表
        List<Announcement> announcements = announcementMapper.selectList(wrapper);
        return announcements.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnouncementVO> getAllAnnouncements() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getDeleted, false)
               .orderByDesc(Announcement::getCreateTime);
        List<Announcement> announcements = announcementMapper.selectList(wrapper);
        return announcements.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public AnnouncementVO getAnnouncementById(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null || announcement.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        return convertToVO(announcement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnnouncementVO createAnnouncement(AnnouncementDTO announcementDTO) {
        Announcement announcement = new Announcement();
        BeanUtil.copyProperties(announcementDTO, announcement);

        // 如果状态为已发布，设置发布时间
        if (announcement.getStatus() != null && announcement.getStatus() == 1) {
            announcement.setPublishTime(LocalDateTime.now());
        }

        announcement.setViewCount(0);
        announcementMapper.insert(announcement);
        return convertToVO(announcement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnnouncementVO updateAnnouncement(Long id, AnnouncementDTO announcementDTO) {
        Announcement existing = announcementMapper.selectById(id);
        if (existing == null || existing.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        BeanUtil.copyProperties(announcementDTO, existing);

        // 如果状态从草稿变为已发布，设置发布时间
        if (announcementDTO.getStatus() != null && announcementDTO.getStatus() == 1 && existing.getPublishTime() == null) {
            existing.setPublishTime(LocalDateTime.now());
        }

        announcementMapper.updateById(existing);
        return convertToVO(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnnouncement(Long id) {
        Announcement existing = announcementMapper.selectById(id);
        if (existing == null || existing.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        announcementMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnnouncements(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            //不存在直接返回
            return;
        }
        announcementMapper.deleteByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishAnnouncement(Long id) {
        Announcement existing = announcementMapper.selectById(id);
        if (existing == null || existing.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        existing.setStatus(1);
        existing.setPublishTime(LocalDateTime.now());
        announcementMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpublishAnnouncement(Long id) {
        Announcement existing = announcementMapper.selectById(id);
        if (existing == null || existing.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        existing.setStatus(0);
        announcementMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long id) {
        announcementMapper.incrementViewCount(id);
    }

    /**
     * 转换为VO
     */
    private AnnouncementVO convertToVO(Announcement announcement) {
        AnnouncementVO vo = new AnnouncementVO();
        BeanUtil.copyProperties(announcement, vo);
        return vo;
    }
}
