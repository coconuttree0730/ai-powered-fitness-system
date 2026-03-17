package com.fitness.modules.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.course.model.dto.CourseQueryDTO;
import com.fitness.modules.course.model.entity.Course;
import com.fitness.modules.course.model.vo.CourseVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 课程数据访问层
 */
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 分页查询课程列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    Page<CourseVO> selectCourseList(Page<Course> page, @Param("query") CourseQueryDTO query);

    /**
     * 查询课程详情（包含教练信息）
     *
     * @param courseId 课程ID
     * @return 课程详情
     */
    CourseVO selectCourseDetail(@Param("courseId") Long courseId);

    /**
     * 更新预约人数
     *
     * @param courseId 课程ID
     * @param delta    变化量（正数为增加，负数为减少）
     * @return 影响行数
     */
    @Update("UPDATE fitness_course SET booked_count = booked_count + #{delta} WHERE id = #{courseId}")
    int updateBookedCount(@Param("courseId") Long courseId, @Param("delta") Integer delta);
}
