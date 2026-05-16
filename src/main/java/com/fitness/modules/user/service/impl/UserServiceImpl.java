package com.fitness.modules.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.integration.minio.service.FileService;
import com.fitness.integration.security.JwtTokenProvider;
import com.fitness.modules.user.mapper.RoleMapper;
import com.fitness.modules.user.mapper.UserMapper;
import com.fitness.modules.user.model.dto.LoginDTO;
import com.fitness.modules.user.model.dto.ResetPasswordDTO;
import com.fitness.modules.user.model.dto.UserDTO;
import com.fitness.modules.user.model.dto.UserQueryDTO;
import com.fitness.modules.user.model.dto.UserUpdateDTO;
import com.fitness.modules.user.model.entity.Role;
import com.fitness.modules.user.model.entity.User;
import com.fitness.modules.user.model.vo.CoachVO;
import com.fitness.modules.user.model.vo.UserVO;
import com.fitness.modules.user.service.CoachDetailService;
import com.fitness.modules.user.service.EmailCodeService;
import com.fitness.modules.user.service.SmsCodeService;
import com.fitness.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.security.SecureRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String DEFAULT_SMS_LOGIN_PASSWORD = "123456";

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final FileService fileService;
    private final SmsCodeService smsCodeService;
    private final EmailCodeService emailCodeService;
    private final CoachDetailService coachDetailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(UserDTO dto) {
        if (userMapper.selectByUsername(dto.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        User user = buildUser(
                dto.getUsername(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getPhone(),
                dto.getEmail(),
                dto.getAvatar()
        );
        userMapper.insert(user);

        if (StringUtils.hasText(dto.getRoleCode())) {
            bindUserRole(user.getId(), dto.getRoleCode());
        }

        log.info("用户注册成功: {}", dto.getUsername());
        return convertToVO(user);
    }

    @Override
    public Map<String, Object> login(LoginDTO dto) {
        User user = findUserByLoginAccount(dto.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        ensureUserEnabled(user);

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        log.info("用户登录成功: {}", dto.getUsername());
        return buildAuthResult(user, Boolean.TRUE.equals(dto.getRememberMe()));
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = getRequiredUser(userId);
        UserVO userVO = convertToVO(user);
        userVO.setRoles(getRoleCodes(userId));
        return userVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = getRequiredUser(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        log.info("用户修改密码成功: {}", user.getUsername());
        return true;
    }

    @Override
    public IPage<UserVO> getUserPage(UserQueryDTO query) {
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
            vo.setRoles(userMapper.selectRoleCodesByUserId(user.getId()));
            return vo;
        }).collect(Collectors.toList());
        resultPage.setRecords(voList);
        return resultPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserDTO dto) {
        if (userMapper.selectByUsername(dto.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        User user = buildUser(
                dto.getUsername(),
                passwordEncoder.encode(dto.getPassword()),
                dto.getPhone(),
                dto.getEmail(),
                dto.getAvatar()
        );
        userMapper.insert(user);

        if (StringUtils.hasText(dto.getRoleCode())) {
            Role role = bindUserRole(user.getId(), dto.getRoleCode());
            if (role != null && "COACH".equals(dto.getRoleCode())) {
                coachDetailService.initCoachDetail(user.getId());
                log.info("自动初始化教练详情: userId={}", user.getId());
            }
        }

        log.info("管理员创建用户成功: {}", dto.getUsername());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long userId, UserUpdateDTO dto) {
        User user = getRequiredUser(userId);

        if (StringUtils.hasText(dto.getAvatar())
                && !dto.getAvatar().equals(user.getAvatar())
                && StringUtils.hasText(user.getAvatar())) {
            deleteAvatarQuietly(userId, user.getAvatar());
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
        userMapper.updateById(user);

        log.info("更新用户成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long userId, ResetPasswordDTO dto) {
        User user = getRequiredUser(userId);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        userMapper.updateById(user);

        log.info("管理员重置用户密码成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        User user = getRequiredUser(userId);
        if (StringUtils.hasText(user.getAvatar())) {
            deleteAvatarQuietly(userId, user.getAvatar());
        }

        userMapper.deleteById(userId);
        log.info("删除用户成功: userId={}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, Integer status) {
        User user = getRequiredUser(userId);
        user.setStatus(status);
        userMapper.updateById(user);

        log.info("更新用户状态成功: userId={}, status={}", userId, status);
    }

    @Override
    public List<CoachVO> getCoachList() {
        return userMapper.selectCoachList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> loginBySmsCode(String phone, String smsCode, boolean rememberMe) {
        if (!smsCodeService.verifySmsCode(phone, smsCode)) {
            throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
        }

        User user = userMapper.selectByPhone(phone);
        if (user == null) {
            user = buildUser(
                    generateUsernameFromPhone(phone),
                    passwordEncoder.encode(DEFAULT_SMS_LOGIN_PASSWORD),
                    phone,
                    null,
                    null
            );
            userMapper.insert(user);

            Role role = bindUserRole(user.getId(), "MEMBER");
            if (role != null) {
                log.info("新用户自动赋予 MEMBER 角色: userId={}, roleId={}", user.getId(), role.getId());
            } else {
                log.error("无法找到 MEMBER 角色，用户注册后没有角色: userId={}", user.getId());
            }
            log.info("短信登录自动注册新用户: phone={}, userId={}", phone, user.getId());
        }

        ensureUserEnabled(user);
        log.info("短信验证码登录成功: phone={}, userId={}", phone, user.getId());
        return buildAuthResult(user, rememberMe);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUsername(Long userId, String username) {
        User user = getRequiredUser(userId);
        if (!username.equals(user.getUsername()) && isUsernameExists(username)) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        user.setUsername(username);
        userMapper.updateById(user);

        log.info("用户修改用户名成功: userId={}, newUsername={}", userId, username);
        return getUserInfo(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updatePhone(Long userId, String phone, String code, String oldCode) {
        User user = getRequiredUser(userId);

        String oldPhone = user.getPhone();
        if (StringUtils.hasText(oldPhone) && !smsCodeService.verifySmsCode(oldPhone, oldCode)) {
            throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
        }
        if (!smsCodeService.verifySmsCode(phone, code)) {
            throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
        }
        if (isPhoneBoundByOther(phone, userId)) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_BOUND);
        }

        user.setPhone(phone);
        userMapper.updateById(user);

        log.info("用户修改手机号成功: userId={}, newPhone={}", userId, phone);
        return getUserInfo(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateEmail(Long userId, String email, String code) {
        User user = getRequiredUser(userId);
        if (!emailCodeService.verifyEmailCode(email, code)) {
            throw new BusinessException(ErrorCode.EMAIL_CODE_ERROR);
        }
        if (isEmailBoundByOther(email, userId)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_BOUND);
        }

        user.setEmail(email);
        userMapper.updateById(user);

        log.info("用户修改邮箱成功: userId={}, newEmail={}", userId, email);
        return getUserInfo(userId);
    }

    @Override
    public boolean isUsernameExists(String username) {
        return userMapper.selectByUsername(username) != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePasswordBySmsCode(Long userId, String smsCode, String newPassword) {
        User user = getRequiredUser(userId);
        String phone = user.getPhone();
        if (!StringUtils.hasText(phone)) {
            throw new BusinessException(ErrorCode.PHONE_NOT_BOUND);
        }
        if (!smsCodeService.verifySmsCode(phone, smsCode)) {
            throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        log.info("用户通过短信验证码修改密码成功: userId={}", userId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO uploadAvatar(Long userId, String avatarUrl) {
        User user = getRequiredUser(userId);
        deleteAvatarQuietly(userId, user.getAvatar());

        user.setAvatar(avatarUrl);
        userMapper.updateById(user);

        log.info("用户头像上传成功: userId={}, avatar={}", userId, avatarUrl);
        return getUserInfo(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateNickname(Long userId, String nickname) {
        User user = getRequiredUser(userId);
        user.setNickname(nickname);
        userMapper.updateById(user);

        log.info("用户更新昵称成功: userId={}, nickname={}", userId, nickname);
        return getUserInfo(userId);
    }

    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        return vo;
    }

    private User findUserByLoginAccount(String account) {
        if (isPhoneNumber(account)) {
            return userMapper.selectByPhone(account);
        }
        return userMapper.selectByUsername(account);
    }

    private User getRequiredUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private void ensureUserEnabled(User user) {
        if (user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private List<String> getRoleCodes(Long userId) {
        return userMapper.selectRolesByUserId(userId).stream()
                .map(Role::getRoleCode)
                .collect(Collectors.toList());
    }

    private Map<String, Object> buildAuthResult(User user, boolean rememberMe) {
        List<String> roleCodes = getRoleCodes(user.getId());
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), roleCodes);
        String refreshToken = rememberMe
                ? jwtTokenProvider.generateRememberMeRefreshToken(user.getId(), user.getUsername(), roleCodes)
                : jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername(), roleCodes);

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("tokenType", "Bearer");
        result.put("expiresIn", jwtTokenProvider.getAccessExpiration());

        UserVO userVO = convertToVO(user);
        userVO.setRoles(roleCodes);
        result.put("userInfo", userVO);
        return result;
    }

    @Override
    public Map<String, Object> refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        List<String> roles = jwtTokenProvider.getRolesFromToken(refreshToken);

        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, username, roles);

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", newAccessToken);
        result.put("tokenType", "Bearer");
        result.put("expiresIn", jwtTokenProvider.getAccessExpiration());
        return result;
    }

    private User buildUser(String username, String encodedPassword, String phone, String email, String avatar) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setPhone(phone);
        user.setEmail(email);
        user.setAvatar(avatar);
        user.setStatus(1);
        return user;
    }

    private Role bindUserRole(Long userId, String roleCode) {
        Role role = roleMapper.selectByRoleCode(roleCode);
        if (role != null) {
            userMapper.insertUserRole(userId, role.getId());
        }
        return role;
    }

    private void deleteAvatarQuietly(Long userId, String avatarUrl) {
        if (!StringUtils.hasText(avatarUrl)) {
            return;
        }

        try {
            fileService.deleteFile(avatarUrl);
            log.info("用户旧头像删除成功: userId={}, oldAvatar={}", userId, avatarUrl);
        } catch (Exception e) {
            log.warn("用户旧头像删除失败: userId={}, oldAvatar={}, error={}", userId, avatarUrl, e.getMessage());
        }
    }

    private String generateUsernameFromPhone(String phone) {
        String phoneSuffix = phone.substring(phone.length() - 8);
        int randomSuffix = SECURE_RANDOM.nextInt(10000);
        return String.format("user_%s_%04d", phoneSuffix, randomSuffix);
    }

    private boolean isPhoneNumber(String account) {
        if (account == null || account.length() != 11) {
            return false;
        }
        return account.matches("^1\\d{10}$");
    }

    private boolean isPhoneBoundByOther(String phone, Long userId) {
        User user = userMapper.selectByPhone(phone);
        return user != null && !user.getId().equals(userId);
    }

    private boolean isEmailBoundByOther(String email, Long userId) {
        User user = userMapper.selectByEmail(email);
        return user != null && !user.getId().equals(userId);
    }
}
