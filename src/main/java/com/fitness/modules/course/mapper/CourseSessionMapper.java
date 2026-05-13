package com.fitness.modules.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.course.model.entity.CourseSession;
import com.fitness.modules.course.model.vo.CourseSessionVO;
import com.fitness.modules.course.model.dto.CourseQueryDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CourseSessionMapper extends BaseMapper<CourseSession> {

    Page<CourseSessionVO> selectSessionList(Page<CourseSession> page, @Param("query") CourseQueryDTO query);

    CourseSessionVO selectSessionDetail(@Param("sessionId") Long sessionId);

    List<CourseSessionVO> selectUpcomingSessions(@Param("limit") Integer limit);

    List<CourseSessionVO> selectSessionsByCourseId(@Param("courseId") Long courseId, @Param("maxDate") LocalDate maxDate);

    int updateBookedCount(@Param("sessionId") Long sessionId, @Param("delta") int delta);
}
