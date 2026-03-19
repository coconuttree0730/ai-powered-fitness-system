package com.fitness.modules.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fitness.common.result.Result;
import com.fitness.modules.user.model.dto.ResetPasswordDTO;
import com.fitness.modules.user.model.dto.UserDTO;
import com.fitness.modules.user.model.dto.UserQueryDTO;
import com.fitness.modules.user.model.dto.UserUpdateDTO;
import com.fitness.modules.user.model.vo.UserVO;
import com.fitness.modules.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<UserVO>> getUserPage(UserQueryDTO query) {
        log.info("分页查询用户列表: {}", query);
        IPage<UserVO> page = userService.getUserPage(query);
        return Result.success(page);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> createUser(@Valid @RequestBody UserDTO dto) {
        log.info("创建用户请求: username={}", dto.getUsername());
        Long userId = userService.createUser(dto);
        return Result.success(userId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        log.info("更新用户请求: userId={}", id);
        userService.updateUser(id, dto);
        return Result.success();
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordDTO dto) {
        log.info("重置用户密码请求: userId={}", id);
        userService.resetPassword(id, dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        log.info("删除用户请求: userId={}", id);
        userService.deleteUser(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        log.info("更新用户状态请求: userId={}, status={}", id, status);
        userService.updateUserStatus(id, status);
        return Result.success();
    }
}
