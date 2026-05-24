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
├── service/
│   ├── XxxService.java      # 业务接口
│   └── impl/
│       └── XxxServiceImpl.java  # 业务实现，调用 DAO 层
├── dao/
│   ├── XxxDao.java          # 数据访问接口（继承 IService<Entity>）
│   └── impl/
│       └── XxxDaoImpl.java  # extends ServiceImpl<Mapper, Entity>
├── mapper/       → MyBatis-Plus Mapper（extends BaseMapper<Entity>）
├── model/
│   ├── vo/
│   │   ├── request/  → API 请求类（Controller/Service 入参）
│   │   └── response/ → API 响应类（Controller/Service 出参）
│   └── dto/      → Entity ↔ VO 有 gap 时使用（不加 Dto 后缀），可选
└── XxxCode.java  → 业务错误码（implements ErrorCode）
```

实体类统一放在 `data` 模块的 `model/` 下（无 `entity/` 子包），业务模块不单独放实体。类名加 `Entity` 后缀（`UserEntity`、`OrderEntity`）。

### 3. 调用链

```
Controller → XxxService（接口）→ XxxServiceImpl → XxxDao（IService）→ XxxMapper（BaseMapper）→ DB
```

### 3. Mapper XML

放在 `src/main/resources/mapper/` 下，文件名与 Mapper 接口同名。

## 当前模块内容

- `controller/HealthController` — `GET /api/health` + `GET /api/hello`
