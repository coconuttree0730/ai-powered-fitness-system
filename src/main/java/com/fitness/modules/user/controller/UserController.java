package com.fitness.modules.user.controller;

import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.Result;
import com.fitness.integration.minio.service.FileService;
import com.fitness.modules.user.model.dto.SendEmailCodeDTO;
import com.fitness.modules.user.model.dto.UpdateEmailDTO;
import com.fitness.modules.user.model.dto.UpdatePasswordBySmsDTO;
import com.fitness.modules.user.model.dto.UpdatePhoneDTO;
import com.fitness.modules.user.model.dto.UpdateUsernameDTO;
import com.fitness.modules.user.model.vo.UserVO;
import com.fitness.modules.user.service.EmailCodeService;
import com.fitness.modules.user.service.SmsCodeService;
import com.fitness.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器 （会员端：个人信息）
 * 处理用户信息相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;
    private final SmsCodeService smsCodeService;
    private final EmailCodeService emailCodeService;
    private final FileService fileService;

    /**
     * 获取当前登录用户信息
     * 需要用户已登录
     *
     * @return 用户信息
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public Result<UserVO> getCurrentUserInfo() {
        Long userId = getCurrentUserId();
        log.info("获取当前用户信息, userId: {}", userId);
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

    /**
     * 修改密码
     * 需要用户已登录
     *
     * @param request 包含旧密码和新密码的请求体
     * @return 操作结果
     */
    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> updatePassword(@RequestBody Map<String, String> request) {
        Long userId = getCurrentUserId();
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        if (oldPassword == null || oldPassword.isEmpty()) {
            return Result.error("旧密码不能为空");
        }
        if (newPassword == null || newPassword.isEmpty()) {
            return Result.error("新密码不能为空");
        }

        log.info("修改密码请求, userId: {}", userId);
        boolean success = userService.updatePassword(userId, oldPassword, newPassword);
        if (success) {
            return Result.success();
        } else {
            return Result.error("修改密码失败");
        }
    }

    /**
     * 更新用户名
     *
     * @param dto 更新用户名DTO
     * @return 更新后的用户信息
     */
    @PutMapping("/username")
    @PreAuthorize("isAuthenticated()")
    public Result<UserVO> updateUsername(@Valid @RequestBody UpdateUsernameDTO dto) {
        Long userId = getCurrentUserId();
        log.info("更新用户名请求, userId: {}, newUsername: {}", userId, dto.getUsername());
        UserVO userVO = userService.updateUsername(userId, dto.getUsername());
        return Result.success(userVO);
    }

    /**
     * 发送短信验证码到当前用户绑定的手机号
     * 用于更换手机号时的身份验证
     *
     * @return 操作结果
     */
    @PostMapping("/phone/code/old")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> sendOldPhoneCode() {
        Long userId = getCurrentUserId();
        UserVO userVO = userService.getUserInfo(userId);
        String phone = userVO.getPhone();

        if (phone == null || phone.isEmpty()) {
            return Result.error("当前用户未绑定手机号");
        }

        if (!smsCodeService.canSend(phone)) {
            long remaining = smsCodeService.getRemainingCooldown(phone);
            Map<String, Object> data = new HashMap<>();
            data.put("remainingSeconds", remaining);
            return Result.error(ErrorCode.SMS_CODE_SEND_TOO_FREQUENT.getCode(), "发送过于频繁，请稍后再试");
        }

        boolean success = smsCodeService.sendSmsCode(phone);
        if (success) {
            Map<String, Object> data = new HashMap<>();
            data.put("remainingSeconds", 60);
            return Result.success(data);
        } else {
            return Result.error("验证码发送失败");
        }
    }

    /**
     * 发送短信验证码到新手机号
     * 用于更换手机号时验证新手机号
     *
     * @param phone 新手机号
     * @return 操作结果
     */
    @PostMapping("/phone/code/new")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> sendNewPhoneCode(@RequestParam String phone) {
        if (!smsCodeService.canSend(phone)) {
            long remaining = smsCodeService.getRemainingCooldown(phone);
            Map<String, Object> data = new HashMap<>();
            data.put("remainingSeconds", remaining);
            return Result.error(ErrorCode.SMS_CODE_SEND_TOO_FREQUENT.getCode(), "发送过于频繁，请稍后再试");
        }

        boolean success = smsCodeService.sendSmsCode(phone);
        if (success) {
            Map<String, Object> data = new HashMap<>();
            data.put("remainingSeconds", 60);
            return Result.success(data);
        } else {
            return Result.error("验证码发送失败");
        }
    }

    /**
     * 更新手机号
     *
     * @param dto 更新手机号DTO
     * @return 更新后的用户信息
     */
    @PutMapping("/phone")
    @PreAuthorize("isAuthenticated()")
    public Result<UserVO> updatePhone(@Valid @RequestBody UpdatePhoneDTO dto) {
        Long userId = getCurrentUserId();
        log.info("更新手机号请求, userId: {}, newPhone: {}", userId, dto.getPhone());
        UserVO userVO = userService.updatePhone(userId, dto.getPhone(), dto.getCode(), dto.getOldCode());
        return Result.success(userVO);
    }

    /**
     * 发送邮箱验证码
     *
     * @param dto 发送邮箱验证码DTO
     * @return 操作结果
     */
    @PostMapping("/email/code")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> sendEmailCode(@Valid @RequestBody SendEmailCodeDTO dto) {
        if (!emailCodeService.canSend(dto.getEmail())) {
            long remaining = emailCodeService.getRemainingCooldown(dto.getEmail());
            Map<String, Object> data = new HashMap<>();
            data.put("remainingSeconds", remaining);
            return Result.error(ErrorCode.SMS_CODE_SEND_TOO_FREQUENT.getCode(), "发送过于频繁，请稍后再试");
        }

        boolean success = emailCodeService.sendEmailCode(dto.getEmail());
        if (success) {
            Map<String, Object> data = new HashMap<>();
            data.put("remainingSeconds", 60);
            return Result.success(data);
        } else {
            return Result.error("验证码发送失败");
        }
    }

    /**
     * 更新邮箱
     *
     * @param dto 更新邮箱DTO
     * @return 更新后的用户信息
     */
    @PutMapping("/email")
    @PreAuthorize("isAuthenticated()")
    public Result<UserVO> updateEmail(@Valid @RequestBody UpdateEmailDTO dto) {
        Long userId = getCurrentUserId();
        log.info("更新邮箱请求, userId: {}, newEmail: {}", userId, dto.getEmail());
        UserVO userVO = userService.updateEmail(userId, dto.getEmail(), dto.getCode());
        return Result.success(userVO);
    }

    /**
     * 发送短信验证码到当前用户绑定的手机号（用于修改密码）
     *
     * @return 操作结果
     */
    @PostMapping("/password/code")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> sendPasswordChangeCode() {
        Long userId = getCurrentUserId();
        UserVO userVO = userService.getUserInfo(userId);
        String phone = userVO.getPhone();

        if (phone == null || phone.isEmpty()) {
            return Result.error("当前用户未绑定手机号，无法使用此方式修改密码");
        }

        // 检查每日发送限制
        if (smsCodeService.isDailyLimitExceeded(phone)) {
            throw new BusinessException(ErrorCode.SMS_CODE_DAILY_LIMIT_EXCEEDED);
        }

        if (!smsCodeService.canSend(phone)) {
            long remaining = smsCodeService.getRemainingCooldown(phone);
            Map<String, Object> data = new HashMap<>();
            data.put("remainingSeconds", remaining);
            return Result.error(ErrorCode.SMS_CODE_SEND_TOO_FREQUENT.getCode(), "发送过于频繁，请稍后再试");
        }

        boolean success = smsCodeService.sendSmsCode(phone);
        if (success) {
            Map<String, Object> data = new HashMap<>();
            data.put("remainingSeconds", 60);
            data.put("remainingDailyCount", smsCodeService.getRemainingDailyCount(phone));
            return Result.success(data);
        } else {
            return Result.error("验证码发送失败");
        }
    }

    /**
     * 通过短信验证码修改密码
     *
     * @param dto 修改密码DTO
     * @return 操作结果
     */
    @PutMapping("/password/by-sms")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> updatePasswordBySms(@Valid @RequestBody UpdatePasswordBySmsDTO dto) {
        Long userId = getCurrentUserId();
        log.info("通过短信验证码修改密码请求, userId: {}", userId);
        boolean success = userService.updatePasswordBySmsCode(userId, dto.getSmsCode(), dto.getNewPassword());
        if (success) {
            return Result.success();
        } else {
            return Result.error("修改密码失败");
        }
    }

    /**
     * 上传用户头像
     * 上传图片到MinIO并更新用户头像URL
     *
     * @param file 头像文件
     * @return 更新后的用户信息
     */
    @PostMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    public Result<UserVO> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Long userId = getCurrentUserId();
        log.info("上传头像请求, userId: {}", userId);

        // 1. 上传图片到MinIO
        com.fitness.integration.minio.model.vo.FileUploadVO uploadResult = fileService.uploadImage(file);

        // 2. 更新用户头像URL到数据库
        UserVO userVO = userService.uploadAvatar(userId, uploadResult.getFileUrl());

        return Result.success(userVO);
    }

    /**
     * 从SecurityContext获取当前用户ID
     *
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        // 从Principal中获取用户ID
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.fitness.integration.security.UserDetailsImpl) {
            return ((com.fitness.integration.security.UserDetailsImpl) principal).getId();
        }
        return null;
    }
}
