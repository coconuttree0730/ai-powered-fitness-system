package com.fitness.modules.user.service;

import com.fitness.modules.user.model.dto.LoginDTO;
import com.fitness.modules.user.model.dto.UserDTO;
import com.fitness.modules.user.model.vo.UserVO;

import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param dto 注册信息
     * @return 用户信息
     */
    UserVO register(UserDTO dto);

    /**
     * 用户登录
     *
     * @param dto 登录信息
     * @return Token信息
     */
    Map<String, Object> login(LoginDTO dto);

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserVO getUserInfo(Long userId);

    /**
     * 修改密码
     *
     * @param userId      用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean updatePassword(Long userId, String oldPassword, String newPassword);
}
