package com.fitness.modules.course.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.course.mapper.CourseMapper;
import com.fitness.modules.course.model.dto.CourseDTO;
import com.fitness.modules.course.model.dto.CourseQueryDTO;
import com.fitness.modules.course.model.entity.Course;
import com.fitness.modules.course.model.vo.CourseVO;
import com.fitness.modules.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 课程服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseMapper courseMapper;

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
