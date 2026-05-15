package com.fitness.modules.announcement.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.announcement.mapper.AnnouncementMapper;
import com.fitness.modules.announcement.model.dto.AnnouncementDTO;
import com.fitness.modules.announcement.model.entity.Announcement;
import com.fitness.modules.announcement.model.vo.AnnouncementVO;
import com.fitness.modules.announcement.service.AnnouncementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    @Override
    public List<AnnouncementVO> getPublishedAnnouncements() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        //条件匹配
        wrapper.eq(Announcement::getStatus, 1)
               .eq(Announcement::getDeleted, false)
               .orderByDesc(Announcement::getPublishTime);
        //获取公告列表
        List<Announcement> announcements = this.list(wrapper);
        return announcements.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AnnouncementVO> getAllAnnouncements() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getDeleted, false)
               .orderByDesc(Announcement::getCreateTime);
        List<Announcement> announcements = this.list(wrapper);
        return announcements.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public AnnouncementVO getAnnouncementById(Long id) {
        Announcement announcement = this.getById(id);
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
        this.save(announcement);
        return convertToVO(announcement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AnnouncementVO updateAnnouncement(Long id, AnnouncementDTO announcementDTO) {
        Announcement existing = this.getById(id);
        if (existing == null || existing.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        BeanUtil.copyProperties(announcementDTO, existing);

        // 如果状态从草稿变为已发布，设置发布时间
        if (announcementDTO.getStatus() != null && announcementDTO.getStatus() == 1 && existing.getPublishTime() == null) {
            existing.setPublishTime(LocalDateTime.now());
        }

        this.updateById(existing);
        return convertToVO(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnnouncement(Long id) {
        Announcement existing = this.getById(id);
        if (existing == null || existing.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnnouncements(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            //不存在直接返回
            return;
        }
        this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishAnnouncement(Long id) {
        Announcement existing = this.getById(id);
        if (existing == null || existing.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        existing.setStatus(1);
        existing.setPublishTime(LocalDateTime.now());
        this.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unpublishAnnouncement(Long id) {
        Announcement existing = this.getById(id);
        if (existing == null || existing.getDeleted()) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        existing.setStatus(0);
        this.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long id) {
        baseMapper.incrementViewCount(id);
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
