package com.bfzy.platform.account.model.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应.
 *
 * @author zhangyu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT Access Token
     */
    private String token;

    /**
     * Token 类型
     */
    private String tokenType;

    /**
     * Access Token 过期时间（毫秒）
     */
    private long expiresIn;

    /**
     * Refresh Token
     */
    private String refreshToken;

    /**
     * Refresh Token 过期时间（毫秒）
     */
    private long refreshExpiresIn;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;
}
