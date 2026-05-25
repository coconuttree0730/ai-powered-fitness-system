package com.fitness.modules.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.course.model.dto.CourseDTO;
import com.fitness.modules.course.model.dto.CourseQueryDTO;
import com.fitness.modules.course.model.vo.CourseCardVO;
import com.fitness.modules.course.model.vo.CourseCategoryVO;
import com.fitness.modules.course.model.vo.CourseVO;

/**
 * 课程服务接口
 */
public interface CourseService {

    /**
     * 创建课程
     *
     * @param dto 课程信息
     * @return 创建的课程ID
     */
    Long createCourse(CourseDTO dto);

    /**
     * 更新课程
     *
     * @param courseId 课程ID
     * @param dto      课程信息
     */
    void updateCourse(Long courseId, CourseDTO dto);

    /**
     * 删除课程（逻辑删除）
     *
     * @param courseId 课程ID
     */
    void deleteCourse(Long courseId);

    /**
     * 获取课程详情
     *
     * @param courseId 课程ID
     * @return 课程详情
     */
    CourseVO getCourseById(Long courseId);

    /**
     * 分页查询课程列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    Page<CourseVO> getCourseList(CourseQueryDTO query);

    /**
     * 获取公开课程列表（游客可看）
     *
     * @param query 查询条件
     * @return 分页结果
     */
    Page<CourseVO> getPublicCourseList(CourseQueryDTO query);

    /**
     * 获取所有课程分类列表
     *
     * @return 分类列表
     */
    java.util.List<String> getCourseCategories();

    /**
     * 获取首页课程体系数据（按分类分组）
     *
     * @return 课程体系分类列表
     */
    java.util.List<CourseCategoryVO> getHomePageCourses();

    /**
     * 获取首页课程卡片列表（不分组，用于展示）
     *
     * @param limit 限制数量
     * @return 课程卡片列表
     */
    java.util.List<CourseCardVO> getHomePageCourseCards(Integer limit);
}
