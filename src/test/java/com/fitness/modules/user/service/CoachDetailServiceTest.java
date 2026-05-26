package com.fitness.modules.user.service;

import com.fitness.common.cache.RedisTemplateCacheSupport;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.integration.minio.model.vo.FileUploadVO;
import com.fitness.integration.minio.service.FileService;
import com.fitness.integration.security.SecurityUtils;
import com.fitness.modules.product.mapper.ProductMapper;
import com.fitness.modules.user.mapper.CoachDetailMapper;
import com.fitness.modules.user.mapper.UserFitnessProfileMapper;
import com.fitness.modules.user.mapper.UserMapper;
import com.fitness.modules.user.model.dto.CoachDetailDTO;
import com.fitness.modules.user.model.entity.CoachDetail;
import com.fitness.modules.user.model.entity.User;
import com.fitness.modules.user.model.vo.CoachDetailVO;
import com.fitness.modules.user.service.impl.CoachDetailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 教练详情服务单元测试
 * 包含用户名字段的CRUD操作和唯一性校验测试
 */
@ExtendWith(MockitoExtension.class)
class CoachDetailServiceTest {

    @Mock
    private CoachDetailMapper coachDetailMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserFitnessProfileMapper userFitnessProfileMapper;

    @Mock
    private FileService fileService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private RedisTemplateCacheSupport redisTemplateCacheSupport;

    @InjectMocks
    private CoachDetailServiceImpl coachDetailService;

    private User currentUser;
    private CoachDetail existingDetail;
    private static final Long CURRENT_USER_ID = 1L;
    private static final String CURRENT_USERNAME = "coach001";

    @BeforeEach
    void setUp() {
        // 准备当前用户数据
        currentUser = new User();
        currentUser.setId(CURRENT_USER_ID);
        currentUser.setUsername(CURRENT_USERNAME);
        currentUser.setPhone("13800138000");
        currentUser.setEmail("coach@fitness.com");

        // 准备现有教练详情
        existingDetail = new CoachDetail();
        existingDetail.setId(1L);
        existingDetail.setUserId(CURRENT_USER_ID);
        existingDetail.setWorkYears(5);
        existingDetail.setTeachingStyle("professional");
    }

    // ==================== 用户名更新成功场景 ====================

