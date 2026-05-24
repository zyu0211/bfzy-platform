package com.bfzy.platform.data.model.security;

import com.baomidou.mybatisplus.annotation.TableName;
import com.bfzy.platform.data.model.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户档案实体.
 * <p>
 * 存储用户核心信息，不包含认证凭据。
 * 认证方式（密码、微信等）存储在 {@link UserAuthEntity} 表中。
 * </p>
 */
@Getter
@Setter
@TableName("sys_user")
public class UserEntity extends BaseEntity {

    /**
     * 用户名（唯一，可为空 — 纯第三方登录用户可能没有用户名）
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像 URL
     */
    private String avatar;

    /**
     * 邮箱（唯一，可为空）
     */
    private String email;

    /**
     * 手机号（唯一，可为空）
     */
    private String phone;
}
