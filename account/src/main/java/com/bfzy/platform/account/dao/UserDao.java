package com.bfzy.platform.account.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bfzy.platform.data.model.security.UserEntity;

/**
 * 用户档案 DAO.
 * <p>
 * 继承 MyBatis-Plus IService，自动获得单表 CRUD。
 * 自定义复杂查询在此接口声明，实现类在 {@code dao/impl/} 中。
 * </p>
 *
 * @author zhangyu
 */
public interface UserDao extends IService<UserEntity> {
}
