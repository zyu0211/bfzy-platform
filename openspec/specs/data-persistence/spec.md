# Data Persistence Specification

## Purpose

BFZY Platform uses SQLite as its embedded database with MyBatis-Plus as the ORM layer for single-server deployment. Entity base classes, auto-fill handling, logical deletion, and pagination are uniformly managed through global configuration.

## Requirements

### Requirement: SQLite Embedded Database

The system SHALL use SQLite as the default embedded database, configured in file mode with the Xerial JDBC driver to persist data across restarts.

#### Scenario: Default connection

- GIVEN no active Spring profile is set
- WHEN the application starts
- THEN a file-based SQLite datasource SHALL be created at `./data/bfzy-platform.db`
- AND MyBatis-Plus SHALL handle table creation (no `ddl-auto` equivalent — tables created via SQL scripts or entity scanning)

#### Scenario: Connection URL

- GIVEN the application configuration
- WHEN inspecting `spring.datasource.url`
- THEN it SHALL be `jdbc:sqlite:./data/bfzy-platform.db`
- AND `spring.datasource.driver-class-name` SHALL be `org.sqlite.JDBC`

#### Scenario: Switching to MySQL

- GIVEN `spring.profiles.active=prod` is set
- WHEN the application starts
- THEN it SHALL use the datasource configured in `application-prod.yml`
- AND switching to MySQL SHALL require changing the `url`, `driver-class-name`, and optionally the MyBatis-Plus dialect

### Requirement: MyBatis-Plus as ORM Layer

The system SHALL use MyBatis-Plus as its ORM framework, with the `spring-boot3` variant for compatibility with Spring Boot 3.x.

#### Scenario: Dependency setup

- GIVEN a business module needs database access
- WHEN it declares a dependency on `data`
- THEN `data` SHALL provide `mybatis-plus-spring-boot3-starter` transitively
- AND the version SHALL be managed by the parent POM's `<dependencyManagement>` — child modules specify no version

#### Scenario: Mapper scanning

- GIVEN mapper interfaces exist across modules
- WHEN the application starts
- THEN `@MapperScan("com.bfzy.platform.**.mapper")` (configured in `start` module) SHALL discover all mapper interfaces
- AND each mapper SHALL extend `BaseMapper<Entity>` to inherit CRUD operations

#### Scenario: Mapper XML scanning

- GIVEN mapper XML files exist across modules
- WHEN the application starts
- THEN `mybatis-plus.mapper-locations: classpath*:mapper/**/*.xml` SHALL load all XML files
- AND XML files SHALL be placed in `src/main/resources/mapper/` of the respective module

### Requirement: Entity Base Class

All database entities SHALL extend `BaseEntity`, a pure POJO with no ORM framework annotations, providing common fields. Entities SHALL use an `Entity` suffix (e.g., `UserEntity`, `UserAuthEntity`) and SHALL be placed in the `data` module's `model/` package (no `entity/` sub-package).

#### Scenario: Entity inheritance

- GIVEN a business entity `UserEntity extends BaseEntity`
- WHEN it is persisted
- THEN `id` SHALL be auto-generated (via `id-type: auto` global config)
- AND `createTime` SHALL be set automatically on insert (via `MyMetaObjectHandler`)
- AND `updateTime` SHALL be updated automatically on modification (via `MyMetaObjectHandler`)
- AND `deleted` SHALL default to `false` (via global `logic-delete-field: deleted` config)

#### Scenario: Entity naming and location

- GIVEN an entity class maps to a database table
- WHEN placed in the `data` module
- THEN its class name SHALL end with `Entity` (e.g., `UserEntity`, `OrderEntity`)
- AND it SHALL be in the `data.model` package (e.g., `com.bfzy.platform.data.model.UserEntity`)
- AND it SHALL NOT be placed in an `entity/` sub-package — `com.bfzy.platform.data.model.entity.UserEntity` is forbidden

#### Scenario: Entity annotation pattern

- GIVEN a business entity extends `BaseEntity`
- WHEN defining entity mapping
- THEN `@TableName("table_name")` SHALL specify the table name
- AND `@TableField(...)` SHALL configure field mapping as needed
- AND the `id` field (inherited from `BaseEntity`) SHALL not require `@TableId` annotation — global `id-type: auto` handles it

### Requirement: Auto-Fill for Timestamps

The system SHALL use MyBatis-Plus's `MetaObjectHandler` to automatically populate `createTime` and `updateTime` on entities.

#### Scenario: Automatic timestamp population on insert

- GIVEN an entity is inserted for the first time
- WHEN `insert` is called
- THEN `createTime` SHALL be set to `LocalDateTime.now()`
- AND `updateTime` SHALL also be set to `LocalDateTime.now()`

#### Scenario: Automatic timestamp update on modification

- GIVEN an existing entity is updated
- WHEN `update` is called
- THEN `updateTime` SHALL be refreshed to `LocalDateTime.now()`
- AND `createTime` SHALL remain unchanged

### Requirement: Logical Deletion

The system SHALL use MyBatis-Plus's global logical deletion instead of physical row removal.

#### Scenario: Logical delete configuration

- GIVEN the global configuration
- WHEN inspecting `mybatis-plus.global-config.db-config`
- THEN `logic-delete-field` SHALL be `deleted`
- AND `logic-delete-value` SHALL be `1` (deleted)
- AND `logic-not-delete-value` SHALL be `0` (active)
- AND `BaseEntity.deleted` (Boolean) SHALL be the target field

#### Scenario: Automatic filter on queries

- GIVEN a `BaseMapper` query is executed
- WHEN the query does not explicitly include the `deleted` field
- THEN MyBatis-Plus SHALL automatically append `WHERE deleted = 0`
- AND soft-deleted records SHALL be excluded from results

### Requirement: Pagination

The system SHALL support MyBatis-Plus's built-in pagination via `Page<T>` and the `PaginationInnerInterceptor`.

#### Scenario: Page query

- GIVEN a mapper method accepts a `Page<T>` parameter
- WHEN the query is executed
- THEN MyBatis-Plus SHALL automatically generate a count query followed by a paginated data query
- AND the result SHALL be a `Page<T>` containing records, total count, and page metadata
