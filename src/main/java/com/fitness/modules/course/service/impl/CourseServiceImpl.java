package com.fitness.modules.course.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.cache.CacheKeyBuilder;
import com.fitness.common.cache.RedisCacheNames;
import com.fitness.common.cache.RedisTemplateCacheSupport;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.integration.minio.service.FileService;
import com.fitness.modules.course.mapper.CourseMapper;
import com.fitness.modules.course.model.dto.CourseDTO;
import com.fitness.modules.course.model.dto.CourseQueryDTO;
import com.fitness.modules.course.model.entity.Course;
import com.fitness.modules.course.model.vo.CourseCardVO;
import com.fitness.modules.course.model.vo.CourseCategoryVO;
import com.fitness.modules.course.model.vo.CourseVO;
import com.fitness.modules.course.service.CourseService;
import com.fitness.modules.ranking.service.RedisRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final FileService fileService;
    private final RedisRankingService redisRankingService;
    private final RedisTemplateCacheSupport redisTemplateCacheSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCourse(CourseDTO dto) {
        validateCourseTime(dto.getStartTime(), dto.getEndTime());

        Course course = new Course();
        BeanUtil.copyProperties(dto, course);
        course.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);

        this.save(course);
        clearPublicCaches();
        log.info("课程创建成功: courseId={}, courseName={}", course.getId(), course.getCourseName());
        return course.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(Long courseId, CourseDTO dto) {
        Course existingCourse = this.getById(courseId);
        if (existingCourse == null || existingCourse.getDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        validateCourseTime(dto.getStartTime(), dto.getEndTime());

        if (StringUtils.hasText(dto.getImageUrl())
                && !dto.getImageUrl().equals(existingCourse.getImageUrl())
                && StringUtils.hasText(existingCourse.getImageUrl())) {
            try {
                fileService.deleteFile(existingCourse.getImageUrl());
                log.info("课程旧图片删除成功: courseId={}, oldImageUrl={}", courseId, existingCourse.getImageUrl());
            } catch (Exception e) {
                log.warn("课程旧图片删除失败: courseId={}, oldImageUrl={}, error={}",
                        courseId, existingCourse.getImageUrl(), e.getMessage());
            }
        }

        Integer originalStatus = existingCourse.getStatus();
        BeanUtil.copyProperties(dto, existingCourse);
        if (dto.getStatus() == null) {
            existingCourse.setStatus(originalStatus);
        }

        this.updateById(existingCourse);
        clearPublicCaches();
        log.info("课程更新成功: courseId={}", courseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long courseId) {
        Course existingCourse = this.getById(courseId);
        if (existingCourse == null || existingCourse.getDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        if (StringUtils.hasText(existingCourse.getImageUrl())) {
            try {
                fileService.deleteFile(existingCourse.getImageUrl());
                log.info("课程图片删除成功: courseId={}, imageUrl={}", courseId, existingCourse.getImageUrl());
            } catch (Exception e) {
                log.warn("课程图片删除失败: courseId={}, imageUrl={}, error={}",
                        courseId, existingCourse.getImageUrl(), e.getMessage());
            }
        }

        this.removeById(courseId);
        clearPublicCaches();
        log.info("课程删除成功: courseId={}", courseId);
    }

    @Override
    public CourseVO getCourseById(Long courseId) {
        CourseVO courseVO = baseMapper.selectCourseDetail(courseId);
        if (courseVO == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }
        return courseVO;
    }

    @Override
    public Page<CourseVO> getCourseList(CourseQueryDTO query) {
        Page<Course> page = new Page<>(query.getPageNum(), query.getPageSize());
        Page<CourseVO> result = baseMapper.selectCourseList(page, query);
        log.debug("[DB QUERY] admin course list, page={}, size={}, total={}, category={}, keyword={}, coachId={}, dayOfWeek={}",
                query.getPageNum(),
                query.getPageSize(),
                result.getTotal(),
                query.getCategory(),
                query.getKeyword(),
                query.getCoachId(),
                query.getDayOfWeek());
        return result;
    }

    @Override
    public Page<CourseVO> getPublicCourseList(CourseQueryDTO query) {
        if (StringUtils.hasText(query.getKeyword())) {
            redisRankingService.recordSearchKeyword("course", query.getKeyword());
        }
        return redisTemplateCacheSupport.getOrLoad(
                RedisCacheNames.COURSE_PUBLIC_LIST,
                CacheKeyBuilder.join(query),
                () -> {
                    Page<Course> page = new Page<>(query.getPageNum(), query.getPageSize());
                    Page<CourseVO> result = baseMapper.selectCourseList(page, query);
                    log.debug("[DB LOAD] public course list, page={}, size={}, total={}, category={}, keyword={}, coachId={}, dayOfWeek={}",
                            query.getPageNum(),
                            query.getPageSize(),
                            result.getTotal(),
                            query.getCategory(),
                            query.getKeyword(),
                            query.getCoachId(),
                            query.getDayOfWeek());
                    return result;
                }
        );
    }

    @Override
    public List<String> getCourseCategories() {
        return redisTemplateCacheSupport.getOrLoad(RedisCacheNames.COURSE_CATEGORIES, "all", () -> {
            List<String> categories = baseMapper.selectDistinctCategories();
            log.debug("[DB LOAD] course categories, count={}", categories.size());
            return categories;
        });
    }

    @Override
    public List<CourseCategoryVO> getHomePageCourses() {
        return redisTemplateCacheSupport.getOrLoad(RedisCacheNames.COURSE_HOME_CATEGORIES, "all", () -> {
            List<CourseCategoryVO> result = new ArrayList<>();

            CourseCategoryVO allCategory = new CourseCategoryVO();
            allCategory.setKey("all");
            allCategory.setLabel("全部课程");
            allCategory.setCourses(baseMapper.selectHomePageCourses(6));
            result.add(allCategory);

            CourseCategoryVO strengthCategory = new CourseCategoryVO();
            strengthCategory.setKey("strength");
            strengthCategory.setLabel("力量训练");
            strengthCategory.setCourses(baseMapper.selectHomePageCoursesByCategory("力量训练", 6));
            result.add(strengthCategory);

            CourseCategoryVO cardioCategory = new CourseCategoryVO();
            cardioCategory.setKey("cardio");
            cardioCategory.setLabel("有氧燃脂");
            cardioCategory.setCourses(baseMapper.selectHomePageCoursesByCategory("有氧燃脂", 6));
            result.add(cardioCategory);

            CourseCategoryVO yogaCategory = new CourseCategoryVO();
            yogaCategory.setKey("yoga");
            yogaCategory.setLabel("瑜伽普拉提");
            yogaCategory.setCourses(baseMapper.selectHomePageCoursesByCategory("瑜伽普拉提", 6));
            result.add(yogaCategory);

            CourseCategoryVO boxingCategory = new CourseCategoryVO();
            boxingCategory.setKey("boxing");
            boxingCategory.setLabel("拳击格斗");
            boxingCategory.setCourses(baseMapper.selectHomePageCoursesByCategory("拳击格斗", 6));
            result.add(boxingCategory);

            log.debug("[DB LOAD] homepage course categories, count={}", result.size());
            return result;
        });
    }

    @Override
    public List<CourseCardVO> getHomePageCourseCards(Integer limit) {
        Integer safeLimit = (limit == null || limit <= 0) ? 6 : limit;
        return redisTemplateCacheSupport.getOrLoad(
                RedisCacheNames.COURSE_HOME_CARDS,
                CacheKeyBuilder.join(safeLimit),
                () -> {
                    List<CourseCardVO> courses = baseMapper.selectHomePageCourses(safeLimit);
                    log.debug("[DB LOAD] homepage course cards, limit={}, count={}", safeLimit, courses.size());
                    return courses;
                }
        );
    }

    private void clearPublicCaches() {
        redisTemplateCacheSupport.evictAll(RedisCacheNames.COURSE_PUBLIC_LIST);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.COURSE_CATEGORIES);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.COURSE_HOME_CATEGORIES);
        redisTemplateCacheSupport.evictAll(RedisCacheNames.COURSE_HOME_CARDS);
    }

    private void validateCourseTime(LocalTime startTime, LocalTime endTime) {
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "结束时间必须晚于开始时间");
        }
    }
}
