package com.fitness.modules.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fitness.common.result.Result;
import com.fitness.modules.user.model.dto.ResetPasswordDTO;
import com.fitness.modules.user.model.dto.UserDTO;
import com.fitness.modules.user.model.dto.UserQueryDTO;
import com.fitness.modules.user.model.dto.UserUpdateDTO;
import com.fitness.modules.user.model.vo.UserVO;
import com.fitness.modules.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Tag(name = "用户管理（后台）", description = "管理员对用户的增删改查及状态管理接口")
public class UserAdminController {

    private final UserService userService;

    @Operation(summary = "分页查询用户列表")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<IPage<UserVO>> getUserPage(@Valid UserQueryDTO query) {
        log.info("分页查询用户列表: {}", query);
        IPage<UserVO> page = userService.getUserPage(query);
        return Result.success(page);
    }

    @Operation(summary = "创建用户")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Long> createUser(@Valid @RequestBody UserDTO dto) {
        log.info("创建用户请求: username={}", dto.getUsername());
        Long userId = userService.createUser(dto);
        return Result.success(userId);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        log.info("更新用户请求: userId={}", id);
        userService.updateUser(id, dto);
        return Result.success();
    }

    @Operation(summary = "重置用户密码")
    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordDTO dto) {
        log.info("重置用户密码请求: userId={}", id);
        userService.resetPassword(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        log.info("删除用户请求: userId={}", id);
        userService.deleteUser(id);
        return Result.success();
    }

    @Operation(summary = "更新用户状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        log.info("更新用户状态请求: userId={}, status={}", id, status);
        userService.updateUserStatus(id, status);
        return Result.success();
    }
}
