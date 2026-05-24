package com.bfzy.platform.account.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bfzy.platform.common.exception.BizException;
import com.bfzy.platform.common.exception.CommonBizCode;
import com.bfzy.platform.data.model.security.UserEntity;
import com.bfzy.platform.data.model.security.UserAuthEntity;
import com.bfzy.platform.data.model.enums.IdentityType;
import com.bfzy.platform.account.dao.UserDao;
import com.bfzy.platform.account.dao.UserAuthDao;
import com.bfzy.platform.account.model.vo.request.UpdatePasswordRequest;
import com.bfzy.platform.account.model.vo.request.UpdateProfileRequest;
import com.bfzy.platform.account.model.vo.response.AccountProfileResponse;
import com.bfzy.platform.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 账号管理服务实现.
 *
 * @author zhangyu
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserDao userDao;
    private final UserAuthDao userAuthDao;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AccountProfileResponse getProfile(Long userId) {
        UserEntity user = userDao.getById(userId);
        if (user == null) {
            throw new BizException(CommonBizCode.USER_NOT_FOUND);
        }
        return toProfileResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccountProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        UserEntity user = userDao.getById(userId);
        if (user == null) {
            throw new BizException(CommonBizCode.USER_NOT_FOUND);
        }

        // 检查邮箱唯一性（如果修改了邮箱）
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            long emailCount = userDao.count(
                    new LambdaQueryWrapper<UserEntity>()
                            .eq(UserEntity::getEmail, request.getEmail())
                            .ne(UserEntity::getId, userId));
            if (emailCount > 0) {
                throw new BizException(CommonBizCode.EMAIL_DUPLICATE);
            }
        }

        // 检查手机号唯一性（如果修改了手机号）
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            long phoneCount = userDao.count(
                    new LambdaQueryWrapper<UserEntity>()
                            .eq(UserEntity::getPhone, request.getPhone())
                            .ne(UserEntity::getId, userId));
            if (phoneCount > 0) {
                throw new BizException(CommonBizCode.PHONE_DUPLICATE);
            }
        }

        user.setNickname(request.getNickname());
        user.setAvatar(request.getAvatar());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        userDao.updateById(user);

        log.info("Profile updated: userId={}", userId);
        return toProfileResponse(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        // 查找该用户的密码认证记录
        UserAuthEntity userAuth = userAuthDao.getOne(
                new LambdaQueryWrapper<UserAuthEntity>()
                        .eq(UserAuthEntity::getUserId, userId)
                        .eq(UserAuthEntity::getIdentityType, IdentityType.PASSWORD));

        if (userAuth == null) {
            throw new BizException(CommonBizCode.USER_NOT_FOUND);
        }

        // 校验原密码
        if (!passwordEncoder.matches(request.getOldPassword(), userAuth.getCredential())) {
            throw new BizException(CommonBizCode.PASSWORD_INVALID);
        }

        // 更新密码
        userAuth.setCredential(passwordEncoder.encode(request.getNewPassword()));
        userAuthDao.updateById(userAuth);

        log.info("Password updated: userId={}", userId);
    }

    private AccountProfileResponse toProfileResponse(UserEntity user) {
        return AccountProfileResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }
}
