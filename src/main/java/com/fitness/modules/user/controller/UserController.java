package com.fitness.modules.user.controller;

import com.fitness.common.constants.ErrorCode;
import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.Result;
import com.fitness.common.util.SensitiveDataMasker;
import com.fitness.integration.minio.service.FileService;
import com.fitness.modules.user.model.dto.SendEmailCodeDTO;
import com.fitness.modules.user.model.dto.UpdateEmailDTO;
import com.fitness.modules.user.model.dto.UpdateNicknameDTO;
import com.fitness.modules.user.model.dto.UpdatePasswordBySmsDTO;
import com.fitness.modules.user.model.dto.UpdatePasswordDTO;
import com.fitness.modules.user.model.dto.UpdatePhoneDTO;
import com.fitness.modules.user.model.dto.UpdateUsernameDTO;
import com.fitness.modules.user.model.vo.UserVO;
import com.fitness.modules.user.service.EmailCodeService;
import com.fitness.modules.user.service.SmsCodeService;
import com.fitness.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户个人信息", description = "当前登录用户的资料查询与资料维护接口")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;
    private final SmsCodeService smsCodeService;
    private final EmailCodeService emailCodeService;
    private final FileService fileService;

    @Operation(summary = "获取当前用户信息", description = "返回当前登录用户的基础资料、角色和账户信息")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public Result<UserVO> getCurrentUserInfo() {
        Long userId = getCurrentUserId();
        log.info("获取当前用户信息, userId: {}", userId);
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

    @Operation(summary = "修改登录密码", description = "校验旧密码后更新当前登录用户的登录密码")
    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto) {
        Long userId = getCurrentUserId();
        log.info("修改密码请求, userId: {}", userId);
        boolean success = userService.updatePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        if (success) {
            return Result.success();
        } else {
            return Result.error("修改密码失败");
        }
    }

    @Operation(summary = "修改用户名", description = "更新当前登录用户的登录用户名")
    @PutMapping("/username")
    @PreAuthorize("isAuthenticated()")
    public Result<UserVO> updateUsername(@Valid @RequestBody UpdateUsernameDTO dto) {
        Long userId = getCurrentUserId();
        log.info("更新用户名请求, userId: {}, newUsername: {}", userId, SensitiveDataMasker.maskUsername(dto.getUsername()));
        UserVO userVO = userService.updateUsername(userId, dto.getUsername());
        return Result.success(userVO);
    }

    @Operation(summary = "发送原手机号验证码", description = "向当前账号已绑定的手机号发送验证码，用于更换手机号前的身份校验")
    @PostMapping("/phone/code/old")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> sendOldPhoneCode() {
        Long userId = getCurrentUserId();
        UserVO userVO = userService.getUserInfo(userId);
        String phone = userVO.getPhone();

        if (phone == null || phone.isEmpty()) {
            return Result.error("当前用户未绑定手机号");
        }

        return sendSmsCodeAndBuildResponse(phone);
    }

    @Operation(summary = "发送新手机号验证码", description = "向待绑定的新手机号发送验证码，用于更换手机号")
    @PostMapping("/phone/code/new")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> sendNewPhoneCode(
            @Parameter(description = "待绑定的新手机号", example = "13800138000")
            @RequestParam String phone) {
        return sendSmsCodeAndBuildResponse(phone);
    }

    @Operation(summary = "修改手机号", description = "校验新旧手机号验证码后更新当前用户绑定手机号")
    @PutMapping("/phone")
    @PreAuthorize("isAuthenticated()")
    public Result<UserVO> updatePhone(@Valid @RequestBody UpdatePhoneDTO dto) {
        Long userId = getCurrentUserId();
        log.info("更新手机号请求, userId: {}, newPhone: {}", userId, SensitiveDataMasker.maskPhone(dto.getPhone()));
        UserVO userVO = userService.updatePhone(userId, dto.getPhone(), dto.getCode(), dto.getOldCode());
        return Result.success(userVO);
    }

    @Operation(summary = "发送邮箱验证码", description = "向指定新邮箱发送验证码，用于后续绑定或更换邮箱")
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

    @Operation(summary = "修改邮箱", description = "校验邮箱验证码后更新当前用户绑定邮箱")
    @PutMapping("/email")
    @PreAuthorize("isAuthenticated()")
    public Result<UserVO> updateEmail(@Valid @RequestBody UpdateEmailDTO dto) {
        Long userId = getCurrentUserId();
        log.info("更新邮箱请求, userId: {}, newEmail: {}", userId, SensitiveDataMasker.maskEmail(dto.getEmail()));
        UserVO userVO = userService.updateEmail(userId, dto.getEmail(), dto.getCode());
        return Result.success(userVO);
    }

    @Operation(summary = "发送改密短信验证码", description = "向当前绑定手机号发送短信验证码，用于短信方式修改密码")
    @PostMapping("/password/code")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> sendPasswordChangeCode() {
        Long userId = getCurrentUserId();
        UserVO userVO = userService.getUserInfo(userId);
        String phone = userVO.getPhone();

        if (phone == null || phone.isEmpty()) {
            return Result.error("当前用户未绑定手机号，无法使用此方式修改密码");
        }

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

    @Operation(summary = "短信验证码修改密码", description = "使用短信验证码校验身份后修改当前用户密码")
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

    @Operation(summary = "上传用户头像", description = "上传头像图片并更新当前登录用户的头像地址")
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public Result<UserVO> uploadAvatar(
            @Parameter(description = "头像图片文件")
            @RequestParam("file") MultipartFile file) {
        Long userId = getCurrentUserId();
        log.info("上传头像请求, userId: {}", userId);

        com.fitness.integration.minio.model.vo.FileUploadVO uploadResult = fileService.uploadImage(file);
        UserVO userVO = userService.uploadAvatar(userId, uploadResult.getFileUrl());

        return Result.success(userVO);
    }

    @Operation(summary = "检查用户名是否可用", description = "用于修改用户名时校验目标用户名是否已被占用")
    @GetMapping("/username/check")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> checkUsernameExists(
            @Parameter(description = "待校验的用户名", example = "fitness_user")
            @RequestParam String username) {
        log.info("检查用户名是否存在: {}", username);
        boolean exists = userService.isUsernameExists(username);
        Map<String, Object> result = new HashMap<>();
        result.put("exists", exists);
        result.put("available", !exists);
        return Result.success(result);
    }

    @Operation(summary = "修改昵称", description = "更新当前登录用户的展示昵称")
    @PutMapping("/nickname")
    @PreAuthorize("isAuthenticated()")
    public Result<UserVO> updateNickname(@Valid @RequestBody UpdateNicknameDTO dto) {
        Long userId = getCurrentUserId();
        log.info("更新昵称请求, userId: {}, nickname: {}", userId, SensitiveDataMasker.maskUsername(dto.getNickname()));
        UserVO userVO = userService.updateNickname(userId, dto.getNickname().trim());
        return Result.success(userVO);
    }

    private Result<Map<String, Object>> sendSmsCodeAndBuildResponse(String phone) {
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
        }
        return Result.error("验证码发送失败");
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof com.fitness.integration.security.UserDetailsImpl) {
            return ((com.fitness.integration.security.UserDetailsImpl) principal).getId();
        }
        return null;
    }
}
