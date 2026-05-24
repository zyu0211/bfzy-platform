package com.bfzy.platform.config.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器.
 * <p>
 * 统一处理 {@link com.bfzy.platform.data.model.entity.BaseEntity} 中的审计字段：
 * <ul>
 *   <li>{@code createTime} — 插入时自动填充</li>
 *   <li>{@code updateTime} — 插入 / 更新时自动填充</li>
 *   <li>{@code deleted} — 插入时填充默认值 0（由全局配置处理）</li>
 * </ul>
 * </p>
 *
 * @author zhangyu
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
