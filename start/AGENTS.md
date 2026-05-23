# start — AGENTS.md

Spring Boot 启动模块，是最终可执行 jar 的聚合模块。

## 职责

- 提供 `@SpringBootApplication` 启动入口
- 聚合所有业务模块依赖 + 运行时必要依赖（sqlite-jdbc、actuator）
- 配置全局 Bean（MyBatis-Plus 配置、自动填充处理器）

详细规范参见 [platform-architecture/spec.md](../openspec/specs/platform-architecture/spec.md) 和 [data-persistence/spec.md](../openspec/specs/data-persistence/spec.md)。

## 内容

```
src/main/java/com/bfzy/platform/
├── BfzyPlatformApplication.java    # 启动入口
└── config/
    ├── MybatisPlusConfig.java      # @MapperScan 全局配置
    └── handler/
        └── MyMetaObjectHandler.java # 自动填充处理器

src/main/resources/
├── application.yml                 # 主配置
├── application-dev.yml             # 开发环境
└── application-prod.yml            # 生产环境
```

## 配置说明

### application.yml 关键配置

| 配置项 | 值 | 说明 |
|--------|:--:|------|
| `spring.datasource.url` | `jdbc:sqlite:./data/bfzy-platform.db` | SQLite 文件数据库 |
| `mybatis-plus.global-config.db-config.id-type` | `auto` | 主键自增 |
| `mybatis-plus.global-config.db-config.logic-delete-field` | `deleted` | 逻辑删除字段 |
| `mybatis-plus.mapper-locations` | `classpath*:mapper/**/*.xml` | 跨模块扫描 |

### MyMetaObjectHandler

- `createTime` → INSERT 时 `LocalDateTime.now()`
- `updateTime` → INSERT/UPDATE 时 `LocalDateTime.now()`

### MybatisPlusConfig

```java
@Configuration
@MapperScan("com.bfzy.platform.**.mapper")
```

扫描所有模块的 `mapper` 包。

## pom.xml 依赖说明

```xml
<dependencies>
    <!-- 内部模块 -->
    <dependency>
        <groupId>com.bfzy.platform</groupId>
        <artifactId>common</artifactId>
    </dependency>
    <dependency>
        <groupId>com.bfzy.platform</groupId>
        <artifactId>common-spring</artifactId>
    </dependency>
    <dependency>
        <groupId>com.bfzy.platform</groupId>
        <artifactId>data</artifactId>
    </dependency>
    <dependency>
        <groupId>com.bfzy.platform</groupId>
        <artifactId>hello-world</artifactId>
    </dependency>

    <!-- 运行时依赖 -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.xerial</groupId>
        <artifactId>sqlite-jdbc</artifactId>
    </dependency>
</dependencies>
```

新增业务模块时，需在此 pom.xml 中加入对应的 `<dependency>`，否则不会被打包进最终 jar。
