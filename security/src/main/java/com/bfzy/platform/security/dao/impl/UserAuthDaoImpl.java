package com.bfzy.platform.security.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bfzy.platform.data.model.security.UserAuthEntity;
import com.bfzy.platform.security.dao.UserAuthDao;
import com.bfzy.platform.security.mapper.UserAuthMapper;
import org.springframework.stereotype.Repository;

/**
 * 用户认证方式 DAO 实现.
 */
@Repository
public class UserAuthDaoImpl extends ServiceImpl<UserAuthMapper, UserAuthEntity> implements UserAuthDao {
}
