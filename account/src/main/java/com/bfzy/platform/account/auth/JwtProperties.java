package com.bfzy.platform.account.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性.
 * <p>
 * 对应 {@code application.yml} 中 {@code jwt.*} 配置项。
 * </p>
 *
 * @author zhangyu
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * HMAC 密钥（至少 256 位）
     */
    private String secret;

    /**
     * Access Token 过期时间（毫秒），默认 24 小时
     */
    private long expiration = 86400000;

    /**
     * Refresh Token 过期时间（毫秒），默认 7 天
     */
    private long refreshExpiration = 604800000;
}
