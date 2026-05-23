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

## 新增业务模块步骤

1. 创建目录 → `pom.xml`（父版本用 `${revision}`，依赖 `common-spring` + `data`，省略 `<version>`）
2. 父 `pom.xml` → `<modules>` 加 module、`<dependencyManagement>` 加依赖
3. `start/pom.xml` → 加入对新模块的依赖
4. Controller 放在 `controller/` 包，Service 放在 `service/` 包，Mapper 放在 `mapper/` 包
5. 实体类继承 `BaseEntity`（位于 data 模块）

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
