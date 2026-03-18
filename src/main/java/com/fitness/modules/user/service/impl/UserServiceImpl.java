package com.fitness.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.utils.JwtUtils;
import com.fitness.modules.user.mapper.RoleMapper;
import com.fitness.modules.user.mapper.UserMapper;
import com.fitness.modules.user.model.dto.LoginDTO;
import com.fitness.modules.user.model.dto.UserDTO;
import com.fitness.modules.user.model.dto.UserQueryDTO;
import com.fitness.modules.user.model.entity.Role;
import com.fitness.modules.user.model.entity.User;
import com.fitness.modules.user.model.vo.UserVO;
import com.fitness.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(UserDTO dto) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(dto.getUsername());
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // 创建新用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(1); // 默认启用状态
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 保存用户
        userMapper.insert(user);

        // 如果指定了角色编码，则关联角色
        if (dto.getRoleCode() != null && !dto.getRoleCode().isEmpty()) {
            Role role = roleMapper.selectByRoleCode(dto.getRoleCode());
            if (role != null) {
                // 插入用户角色关联
                userMapper.insertUserRole(user.getId(), role.getId());
            }
        }

        log.info("用户注册成功: {}", dto.getUsername());
        return convertToVO(user);
    }

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        // 查询用户
        User user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 获取用户角色
        List<Role> roles = userMapper.selectRolesByUserId(user.getId());
        List<String> roleCodes = roles.stream()
                .map(Role::getRoleCode) //角色编码：ROLE_USER 、 ROLE_ADMIN 、 ROLE_MANAGER
                .collect(Collectors.toList()); //一个用户可以有多个角色：list

        // 生成JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("roles", roleCodes);
        String token = jwtUtils.generateToken(user.getUsername(), claims);

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("tokenType", "Bearer");
        result.put("expiresIn", jwtUtils.getExpirationTime());

        // 添加用户信息(实体转化)
        UserVO userVO = convertToVO(user);
        userVO.setRoles(roleCodes);
        result.put("userInfo", userVO);

        log.info("用户登录成功: {}", dto.getUsername());
        return result;
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserVO userVO = convertToVO(user);

        // 获取用户角色
        List<Role> roles = userMapper.selectRolesByUserId(userId);
        List<String> roleCodes = roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());
        //加入角色信息到userVo返回
        userVO.setRoles(roleCodes);

        return userVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户修改密码成功: {}", user.getUsername());
        return true;
    }

    /**
     * 将User实体转换为UserVO
     *
     * @param user 用户实体
     * @return 用户VO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setEmail(user.getEmail());
        vo.setAvatar(user.getAvatar());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    @Override
    public IPage<UserVO> getUserPage(UserQueryDTO query) {
        Page<User> page = new Page<>(query.getPageNum(), query.getPageSize());
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getUsername()), User::getUsername, query.getUsername())
               .like(StringUtils.hasText(query.getPhone()), User::getPhone, query.getPhone())
               .eq(query.getStatus() != null, User::getStatus, query.getStatus())
               .orderByDesc(User::getCreateTime);
        
        IPage<User> userPage = userMapper.selectPage(page, wrapper);
        
        return userPage.convert(user -> {
            UserVO vo = convertToVO(user);
            vo.setStatus(user.getStatus());
            List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getId());
            vo.setRoles(roleCodes);
            return vo;
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserDTO dto) {
        User existingUser = userMapper.selectByUsername(dto.getUsername());
        if (existingUser != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);

        if (StringUtils.hasText(dto.getRoleCode())) {
            Role role = roleMapper.selectByRoleCode(dto.getRoleCode());
            if (role != null) {
                userMapper.insertUserRole(user.getId(), role.getId());
            }
        }

        log.info("管理员创建用户成功: {}", dto.getUsername());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long userId, UserDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (StringUtils.hasText(dto.getPhone())) {
            user.setPhone(dto.getPhone());
        }
        if (StringUtils.hasText(dto.getEmail())) {
            user.setEmail(dto.getEmail());
        }
        user.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);

        log.info("更新用户成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        userMapper.deleteById(userId);

        log.info("删除用户成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("更新用户状态成功: userId={}, status={}", userId, status);
    }
}
