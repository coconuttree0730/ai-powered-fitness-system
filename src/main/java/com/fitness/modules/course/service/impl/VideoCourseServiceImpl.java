package com.fitness.modules.course.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.integration.minio.service.FileService;
import com.fitness.modules.course.mapper.VideoCourseMapper;
import com.fitness.modules.course.model.dto.VideoCourseDTO;
import com.fitness.modules.course.model.dto.VideoCourseQueryDTO;
import com.fitness.modules.course.model.entity.VideoCourse;
import com.fitness.modules.course.model.vo.VideoCourseVO;
import com.fitness.modules.course.service.VideoCourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VideoCourseServiceImpl extends ServiceImpl<VideoCourseMapper, VideoCourse> implements VideoCourseService {

    private final FileService fileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVideoCourse(VideoCourseDTO dto) {
        VideoCourse course = new VideoCourse();
        BeanUtil.copyProperties(dto, course);
        course.setViewCount(0);
        course.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        course.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);

        this.save(course);
        log.info("视频课程创建成功: id={}, title={}", course.getId(), course.getTitle());

        return course.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVideoCourse(Long id, VideoCourseDTO dto) {
        VideoCourse existing = this.getById(id);
        if (existing == null || existing.getDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        if (StringUtils.hasText(dto.getCoverUrl())
                && !dto.getCoverUrl().equals(existing.getCoverUrl())
                && StringUtils.hasText(existing.getCoverUrl())) {
            try {
                fileService.deleteFile(existing.getCoverUrl());
                log.info("旧封面图片删除成功: id={}, oldCoverUrl={}", id, existing.getCoverUrl());
            } catch (Exception e) {
                log.warn("旧封面图片删除失败: id={}, oldCoverUrl={}, error={}", id, existing.getCoverUrl(), e.getMessage());
            }
        }

        if (StringUtils.hasText(dto.getVideoUrl())
                && !dto.getVideoUrl().equals(existing.getVideoUrl())
                && StringUtils.hasText(existing.getVideoUrl())) {
            try {
                fileService.deleteFile(existing.getVideoUrl());
                log.info("旧视频文件删除成功: id={}, oldVideoUrl={}", id, existing.getVideoUrl());
            } catch (Exception e) {
                log.warn("旧视频文件删除失败: id={}, oldVideoUrl={}, error={}", id, existing.getVideoUrl(), e.getMessage());
            }
        }

        BeanUtil.copyProperties(dto, existing);

        this.updateById(existing);
        log.info("视频课程更新成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteVideoCourse(Long id) {
        VideoCourse existing = this.getById(id);
        if (existing == null || existing.getDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        if (StringUtils.hasText(existing.getCoverUrl())) {
            try {
                fileService.deleteFile(existing.getCoverUrl());
            } catch (Exception e) {
                log.warn("封面图片删除失败: id={}, coverUrl={}", id, existing.getCoverUrl());
            }
        }

        if (StringUtils.hasText(existing.getVideoUrl())) {
            try {
                fileService.deleteFile(existing.getVideoUrl());
            } catch (Exception e) {
                log.warn("视频文件删除失败: id={}, videoUrl={}", id, existing.getVideoUrl());
            }
        }

        this.removeById(id);
        log.info("视频课程删除成功: id={}", id);
    }

    @Override
    public VideoCourseVO getVideoCourseById(Long id) {
        VideoCourseVO vo = baseMapper.selectVideoCourseDetail(id);
        if (vo == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }
        return vo;
    }

    @Override
    public Page<VideoCourseVO> getVideoCourseList(VideoCourseQueryDTO query) {
        Page<VideoCourse> page = new Page<>(query.getPageNum(), query.getPageSize());
        return baseMapper.selectVideoCourseList(page, query);
    }

    @Override
    public List<String> getCategories() {
        return baseMapper.selectDistinctCategories();
    }
}
