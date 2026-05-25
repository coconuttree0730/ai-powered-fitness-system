package com.fitness.integration.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fitness.modules.user.model.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * UserDetails [实现类]
 * 包装 User 实体，提供 Spring Security 所需的用户详情信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;
    private String username;

    /**
     * 密码 json 解析忽略
     */
    @JsonIgnore
    private String password;
    private String phone;
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;

    /**
     * 权限列表
     */
    private Collection<? extends GrantedAuthority> authorities;



    /**
     * 从 User 实体构建 UserDetailsImpl
     *
     * @param user  用户实体
     * @param roles 角色列表
     * @return UserDetailsImpl 对象
     */
    public static UserDetailsImpl build(User user, List<String> roles) {
        List<GrantedAuthority> authorities = roles.stream()
                // 将角色列表转换为 SimpleGrantedAuthority 对象
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getPhone(),
                user.getEmail(),
                user.getAvatar(),
                user.getStatus(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    /**
     * 账户是否未过期
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账户是否未锁定
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 凭证是否未过期
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账户是否启用
     */
    @Override
    public boolean isEnabled() {
        return status != null && status == 1;
    }
}
