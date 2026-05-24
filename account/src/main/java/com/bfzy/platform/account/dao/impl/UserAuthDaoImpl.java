package com.bfzy.platform.account.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bfzy.platform.data.model.security.UserAuthEntity;
import com.bfzy.platform.account.dao.UserAuthDao;
import com.bfzy.platform.account.mapper.UserAuthMapper;
import org.springframework.stereotype.Repository;

/**
 * 用户认证方式 DAO 实现.
 *
 * @author zhangyu
 */
@Repository
public class UserAuthDaoImpl extends ServiceImpl<UserAuthMapper, UserAuthEntity> implements UserAuthDao {
}
