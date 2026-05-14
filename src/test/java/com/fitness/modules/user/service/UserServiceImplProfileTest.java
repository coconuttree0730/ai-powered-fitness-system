package com.fitness.modules.user.service;

import com.fitness.integration.minio.service.FileService;
import com.fitness.integration.security.JwtTokenProvider;
import com.fitness.modules.user.mapper.RoleMapper;
import com.fitness.modules.user.mapper.UserMapper;
import com.fitness.modules.user.model.dto.UserUpdateDTO;
import com.fitness.modules.user.model.entity.User;
import com.fitness.modules.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplProfileTest {

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
    void updateUserShouldDeleteOldAvatarWhenAvatarChanges() {
        User user = new User();
        user.setId(1L);
        user.setAvatar("http://minio/old-avatar.png");

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setAvatar("http://minio/new-avatar.png");

        when(userMapper.selectById(1L)).thenReturn(user);

        service.updateUser(1L, dto);

        verify(fileService).deleteFile("http://minio/old-avatar.png");
        verify(userMapper).updateById(user);
    }

    @Test
    void updateUserShouldContinueWhenOldAvatarDeletionFails() {
        User user = new User();
        user.setId(1L);
        user.setAvatar("http://minio/old-avatar.png");

        UserUpdateDTO dto = new UserUpdateDTO();
        dto.setAvatar("http://minio/new-avatar.png");

        when(userMapper.selectById(1L)).thenReturn(user);
        doThrow(new RuntimeException("delete failed")).when(fileService).deleteFile("http://minio/old-avatar.png");

        service.updateUser(1L, dto);

        verify(fileService).deleteFile("http://minio/old-avatar.png");
        verify(userMapper).updateById(any(User.class));
    }
}
