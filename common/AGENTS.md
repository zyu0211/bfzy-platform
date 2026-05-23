# common — AGENTS.md

纯 Java 模块，**零 Spring 框架依赖**，仅依赖 Jackson（databind + jsr310）。

## 包结构

```
constant/      → 分类常量（PageConstant, DateConstant）
exception/     → 错误码体系、异常类
model/         → ApiResponse、PageResult
utils/         → JsonUtil（Jackson 工具类）
```

## 核心设计

### 1. 错误码体系

- `ErrorCode` 接口 → `getCode()` / `getHttpStatus()` / `getMessage()`
- `SystemErrorCode` → HTTP 状态码（使用 `HttpURLConnection` 常量），code 与 HTTP 状态一致
- `CommonBizCode` → 公共业务码（HTTP 200，业务码 10001+）
- `BaseException` → 携带 `ErrorCode`，`httpStatus` 由 `ErrorCode.getHttpStatus()` 自动填充

详细规范参见 [common-infrastructure/spec.md](../openspec/specs/common-infrastructure/spec.md)。

### 2. 响应体

`ApiResponse<T>` JSON 结构：`{code, success, message, data, timestamp, trace_id}`。`traceId` 序列化为 `trace_id`（snake_case）。

### 3. JsonUtil

全局单例 `ObjectMapper`，配置 JavaTimeModule + lenient 模式。

常用方法：

```java
JsonUtil.toJson(obj);
JsonUtil.fromJson(json, User.class);
JsonUtil.fromJson(json, new TypeReference<Map<String, List<User>>>() {});
JsonUtil.toList(json, String.class);
JsonUtil.toMap(json);                               // Map<String, Object>
JsonUtil.toMap(json, User.class);                   // Map<String, User>
JsonUtil.toMap(json, Integer.class, String.class);  // Map<Integer, String>
```

### 4. PageResult

```java
PageResult.of(records, total, page, pageSize);  // 自动算 totalPages
PageResult.empty();                              // 空结果
```

## 依赖说明

```xml
<!-- common/pom.xml — 仅 Jackson -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.datatype</groupId>
    <artifactId>jackson-datatype-jsr310</artifactId>
</dependency>
```

不要在此模块添加任何 Spring 或 MyBatis-Plus 依赖。
