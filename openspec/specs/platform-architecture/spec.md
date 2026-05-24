# Platform Architecture Specification

## Purpose

BFZY Platform is a personal monolithic server framework built on Java 21 and Spring Boot 3.5. It uses Maven multi-module to isolate unrelated functionalities (blog, mini-program backends, tool APIs) while producing a single deployable fat jar.

## Requirements

### Requirement: Monolithic Deployment

The system SHALL deploy as a single executable fat jar, with all functional modules aggregated into a unified Spring Boot application context.

#### Scenario: Single process start

- GIVEN the project has been built with `mvn package -DskipTests`
- WHEN the user runs `java -jar start/target/start-*.jar`
- THEN a single Spring Boot application SHALL start on port 8080
- AND all business modules SHALL be discoverable via `scanBasePackages = "com.bfzy.platform"`

#### Scenario: No microservice infrastructure

- GIVEN the system is designed for single-server deployment
- WHEN evaluating infrastructure needs
- THEN no service discovery, API gateway, or distributed tracing SHALL be required
- AND all inter-module communication SHALL happen within the same JVM

### Requirement: Multi-Module Functional Separation

The system SHALL use Maven multi-module to separate unrelated business capabilities into independent modules, each with its own `pom.xml` and package namespace.

#### Scenario: Module boundaries

- GIVEN the project has 5 modules: `common`, `common-spring`, `data`, busines modules, and `start`
- WHEN building the dependency graph
- THEN `common` SHALL have no project-internal dependencies (pure Java, only Jackson)
- AND `common-spring` SHALL depend on `common` + Spring Web/Validation
- AND `data` SHALL depend on `common` + MyBatis-Plus (version managed by parent)
- AND business modules SHALL depend on `common-spring` + `data`
- AND `start` SHALL depend on all other modules + runtime drivers (sqlite-jdbc, actuator)

#### Scenario: Adding a new business module

- GIVEN a new business capability needs to be added
- WHEN the developer creates a new Maven submodule
- THEN the new module SHALL declare `com.bfzy.platform:bfzy-platform` as its parent with `${revision}` as version
- AND its `pom.xml` SHALL declare dependencies on `common-spring` (for Spring Web + common) and `data` (for MyBatis-Plus + entity base)
- AND its base package SHALL be `com.bfzy.platform.<module>`
- AND it SHALL NOT require its own `@SpringBootApplication` or `application.yml`

#### Scenario: Cross-module dependency chain

- GIVEN a business module `my-module` is being set up
- WHEN declaring its dependencies
- THEN `common-spring` SHALL provide transitive access to `common` + `spring-boot-starter-web` + `spring-boot-starter-validation`
- AND `data` SHALL provide transitive access to `mybatis-plus-spring-boot3-starter` + entity base classes
- AND the module SHALL NOT need to declare Jackson, Spring Web, or MyBatis-Plus individually

### Requirement: Module Aggregation via Start Module

The system SHALL provide a `start` module that aggregates all business modules and serves as the single Spring Boot entry point.

#### Scenario: Adding a new module to the application

- GIVEN a new business module `my-module` exists
- WHEN the developer wants it included in the application
- THEN `start/pom.xml` SHALL declare a dependency on `com.bfzy.platform:my-module`
- AND `@SpringBootApplication(scanBasePackages = "com.bfzy.platform")` SHALL automatically discover its beans

#### Scenario: Global MyBatis-Plus configuration

- GIVEN MyBatis-Plus configuration lives in the `start` module
- WHEN the application starts
- THEN `@MapperScan("com.bfzy.platform.**.mapper")` SHALL discover mapper interfaces across all modules
- AND `classpath*:mapper/**/*.xml` SHALL load mapper XML files across all modules
- AND the global config (`id-type: auto`, `logic-delete-field: deleted`) SHALL apply to all entities

### Requirement: Logging Configuration

The system SHALL provide a centralized logging configuration via `logback-spring.xml` in the `start` module.

#### Scenario: Console logging (dev)

- GIVEN the application runs with the `dev` or `test` Spring profile
- WHEN logging output is produced
- THEN a `CONSOLE` appender SHALL output colorized logs to stdout
- AND the pattern SHALL include `[%X{traceId}]` for trace correlation

#### Scenario: Rolling file logging (prod)

- GIVEN the application runs with a non-`dev`, non-`test` profile (e.g., `prod`)
- WHEN logging output is produced
- THEN three rolling file appenders SHALL be active:

  | Appender | File | Retention | Content |
  |----------|------|:---------:|---------|
  | `APP_FILE` | `./logs/bfzy-platform.log` | 30 days | INFO/WARN (ERROR filtered out) |
  | `ERROR_FILE` | `./logs/bfzy-platform-error.log` | 60 days | ERROR+ only |
  | `SQL_FILE` | `./logs/bfzy-platform-sql.log` | 30 days | `"SQL"` logger (MyBatis execution logs) |

