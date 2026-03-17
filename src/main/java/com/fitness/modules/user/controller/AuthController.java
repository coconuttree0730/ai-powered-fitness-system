package com.fitness.modules.user.controller;

import com.fitness.common.result.Result;
import com.fitness.modules.user.model.dto.LoginDTO;
import com.fitness.modules.user.model.dto.UserDTO;
import com.fitness.modules.user.model.vo.UserVO;
import com.fitness.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
}
