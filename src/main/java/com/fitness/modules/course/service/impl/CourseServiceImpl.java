package com.fitness.modules.course.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.course.mapper.CourseMapper;
import com.fitness.modules.course.model.dto.CourseDTO;
import com.fitness.modules.course.model.dto.CourseQueryDTO;
import com.fitness.modules.course.model.entity.Course;
import com.fitness.modules.course.model.vo.CourseCardVO;
import com.fitness.modules.course.model.vo.CourseCategoryVO;
import com.fitness.modules.course.model.vo.CourseVO;
import com.fitness.integration.minio.service.FileService;
import com.fitness.modules.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 课程服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;
    private final FileService fileService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCourse(CourseDTO dto) {
        // 校验时间合法性
        validateCourseTime(dto.getStartTime(), dto.getEndTime());

        Course course = new Course();
        course.setCourseName(dto.getCourseName());
        course.setDescription(dto.getDescription());
        course.setCoachId(dto.getCoachId());
        course.setCategory(dto.getCategory());
        course.setStartTime(dto.getStartTime());
        course.setEndTime(dto.getEndTime());
        course.setCapacity(dto.getCapacity());
        course.setBookedCount(0);
        course.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        course.setImageUrl(dto.getImageUrl());
        course.setDifficultyLevel(dto.getDifficultyLevel());
        course.setDurationMinutes(dto.getDurationMinutes());
        course.setCaloriesMin(dto.getCaloriesMin());
        course.setCaloriesMax(dto.getCaloriesMax());
        course.setCreateTime(LocalDateTime.now());
        course.setUpdateTime(LocalDateTime.now());

        courseMapper.insert(course);
        log.info("课程创建成功: courseId={}, courseName={}", course.getId(), course.getCourseName());

        return course.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCourse(Long courseId, CourseDTO dto) {
        Course existingCourse = courseMapper.selectById(courseId);
        if (existingCourse == null || existingCourse.getDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        // 校验时间合法性
        validateCourseTime(dto.getStartTime(), dto.getEndTime());

        // 如果更换了图片，删除旧图片
        if (StringUtils.hasText(dto.getImageUrl()) 
                && !dto.getImageUrl().equals(existingCourse.getImageUrl())
                && StringUtils.hasText(existingCourse.getImageUrl())) {
            try {
                fileService.deleteFile(existingCourse.getImageUrl());
                log.info("课程旧图片删除成功: courseId={}, oldImageUrl={}", courseId, existingCourse.getImageUrl());
            } catch (Exception e) {
                log.warn("课程旧图片删除失败: courseId={}, oldImageUrl={}, error={}", courseId, existingCourse.getImageUrl(), e.getMessage());
            }
        }

        existingCourse.setCourseName(dto.getCourseName());
        existingCourse.setDescription(dto.getDescription());
        existingCourse.setCoachId(dto.getCoachId());
        existingCourse.setCategory(dto.getCategory());
        existingCourse.setStartTime(dto.getStartTime());
        existingCourse.setEndTime(dto.getEndTime());
        existingCourse.setCapacity(dto.getCapacity());
        if (dto.getStatus() != null) {
            existingCourse.setStatus(dto.getStatus());
        }
        existingCourse.setImageUrl(dto.getImageUrl());
        existingCourse.setDifficultyLevel(dto.getDifficultyLevel());
        existingCourse.setDurationMinutes(dto.getDurationMinutes());
        existingCourse.setCaloriesMin(dto.getCaloriesMin());
        existingCourse.setCaloriesMax(dto.getCaloriesMax());
        existingCourse.setUpdateTime(LocalDateTime.now());

        courseMapper.updateById(existingCourse);
        log.info("课程更新成功: courseId={}", courseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long courseId) {
        Course existingCourse = courseMapper.selectById(courseId);
        if (existingCourse == null || existingCourse.getDeleted()) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        // 删除课程图片
        if (StringUtils.hasText(existingCourse.getImageUrl())) {
            try {
                fileService.deleteFile(existingCourse.getImageUrl());
                log.info("课程图片删除成功: courseId={}, imageUrl={}", courseId, existingCourse.getImageUrl());
            } catch (Exception e) {
                log.warn("课程图片删除失败: courseId={}, imageUrl={}, error={}", courseId, existingCourse.getImageUrl(), e.getMessage());
            }
        }

        courseMapper.deleteById(courseId);
        log.info("课程删除成功: courseId={}", courseId);
    }

    @Override
    public CourseVO getCourseById(Long courseId) {
        CourseVO courseVO = courseMapper.selectCourseDetail(courseId);
        if (courseVO == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }
        return courseVO;
    }

    @Override
    public Page<CourseVO> getCourseList(CourseQueryDTO query) {
        Page<Course> page = new Page<>(query.getPageNum(), query.getPageSize());
        return courseMapper.selectCourseList(page, query);
    }

    @Override
    public Page<CourseVO> getPublicCourseList(CourseQueryDTO query) {
        // 公开列表只显示未开始和进行中的课程
        Page<Course> page = new Page<>(query.getPageNum(), query.getPageSize());
        return courseMapper.selectCourseList(page, query);
    }

    @Override
    public List<String> getCourseCategories() {
        return courseMapper.selectDistinctCategories();
    }

    @Override
    public List<CourseCategoryVO> getHomePageCourses() {
        // 定义固定的分类映射（与前端保持一致）- 使用数据库中的中文分类名称
        List<CourseCategoryVO> result = new ArrayList<>();

        // 全部课程
        CourseCategoryVO allCategory = new CourseCategoryVO();
        allCategory.setKey("all");
        allCategory.setLabel("全部课程");
        allCategory.setCourses(courseMapper.selectHomePageCourses(6));
        result.add(allCategory);

        // 力量训练
        CourseCategoryVO strengthCategory = new CourseCategoryVO();
        strengthCategory.setKey("strength");
        strengthCategory.setLabel("力量训练");
        strengthCategory.setCourses(courseMapper.selectHomePageCoursesByCategory("力量训练", 6));
        result.add(strengthCategory);

        // 有氧燃脂
        CourseCategoryVO cardioCategory = new CourseCategoryVO();
        cardioCategory.setKey("cardio");
        cardioCategory.setLabel("有氧燃脂");
        cardioCategory.setCourses(courseMapper.selectHomePageCoursesByCategory("有氧燃脂", 6));
        result.add(cardioCategory);

        // 瑜伽普拉提
        CourseCategoryVO yogaCategory = new CourseCategoryVO();
        yogaCategory.setKey("yoga");
        yogaCategory.setLabel("瑜伽普拉提");
        List<CourseCardVO> yogaCourses = new ArrayList<>();
        yogaCourses.addAll(courseMapper.selectHomePageCoursesByCategory("瑜伽普拉提", 6));
        yogaCategory.setCourses(yogaCourses);
        result.add(yogaCategory);

        // 拳击格斗
        CourseCategoryVO boxingCategory = new CourseCategoryVO();
        boxingCategory.setKey("boxing");
        boxingCategory.setLabel("拳击格斗");
        boxingCategory.setCourses(courseMapper.selectHomePageCoursesByCategory("拳击格斗", 6));
        result.add(boxingCategory);

        log.info("获取首页课程体系数据成功，共{}个分类", result.size());
        return result;
    }

    @Override
    public List<CourseCardVO> getHomePageCourseCards(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 6;
        }
        List<CourseCardVO> courses = courseMapper.selectHomePageCourses(limit);
        log.info("获取首页课程卡片数据成功，共{}条", courses.size());
        return courses;
    }

    /**
     * 校验课程时间合法性
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     */
    private void validateCourseTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (endTime.isBefore(startTime) || endTime.isEqual(startTime)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "结束时间必须晚于开始时间");
        }
    }
}
