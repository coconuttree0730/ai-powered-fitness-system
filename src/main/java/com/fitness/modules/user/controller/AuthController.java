package com.fitness.modules.user.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.user.model.dto.LoginDTO;
import com.fitness.modules.user.model.dto.SliderVerifyDTO;
import com.fitness.modules.user.model.dto.SmsCodeDTO;
import com.fitness.modules.user.model.dto.UserDTO;
import com.fitness.modules.user.model.vo.SliderVerifyResultVO;
import com.fitness.modules.user.model.vo.SliderVerifyTokenVO;
import com.fitness.modules.user.model.vo.UserVO;
import com.fitness.modules.user.service.SliderVerifyService;
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
        log.info("发送短信验证码请求: phone={}", dto.getPhone());

        // 1. 检查滑块验证是否通过
        if (!sliderVerifyService.isVerified(dto.getVerifyToken())) {
            log.warn("发送短信验证码失败: 滑块验证未通过或已过期, phone={}", dto.getPhone());
            return Result.error(400, "请先完成滑块验证");
        }

        // 2. 使验证令牌失效（一次性使用）
        sliderVerifyService.invalidateToken(dto.getVerifyToken());

        // 3. 模拟发送短信验证码（后续接入真实短信服务）
        // TODO: 接入真实短信服务，生成并发送验证码 ；后续接入短信服务
        log.info(": phone={}，短信验证码发送成功", dto.getPhone());

        Map<String, Object> result = new HashMap<>();
        result.put("sent", true);
        result.put("message", "验证码已发送");
        // 开发环境返回模拟验证码，生产环境应移除！ 使用短信服务取代
        result.put("demoCode", "123456");

        return Result.success(result);
    }
}
