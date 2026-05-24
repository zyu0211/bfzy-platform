# data — AGENTS.md

数据模型模块，托管 MyBatis-Plus 依赖 + 实体基类 `BaseEntity`。

## 用途

- 所有业务模块的实体类都应放在此模块（或自身模块的实体包中）
- 实体基类 `BaseEntity` 是纯 POJO，**不依赖任何 ORM 框架注解**
- MyBatis-Plus 的映射能力由 `start` 模块的全局配置自动生效

详细规范参见 [data-persistence/spec.md](../openspec/specs/data-persistence/spec.md)。

## 内容

```
logging/
  └── SqlLogImpl.java      # MyBatis SQL 日志适配器（将日志统一路由到 "SQL" logger）
model/
  ├── BaseEntity.java      # 实体基类（纯 POJO，主键 + 审计字段 + 逻辑删除）
  ├── security/
  │   ├── UserEntity.java      # 用户档案（@TableName("sys_user")）
  │   └── UserAuthEntity.java  # 用户认证方式（@TableName("sys_user_auth")）
  └── enums/
      └── IdentityType.java # 认证类型枚举（PASSWORD, WECHAT_OPENID ...）
```

```yaml
mybatis-plus:
  configuration:
    log-impl: com.bfzy.platform.data.logging.SqlLogImpl
```

无论 MyBatis 执行哪个 mapper 的 SQL，所有日志（SQL 语句、参数等）都通过 `"SQL"` 这个 logger 输出。生产环境下由 logback 的 `<logger name="SQL">` 路由到独立文件，与业务代码的 DEBUG 日志完全隔离。

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

### 实体类命名

- 所有实体类**加 `Entity` 后缀**（`UserEntity`、`UserAuthEntity`）
- 包名中不包含 `entity`，直接放在 `model/` 下：`com.bfzy.platform.data.model.UserEntity`
- 避免 `model.entity.UserEntity` 的包+类名冗余

业务实体示例：

```java
@TableName("sys_user")
public class UserEntity extends BaseEntity {
    private String username;
    private String nickname;
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
