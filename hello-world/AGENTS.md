# hello-world — AGENTS.md

业务模块脚手架示例，作为新增模块的参考模板。

详细规范参见 [hello-world/spec.md](../openspec/specs/hello-world/spec.md)。

## 创建新业务模块时的参考模式

### 1. pom.xml

```xml
<parent>
    <groupId>com.bfzy.platform</groupId>
    <artifactId>bfzy-platform</artifactId>
    <version>${revision}</version>
</parent>
<artifactId>my-module</artifactId>

<dependencies>
    <!-- common-spring 传递 common + spring-boot-starter-web + validation -->
    <dependency>
        <groupId>com.bfzy.platform</groupId>
        <artifactId>common-spring</artifactId>
    </dependency>
    <!-- data 传递 mybatis-plus-spring-boot3-starter + BaseEntity -->
    <dependency>
        <groupId>com.bfzy.platform</groupId>
        <artifactId>data</artifactId>
    </dependency>
</dependencies>
```

### 2. 包结构

```
src/main/java/com/bfzy/platform/{module}/
├── controller/   → REST 控制器
├── service/      → 业务逻辑
├── mapper/       → MyBatis-Plus Mapper（extends BaseMapper<Entity>）
├── model/
│   ├── dto/      → 传输对象
│   ├── vo/       → 视图对象
│   └── entity/   → 实体类（继承 BaseEntity）
└── XxxCode.java  → 业务错误码（implements ErrorCode）
```

### 3. Mapper XML

放在 `src/main/resources/mapper/` 下，文件名与 Mapper 接口同名。

## 当前模块内容

- `controller/HealthController` — `GET /api/health` + `GET /api/hello`