    @Test
    void updateCoachDetail_WithValidUsername_Success() {
        // Given
        String newUsername = "coach_new";
        CoachDetailDTO dto = new CoachDetailDTO();
        dto.setUsername(newUsername);
        dto.setWorkYears(10);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(userMapper.selectByUsername(newUsername)).thenReturn(null);
            when(userMapper.selectById(CURRENT_USER_ID)).thenReturn(currentUser);
            when(coachDetailMapper.selectByUserId(CURRENT_USER_ID)).thenReturn(existingDetail);
            when(coachDetailMapper.updateById(any(CoachDetail.class))).thenReturn(1);

            // When
            CoachDetailVO result = coachDetailService.updateCoachDetail(dto);

            // Then
            assertNotNull(result);
            assertEquals(newUsername, result.getUsername());
            verify(userMapper).updateById(argThat((User user) ->
                user.getId().equals(CURRENT_USER_ID) && user.getUsername().equals(newUsername)
            ));
        }
    }

    @Test
    void updateCoachDetail_WithSameUsername_NoUpdate() {
        // Given - 使用与当前相同的用户名
        CoachDetailDTO dto = new CoachDetailDTO();
        dto.setUsername(CURRENT_USERNAME);
        dto.setWorkYears(10);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(userMapper.selectById(CURRENT_USER_ID)).thenReturn(currentUser);
            when(coachDetailMapper.selectByUserId(CURRENT_USER_ID)).thenReturn(existingDetail);
            when(coachDetailMapper.updateById(any(CoachDetail.class))).thenReturn(1);

            // When
            CoachDetailVO result = coachDetailService.updateCoachDetail(dto);

            // Then - 用户名相同，不应该调用updateById更新用户
            assertNotNull(result);
            verify(userMapper, never()).updateById(any(User.class));
        }
    }

    // ==================== 用户名格式验证失败场景 ====================

    @ParameterizedTest
    @ValueSource(strings = {"abc", "ab", "a", "12345"}) // 少于6个字符
    void updateCoachDetail_WithShortUsername_ThrowsException(String shortUsername) {
        // Given
        CoachDetailDTO dto = new CoachDetailDTO();
        dto.setUsername(shortUsername);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> coachDetailService.updateCoachDetail(dto));
            assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
            assertTrue(exception.getMessage().contains("长度"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"abcdefghijklmnopqrstuvwxyz123", "this_is_a_very_long_username"}) // 超过20个字符
    void updateCoachDetail_WithLongUsername_ThrowsException(String longUsername) {
        // Given
        CoachDetailDTO dto = new CoachDetailDTO();
        dto.setUsername(longUsername);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> coachDetailService.updateCoachDetail(dto));
            assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
            assertTrue(exception.getMessage().contains("长度"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"user@name", "user.name", "user-name", "user name", "user#name", "user$name"})
    void updateCoachDetail_WithInvalidChars_ThrowsException(String invalidUsername) {
        // Given
        CoachDetailDTO dto = new CoachDetailDTO();
        dto.setUsername(invalidUsername);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> coachDetailService.updateCoachDetail(dto));
            assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
            assertTrue(exception.getMessage().contains("只能包含字母、数字和下划线"));
        }
    }

    // ==================== 用户名唯一性验证失败场景 ====================

    @Test
    void updateCoachDetail_WithDuplicateUsername_ThrowsException() {
        // Given
        String duplicateUsername = "existing_user";
        CoachDetailDTO dto = new CoachDetailDTO();
        dto.setUsername(duplicateUsername);

        User existingUser = new User();
        existingUser.setId(2L); // 不同的用户ID
        existingUser.setUsername(duplicateUsername);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(userMapper.selectByUsername(duplicateUsername)).thenReturn(existingUser);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> coachDetailService.updateCoachDetail(dto));
            assertEquals(ErrorCode.PARAM_ERROR.getCode(), exception.getCode());
            assertTrue(exception.getMessage().contains("已被使用"));
        }
    }

    // ==================== 边界值测试 ====================

    @ParameterizedTest
    @CsvSource({
        "abcdef, true",      // 正好6个字符
        "abcdefghijklmnopqrst, true",  // 正好20个字符
        "user_123, true",    // 包含下划线
        "USER_123, true",    // 大写字母
        "123456, true",      // 纯数字
        "user123_test, true" // 混合字符
    })
    void updateCoachDetail_ValidUsernames_Success(String username, boolean expectedValid) {
        // Given
        CoachDetailDTO dto = new CoachDetailDTO();
        dto.setUsername(username);
        dto.setWorkYears(5);

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(userMapper.selectByUsername(username)).thenReturn(null);
            when(userMapper.selectById(CURRENT_USER_ID)).thenReturn(currentUser);
            when(coachDetailMapper.selectByUserId(CURRENT_USER_ID)).thenReturn(existingDetail);
            when(coachDetailMapper.updateById(any(CoachDetail.class))).thenReturn(1);

            // When
            CoachDetailVO result = coachDetailService.updateCoachDetail(dto);

            // Then
            assertNotNull(result);
            verify(userMapper).updateById(argThat((User user) ->
                user.getUsername().equals(username)
            ));
        }
    }

    // ==================== 未授权场景 ====================

    @Test
    void updateCoachDetail_Unauthorized_ThrowsException() {
        // Given
        CoachDetailDTO dto = new CoachDetailDTO();
        dto.setUsername("valid_user");

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(null);

            // When & Then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> coachDetailService.updateCoachDetail(dto));
            assertEquals(ErrorCode.UNAUTHORIZED.getCode(), exception.getCode());
        }
    }

    // ==================== 获取教练详情测试 ====================

    @Test
    void getCoachDetail_WithExistingData_ReturnsDetailWithUsername() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(coachDetailMapper.selectByUserId(CURRENT_USER_ID)).thenReturn(existingDetail);
            when(userMapper.selectById(CURRENT_USER_ID)).thenReturn(currentUser);

            // When
            CoachDetailVO result = coachDetailService.getCurrentCoachDetail();

            // Then
            assertNotNull(result);
            assertEquals(CURRENT_USERNAME, result.getUsername());
            assertEquals(CURRENT_USER_ID, result.getUserId());
        }
    }

    @Test
    void getCoachDetail_WithoutDetail_ReturnsEmptyWithUserInfo() {
        // Given
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(coachDetailMapper.selectByUserId(CURRENT_USER_ID)).thenReturn(null);
            when(userMapper.selectById(CURRENT_USER_ID)).thenReturn(currentUser);

            // When
            CoachDetailVO result = coachDetailService.getCurrentCoachDetail();

            // Then
            assertNotNull(result);
            assertEquals(CURRENT_USERNAME, result.getUsername());
        }
    }

    @Test
    void getCoachDetail_WithLegacyPlainTextListFields_ReturnsFallbackList() {
        existingDetail.setTags("增肌塑形");
        existingDetail.setSpecialties("力量训练");
        existingDetail.setLanguages("中文");
        existingDetail.setHonors("年度明星教练");

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(coachDetailMapper.selectByUserId(CURRENT_USER_ID)).thenReturn(existingDetail);
            when(userMapper.selectById(CURRENT_USER_ID)).thenReturn(currentUser);

            CoachDetailVO result = coachDetailService.getCurrentCoachDetail();

            assertNotNull(result);
            assertIterableEquals(java.util.List.of("增肌塑形"), result.getTags());
            assertIterableEquals(java.util.List.of("力量训练"), result.getSpecialties());
            assertIterableEquals(java.util.List.of("中文"), result.getLanguages());
            assertIterableEquals(java.util.List.of("年度明星教练"), result.getHonors());
        }
    }

    @Test
    void getCoachDetail_WithJsonbListFields_ReturnsListValues() {
        existingDetail.setTags(java.util.List.of("增肌塑形"));
        existingDetail.setSpecialties(java.util.List.of("力量训练"));
        existingDetail.setLanguages(java.util.List.of("中文"));
        existingDetail.setHonors(java.util.List.of("年度明星教练"));

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(coachDetailMapper.selectByUserId(CURRENT_USER_ID)).thenReturn(existingDetail);
            when(userMapper.selectById(CURRENT_USER_ID)).thenReturn(currentUser);

            CoachDetailVO result = coachDetailService.getCurrentCoachDetail();

            assertNotNull(result);
            assertIterableEquals(java.util.List.of("增肌塑形"), result.getTags());
            assertIterableEquals(java.util.List.of("力量训练"), result.getSpecialties());
            assertIterableEquals(java.util.List.of("中文"), result.getLanguages());
            assertIterableEquals(java.util.List.of("年度明星教练"), result.getHonors());
        }
    }

    @Test
    void uploadPersonalImageShouldDeleteOldImageBeforeSavingNewOne() {
        MockMultipartFile file = new MockMultipartFile("file", "coach.png", "image/png", "png".getBytes());
        existingDetail.setPersonalImageUrl("http://minio/coach-old.png");

        FileUploadVO uploadVO = FileUploadVO.builder()
                .fileUrl("http://minio/coach-new.png")
                .build();

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(CURRENT_USER_ID);

            when(coachDetailMapper.selectByUserId(CURRENT_USER_ID)).thenReturn(existingDetail);
            when(fileService.uploadFile(file, "coach-images/" + CURRENT_USER_ID)).thenReturn(uploadVO);

            String result = coachDetailService.uploadPersonalImage(file);

            assertEquals("http://minio/coach-new.png", result);
            verify(fileService).deleteFile("http://minio/coach-old.png");
            verify(coachDetailMapper).updateById(existingDetail);
        }
    }
}
