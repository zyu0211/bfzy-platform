package com.bfzy.platform.security.service;

import com.bfzy.platform.security.model.vo.request.LoginRequest;
import com.bfzy.platform.security.model.vo.request.RegisterRequest;
import com.bfzy.platform.security.model.vo.response.LoginResponse;

/**
 * 认证服务接口.
 * <p>
 * 处理用户注册、登录业务逻辑。
 * </p>
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
     * @return 登录响应（含 Token）
     */
    LoginResponse login(LoginRequest request);
}
