package com.fitness.modules.plan.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.integration.ai.model.dto.FitnessPlanResponseDTO;
import com.fitness.integration.ai.service.AIService;
import com.fitness.modules.course.model.vo.CourseCardVO;
import com.fitness.modules.course.service.CourseService;
import com.fitness.modules.equipment.model.vo.EquipmentVO;
import com.fitness.modules.equipment.service.EquipmentService;
import com.fitness.modules.plan.mapper.FitnessPlanDetailMapper;
import com.fitness.modules.plan.mapper.FitnessPlanMapper;
import com.fitness.modules.plan.model.entity.FitnessPlan;
import com.fitness.modules.plan.model.entity.FitnessPlanDetail;
import com.fitness.modules.plan.model.vo.PlanDetailVO;
import com.fitness.modules.plan.model.vo.PlanVO;
import com.fitness.modules.plan.service.impl.FitnessPlanServiceImpl;
import com.fitness.modules.user.model.vo.UserFitnessProfileVO;
import com.fitness.modules.user.service.UserFitnessProfileService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FitnessPlanServiceImplTest {

    @Mock
    private FitnessPlanMapper fitnessPlanMapper;

    @Mock
    private FitnessPlanDetailMapper fitnessPlanDetailMapper;

    @Mock
    private AIService aiService;

    @Mock
    private UserFitnessProfileService userFitnessProfileService;

    @Mock
    private CourseService courseService;

    @Mock
    private EquipmentService equipmentService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PlanGenerationTaskManager taskManager;

    @InjectMocks
    private FitnessPlanServiceImpl service;

    @Test
    void getPlanDetailShouldParseEquipmentAndExercisesJson() {
        FitnessPlan plan = new FitnessPlan();
        plan.setId(9L);
        plan.setUserId(1L);

        FitnessPlanDetail detail = new FitnessPlanDetail();
        detail.setId(100L);
        detail.setDayIndex(1);
        detail.setDayName("Monday");
        detail.setFocus("Chest");
        detail.setCourseId(11L);
        detail.setCourseName("Bench Basics");
        detail.setEquipmentJson("[{\"name\":\"Bench Press\",\"image\":\"bench.png\"}]");
        detail.setExercisesJson("[{\"name\":\"Push Up\",\"sets\":4}]");

        when(fitnessPlanMapper.selectById(9L)).thenReturn(plan);
        when(fitnessPlanDetailMapper.selectByPlanId(9L)).thenReturn(List.of(detail));

        PlanVO result = service.getPlanDetail(1L, 9L);

        assertNotNull(result);
        assertEquals(1, result.getDetails().size());

        PlanDetailVO detailVO = result.getDetails().get(0);
        assertEquals("Monday", detailVO.getDayName());
        assertEquals("Bench Press", detailVO.getEquipment().get(0).get("name"));
        assertEquals("Push Up", detailVO.getExercises().get(0).get("name"));
    }

    @Test
    void generatePlanFromProfileShouldReturnDefaultPlanWhenAiResponseIsInvalid() {
        when(userFitnessProfileService.isProfileComplete(1L)).thenReturn(true);
        when(userFitnessProfileService.getProfile(1L)).thenReturn(buildProfile());
        when(courseService.getHomePageCourseCards(20)).thenReturn(List.of(buildCourse(1L, "Catalog Course")));
        when(equipmentService.getEquipmentList(any())).thenReturn(new Page<EquipmentVO>().setRecords(List.of(buildEquipment(2L, "Treadmill"))));
        when(aiService.generateFitnessPlanFromProfile(any())).thenReturn(null);

        Map<String, Object> result = service.generatePlanFromProfile(1L);

        Map<String, Object> userInfo = castMap(result.get("userInfo"));
        List<Map<String, Object>> weeklyPlan = castList(result.get("weeklyPlan"));

        assertEquals("22.9", userInfo.get("bmi"));
        assertEquals(7, weeklyPlan.size());
        assertNull(weeklyPlan.get(3).get("courses"));
        assertInstanceOf(List.class, weeklyPlan.get(3).get("equipment"));
        assertEquals(0, castList(weeklyPlan.get(3).get("equipment")).size());
        assertEquals(0, castList(weeklyPlan.get(3).get("exercises")).size());
    }

    @Test
    void generatePlanFromProfileShouldReplaceCourseByIdAndFallbackEquipmentFromCatalog() {
        when(userFitnessProfileService.isProfileComplete(1L)).thenReturn(true);
        when(userFitnessProfileService.getProfile(1L)).thenReturn(buildProfile());
        when(courseService.getHomePageCourseCards(20)).thenReturn(List.of(buildCourse(1L, "Catalog Course")));
        when(equipmentService.getEquipmentList(any())).thenReturn(new Page<EquipmentVO>().setRecords(List.of(buildEquipment(2L, "Treadmill"))));
        when(aiService.generateFitnessPlanFromProfile(any())).thenReturn(buildAiResponse());

        Map<String, Object> result = service.generatePlanFromProfile(1L);

        List<Map<String, Object>> weeklyPlan = castList(result.get("weeklyPlan"));
        List<Map<String, Object>> courses = castList(weeklyPlan.get(0).get("courses"));
        List<Map<String, Object>> equipment = castList(weeklyPlan.get(0).get("equipment"));

        assertEquals(1L, courses.get(0).get("id"));
        assertEquals("Catalog Course", courses.get(0).get("name"));
        assertEquals("Treadmill", equipment.get(0).get("name"));
    }

    private UserFitnessProfileVO buildProfile() {
        UserFitnessProfileVO profile = new UserFitnessProfileVO();
        profile.setHeight(new BigDecimal("175"));
        profile.setWeight(new BigDecimal("70"));
        profile.setAge(28);
        profile.setFitnessGoal("MUSCLE_GAIN");
        profile.setExperience("BEGINNER");
        return profile;
    }

    private CourseCardVO buildCourse(Long id, String name) {
        CourseCardVO course = new CourseCardVO();
        course.setId(id);
        course.setName(name);
        course.setCategory("strength");
        course.setDuration(45);
        course.setImage("course.png");
        course.setDesc("Course description");
        return course;
    }

    private EquipmentVO buildEquipment(Long id, String name) {
        EquipmentVO equipment = new EquipmentVO();
        equipment.setId(id);
        equipment.setEquipmentName(name);
        equipment.setImageUrl("equipment.png");
        equipment.setDescription("Equipment description");
        return equipment;
    }

    private FitnessPlanResponseDTO buildAiResponse() {
        FitnessPlanResponseDTO response = new FitnessPlanResponseDTO();
        response.setSubtitle("AI plan");

        FitnessPlanResponseDTO.UserInfo userInfo = new FitnessPlanResponseDTO.UserInfo();
        userInfo.setHeight("175");
        userInfo.setWeight("70");
        response.setUserInfo(userInfo);

        FitnessPlanResponseDTO.CourseDTO course = new FitnessPlanResponseDTO.CourseDTO();
        course.setCourseId(1L);
        course.setName("Unknown course");

        FitnessPlanResponseDTO.EquipmentDTO equipment = new FitnessPlanResponseDTO.EquipmentDTO();
        equipment.setName("Unknown equipment");

        FitnessPlanResponseDTO.DayPlanDTO dayPlan = new FitnessPlanResponseDTO.DayPlanDTO();
        dayPlan.setDayName("Monday");
        dayPlan.setFocus("Chest");
        dayPlan.setCourses(List.of(course));
        dayPlan.setEquipment(List.of(equipment));

        response.setWeeklyPlan(List.of(dayPlan));
        return response;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> castMap(Object value) {
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castList(Object value) {
        return (List<Map<String, Object>>) value;
    }
}
