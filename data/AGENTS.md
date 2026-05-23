# data — AGENTS.md

数据模型模块，托管 MyBatis-Plus 依赖 + 实体基类 `BaseEntity`。

## 用途

- 所有业务模块的实体类都应放在此模块（或自身模块的实体包中）
- 实体基类 `BaseEntity` 是纯 POJO，**不依赖任何 ORM 框架注解**
- MyBatis-Plus 的映射能力由 `start` 模块的全局配置自动生效

详细规范参见 [data-persistence/spec.md](../openspec/specs/data-persistence/spec.md)。

## 内容

```
model/entity/
  └── BaseEntity.java
```

### BaseEntity

```java
@Getter
@Setter
public abstract class BaseEntity {
    private Long id;                    // 主键（id-type: auto）
    private LocalDateTime createTime;   // 由 MyMetaObjectHandler 自动填充
    private LocalDateTime updateTime;   // 由 MyMetaObjectHandler 自动填充
    private Boolean deleted;            // 逻辑删除（全局配置处理）
}
```

业务实体示例：

```java
@TableName("sys_user")
public class User extends BaseEntity {
    private String username;
    private String password;
}
```

## 依赖说明

```xml
<!-- data/pom.xml — 版本由父 POM dependencyManagement 统一管理 -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
</dependency>
```

注意：使用 `spring-boot3` 后缀，不是 `mybatis-plus-spring-boot-starter`。
