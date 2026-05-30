package com.fitness.modules.user.controller;

import com.fitness.common.ratelimit.RateLimit;
import com.fitness.common.result.Result;
import com.fitness.common.util.SensitiveDataMasker;
import com.fitness.modules.user.model.dto.LoginDTO;
import com.fitness.modules.user.model.dto.LogoutDTO;
import com.fitness.modules.user.model.dto.RefreshTokenDTO;
import com.fitness.modules.user.model.dto.SliderVerifyDTO;
import com.fitness.modules.user.model.dto.SmsCodeDTO;
import com.fitness.modules.user.model.dto.SmsCodeLoginDTO;
import com.fitness.modules.user.model.dto.UserDTO;
import com.fitness.modules.user.model.vo.SliderVerifyResultVO;
import com.fitness.modules.user.model.vo.SliderVerifyTokenVO;
import com.fitness.modules.user.model.vo.UserVO;
import com.fitness.modules.user.service.SliderVerifyService;
import com.fitness.modules.user.service.SmsCodeService;
import com.fitness.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "注册、登录、验证码与令牌刷新相关接口")
public class AuthController {

    private final UserService userService;
    private final SliderVerifyService sliderVerifyService;
    private final SmsCodeService smsCodeService;

    @Operation(summary = "用户注册", description = "创建新的普通用户账号并返回注册后的用户信息")
    @RateLimit(key = "auth:register", limit = 5, window = 60)
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody UserDTO dto) {
        log.info("用户注册请求: {}", SensitiveDataMasker.maskUsername(dto.getUsername()));
        UserVO userVO = userService.register(dto);
        return Result.success(userVO);
    }

    @Operation(summary = "用户名密码登录", description = "校验用户名和密码并返回访问令牌与刷新令牌")
    @RateLimit(key = "auth:login", limit = 10, window = 60)
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        log.info("用户登录请求: {}", SensitiveDataMasker.maskUsername(dto.getUsername()));
        Map<String, Object> tokenInfo = userService.login(dto);
        return Result.success(tokenInfo);
    }

    @Operation(summary = "获取滑块验证令牌", description = "生成滑块验证所需的临时令牌")
    @GetMapping("/slider-verify/token")
    public Result<SliderVerifyTokenVO> getSliderVerifyToken() {
        log.info("获取滑块验证令牌");
        SliderVerifyTokenVO tokenVO = sliderVerifyService.generateToken();
        return Result.success(tokenVO);
    }

    @Operation(summary = "校验滑块验证结果", description = "验证滑块偏移量和时间戳，成功后返回后续操作可用的验证令牌")
    @PostMapping("/slider-verify/verify")
    public Result<SliderVerifyResultVO> verifySlider(@Valid @RequestBody SliderVerifyDTO dto) {
        log.info("滑块验证请求: token={}", dto.getToken());
        SliderVerifyResultVO result = sliderVerifyService.verify(
                dto.getToken(),
                dto.getSliderValue(),
                dto.getTimestamp()
        );

        if (result.getVerified()) {
            return Result.success(result);
        } else {
            return Result.error(400, result.getMessage());
        }
    }

    @Operation(summary = "发送登录短信验证码", description = "在滑块验证通过后向指定手机号发送短信验证码")
    @RateLimit(key = "auth:sms", limit = 10, window = 60)
    @PostMapping("/sms-code")
    public Result<Map<String, Object>> sendSmsCode(@Valid @RequestBody SmsCodeDTO dto) {
        log.info("发送短信验证码请求: phone={}", SensitiveDataMasker.maskPhone(dto.getPhone()));

        if (!sliderVerifyService.isVerified(dto.getVerifyToken())) {
            log.warn("发送短信验证码失败: 滑块验证未通过或已过期, phone={}", dto.getPhone());
            return Result.error(400, "请先完成滑块验证");
        }

        if (!smsCodeService.canSend(dto.getPhone())) {
            long remainingSeconds = smsCodeService.getRemainingCooldown(dto.getPhone());
            log.warn("发送短信验证码失败: 发送过于频繁, phone={}, remaining={}s", dto.getPhone(), remainingSeconds);
            return Result.error(429, "发送过于频繁，请" + remainingSeconds + "秒后再试");
        }

        sliderVerifyService.invalidateToken(dto.getVerifyToken());
        smsCodeService.sendSmsCode(dto.getPhone());

        Map<String, Object> result = new HashMap<>();
        result.put("sent", true);
        result.put("message", "验证码已发送");

        return Result.success(result);
    }

    @Operation(summary = "短信验证码登录", description = "使用手机号和短信验证码登录，未注册手机号将自动创建账号")
    @PostMapping("/login/sms")
    public Result<Map<String, Object>> loginBySmsCode(@Valid @RequestBody SmsCodeLoginDTO dto) {
        log.info("短信验证码登录请求: phone={}", SensitiveDataMasker.maskPhone(dto.getPhone()));
        Map<String, Object> tokenInfo = userService.loginBySmsCode(
                dto.getPhone(), dto.getSmsCode(), Boolean.TRUE.equals(dto.getRememberMe()));
        return Result.success(tokenInfo);
    }

    @Operation(summary = "刷新访问令牌", description = "使用刷新令牌换取新的访问令牌")
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshToken(@Valid @RequestBody RefreshTokenDTO dto) {
        log.info("刷新Token请求");
        Map<String, Object> tokenInfo = userService.refreshToken(dto.getRefreshToken());
        return Result.success(tokenInfo);
    }

    @Operation(summary = "用户登出", description = "注销当前Access Token和Refresh Token，使其无法继续使用")
    @PostMapping("/logout")
    public Result<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) LogoutDTO dto) {
        log.info("用户登出请求");

        String accessToken = null;
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            accessToken = authorization.substring(7);
        }

        String refreshToken = (dto != null) ? dto.getRefreshToken() : null;

        if (StringUtils.hasText(accessToken)) {
            userService.logout(accessToken, refreshToken);
        }

        return Result.success();
    }
}
