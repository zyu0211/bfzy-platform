package com.bfzy.platform.account.service;

import com.bfzy.platform.account.model.vo.request.UpdatePasswordRequest;
import com.bfzy.platform.account.model.vo.request.UpdateProfileRequest;
import com.bfzy.platform.account.model.vo.response.AccountProfileResponse;

/**
 * 账号管理服务接口.
 *
 * @author zhangyu
 */
public interface AccountService {

    /**
     * 获取用户资料.
     *
     * @param userId 用户 ID
     * @return 用户资料
     */
    AccountProfileResponse getProfile(Long userId);

    /**
     * 更新用户资料.
     *
     * @param userId  用户 ID
     * @param request 更新请求
     * @return 更新后的资料
     */
    AccountProfileResponse updateProfile(Long userId, UpdateProfileRequest request);

    /**
     * 修改密码.
     *
     * @param userId  用户 ID
     * @param request 修改密码请求
     */
    void updatePassword(Long userId, UpdatePasswordRequest request);
}
