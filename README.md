# BFZY Platform

> 个人服务平台 — 通用服务端应用程序框架

基于 **Java 21 + Spring Boot 3.5 + Maven 多模块架构** 的单体服务框架。

## 快速开始

```bash
# 编译
mvn clean compile

# 打包
mvn clean package -DskipTests

# 启动（默认端口 8080，SQLite 文件数据库 ./data/bfzy-platform.db）
java -jar start/target/start-1.0.0-SNAPSHOT.jar

# 验证
curl http://localhost:8080/api/health
```

## 模块结构

```
bfzy-platform/
├── pom.xml              # 父 POM — CI-Friendly 版本管理 (${revision})
├── common/              # 纯 Java 公共组件（常量、异常、模型、工具类）
├── common-spring/       # Spring 感知的公共组件（全局异常处理）
├── data/                # 数据模型模块（实体基类，托管 MyBatis-Plus 依赖）
├── hello-world/         # 业务模块示例（HealthController）
└── start/               # 启动模块 — Spring Boot 入口，聚合所有模块
```

### 模块依赖关系

```
common              ← 纯 Java，零框架依赖（仅 jackson）
common-spring       ← 依赖 common + spring-boot-starter-web + validation
data                ← 依赖 common + mybatis-plus-spring-boot3-starter
hello-world         ← 依赖 common-spring（透传 common + web 依赖）
start               ← 聚合 common / common-spring / data / hello-world + sqlite-jdbc
```

## 技术栈

| 组件 | 选型 | 说明 |
|------|------|------|
| 语言 | Java 21 | 虚拟线程、模式匹配、Record |
| 框架 | Spring Boot 3.5.14 | 最新 3.x 稳定版 |
| 构建 | Maven 3.9+ | 多模块 + CI-Friendly `${revision}` |
| ORM | MyBatis-Plus 3.5.16 | 自动分页、自动填充、逻辑删除 |
| 数据库 | SQLite | `jdbc:sqlite:./data/bfzy-platform.db` |
| 对象映射 | Jackson | 全局 ObjectMapper 单例 |
| 代码简化 | Lombok | @Data、@Builder，由父 POM 统一管理 |
| 校验 | Jakarta Validation | @Valid、@NotBlank |

## 新增业务模块

1. 创建新目录 `my-module/`，写 `pom.xml`
2. 在父 `pom.xml` 的 `<modules>` 中加入 `<module>my-module</module>`
3. 在父 `pom.xml` 的 `<dependencyManagement>` 中加入 `my-module`
4. 在 `start/pom.xml` 中加入对 `my-module` 的依赖
5. 业务模块的依赖通常只需要 `common-spring` (含 common + web) + `data` (含实体 + MyBatis-Plus)

## 关键约定

- **版本统一**：所有版本号定义在父 POM 的 `<properties>` 中，子模块通过 `${revision}` 继承
- **Mapper XML**：放在各业务模块的 `src/main/resources/mapper/` 下，由 `classpath*:mapper/**/*.xml` 全局扫描
- **Mapper 接口**：放在各业务模块的 `mapper` 包下，由 `@MapperScan("com.bfzy.platform.**.mapper")` 自动扫描
- **Controller / Service / Config**：通过 `@SpringBootApplication(scanBasePackages = "com.bfzy.platform")` 自动发现
