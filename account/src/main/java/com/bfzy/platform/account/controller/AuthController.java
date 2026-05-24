package com.bfzy.platform.account.controller;

import com.bfzy.platform.common.model.ApiResponse;
import com.bfzy.platform.account.model.vo.request.LoginRequest;
import com.bfzy.platform.account.model.vo.request.RefreshTokenRequest;
import com.bfzy.platform.account.model.vo.request.RegisterRequest;
import com.bfzy.platform.account.model.vo.response.LoginResponse;
import com.bfzy.platform.account.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器.
 *
 * @author zhangyu
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户注册.
     * <p>POST /api/auth/register</p>
     */
    @PostMapping("/register")
    public ApiResponse<Long> register(@Valid @RequestBody RegisterRequest request) {
        Long userId = authService.register(request);
        return ApiResponse.success(userId);
    }

    /**
     * 用户登录.
     * <p>POST /api/auth/login</p>
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }

    /**
     * 刷新 Token.
     * <p>POST /api/auth/refresh</p>
     */
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refresh(request.getRefreshToken());
        return ApiResponse.success(response);
    }
}
