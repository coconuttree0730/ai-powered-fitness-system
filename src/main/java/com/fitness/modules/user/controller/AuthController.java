package com.fitness.modules.user.controller;

import com.fitness.common.exception.BusinessException;
import com.fitness.common.result.Result;
import com.fitness.modules.user.model.dto.LoginDTO;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户注册、登录等认证相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final SliderVerifyService sliderVerifyService;
    private final SmsCodeService smsCodeService;

    /**
     * 用户注册
     *
     * @param dto 注册信息
     * @return 注册成功的用户信息
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody UserDTO dto) {
        log.info("用户注册请求: {}", dto.getUsername());
        UserVO userVO = userService.register(dto);
        return Result.success(userVO);
    }

    /**
     * 用户登录
     *
     * @param dto 登录信息
     * @return Token信息
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        log.info("用户登录请求: {}", dto.getUsername());
        Map<String, Object> tokenInfo = userService.login(dto);
        return Result.success(tokenInfo);
    }

    /**
     * 获取滑块验证令牌
     *
     * @return 验证令牌
     */
    @GetMapping("/slider-verify/token")
    public Result<SliderVerifyTokenVO> getSliderVerifyToken() {
        log.info("获取滑块验证令牌");
        SliderVerifyTokenVO tokenVO = sliderVerifyService.generateToken();
        return Result.success(tokenVO);
    }

    /**
     * 验证滑块结果
     *
     * @param dto 验证数据
     * @return 验证结果
     */
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

    /**
     * 发送短信验证码
     * 需要先完成滑块验证
     *
     * @param dto 请求数据
     * @return 发送结果
     */
    @PostMapping("/sms-code")
    public Result<Map<String, Object>> sendSmsCode(@Valid @RequestBody SmsCodeDTO dto) {
        try {
        log.info("发送短信验证码请求: phone={}", dto.getPhone());

        // 1. 检查滑块验证是否通过
        if (!sliderVerifyService.isVerified(dto.getVerifyToken())) {
            log.warn("发送短信验证码失败: 滑块验证未通过或已过期, phone={}", dto.getPhone());
            return Result.error(400, "请先完成滑块验证");
        }

        // 2. 检查发送频率
        if (!smsCodeService.canSend(dto.getPhone())) {
            long remainingSeconds = smsCodeService.getRemainingCooldown(dto.getPhone());
            log.warn("发送短信验证码失败: 发送过于频繁, phone={}, remaining={}s", dto.getPhone(), remainingSeconds);
            return Result.error(429, "发送过于频繁，请" + remainingSeconds + "秒后再试");
        }

        // 3. 使验证令牌失效（一次性使用）
        sliderVerifyService.invalidateToken(dto.getVerifyToken());

        // 4. 发送短信验证码
        boolean sent = smsCodeService.sendSmsCode(dto.getPhone());

        Map<String, Object> result = new HashMap<>();
        result.put("sent", true);
        result.put("message", "验证码已发送");

        return Result.success(result);
        } catch (BusinessException e) {
        log.warn("发送短信验证码业务异常: phone={}, message={}", dto.getPhone(), e.getMessage());
        return Result.error(400, e.getMessage());
        }
    }

    /**
     * 短信验证码登录
     * 如果手机号未注册，自动创建新用户
     *
     * @param dto 登录信息
     * @return Token信息
     */
    @PostMapping("/login/sms")
    public Result<Map<String, Object>> loginBySmsCode(@Valid @RequestBody SmsCodeLoginDTO dto) {
        log.info("短信验证码登录请求: phone={}", dto.getPhone());
        Map<String, Object> tokenInfo = userService.loginBySmsCode(dto.getPhone(), dto.getSmsCode());
        return Result.success(tokenInfo);
    }
}
