package com.bfzy.platform.data.model.enums;

/**
 * 认证类型枚举.
 * <p>
 * 定义系统支持的认证方式，后续可在此基础上扩展 OAuth2 登录。
 * Phase 1 只使用 PASSWORD 类型。
 * </p>
 */
public enum IdentityType {

    /**
     * 用户名+密码登录
     */
    PASSWORD,
    /**
     * 微信登录（openId）
     */
    WECHAT_OPENID,
    /**
     * QQ 登录
     */
    QQ_OPENID,
    /**
     * GitHub OAuth
     */
    GITHUB,
    /**
     * 邮箱验证码登录
     */
    EMAIL,
    /**
     * 手机号验证码登录
     */
    PHONE,
    ;

    /**
     * 当前是否为密码登录方式
     */
    public boolean isPassword() {
        return this == PASSWORD;
    }
}
