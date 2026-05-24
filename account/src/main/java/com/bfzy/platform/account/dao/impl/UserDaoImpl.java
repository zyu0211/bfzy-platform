package com.bfzy.platform.account.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bfzy.platform.data.model.security.UserEntity;
import com.bfzy.platform.account.dao.UserDao;
import com.bfzy.platform.account.mapper.UserMapper;
import org.springframework.stereotype.Repository;

/**
 * 用户档案 DAO 实现.
 *
 * @author zhangyu
 */
@Repository
public class UserDaoImpl extends ServiceImpl<UserMapper, UserEntity> implements UserDao {
}
