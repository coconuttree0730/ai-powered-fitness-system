package com.fitness.modules.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.course.model.dto.VideoCourseQueryDTO;
import com.fitness.modules.course.model.entity.VideoCourse;
import com.fitness.modules.course.model.vo.VideoCourseVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface VideoCourseMapper extends BaseMapper<VideoCourse> {

    Page<VideoCourseVO> selectVideoCourseList(Page<VideoCourse> page, @Param("query") VideoCourseQueryDTO query);

    VideoCourseVO selectVideoCourseDetail(@Param("id") Long id);

    @Update("UPDATE fitness_video_course SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(@Param("id") Long id);

    @Select("SELECT DISTINCT category FROM fitness_video_course WHERE deleted = false AND category IS NOT NULL ORDER BY category")
    List<String> selectDistinctCategories();
}
