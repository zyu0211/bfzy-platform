package com.bfzy.platform.account.controller;

import com.bfzy.platform.account.model.vo.request.UpdatePasswordRequest;
import com.bfzy.platform.account.model.vo.request.UpdateProfileRequest;
import com.bfzy.platform.account.model.vo.response.AccountProfileResponse;
import com.bfzy.platform.account.service.AccountService;
import com.bfzy.platform.common.exception.BizException;
import com.bfzy.platform.common.exception.CommonBizCode;
import com.bfzy.platform.common.model.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 账号管理控制器.
 * <p>
 * 需要已认证用户访问，通过 {@link Authentication#getName()} 获取用户 ID。
 * </p>
 *
 * @author zhangyu
 */
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * 获取当前登录用户资料.
     * <p>GET /api/account/profile</p>
     */
    @GetMapping("/profile")
    public ApiResponse<AccountProfileResponse> getProfile(Authentication authentication) {
        return ApiResponse.success(accountService.getProfile(getUserId(authentication)));
    }

    /**
     * 更新当前登录用户资料.
     * <p>PUT /api/account/profile</p>
     */
    @PutMapping("/profile")
    public ApiResponse<AccountProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success(accountService.updateProfile(getUserId(authentication), request));
    }

    /**
     * 修改当前登录用户密码.
     * <p>PUT /api/account/password</p>
     */
    @PutMapping("/password")
    public ApiResponse<Void> updatePassword(
            Authentication authentication,
            @Valid @RequestBody UpdatePasswordRequest request) {
        accountService.updatePassword(getUserId(authentication), request);
        return ApiResponse.success();
    }

    /**
     * 从 Authentication 中提取用户 ID.
     * <p>JwtAuthFilter 将 principal 设为 userId（Long），getName() 返回 userId.toString()。</p>
     */
    private static Long getUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BizException(CommonBizCode.TOKEN_INVALID);
        }
        return Long.parseLong(authentication.getName());
    }
}
