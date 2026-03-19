package com.fitness.modules.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fitness.modules.user.model.dto.LoginDTO;
import com.fitness.modules.user.model.dto.ResetPasswordDTO;
import com.fitness.modules.user.model.dto.UserDTO;
import com.fitness.modules.user.model.dto.UserQueryDTO;
import com.fitness.modules.user.model.dto.UserUpdateDTO;
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

    /**
     * 分页查询用户列表
     *
     * @param query 查询条件
     * @return 用户分页数据
     */
    IPage<UserVO> getUserPage(UserQueryDTO query);

    /**
     * 创建用户（管理员）
     *
     * @param dto 用户信息
     * @return 用户ID
     */
    Long createUser(UserDTO dto);

    /**
     * 更新用户
     *
     * @param userId 用户ID
     * @param dto    用户信息
     */
    void updateUser(Long userId, UserUpdateDTO dto);

    /**
     * 重置用户密码（管理员）
     *
     * @param userId 用户ID
     * @param dto    密码信息
     */
    void resetPassword(Long userId, ResetPasswordDTO dto);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     */
    void deleteUser(Long userId);

    /**
     * 更新用户状态
     *
     * @param userId 用户ID
     * @param status 状态
     */
    void updateUserStatus(Long userId, Integer status);

    /**
     * 获取教练列表
     *
     * @return 教练列表
     */
    java.util.List<com.fitness.modules.user.model.vo.CoachVO> getCoachList();
}
