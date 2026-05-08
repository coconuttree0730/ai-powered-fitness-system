package com.fitness.modules.course.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.course.model.dto.VideoCourseDTO;
import com.fitness.modules.course.model.dto.VideoCourseQueryDTO;
import com.fitness.modules.course.model.vo.VideoCourseVO;

import java.util.List;

public interface VideoCourseService {

    Long createVideoCourse(VideoCourseDTO dto);

    void updateVideoCourse(Long id, VideoCourseDTO dto);

    void deleteVideoCourse(Long id);

    VideoCourseVO getVideoCourseById(Long id);

    Page<VideoCourseVO> getVideoCourseList(VideoCourseQueryDTO query);

    List<String> getCategories();
}
