package com.bfzy.platform.security.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bfzy.platform.common.constant.GlobalConstant;
import com.bfzy.platform.common.exception.BizException;
import com.bfzy.platform.common.exception.CommonBizCode;
import com.bfzy.platform.data.model.security.UserEntity;
import com.bfzy.platform.data.model.security.UserAuthEntity;
import com.bfzy.platform.data.model.enums.IdentityType;
import com.bfzy.platform.security.dao.UserAuthDao;
import com.bfzy.platform.security.dao.UserDao;
import com.bfzy.platform.security.model.vo.request.LoginRequest;
import com.bfzy.platform.security.model.vo.request.RegisterRequest;
import com.bfzy.platform.security.model.vo.response.LoginResponse;
import com.bfzy.platform.security.provider.JwtTokenProvider;
import com.bfzy.platform.security.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现.
 * <p>
 * 调用 DAO 层（IService）进行数据库操作，不直接引用 Mapper。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;
    private final UserAuthDao userAuthDao;
    private final JwtTokenProvider jwtTokenProvider;

    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String password = request.getPassword();

        // 检查用户名是否已存在（User 表）
        long count = userDao.count(
                new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getUsername, username));
        if (count > 0) {
            throw new BizException(CommonBizCode.USERNAME_DUPLICATE);
        }

        // 创建 User 档案
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setNickname(username);
        userDao.save(user);

        // 创建 UserAuth 密码认证
        UserAuthEntity userAuth = new UserAuthEntity();
        userAuth.setUserId(user.getId());
        userAuth.setIdentityType(IdentityType.PASSWORD);
        userAuth.setIdentifier(username);
        userAuth.setCredential(passwordEncoder.encode(password));
        userAuthDao.save(userAuth);

        log.info("User registered: id={}, username={}", user.getId(), username);
        return user.getId();
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername().trim();
        String password = request.getPassword();

        // 查 UserAuth(PASSWORD) 记录
        UserAuthEntity userAuth = userAuthDao.getOne(
                new LambdaQueryWrapper<UserAuthEntity>()
                        .eq(UserAuthEntity::getIdentityType, IdentityType.PASSWORD)
                        .eq(UserAuthEntity::getIdentifier, username));

        if (userAuth == null) {
            throw new BizException(CommonBizCode.CREDENTIALS_INVALID);
        }

        // 校验密码
        if (!passwordEncoder.matches(password, userAuth.getCredential())) {
            throw new BizException(CommonBizCode.CREDENTIALS_INVALID);
        }

        // 查 User 档案
        UserEntity user = userDao.getById(userAuth.getUserId());
        if (user == null) {
            throw new BizException(CommonBizCode.USER_NOT_FOUND);
        }

        // 签发 Token
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        long expiresIn = jwtTokenProvider.getExpiration();

        log.info("User login: id={}, username={}", user.getId(), username);

        return LoginResponse.builder()
                .token(token)
                .tokenType(GlobalConstant.Auth.TOKEN_TYPE_BEARER)
                .expiresIn(expiresIn)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build();
    }
}
