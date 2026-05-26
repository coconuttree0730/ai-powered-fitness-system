package com.fitness.modules.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.course.model.dto.CourseQueryDTO;
import com.fitness.modules.course.model.entity.Course;
import com.fitness.modules.course.model.vo.CourseCardVO;
import com.fitness.modules.course.model.vo.CourseVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

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
     * 原子性增加总预约人数
     * 用于统计所有预约过该课程的独立会员数量
     *
     * @param courseId 课程ID
     * @return 影响行数
     */
    @Update("UPDATE fitness_course SET total_booking_count = total_booking_count + 1 WHERE id = #{courseId}")
    int incrementTotalBookingCount(@Param("courseId") Long courseId);

    /**
     * 查询所有不重复的课程分类
     *
     * @return 分类列表
     */
    @Select("SELECT DISTINCT category FROM fitness_course WHERE deleted = false AND category IS NOT NULL ORDER BY category")
    List<String> selectDistinctCategories();

    /**
     * 查询首页课程卡片列表
     *
     * @param limit 限制数量
     * @return 课程卡片列表
     */
    @Select("SELECT id, course_name as name, category, difficulty_level as level, " +
            "duration_minutes as duration, " +
            "CONCAT(calories_min, '-', calories_max, '卡') as calories, " +
            "total_booking_count as totalBookings, image_url as image, description as desc " +
            "FROM fitness_course " +
            "WHERE deleted = false AND status = 1 " +
            "ORDER BY total_booking_count DESC " +
            "LIMIT #{limit}")
    List<CourseCardVO> selectHomePageCourses(@Param("limit") Integer limit);

    /**
     * 根据分类查询课程卡片列表
     *
     * @param category 分类
     * @param limit    限制数量
     * @return 课程卡片列表
     */
    @Select("SELECT id, course_name as name, category, difficulty_level as level, " +
            "duration_minutes as duration, " +
            "CONCAT(calories_min, '-', calories_max, '卡') as calories, " +
            "total_booking_count as totalBookings, image_url as image, description as desc " +
            "FROM fitness_course " +
            "WHERE deleted = false AND status = 1 AND category = #{category} " +
            "ORDER BY total_booking_count DESC " +
            "LIMIT #{limit}")
    List<CourseCardVO> selectHomePageCoursesByCategory(@Param("category") String category, @Param("limit") Integer limit);
}
