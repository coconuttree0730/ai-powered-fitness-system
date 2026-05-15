package com.fitness.modules.chat.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.modules.chat.model.vo.CourseRecommendVO;
import com.fitness.modules.chat.model.vo.EquipmentRecommendVO;
import com.fitness.modules.chat.model.vo.WeeklyAdviceVO;
import com.fitness.modules.course.model.vo.CourseCardVO;
import com.fitness.modules.course.service.CourseService;
import com.fitness.modules.equipment.model.dto.EquipmentQueryDTO;
import com.fitness.modules.equipment.model.vo.EquipmentVO;
import com.fitness.modules.equipment.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final CourseService courseService;
    private final EquipmentService equipmentService;

    public WeeklyAdviceVO generateWeeklyAdvice(String goal, String experience) {
        WeeklyAdviceVO advice = new WeeklyAdviceVO();
        advice.setOverallGoal("根据你的训练目标制定 7 天循序渐进计划。");
        advice.setTrainingPrinciples("先保证动作标准，再逐步增加训练量。");
        advice.setDietAdvice("保证蛋白质摄入，控制总热量，注意补水。");
        advice.setRestAdvice("保持规律睡眠，训练后进行轻度拉伸恢复。");
        advice.setSafetyTips("训练前热身，训练中如有疼痛或不适应立即停止。");
        advice.setProgressionTips("每周根据恢复情况微调重量、次数或训练时长。");
        return advice;
    }

    public List<CourseRecommendVO> recommendCourses(String goal, String bodyPart) {
        List<CourseRecommendVO> courses = new ArrayList<>();
        try {
            List<CourseCardVO> courseCards = courseService.getHomePageCourseCards(6);
            for (CourseCardVO card : courseCards) {
                CourseRecommendVO recommend = new CourseRecommendVO();
                recommend.setCourseId(card.getId());
                recommend.setCourseName(card.getName());
                recommend.setCategory(card.getCategory());
                recommend.setImageUrl(card.getImage());
                recommend.setDifficultyLevel(card.getLevel());
                recommend.setDurationMinutes(card.getDuration());
                recommend.setRecommendReason("适合当前 " + goal + " / " + bodyPart + " 训练目标。");
                courses.add(recommend);
            }
        } catch (Exception e) {
            log.warn("Failed to load recommended courses: {}", e.getMessage());
        }
        return courses;
    }

    public List<EquipmentRecommendVO> recommendEquipment(String bodyPart) {
        List<EquipmentRecommendVO> equipments = new ArrayList<>();
        try {
            Page<EquipmentVO> page = equipmentService.getEquipmentList(buildEquipmentQuery(10));
            if (page != null && CollUtil.isNotEmpty(page.getRecords())) {
                for (EquipmentVO equipment : page.getRecords()) {
                    EquipmentRecommendVO recommend = new EquipmentRecommendVO();
                    recommend.setEquipmentId(equipment.getId());
                    recommend.setEquipmentName(equipment.getEquipmentName());
                    recommend.setTypeName(equipment.getTypeName());
                    recommend.setLocation(equipment.getLocation());
                    recommend.setImageUrl(equipment.getImageUrl());
                    recommend.setDescription(equipment.getDescription());
                    recommend.setRecommendReason("适合 " + bodyPart + " 训练使用。");
                    equipments.add(recommend);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to load recommended equipment: {}", e.getMessage());
        }
        return equipments;
    }

    public String buildAvailableCourseCatalog() {
        try {
            List<CourseCardVO> courses = courseService.getHomePageCourseCards(20);
            if (CollUtil.isEmpty(courses)) return "[]";
            return courses.stream()
                    .map(course -> String.format(
                            "- id=%s, name=%s, category=%s, duration=%s",
                            course.getId(), course.getName(), course.getCategory(), course.getDuration()))
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.warn("Failed to load course catalog for structured AI plan: {}", e.getMessage());
            return "[]";
        }
    }

    public String buildAvailableEquipmentCatalog() {
        try {
            Page<EquipmentVO> page = equipmentService.getEquipmentList(buildEquipmentQuery(20));
            if (page == null || CollUtil.isEmpty(page.getRecords())) return "[]";
            return page.getRecords().stream()
                    .map(equipment -> String.format(
                            "- id=%s, name=%s, type=%s, location=%s",
                            equipment.getId(), equipment.getEquipmentName(),
                            equipment.getTypeName(), equipment.getLocation()))
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.warn("Failed to load equipment catalog for structured AI plan: {}", e.getMessage());
            return "[]";
        }
    }

    private EquipmentQueryDTO buildEquipmentQuery(int pageSize) {
        EquipmentQueryDTO query = new EquipmentQueryDTO();
        query.setStatus(1);
        query.setPageNum(1);
        query.setPageSize(pageSize);
        return query;
    }
}