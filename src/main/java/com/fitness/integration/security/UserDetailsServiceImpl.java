package com.fitness.integration.security;

import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.modules.user.mapper.UserMapper;
import com.fitness.modules.user.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * UserDetailsService 实现类
 * 用于从数据库加载用户信息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    /**
     * 根据用户名加载用户详情
     *
     * @param username 用户名
     * @return UserDetails 对象
     * @throws UsernameNotFoundException 用户不存在时抛出
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("加载用户信息: {}", username);

        // 查询用户
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            log.warn("用户不存在: {}", username);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            log.warn("用户已被禁用: {}", username);
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 查询用户角色
        List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
        if (roles.isEmpty()) {
            // 如果没有角色，默认赋予普通用户角色
            roles = List.of("USER");
        }

        log.debug("用户 {} 的角色: {}", username, roles);

        return UserDetailsImpl.build(user, roles);
    }

    /**
     * 根据用户ID加载用户详情
     *
     * @param userId 用户ID
     * @return UserDetails 对象
     * @throws UsernameNotFoundException 用户不存在时抛出
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        log.debug("根据ID加载用户信息: {}", userId);

        // 查询用户
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("用户不存在, ID: {}", userId);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 检查用户状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            log.warn("用户已被禁用, ID: {}", userId);
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 查询用户角色
        List<String> roles = userMapper.selectRoleCodesByUserId(user.getId());
        if (roles.isEmpty()) {
            roles = List.of("USER");
        }

        return UserDetailsImpl.build(user, roles);
    }
}
