# BFZY Platform — AGENTS.md

## 项目概况

Java 21 + Spring Boot 3.5 + MyBatis-Plus + SQLite 的单体服务框架。5 个 Maven 模块，模块边界清晰：`common` 纯 Java 零框架依赖，`common-spring` 托管 Spring 依赖，`data` 托管实体 + MyBatis-Plus，业务模块只依赖 `common-spring` + `data`。

## 关键命令

```bash
mvn compile              # 全量编译（最常用）
mvn clean package -DskipTests  # 打包可执行 jar
java -jar start/target/start-1.0.0-SNAPSHOT.jar  # 启动
```

## 模块依赖关系

```
common (纯 Java, 仅 jackson)
  ↑         ↑
common-spring  data
(web/valid)   (MP + 实体)
  ↑         ↑
hello-world  │
  ↑         ↑
  └── start ←┘  (sqlite-jdbc, actuator)
```

## 规范文档

详细规范请参考 `openspec/specs/` 目录：

| 模块/领域 | 规范文档 |
|-----------|----------|
| 平台架构 | [platform-architecture/spec.md](openspec/specs/platform-architecture/spec.md) |
| 公共基础设施（common + common-spring） | [common-infrastructure/spec.md](openspec/specs/common-infrastructure/spec.md) |
| 数据持久化（data + SQLite + MyBatis-Plus） | [data-persistence/spec.md](openspec/specs/data-persistence/spec.md) |
| hello-world 业务模块 | [hello-world/spec.md](openspec/specs/hello-world/spec.md) |

## 模块概览

- [common/AGENTS.md](common/AGENTS.md) — 常量、异常体系、API 响应、工具类
- [common-spring/AGENTS.md](common-spring/AGENTS.md) — 全局异常处理器
- [data/AGENTS.md](data/AGENTS.md) — 实体基类、MyBatis-Plus 依赖托管
- [hello-world/AGENTS.md](hello-world/AGENTS.md) — 业务模块脚手架参考
- [start/AGENTS.md](start/AGENTS.md) — 启动入口、全局配置、自动填充

## 关键约定（不遵守会踩坑）

- **版本管理**：父 POM `<properties>` 中的 `${revision}` 是所有子模块的统一版本号，直接在子模块 `<parent><version>` 中引用。**不要在任何子模块的 pom.xml 中硬编码版本号。**
- **Lombok**：由父 POM 统一声明 `<optional>true</optional>`，子模块不需要重复声明。**不要在子模块 pom 中再写 lombok 依赖。**
- **MyBatis-Plus 版本**：使用 `mybatis-plus-spring-boot3-starter`（带 `spring-boot3` 后缀），**不是** `mybatis-plus-spring-boot-starter`（少 spring3 会引发启动错误）。
- **Mapper XML 路径**：必须放在 `resources/mapper/**/*.xml`，因为 `application.yml` 中配置了 `classpath*:mapper/**/*.xml` 跨模块扫描。
- **依赖版本**：第三方依赖版本由父 POM `<dependencyManagement>` 统一声明，子模块在 `<dependencies>` 中**省略 `<version>`**。
- **实体命名**：实体类**加 `Entity` 后缀**（`UserEntity.java` 而非 `User.java`），放在 `data` 模块的 `model/{模块}/` 下（如 `model/security/UserEntity.java`），避免 `model.entity.UserEntity` 冗余。
- **VO 分包**：API 请求类放在 `model/vo/request/`，响应类放在 `model/vo/response/`，不再混放于 `dto/`。
- **DTO 按需使用**：`model/dto/` 仅在 Entity 与 VO 存在字段 gap 时使用，结构一致时不创建 DTO。**不加 `Dto` 后缀**。
- **DAO 层**：复杂 SQL（多表关联/聚合）放在 `dao/` 包下，简单单表 CRUD 直接调用 Mapper。

## 新增业务模块步骤

1. 创建目录 → `pom.xml`（父版本用 `${revision}`，依赖 `common-spring` + `data`，省略 `<version>`）
2. 父 `pom.xml` → `<modules>` 加 module、`<dependencyManagement>` 加依赖
3. `start/pom.xml` → 加入对新模块的依赖
4. 包结构：`controller/`、`service/`（接口） + `service/impl/`（实现）、`dao/`（接口） + `dao/impl/`（实现）、`mapper/`
5. 实体类继承 `BaseEntity`（位于 data 模块的 `model/`），**实体集中放在 data 模块，业务模块不单独放实体**。类名加 `Entity` 后缀（`UserEntity`、`OrderEntity`）
6. Service 接口（`service/XxxService.java`）→ 实现（`service/impl/XxxServiceImpl.java`），实现类依赖 DAO 而非直接调 Mapper
7. DAO 接口（`dao/XxxDao.java`）继承 `IService<Entity>` → 实现（`dao/impl/XxxDaoImpl.java`）继承 `ServiceImpl<Mapper, Entity>`

更多细节参见 [hello-world/spec.md](openspec/specs/hello-world/spec.md) 和 [platform-architecture/spec.md](openspec/specs/platform-architecture/spec.md)。

## 规范更新流程

> 当需求或实现发生变更时，必须先更新 `openspec/specs/` 中对应的规范文档，再更新本 AGENTS.md。
> 保持 "规范文档 → AGENTS.md → 代码" 三者一致，避免 drift。

## 错误码体系（简述）

- `ErrorCode` 接口：`getCode()`（业务码）+ `getHttpStatus()`（HTTP 状态码）+ `getMessage()`
- `SystemErrorCode`：系统级状态映射（HTTP 状态与业务码一致）
- `CommonBizCode`：公共业务错误码（HTTP 状态固定 200，业务码 10001~）
- 业务模块可自定义 `enum XxxCode implements ErrorCode`
- `GlobalExceptionHandler` 通过 `ResponseEntity` 动态设置 HTTP 状态

详细规范参见 [common-infrastructure/spec.md](openspec/specs/common-infrastructure/spec.md)。

## 序列化（简述）

- `ApiResponse` JSON 结构：`{code, success, message, data, timestamp, trace_id}`
- `traceId` → `@JsonProperty("trace_id")`（Snake-Case）
- Jackson 全局配置：`map-underscore-to-camel-case: true`
