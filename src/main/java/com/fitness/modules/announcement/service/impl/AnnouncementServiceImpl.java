package com.fitness.modules.announcement.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.cache.RedisCacheNames;
import com.fitness.common.cache.RedisTemplateCacheSupport;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.announcement.mapper.AnnouncementMapper;
import com.fitness.modules.announcement.model.dto.AnnouncementDTO;
import com.fitness.modules.announcement.model.entity.Announcement;
import com.fitness.modules.announcement.model.vo.AnnouncementVO;
import com.fitness.modules.announcement.service.AnnouncementService;
import com.fitness.modules.ranking.service.RedisRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {

    private final RedisRankingService redisRankingService;
    private final RedisTemplateCacheSupport redisTemplateCacheSupport;

    @Override
    public List<AnnouncementVO> getPublishedAnnouncements() {
        return redisTemplateCacheSupport.getOrLoad(RedisCacheNames.PUBLISHED_ANNOUNCEMENTS, "all", () -> {
            LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Announcement::getStatus, 1)
                    .eq(Announcement::getDeleted, false)
                    .orderByDesc(Announcement::getPublishTime);
            List<Announcement> announcements = this.list(wrapper);
            List<AnnouncementVO> announcementVOs = announcements.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            log.debug("[DB LOAD] published announcements, count={}", announcementVOs.size());
            return announcementVOs;
        });
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

        if (announcement.getStatus() != null && announcement.getStatus() == 1) {
            announcement.setPublishTime(LocalDateTime.now());
        }

        announcement.setViewCount(0);
        this.save(announcement);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.PUBLISHED_ANNOUNCEMENTS);
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
        if (announcementDTO.getStatus() != null && announcementDTO.getStatus() == 1 && existing.getPublishTime() == null) {
            existing.setPublishTime(LocalDateTime.now());
        }

        this.updateById(existing);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.PUBLISHED_ANNOUNCEMENTS);
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
        redisTemplateCacheSupport.evictAll(RedisCacheNames.PUBLISHED_ANNOUNCEMENTS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAnnouncements(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        this.removeByIds(ids);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.PUBLISHED_ANNOUNCEMENTS);
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
        redisTemplateCacheSupport.evictAll(RedisCacheNames.PUBLISHED_ANNOUNCEMENTS);
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
        redisTemplateCacheSupport.evictAll(RedisCacheNames.PUBLISHED_ANNOUNCEMENTS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long id) {
        baseMapper.incrementViewCount(id);
        redisRankingService.incrementAnnouncementViewScore(id, 1D);
    }

    private AnnouncementVO convertToVO(Announcement announcement) {
        AnnouncementVO vo = new AnnouncementVO();
        BeanUtil.copyProperties(announcement, vo);
        return vo;
    }
}
