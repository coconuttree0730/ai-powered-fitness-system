package com.fitness.modules.user.service;

import com.fitness.modules.user.mapper.CoachStudentMapper;
import com.fitness.modules.user.mapper.UserFitnessProfileMapper;
import com.fitness.modules.user.model.entity.CoachStudent;
import com.fitness.modules.user.model.vo.CoachStudentVO;
import com.fitness.modules.user.service.impl.CoachStudentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoachStudentServiceImplTest {

    @Mock
    private CoachStudentMapper coachStudentMapper;

    @Mock
    private UserFitnessProfileMapper userFitnessProfileMapper;

    @InjectMocks
    private CoachStudentServiceImpl coachStudentService;

    @Test
    void bindStudentRenewsExistingActiveBinding() {
        ReflectionTestUtils.setField(coachStudentService, "baseMapper", coachStudentMapper);
        CoachStudent existing = new CoachStudent();
        existing.setId(1L);
        existing.setMemberId(100L);
        existing.setCoachId(200L);
        existing.setCoachPackageId(10L);
        existing.setPackageCode("PT_OLD");
        existing.setExpireTime(LocalDateTime.now().plusDays(10));
        existing.setStatus("ACTIVE");

        when(coachStudentMapper.selectByMemberAndCoach(100L, 200L)).thenReturn(existing);
        when(coachStudentMapper.updateById(existing)).thenReturn(1);

        coachStudentService.bindStudent(100L, 200L, 20L, "PT_NEW", 30);

        assertEquals(20L, existing.getCoachPackageId());
        assertEquals("PT_NEW", existing.getPackageCode());
        assertTrue(existing.getExpireTime().isAfter(LocalDateTime.now().plusDays(35)));
        verify(coachStudentMapper).updateById(existing);
    }

    @Test
    void getMyStudentsNormalizesMissingDisplayNameAndPhone() {
        CoachStudentVO binding = new CoachStudentVO();
        binding.setId(1L);
        binding.setMemberId(100L);
        binding.setCoachId(200L);
        binding.setStudentName(null);
        binding.setStudentPhone(null);

        when(coachStudentMapper.selectByCoachId(200L)).thenReturn(List.of(binding));

        List<CoachStudentVO> results = coachStudentService.getMyStudents(200L);

        assertEquals(1, results.size());
        assertEquals("学员100", results.get(0).getStudentName());
        assertEquals("", results.get(0).getStudentPhone());
    }
}
