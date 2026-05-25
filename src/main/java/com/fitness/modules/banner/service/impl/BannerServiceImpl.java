package com.fitness.modules.banner.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.cache.RedisCacheNames;
import com.fitness.common.cache.RedisTemplateCacheSupport;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.integration.minio.service.FileService;
import com.fitness.modules.banner.mapper.BannerMapper;
import com.fitness.modules.banner.model.dto.BannerDTO;
import com.fitness.modules.banner.model.entity.Banner;
import com.fitness.modules.banner.model.vo.BannerVO;
import com.fitness.modules.banner.service.BannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    private final FileService fileService;
    private final RedisTemplateCacheSupport redisTemplateCacheSupport;

    @Override
    public List<BannerVO> getActiveBanners() {
        return redisTemplateCacheSupport.getOrLoad(RedisCacheNames.ACTIVE_BANNERS, "all", () -> {
            List<Banner> banners = baseMapper.selectActiveBanners();
            List<BannerVO> bannerVOs = banners.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            log.debug("[DB LOAD] active banners, count={}", bannerVOs.size());
            return bannerVOs;
        });
    }

    @Override
    public List<BannerVO> getAllBanners() {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Banner::getSortOrder)
                .orderByDesc(Banner::getId);
        List<Banner> banners = list(wrapper);
        return banners.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public BannerVO getBannerById(Long id) {
        Banner banner = getById(id);
        if (banner == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "轮播图不存在");
        }
        return convertToVO(banner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BannerVO createBanner(BannerDTO bannerDTO) {
        Banner banner = new Banner();
        BeanUtil.copyProperties(bannerDTO, banner);
        banner.setId(null);

        save(banner);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.ACTIVE_BANNERS);
        log.info("创建轮播图成功: id={}, title={}", banner.getId(), banner.getTitle());
        return convertToVO(banner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BannerVO updateBanner(Long id, BannerDTO bannerDTO) {
        Banner existingBanner = getById(id);
        if (existingBanner == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "轮播图不存在");
        }

        String oldImageUrl = existingBanner.getImageUrl();
        String newImageUrl = bannerDTO.getImageUrl();

        BeanUtil.copyProperties(bannerDTO, existingBanner);
        existingBanner.setId(id);

        updateById(existingBanner);

        if (StringUtils.hasText(oldImageUrl) && !oldImageUrl.equals(newImageUrl)) {
            try {
                fileService.deleteFile(oldImageUrl);
                log.info("删除旧轮播图图片: {}", oldImageUrl);
            } catch (Exception e) {
                log.warn("删除旧轮播图图片失败: {}", oldImageUrl, e);
            }
        }

        redisTemplateCacheSupport.evictAll(RedisCacheNames.ACTIVE_BANNERS);
        log.info("更新轮播图成功: id={}, title={}", id, existingBanner.getTitle());
        return convertToVO(existingBanner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBanner(Long id) {
        Banner banner = getById(id);
        if (banner == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "轮播图不存在");
        }

        if (StringUtils.hasText(banner.getImageUrl())) {
            try {
                fileService.deleteFile(banner.getImageUrl());
                log.info("删除轮播图图片: {}", banner.getImageUrl());
            } catch (Exception e) {
                log.warn("删除轮播图图片失败: {}", banner.getImageUrl(), e);
            }
        }

        removeById(id);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.ACTIVE_BANNERS);
        log.info("删除轮播图成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBanners(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<Banner> banners = listByIds(ids);
        for (Banner banner : banners) {
            if (StringUtils.hasText(banner.getImageUrl())) {
                try {
                    fileService.deleteFile(banner.getImageUrl());
                    log.info("批量删除轮播图图片: {}", banner.getImageUrl());
                } catch (Exception e) {
                    log.warn("批量删除轮播图图片失败: {}", banner.getImageUrl(), e);
                }
            }
        }

        removeByIds(ids);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.ACTIVE_BANNERS);
        log.info("批量删除轮播图成功: ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBannerStatus(Long id, Integer status) {
        Banner banner = getById(id);
        if (banner == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "轮播图不存在");
        }

        banner.setStatus(status);
        updateById(banner);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.ACTIVE_BANNERS);
        log.info("更新轮播图状态成功: id={}, status={}", id, status);
    }

    private BannerVO convertToVO(Banner banner) {
        BannerVO vo = new BannerVO();
        BeanUtil.copyProperties(banner, vo);
        return vo;
    }
}
