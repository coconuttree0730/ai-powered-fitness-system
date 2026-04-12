package com.fitness.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.utils.JwtUtils;
import com.fitness.integration.minio.service.FileService;
import com.fitness.modules.user.mapper.RoleMapper;
import com.fitness.modules.user.mapper.UserMapper;
import com.fitness.modules.user.model.dto.LoginDTO;
import com.fitness.modules.user.model.dto.ResetPasswordDTO;
import com.fitness.modules.user.model.dto.UserDTO;
import com.fitness.modules.user.model.dto.UserQueryDTO;
import com.fitness.modules.user.model.dto.UserUpdateDTO;
import com.fitness.modules.user.model.entity.Role;
import com.fitness.modules.user.model.entity.User;
import com.fitness.modules.user.model.vo.UserVO;
import com.fitness.modules.user.model.vo.CoachVO;
import com.fitness.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import cn.hutool.core.bean.BeanUtil;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    private final FileService fileService;
    private final com.fitness.modules.user.service.SmsCodeService smsCodeService;
    private final com.fitness.modules.user.service.EmailCodeService emailCodeService;
    private final com.fitness.modules.user.service.CoachDetailService coachDetailService;

    /**
     * 注册（短信校验登录）方法
     */
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
        user.setAvatar(dto.getAvatar());
        user.setStatus(1); // 默认启用状态
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 保存用户
        userMapper.insert(user);

        // 如果指定了角色编码，则关联角色
        if (dto.getRoleCode() != null && !dto.getRoleCode().isEmpty()) {
            Role role = roleMapper.selectByRoleCode(dto.getRoleCode());
            if (role != null) {
                // 插入用户-角色关联
                userMapper.insertUserRole(user.getId(), role.getId());
            }
        }

        log.info("用户注册成功: {}", dto.getUsername());
        return convertToVO(user);
    }

    /**
     * 用户名/手机号和密码登录方式
     * 支持手机号 + 密码 或 用户名 + 密码 登录
     */
    @Override
    public Map<String, Object> login(LoginDTO dto) {
        String account = dto.getUsername();
        User user = null;

        // 判断输入的是手机号还是用户名（手机号以1开头且长度为11位）
        if (isPhoneNumber(account)) {
            // 手机号登录
            user = userMapper.selectByPhone(account);
        } else {
            // 用户名登录
            user = userMapper.selectByUsername(account);
        }

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        // 检查用户状态 是否可用
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
        //生成jwt -  token
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
        BeanUtil.copyProperties(user, vo);
        return vo;
    }

    @Override
    public IPage<UserVO> getUserPage(UserQueryDTO query) {
        // 使用自定义查询支持角色筛选
        long offset = (long) (query.getPageNum() - 1) * query.getPageSize();
        long limit = query.getPageSize();

        List<User> userList = userMapper.selectUserPageWithRole(
            query.getUsername(),
            query.getPhone(),
            query.getStatus(),
            query.getRole(),
            offset,
            limit
        );

        long total = userMapper.selectUserCountWithRole(
            query.getUsername(),
            query.getPhone(),
            query.getStatus(),
            query.getRole()
        );

        Page<UserVO> resultPage = new Page<>(query.getPageNum(), query.getPageSize(), total);
        List<UserVO> voList = userList.stream().map(user -> {
            UserVO vo = convertToVO(user);
            vo.setStatus(user.getStatus());
            List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getId());
            vo.setRoles(roleCodes);
            return vo;
        }).collect(Collectors.toList());

        resultPage.setRecords(voList);
        return resultPage;
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
        user.setAvatar(dto.getAvatar());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userMapper.insert(user);

        if (StringUtils.hasText(dto.getRoleCode())) {
            Role role = roleMapper.selectByRoleCode(dto.getRoleCode());
            if (role != null) {
                // 添加角色关联表
                userMapper.insertUserRole(user.getId(), role.getId());

                // 如果创建的是教练角色用户，自动初始化教练详情 ********
                if ("COACH".equals(dto.getRoleCode())) {
                    coachDetailService.initCoachDetail(user.getId());
                    log.info("自动初始化教练详情: userId={}", user.getId());
                }
            }
        }

        log.info("管理员创建用户成功: {}", dto.getUsername());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long userId, UserUpdateDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 如果更换了头像，删除旧头像
        if (StringUtils.hasText(dto.getAvatar())
                && !dto.getAvatar().equals(user.getAvatar())
                && StringUtils.hasText(user.getAvatar())) {
            try {
                fileService.deleteFile(user.getAvatar());
                log.info("用户旧头像删除成功: userId={}, oldAvatar={}", userId, user.getAvatar());
            } catch (Exception e) {
                log.warn("用户旧头像删除失败: userId={}, oldAvatar={}, error={}", userId, user.getAvatar(), e.getMessage());
            }
        }

        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        user.setUpdateTime(LocalDateTime.now());

        userMapper.updateById(user);

        log.info("更新用户成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, ResetPasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("管理员重置用户密码成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 删除用户头像
        if (StringUtils.hasText(user.getAvatar())) {
            try {
                fileService.deleteFile(user.getAvatar());
                log.info("用户头像删除成功: userId={}, avatar={}", userId, user.getAvatar());
            } catch (Exception e) {
                log.warn("用户头像删除失败: userId={}, avatar={}, error={}", userId, user.getAvatar(), e.getMessage());
            }
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

    @Override
    public List<CoachVO> getCoachList() {
        return userMapper.selectCoachList();
    }

    /**
     * 使用短信验证码登录
     * @param phone 手机号
     * @return 用户名
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> loginBySmsCode(String phone, String smsCode) {
        // 1. 验证短信验证码
        if (!smsCodeService.verifySmsCode(phone, smsCode)) {
            throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
        }

        // 2. 根据手机号查询用户
        User user = userMapper.selectByPhone(phone);

        // 3. 如果用户不存在，自动注册
        if (user == null) {
            user = new User();
            user.setUsername(generateUsernameFromPhone(phone));
            user.setPhone(phone);
            // 新用户设置默认密码 "123456"
            user.setPassword(passwordEncoder.encode("123456"));
            user.setStatus(1);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());

            userMapper.insert(user);

            // 赋予默认角色 MEMBER（普通会员）
            Role role = roleMapper.selectByRoleCode("MEMBER");
            if (role != null) {
                userMapper.insertUserRole(user.getId(), role.getId());
                log.info("新用户自动赋予 MEMBER 角色: userId={}, roleId={}", user.getId(), role.getId());
            } else {
                log.error("无法找到 MEMBER 角色，用户注册后没有角色: userId={}", user.getId());
            }

            log.info("短信登录自动注册新用户: phone={}, userId={}", phone, user.getId());
        }

        // 4. 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 5. 获取用户角色
        List<Role> roles = userMapper.selectRolesByUserId(user.getId());
        List<String> roleCodes = roles.stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());

        // 6. 生成JWT Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("roles", roleCodes);
        String token = jwtUtils.generateToken(user.getUsername(), claims);

        // 7. 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("tokenType", "Bearer");
        result.put("expiresIn", jwtUtils.getExpirationTime());

        UserVO userVO = convertToVO(user);
        userVO.setRoles(roleCodes);
        result.put("userInfo", userVO);

        log.info("短信验证码登录成功: phone={}, userId={}", phone, user.getId());
        return result;
    }

    /**
     * 根据手机号生成用户名
     * 格式：user_手机号后8位_随机4位
     *
     * @param phone 手机号
     * @return 用户名
     */
    private String generateUsernameFromPhone(String phone) {
        String phoneSuffix = phone.substring(phone.length() - 8);
        Random random = new Random();
        int randomSuffix = random.nextInt(10000);
        return String.format("user_%s_%04d", phoneSuffix, randomSuffix);
    }

    /**
     * 生成随机密码
     *
     * @return 随机密码
     */
    private String generateRandomPassword() {
        Random random = new Random();
        StringBuilder password = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        for (int i = 0; i < 16; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
        return password.toString();
    }

    /**
     * 判断输入的账号是否为手机号
     * 规则：以1开头，且长度为11位的纯数字
     *
     * @param account 登录账号
     * @return true-是手机号，false-是用户名
     */
    private boolean isPhoneNumber(String account) {
        if (account == null || account.length() != 11) {
            return false;
        }
        // 检查是否以1开头且全部为数字
        return account.matches("^1\\d{10}$");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUsername(Long userId, String username) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 检查新用户名是否已存在
        if (!username.equals(user.getUsername()) && isUsernameExists(username)) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        user.setUsername(username);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户修改用户名成功: userId={}, newUsername={}", userId, username);
        return getUserInfo(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updatePhone(Long userId, String phone, String code, String oldCode) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证旧手机号验证码
        String oldPhone = user.getPhone();
        if (oldPhone != null && !oldPhone.isEmpty()) {
            if (!smsCodeService.verifySmsCode(oldPhone, oldCode)) {
                throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
            }
        }

        // 验证新手机号验证码
        if (!smsCodeService.verifySmsCode(phone, code)) {
            throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
        }

        // 检查新手机号是否已被其他用户绑定
        if (isPhoneBoundByOther(phone, userId)) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_BOUND);
        }

        user.setPhone(phone);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户修改手机号成功: userId={}, newPhone={}", userId, phone);
        return getUserInfo(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateEmail(Long userId, String email, String code) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证邮箱验证码
        if (!emailCodeService.verifyEmailCode(email, code)) {
            throw new BusinessException(ErrorCode.EMAIL_CODE_ERROR);
        }

        // 检查新邮箱是否已被其他用户绑定
        if (isEmailBoundByOther(email, userId)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_BOUND);
        }

        user.setEmail(email);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户修改邮箱成功: userId={}, newEmail={}", userId, email);
        return getUserInfo(userId);
    }

    @Override
    public boolean isUsernameExists(String username) {
        User user = userMapper.selectByUsername(username);
        return user != null;
    }

    @Override
    public boolean isPhoneBoundByOther(String phone, Long userId) {
        User user = userMapper.selectByPhone(phone);
        return user != null && !user.getId().equals(userId);
    }

    @Override
    public boolean isEmailBoundByOther(String email, Long userId) {
        User user = userMapper.selectByEmail(email);
        return user != null && !user.getId().equals(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePasswordBySmsCode(Long userId, String smsCode, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证短信验证码
        String phone = user.getPhone();
        if (phone == null || phone.isEmpty()) {
            throw new BusinessException(ErrorCode.PHONE_NOT_BOUND);
        }

        if (!smsCodeService.verifySmsCode(phone, smsCode)) {
            throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户通过短信验证码修改密码成功: userId={}", userId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO uploadAvatar(Long userId, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 如果用户已有头像，删除旧头像
        if (StringUtils.hasText(user.getAvatar())) {
            try {
                fileService.deleteFile(user.getAvatar());
                log.info("用户旧头像删除成功: userId={}, oldAvatar={}", userId, user.getAvatar());
            } catch (Exception e) {
                log.warn("用户旧头像删除失败: userId={}, oldAvatar={}, error={}", userId, user.getAvatar(), e.getMessage());
            }
        }

        // 更新用户头像
        user.setAvatar(avatarUrl);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户头像上传成功: userId={}, avatar={}", userId, avatarUrl);
        return getUserInfo(userId);
    }
}
