package com.bfzy.platform.data.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 实体基类.
 * <p>
 * 所有业务模块的实体类应继承此类，统一主键策略与审计字段。
 * 纯 POJO 设计，零 ORM 框架注解，由具体的 ORM 层（MyBatis-Plus）负责映射。
 * </p>
 */
@Getter
@Setter
public abstract class BaseEntity {

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记（0-正常，1-删除）
     */
    private Boolean deleted;
}
