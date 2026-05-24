package com.bfzy.platform.security.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bfzy.platform.data.model.security.UserEntity;
import com.bfzy.platform.security.dao.UserDao;
import com.bfzy.platform.security.mapper.UserMapper;
import org.springframework.stereotype.Repository;

/**
 * 用户档案 DAO 实现.
 */
@Repository
public class UserDaoImpl extends ServiceImpl<UserMapper, UserEntity> implements UserDao {
}
