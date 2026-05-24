package com.bfzy.platform.data.model.security;

import com.baomidou.mybatisplus.annotation.TableName;
import com.bfzy.platform.data.model.BaseEntity;
import com.bfzy.platform.data.model.enums.IdentityType;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户认证方式实体.
 * <p>
 * 一个用户可绑定多种登录方式：密码、微信、QQ、邮箱、手机号等。
 * 唯一约束 (identity_type, identifier)，防止重复绑定。
 * </p>
 *
 * @author zhangyu
 */
@Getter
@Setter
@TableName("sys_user_auth")
public class UserAuthEntity extends BaseEntity {

    /**
     * 用户 ID（关联 sys_user.id）
     */
    private Long userId;

    /**
     * 认证类型：PASSWORD, WECHAT_OPENID, QQ_OPENID, GITHUB, EMAIL, PHONE ...
     */
    private IdentityType identityType;

    /**
     * 标识符：密码登录→username，微信→openId，邮箱→email
     */
    private String identifier;

    /**
     * 凭据：密码登录→BCrypt hash，OAuth2→unionId 或空
     */
    private String credential;
}
