package com.bfzy.platform.security.controller;

import com.bfzy.platform.common.model.ApiResponse;
import com.bfzy.platform.security.model.vo.request.LoginRequest;
import com.bfzy.platform.security.model.vo.request.RegisterRequest;
import com.bfzy.platform.security.model.vo.response.LoginResponse;
import com.bfzy.platform.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器.
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
}
