package com.fitness.modules.user.service;

import com.fitness.integration.minio.service.FileService;
import com.fitness.integration.security.JwtTokenProvider;
import com.fitness.modules.user.mapper.RoleMapper;
import com.fitness.modules.user.mapper.UserMapper;
import com.fitness.modules.user.model.dto.LoginDTO;
import com.fitness.modules.user.model.entity.Role;
import com.fitness.modules.user.model.entity.User;
import com.fitness.modules.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplAuthTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private FileService fileService;

    @Mock
    private SmsCodeService smsCodeService;

    @Mock
    private EmailCodeService emailCodeService;

    @Mock
    private CoachDetailService coachDetailService;

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void loginShouldReturnTokenFromJwtTokenProvider() {
        User user = new User();
        user.setId(1L);
        user.setUsername("member001");
        user.setPassword("encoded");
        user.setStatus(1);

        Role role = new Role();
        role.setRoleCode("MEMBER");

        LoginDTO dto = new LoginDTO();
        dto.setUsername("member001");
        dto.setPassword("123456");

        when(userMapper.selectByUsername("member001")).thenReturn(user);
        when(passwordEncoder.matches("123456", "encoded")).thenReturn(true);
        when(userMapper.selectRolesByUserId(1L)).thenReturn(List.of(role));
        when(jwtTokenProvider.generateToken(1L, "member001", List.of("MEMBER"))).thenReturn("jwt-token");
        when(jwtTokenProvider.getExpirationTime()).thenReturn(3600000L);

        Map<String, Object> result = service.login(dto);

        assertEquals("jwt-token", result.get("token"));
        assertEquals(3600000L, result.get("expiresIn"));
    }
}
