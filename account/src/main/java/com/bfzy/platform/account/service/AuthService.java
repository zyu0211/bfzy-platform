package com.bfzy.platform.account.service;

import com.bfzy.platform.account.model.vo.request.LoginRequest;
import com.bfzy.platform.account.model.vo.request.RegisterRequest;
import com.bfzy.platform.account.model.vo.response.LoginResponse;

/**
 * 认证服务接口.
 * <p>
 * 处理用户注册、登录、Token 刷新业务逻辑。
 * </p>
 *
 * @author zhangyu
 */
public interface AuthService {

    /**
     * 用户注册.
     *
     * @param request 注册请求
     * @return 用户 ID
     */
    Long register(RegisterRequest request);

    /**
     * 用户登录.
     *
     * @param request 登录请求
     * @return 登录响应（含 Access Token + Refresh Token）
     */
    LoginResponse login(LoginRequest request);

    /**
     * 刷新 Token.
     *
     * @param refreshToken Refresh Token
     * @return 新的登录响应（换发 Access Token + Refresh Token）
     */
    LoginResponse refresh(String refreshToken);
}