- AND all three SHALL use `TimeBasedRollingPolicy` with daily rotation and gzip compression
- AND `APP_FILE` SHALL use a `LevelFilter` combined with a `ThresholdFilter` to keep only INFO/WARN
- AND `ERROR_FILE` SHALL use a `ThresholdFilter` to accept only ERROR+
- AND `SQL_FILE` SHALL use a `LevelFilter` to accept only DEBUG level
- AND a dedicated `<logger name="SQL" level="DEBUG" additivity="false">` SHALL route MyBatis logs exclusively to `SQL_FILE`
- AND `application-prod.yml` SHALL configure `log-impl: com.bfzy.platform.data.logging.SqlLogImpl` — a custom `org.apache.ibatis.logging.Log` adapter that writes all MyBatis logs to the `"SQL"` logger, isolating them from project DEBUG logs
- AND `application-dev.yml` SHALL keep `log-impl: org.apache.ibatis.logging.stdout.StdOutImpl` for direct console output

#### Scenario: Dev profile logging

- GIVEN the application runs with the `dev` or `test` Spring profile
- WHEN logging output is produced
- THEN only the `CONSOLE` appender SHALL be active
- AND no files SHALL be written

### Requirement: Access Log

The system SHALL log HTTP request and response information for every API call via `AccessLogFilter`.

#### Scenario: Request log

- GIVEN a request reaches the application
- WHEN `AccessLogFilter` executes before the controller
- THEN it SHALL log `→ {METHOD} {URI} | from {IP} | agent: {User-Agent}`

#### Scenario: Response log

- GIVEN a request has been processed and a response is returning
- WHEN `AccessLogFilter` executes after the controller (in the `finally` block)
- THEN it SHALL log `← {METHOD} {URI} | status={HTTP_STATUS} | bizCode={API_CODE} {DURATION}ms`
- AND the business code SHALL be read from the request attribute `_apiResponseCode` set by `TraceIdResponseAdvice`

### Requirement: Consistent Package Convention

All Java source code in every module SHALL reside under the `com.bfzy.platform` package namespace.

#### Scenario: Module package layout

- GIVEN a module with artifactId `hello-world`
- WHEN examining its source root
- THEN the base package SHALL be `com.bfzy.platform.helloworld`
- AND controllers SHALL be in `com.bfzy.platform.helloworld.controller`
- AND services SHALL be in `com.bfzy.platform.helloworld.service`
- AND mappers SHALL be in `com.bfzy.platform.helloworld.mapper`
- AND mapper XML SHALL be in `src/main/resources/mapper/`

### Requirement: Group and Artifact Naming

All Maven coordinates SHALL use `com.bfzy.platform` as the group ID and `bfzy-platform` as the parent artifact ID.

#### Scenario: Parent POM identity

- GIVEN the root `pom.xml`
- WHEN inspecting its coordinates
- THEN `groupId` SHALL be `com.bfzy.platform`
- AND `artifactId` SHALL be `bfzy-platform`
- AND `version` SHALL follow `${revision}` CI-Friendly pattern

#### Scenario: Child module naming

- GIVEN a child module `common`
- WHEN inspecting its POM
- THEN its `parent` SHALL reference `com.bfzy.platform:bfzy-platform:${revision}`
- AND its `artifactId` SHALL be short (e.g., `common`, `common-spring`, `data`, `hello-world`, `start`)
- AND its version tag SHALL NOT exist (inherited from parent via `${revision}`)

### Requirement: Version Management

All dependency versions SHALL be managed centrally in the parent POM.

#### Scenario: CI-Friendly versioning

- GIVEN the parent `pom.xml`
- WHEN inspecting its version scheme
- THEN `<revision>` SHALL be defined in `<properties>` as `1.0.0-SNAPSHOT`
- AND all child modules SHALL use `${revision}` in their `<parent><version>`
- AND `<dependencyManagement>` SHALL declare versions for internal modules and selected third-party dependencies (e.g., MyBatis-Plus)
- AND child modules SHALL omit `<version>` in their `<dependencies>` when the artifact is managed in `<dependencyManagement>`

#### Scenario: Lombok management

- GIVEN Lombok is declared in the parent POM
- WHEN a child module needs Lombok
- THEN it SHALL inherit Lombok from the parent (no re-declaration in child `pom.xml`)
- AND the parent SHALL declare Lombok with `<optional>true</optional>`
